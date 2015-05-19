package edu.columbia.ldpd.hrwa.tasks.workers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.Iterator;

import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.warc.WARCReaderFactory;

import edu.columbia.ldpd.hrwa.HrwaManager;
import edu.columbia.ldpd.hrwa.PageData;
import edu.columbia.ldpd.hrwa.util.MysqlHelper;

public class ProcessPageDataWorker implements Runnable {
	
	public static final int HIGH_MEMORY_USAGE_POLLING_DELAY_IN_MILLIS = 5000; // While waiting in times of high memory usage, check back every X milliseconds to see if a new job can start.
	public static final int NUM_MILLIS_OF_WAIT_TIME_BEFORE_LOGGING_WARNING = 120000; // If we wait for too long, this should be logged so that the user can tweak memory limits.
	public static final int MAX_FULLTEXT_CHARS_TO_EXTRACT = 100000; // Higher numbers will result in higher memory usage for larger files
	
	//private Connection conn;
	private File archiveFile;
	
	public ProcessPageDataWorker(String pathToArchiveFile) {
		archiveFile = new File(pathToArchiveFile);
	}

	@Override
	public void run() {
		
		//Don't start the processing yet if current free memory is lower than our minimum threshold for creating new processes
		boolean alreadyLoggedUnusuallyLongWaitTimeMessage = false;
		long startTime = System.currentTimeMillis();
		while(HrwaManager.getFreeMemoryInBytes() < HrwaManager.minAvailableMemoryInBytesForNewProcess) {			
			if( ! alreadyLoggedUnusuallyLongWaitTimeMessage && (System.currentTimeMillis() - startTime) > NUM_MILLIS_OF_WAIT_TIME_BEFORE_LOGGING_WARNING) {
				HrwaManager.logger.warn("A new " + this.getClass().getSimpleName() + " process has been waiting for at least " + (NUM_MILLIS_OF_WAIT_TIME_BEFORE_LOGGING_WARNING/1000) + " seconds to run.  If you see a lot of messages like this, you may want to allocate more memory to this application, or lower the value of the minAvailableMemoryInBytesForNewProcess command line option.");
				alreadyLoggedUnusuallyLongWaitTimeMessage = true;	
			}
			try { Thread.sleep(HIGH_MEMORY_USAGE_POLLING_DELAY_IN_MILLIS); } catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		//Get a MySQL connection for this worker
		//this.conn = MysqlHelper.getNewDBConnection();
		
		
		processArchiveFile();
		
		//Be sure to close the connection when we're done
		//MysqlHelper.closeConnection(conn);
		
		
	}
	
	public void processArchiveFile() {
		
		Iterator<ArchiveRecord> archiveRecordIterator;
		
		try {
			if(archiveFile.getName().endsWith(".warc.gz")) {
				archiveRecordIterator = WARCReaderFactory.get(archiveFile).iterator();
			}
			else if(archiveFile.getName().endsWith(".arc.gz")) {
				archiveRecordIterator = ARCReaderFactory.get(archiveFile).iterator();
			} else {
				HrwaManager.logger.error("Skipping archive file with unexpected extension: " + archiveFile.getAbsolutePath());
				return;
			}
		
		} catch (IOException e) {
			HrwaManager.logger.error(
				"Skipping archive file due to IOException: " + archiveFile.getAbsolutePath() + "\n" +
				"Message: " + e.getMessage()
			);
			return;
		}
		
		while (archiveRecordIterator.hasNext()) {
			processArchiveRecord(archiveRecordIterator.next());
		}
	}
	
	public void processArchiveRecord(ArchiveRecord archiveRecord) {
		PageData pageData = new PageData(archiveRecord);
		
	}

}