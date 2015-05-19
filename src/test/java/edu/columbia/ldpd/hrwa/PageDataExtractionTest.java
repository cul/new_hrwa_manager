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

public class PageDataExtractionTest {

	@Ignore @Test
    public void extractDataFromArcFile() throws IOException {
    	
    	//ARC File
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
			assertTrue(pageData.recordIdentifier != null);
			assertTrue(pageData.readerIdentifier != null);
		    assertTrue(pageData.statusCode != 0);
		}
    }
    
    @Test
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
			assertTrue(pageData.recordIdentifier != null);
			assertTrue(pageData.readerIdentifier != null);
		    assertTrue(pageData.statusCode != 0);
		}
    }
    
    @Test
    /**
     * Certain types of ArchiveRecords should be skipped.
     * @throws IOException
     */
    public void skipIgnoredRecordsTest() throws IOException {
    	
    	// TODO: Implement this test
    	
//    	PageData page = new PageData();
//		
//		if( page.shouldBeSkipped() ) {
//			numberOfSkippedRecords++;
//		}
    	
    }

}
