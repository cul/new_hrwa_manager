package edu.columbia.ldpd.hrwa;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;

import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.warc.WARCReaderFactory;
import org.junit.Test;

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

}
