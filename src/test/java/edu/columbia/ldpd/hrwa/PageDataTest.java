package edu.columbia.ldpd.hrwa;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.jwat.arc.ArcReader;
import org.jwat.arc.ArcReaderFactory;
import org.jwat.arc.ArcRecordBase;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

import edu.columbia.ldpd.hrwa.util.MetadataUtils;

public class PageDataTest {

	@Test
    public void extractDataFromArcFile() throws Exception {
    	
    	//ARC File
    	ArcReader arcReader = ArcReaderFactory.getReader(this.getClass().getResourceAsStream("/ARCHIVEIT-1068-Columbia-HRWEB-20090218094035-00016-crawling09.us.archive.org.arc.gz"));
    	
    	//Get the first record, which is the arc info record
		ArchiveFileInfoRecord infoRecord = new ArchiveFileInfoRecord(arcReader.getNextRecord());
		
		boolean extractedAtLeastOneTitle = false;
		
		int counter = 0;
		while (true) {
			ArcRecordBase arcRecord = arcReader.getNextRecord();
			if(arcRecord == null) {break;}
			
			PageData pageData = new PageData(arcRecord, infoRecord.archiveFileName);
			
			if( pageData.shouldBeSkipped() ) { continue; }
			
			assertTrue(pageData.originalUrl != null);
			assertTrue(pageData.hostString != null);
			assertTrue(pageData.hostStringWithPath != null);
			assertTrue(pageData.archiveFileName != null);
			assertTrue(pageData.archiveFileOffset != 0);
			assertTrue(pageData.contentLength != 0);
			assertTrue(pageData.crawlDate != null);
			assertTrue(pageData.fulltext != null);
			assertTrue(pageData.mimetypeFromHeader != null);
			assertTrue(pageData.detectedMimetype != null);
			
			if(pageData.title != null) {
				extractedAtLeastOneTitle = true;
			}
			
			counter++;
			if(counter == 50) {break;} // Only testing the first 50 records
		}
		
		assertTrue(extractedAtLeastOneTitle);
    }
    
    @Test
    public void extractDataFromWarcFile() throws Exception {
    	
		WarcReader warcReader = WarcReaderFactory.getReader(this.getClass().getResourceAsStream("/ARCHIVEIT-1068-QUARTERLY-20748-20131004123919268-00808-wbgrp-crawl066.us.archive.org-6444.warc.gz"));
		
		//Get the first record, which is the warc info record
		ArchiveFileInfoRecord infoRecord = new ArchiveFileInfoRecord(warcReader.getNextRecord());
		
		boolean extractedAtLeastOneTitle = false;
		
		int counter = 0;
		while (true) {
			WarcRecord warcRecord = warcReader.getNextRecord();
			if(warcRecord == null) {break;}

			PageData pageData = new PageData(warcRecord, infoRecord.archiveFileName);
			
			if( pageData.shouldBeSkipped() ) { continue; }
			
			assertTrue(pageData.originalUrl != null);
			assertTrue(pageData.hostString != null);
			assertTrue(pageData.hostStringWithPath != null);
			assertTrue(pageData.archiveFileName != null);
			assertTrue(pageData.archiveFileOffset != 0);
			assertTrue(pageData.contentLength != 0);
			assertTrue(pageData.crawlDate != null);
			assertTrue(pageData.fulltext != null);
			assertTrue(pageData.mimetypeFromHeader != null);
			assertTrue(pageData.detectedMimetype != null);
			
			if(pageData.title != null) {
				extractedAtLeastOneTitle = true;
			}
			
			counter++;
			if(counter == 50) {break;} // Only testing the first 50 records
		}
		
		assertTrue(extractedAtLeastOneTitle);
    }
    
    @Test
    public void serializePageDataToElasticsearchJson() {
    	PageData pageData = new PageData();
    	
    	pageData.originalUrl = "http://www.example.com/123";
    	pageData.hostStringWithPath = MetadataUtils.extractHostStringWithPath(pageData.originalUrl);
    	pageData.hostString = MetadataUtils.extractHostString(pageData.originalUrl);
		pageData.archiveFileName = "some-archive-file-12345.warc.gz";
		pageData.archiveFileOffset = 42;
		pageData.contentLength = 56779;
		pageData.crawlDate = "20150728";
		pageData.fulltext = "This is the full text.";
		pageData.mimetypeFromHeader = "text/plain";
		pageData.detectedMimetype = "text/plain";
		pageData.title = "The Title";

		String generatedJson = null;
		try {
			generatedJson = pageData.toElasticsearchJsonBuilder().string();
		} catch (IOException e) {
			System.out.println("Unexpected IOException: " + e.getMessage());
			e.printStackTrace();
		}
		
		String expectedJson = "{" +
			"\"originalUrl\":\"http://www.example.com/123\"," +
			"\"hostString\":\"example.com\"," +
			"\"hostStringWithPath\":\"example.com/123\"," +
			"\"archiveFileName\":\"some-archive-file-12345.warc.gz\"," +
			"\"archiveFileOffset\":42," +
			"\"contentLength\":56779," +
			"\"crawlDate\":\"20150728\"," +
			"\"fulltext\":\"This is the full text.\"," +
			"\"mimetypeFromHeader\":\"text/plain\"," +
			"\"detectedMimetype\":\"text/plain\"," +
			"\"title\":\"The Title\"" +
		"}";
		
		assertEquals(expectedJson, generatedJson);
    }

}
