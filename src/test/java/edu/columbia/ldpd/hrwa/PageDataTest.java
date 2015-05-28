package edu.columbia.ldpd.hrwa;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;

import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.warc.WARCReaderFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.jwat.arc.ArcReader;
import org.jwat.arc.ArcReaderFactory;
import org.jwat.arc.ArcRecordBase;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

import edu.columbia.ldpd.hrwa.ArchiveFileInfoRecord.MissingArchiveHeaderValueException;
import edu.columbia.ldpd.hrwa.ArchiveFileInfoRecord.UnexpectedRecordTypeException;

public class PageDataTest {
	
	@Ignore @Test
    public void extractDataFromArcFile() throws IOException {
    	
		Iterator<ArchiveRecord> recordIterator = ARCReaderFactory.get(this.getClass().getResource("/ARCHIVEIT-1068-Columbia-HRWEB-20090218094035-00016-crawling09.us.archive.org.arc.gz")).iterator();
		while (recordIterator.hasNext()) {
			ArchiveRecord archiveRecord = recordIterator.next();
			
			PageData pageData = new PageData(archiveRecord);
			
			if( pageData.shouldBeSkipped() ) { continue; }
			
			System.out.println("Processing");
			assertTrue(pageData.originalUrl != null);
			assertTrue(pageData.hostString != null);
			assertTrue(pageData.archiveFileName != null);
			assertTrue(pageData.archiveFileOffset != 0);
			assertTrue(pageData.contentLength != 0);
			assertTrue(pageData.crawlDate != null);
			assertTrue(pageData.fulltext != null);
			assertTrue(pageData.mimetypeFromHeader != null);
			assertTrue(pageData.detectedMimetype != null);
		}
    }
    
	@Ignore @Test
    public void extractDataFromWarcFile() throws IOException {
    	
    	Iterator<ArchiveRecord> recordIterator = WARCReaderFactory.get(this.getClass().getResource("/ARCHIVEIT-1068-QUARTERLY-20748-20131004123919268-00808-wbgrp-crawl066.us.archive.org-6444.warc.gz")).iterator();
		while (recordIterator.hasNext()) {
			ArchiveRecord archiveRecord = recordIterator.next();

			PageData pageData = new PageData(archiveRecord);
			
			if( pageData.shouldBeSkipped() ) { continue; }
			
			System.out.println("Processing");
			assertTrue(pageData.originalUrl != null);
			assertTrue(pageData.hostString != null);
			assertTrue(pageData.archiveFileName != null);
			assertTrue(pageData.archiveFileOffset != 0);
			assertTrue(pageData.contentLength != 0);
			assertTrue(pageData.crawlDate != null);
			assertTrue(pageData.fulltext != null);
			assertTrue(pageData.mimetypeFromHeader != null);
			assertTrue(pageData.detectedMimetype != null);
			
			System.out.println(pageData.fulltext);
//			System.out.println(pageData.mimetypeFromHeader);
//			System.out.println(pageData.detectedMimetype);
//			System.out.println(pageData.crawlDate + "/" + pageData.originalUrl);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }

	@Ignore @Test
    public void altExtractDataFromArcFile() throws Exception {
    	
    	//ARC File
    	ArcReader arcReader = ArcReaderFactory.getReader(this.getClass().getResourceAsStream("/ARCHIVEIT-1068-Columbia-HRWEB-20090218094035-00016-crawling09.us.archive.org.arc.gz"));
    	
    	//Get the first record, which is the arc info record
		ArchiveFileInfoRecord infoRecord = new ArchiveFileInfoRecord(arcReader.getNextRecord());
    	
		int counter = 0;
		
		while (true) {
			ArcRecordBase arcRecord = arcReader.getNextRecord();
			if(arcRecord == null) {break;}
			
			PageData pageData = new PageData(arcRecord, infoRecord.archiveFileName);
			
			if( pageData.shouldBeSkipped() ) { continue; }
			
			assertTrue(pageData.originalUrl != null);
			assertTrue(pageData.hostString != null);
			assertTrue(pageData.archiveFileName != null);
			assertTrue(pageData.archiveFileOffset != 0);
			assertTrue(pageData.contentLength != 0);
			assertTrue(pageData.crawlDate != null);
			assertTrue(pageData.fulltext != null);
			assertTrue(pageData.mimetypeFromHeader != null);
			assertTrue(pageData.detectedMimetype != null);
			
			//System.out.println(pageData.fulltext.length() > 100 ? pageData.fulltext.substring(0, 100) : pageData.fulltext);
			
			//counter++;
			//System.out.println("Processed: " + counter);
		}
    }
    
    @Test
    public void altExtractDataFromWarcFile() throws Exception {
    	
		WarcReader warcReader = WarcReaderFactory.getReader(this.getClass().getResourceAsStream("/ARCHIVEIT-1068-QUARTERLY-20748-20131004123919268-00808-wbgrp-crawl066.us.archive.org-6444.warc.gz"));
		
		//Get the first record, which is the warc info record
		ArchiveFileInfoRecord infoRecord = new ArchiveFileInfoRecord(warcReader.getNextRecord());
		
		int counter = 0;
		while (true) {
			WarcRecord warcRecord = warcReader.getNextRecord();
			if(warcRecord == null) {break;}

			PageData pageData = new PageData(warcRecord, infoRecord.archiveFileName);
			
			if( pageData.shouldBeSkipped() ) { continue; }
			
			assertTrue(pageData.originalUrl != null);
			assertTrue(pageData.hostString != null);
			assertTrue(pageData.archiveFileName != null);
			assertTrue(pageData.archiveFileOffset != 0);
			assertTrue(pageData.contentLength != 0);
			assertTrue(pageData.crawlDate != null);
			assertTrue(pageData.fulltext != null);
			assertTrue(pageData.mimetypeFromHeader != null);
			assertTrue(pageData.detectedMimetype != null);
			
			//counter++;
			//System.out.println("Processed: " + counter);
		}
    }

}
