package edu.columbia.ldpd.hrwa.tasks.workers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.search.SearchHits;
import org.jwat.arc.ArcReader;
import org.jwat.arc.ArcReaderFactory;
import org.jwat.arc.ArcRecordBase;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

import edu.columbia.ldpd.hrwa.ArchiveFileInfoRecord;
import edu.columbia.ldpd.hrwa.ArchiveFileInfoRecord.MissingArchiveHeaderValueException;
import edu.columbia.ldpd.hrwa.ArchiveFileInfoRecord.UnexpectedRecordTypeException;
import edu.columbia.ldpd.hrwa.util.ElasticsearchHelper;
import edu.columbia.ldpd.hrwa.HrwaManager;
import edu.columbia.ldpd.hrwa.PageData;

public class ProcessPageDataWorker implements Runnable {
	
	public static final int HIGH_MEMORY_USAGE_POLLING_DELAY_IN_MILLIS = 10000; // While waiting in times of high memory usage, check back every X milliseconds to see if a new job can start.
	public static final int NUM_MILLIS_OF_WAIT_TIME_BEFORE_LOGGING_WARNING = 120000; // If we wait for too long, this should be logged so that the user can tweak memory limits.
	
	private File archiveFile;
	
	public ProcessPageDataWorker(String pathToArchiveFile) {
		archiveFile = new File(pathToArchiveFile);
	}

	@Override
	public void run() {
		
		//Don't start the processing yet if current free memory is lower than our minimum threshold for creating new processes
		//boolean alreadyLoggedUnusuallyLongWaitTimeMessage = false;
		long startTime = System.currentTimeMillis();
		int attempts = 0;
		while(HrwaManager.getFreeMemoryInBytes() < HrwaManager.minAvailableMemoryInBytesForNewProcess) {			
			if( (System.currentTimeMillis() - startTime) > NUM_MILLIS_OF_WAIT_TIME_BEFORE_LOGGING_WARNING ) {
				HrwaManager.logger.error("A new " + this.getClass().getSimpleName() + " process has been waiting for at least " + (NUM_MILLIS_OF_WAIT_TIME_BEFORE_LOGGING_WARNING/1000) + " seconds to run.  If you see a lot of messages like this, you may want to allocate more memory to this application, or lower the value of the minAvailableMemoryInBytesForNewProcess command line option.");
				//alreadyLoggedUnusuallyLongWaitTimeMessage = true;
			}
			if((attempts+1) % 4 == 0) {
				//And pass a hint that the garbage collector should run just in case.
				System.gc();
				HrwaManager.logger.error("Ran garbage collector in an effort to free up memory for additional workers.");
			}
			try {
				Thread.sleep(HIGH_MEMORY_USAGE_POLLING_DELAY_IN_MILLIS);
			} catch (InterruptedException e) { e.printStackTrace(); }
			attempts++;
		}
		
		try {
			if( ! hasArchiveFileBeenProcessed(this.archiveFile.getName()) ) {
				processArchiveFile();
			}
		} catch (IOException e) {
			HrwaManager.logger.error("An IOException occurred while checking whether an archive file had already been processed. Archive file name: " + this.archiveFile.getName());
		}
	}
	
	public void processArchiveFile() {
		
		try {
			if(archiveFile.getName().endsWith(".warc.gz")) {
				InputStream is = new FileInputStream(archiveFile);
				WarcReader warcReader = WarcReaderFactory.getReader(is);
				//Get the first record, which is the info record
				ArchiveFileInfoRecord infoRecord = new ArchiveFileInfoRecord(warcReader.getNextRecord());
				while (true) {
					WarcRecord warcRecord = warcReader.getNextRecord();
					if(warcRecord == null) {break;}
					processWarcRecord(warcRecord, infoRecord.archiveFileName);
				}
			}
			else if(archiveFile.getName().endsWith(".arc.gz")) {
				InputStream is = new FileInputStream(archiveFile);
				ArcReader arcReader = ArcReaderFactory.getReader(is);
				//Get the first record, which is the info record
				ArchiveFileInfoRecord infoRecord = new ArchiveFileInfoRecord(arcReader.getNextRecord());
				while (true) {
					ArcRecordBase arcRecord = arcReader.getNextRecord();
					if(arcRecord == null) {break;}
					processArcRecord(arcRecord, infoRecord.archiveFileName);
				}
				
			} else {
				HrwaManager.logger.error("Skipping archive file with unexpected extension: " + archiveFile.getAbsolutePath());
				return;
			}
		
		} catch (FileNotFoundException e) {
			HrwaManager.logger.error(
				"Could not find file: " + archiveFile.getAbsolutePath() + "\n" +
				"Message: " + e.getMessage()
			);
			return;
		} catch (IOException e) {
			HrwaManager.logger.error(
				"IOException encountered while reading file: " + archiveFile.getAbsolutePath() + "\n" +
				"Message: " + e.getMessage()
			);
			return;
		} catch (UnexpectedRecordTypeException e) {
			HrwaManager.logger.error(
				"Skipping archive file because the first record wasn't an info record (and this is unexpected).  Archive file: " + archiveFile.getAbsolutePath() + "\n" +
				"Message: " + e.getMessage()
			);
			return;
		} catch (MissingArchiveHeaderValueException e) {
			HrwaManager.logger.error(
				"Skipping archive file because the first record (an info record) was missing an expected header.  Archive file: " + archiveFile.getAbsolutePath() + "\n" +
				"Message: " + e.getMessage()
			);
		}
		
		//If we finished processing the file and didn't encounter any errors, mark this archive file as processed 
		try {
			this.markArchiveFileAsProcessed();
		} catch (IOException e) {
			HrwaManager.logger.error("An IOException occurred while trying to mark an archive file as processed. Archive file name: " + this.archiveFile.getName());
		}
	}
	
	public void markArchiveFileAsProcessed() throws IOException {
		XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()
		    .startObject()
		    	.field("processed", true)
		        .field("processDate", new Date())
		    .endObject();
		
		IndexResponse response = ElasticsearchHelper.getTransportClient().prepareIndex(HrwaManager.ELASTICSEARCH_ARCHIVE_FILE_INDEX_NAME, HrwaManager.ELASTICSEARCH_ARCHIVE_FILE_TYPE_NAME, this.archiveFile.getName())
	        .setSource(jsonBuilder)
	        .execute()
	        .actionGet();
		
		//And flush changes because we want this to be reflected immediately to avoid any redundant processing
		ElasticsearchHelper.flushIndexChanges(HrwaManager.ELASTICSEARCH_ARCHIVE_FILE_INDEX_NAME);
	}
	
	public boolean hasArchiveFileBeenProcessed(String archiveFileName) throws IOException {
		try {
			SearchResponse response = ElasticsearchHelper.getTransportClient().prepareSearch(HrwaManager.ELASTICSEARCH_ARCHIVE_FILE_INDEX_NAME)
			        .setTypes(HrwaManager.ELASTICSEARCH_ARCHIVE_FILE_TYPE_NAME)
			        .setQuery(QueryBuilders.termQuery("_id", archiveFileName))
			        .setFrom(0).setSize(1)
			        .execute()
			        .actionGet();
			
			SearchHits searchHits = response.getHits();
			if(searchHits.getTotalHits() == 1) {
				return (Boolean)(searchHits.getAt(0).sourceAsMap().get("processed"));
			}
			
			return false;
		} catch (IndexMissingException e) {
			//If this index hasn't been created yet, then none of the archive files have been processed
			return false;
		}
	}
	
	public void processWarcRecord(WarcRecord warcRecord, String archiveFileName) {
		PageData pageData = new PageData(warcRecord, archiveFileName);
		processPageData(pageData);
	}
	
	public void processArcRecord(ArcRecordBase arcRecord, String archiveFileName) {
		PageData pageData = new PageData(arcRecord, archiveFileName);
		processPageData(pageData);
	}
	
	public void processPageData(PageData pageData) {
		if( pageData.shouldBeSkipped() ) { return; }
		
		try {
			pageData.sendToElasticsearch(ElasticsearchHelper.getTransportClient());
		} catch (ElasticsearchException e) {
			HrwaManager.logger.error("ElasticsearchException encountered while sending PageData to Elasticsearch. File: " + pageData.archiveFileName + ", Byte Offset: " + pageData.archiveFileOffset + ", Error Message: " + e.getMessage());
		} catch (IOException e) {
			HrwaManager.logger.error("IOException encountered while sending PageData to Elasticsearch. File: " + pageData.archiveFileName + ", Byte Offset: " + pageData.archiveFileOffset + ", Error Message: " + e.getMessage());
		}
	}

}
