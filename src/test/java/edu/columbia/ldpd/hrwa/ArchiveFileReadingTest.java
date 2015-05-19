package edu.columbia.ldpd.hrwa;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;

import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.warc.WARCReaderFactory;
import org.junit.Test;
import org.jwat.arc.ArcReader;
import org.jwat.arc.ArcReaderFactory;
import org.jwat.arc.ArcRecord;
import org.jwat.arc.ArcRecordBase;
import org.jwat.common.HttpHeader;
import org.jwat.common.Payload;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

public class ArchiveFileReadingTest {

    @Test
    public void readArcFile() throws IOException {
    	//Content length of records should be > 0 if the ARC file is read successfully
    	
    	long contentLength = 0;
    	
    	Iterator<ArchiveRecord> recordIterator = ARCReaderFactory.get(this.getClass().getResource("/ARCHIVEIT-1068-Columbia-HRWEB-20090218094035-00016-crawling09.us.archive.org.arc.gz")).iterator();
		while (recordIterator.hasNext()) {
			ArchiveRecord archiveRecord = recordIterator.next();
			ArchiveRecordHeader header = archiveRecord.getHeader();
			contentLength += header.getContentLength();
			break;  // Break because we only need to successfully read one record to prove that the file is readable
		}
		
		assertTrue(contentLength > 0);
    }
    
    @Test
    public void readWarcFile() throws IOException {
    	//Content length of records should be > 0 if the WARC file is read successfully
    	
    	long contentLength = 0;
    	
    	Iterator<ArchiveRecord> recordIterator = WARCReaderFactory.get(this.getClass().getResource("/ARCHIVEIT-1068-QUARTERLY-20748-20131004123919268-00808-wbgrp-crawl066.us.archive.org-6444.warc.gz")).iterator();
		while (recordIterator.hasNext()) {
			ArchiveRecord archiveRecord = recordIterator.next();
			ArchiveRecordHeader header = archiveRecord.getHeader();
			contentLength += header.getContentLength();
			break;  // Break because we only need to successfully read one record to prove that the file is readable
		}
		
		assertTrue(contentLength > 0);
    }
    
    @Test
    public void alternateReadArcFile() throws IOException {
    	//Content length of records should be > 0 if the ARC file is read successfully
    	
    	long contentLength = 0;
    	
    	ArcReader arcReader = ArcReaderFactory.getReader(this.getClass().getResourceAsStream("/ARCHIVEIT-1068-Columbia-HRWEB-20090218094035-00016-crawling09.us.archive.org.arc.gz"));
    	
    	
		while (true) {
			ArcRecordBase arcRecord = arcReader.getNextRecord();
			if(arcRecord == null) {break;}
			
//			if(arcRecord.getHttpHeader() != null) {
//				String statusCode = arcRecord.getHttpHeader().statusCodeStr;
//				if(statusCode != null) {
//					System.out.println("arc: " + arcRecord.getHttpHeader().statusCodeStr);				
//				}
//			}
			
			Payload payload = arcRecord.getPayload();
			contentLength += payload.getTotalLength();
			
			break;  // Break because we only need to successfully read one record to prove that the file is readable
		}
		
		assertTrue(contentLength > 0);
    }
    
    @Test
    public void alternateReadWarcFile() throws IOException {
    	//Content length of records should be > 0 if the WARC file is read successfully
    	
    	long contentLength = 0;
    	
    	WarcReader warcReader = WarcReaderFactory.getReader(this.getClass().getResourceAsStream("/ARCHIVEIT-1068-QUARTERLY-20748-20131004123919268-00808-wbgrp-crawl066.us.archive.org-6444.warc.gz"));
    	
    	
		while (true) {
			WarcRecord warcRecord = warcReader.getNextRecord();
			if(warcRecord == null) {break;}
			
//			if(warcRecord.getHttpHeader() != null) {
//				String statusCode = warcRecord.getHttpHeader().statusCodeStr;
//				if(statusCode != null) {
//					System.out.println("WARC: " + warcRecord.getHttpHeader().statusCodeStr);				
//				}
//			}
			
			Payload payload = warcRecord.getPayload();
			contentLength += payload.getTotalLength();
			
			break;  // Break because we only need to successfully read one record to prove that the file is readable
		}
		
		assertTrue(contentLength > 0);
    }

}
