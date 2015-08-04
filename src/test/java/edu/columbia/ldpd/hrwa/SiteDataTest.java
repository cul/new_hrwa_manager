package edu.columbia.ldpd.hrwa;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.jwat.arc.ArcReader;
import org.jwat.arc.ArcReaderFactory;
import org.jwat.arc.ArcRecordBase;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

import edu.columbia.ldpd.hrwa.util.MetadataUtils;

public class SiteDataTest {

	@Test
    public void extractDataFromMarcXmlFile() throws Exception {
    	
		SiteData.overrideRelatedHostsDataFromStreamSource(this.getClass().getResourceAsStream("/related_hosts.csv")); //Because we don't want to rely on command line args in our test
		
    	//Marc XML File
    	SiteData siteData = new SiteData(this.getClass().getResourceAsStream("/sample-marcxml.xml"));
    	
    	//originalUrl
    	ArrayList<String> expectedOriginalUrl = new ArrayList<String>();
    	expectedOriginalUrl.add("http://www.518.org");
		assertEquals(expectedOriginalUrl, siteData.originalUrl);
		
		//archivedUrl
    	ArrayList<String> expectedArchivedUrl = new ArrayList<String>();
    	expectedArchivedUrl.add("http://wayback.archive-it.org/1068/*/http://www.518.org");
		assertEquals(expectedArchivedUrl, siteData.archivedUrl);
    	
    	//hostString
    	ArrayList<String> expectedHostStrings = new ArrayList<String>();
    	expectedHostStrings.add("518.org");
		assertEquals(expectedHostStrings, siteData.hostStrings);
		
		//relatedHostStrings
		HashSet<String> expectedRelatedHostStrings = new HashSet<String>();
		expectedRelatedHostStrings.add("hrwa-test-mapping.columbia.edu");
		assertEquals(expectedRelatedHostStrings, siteData.relatedHostStrings);
		
		//organizationType
		assertEquals("Non-governmental organizations", siteData.organizationType);
		
		//subject
		HashSet<String> expectedSubject = new HashSet<String>();
		expectedSubject.add("Kwangju Uprising, Kwangju-si, Korea, 1980");
		expectedSubject.add("Civil rights movements");
		expectedSubject.add("Human rights");
		assertEquals(expectedSubject, siteData.subject);
		
		//geographicFocus
		ArrayList<String> expectedGeographicFocus = new ArrayList<String>();
		expectedGeographicFocus.add("Korea (South)");
		assertEquals(expectedGeographicFocus, siteData.geographicFocus);

		//organizationBasedIn
		assertEquals("Korea (South)", siteData.organizationBasedIn);
		
		//expectedLanguage
		ArrayList<String> expectedLanguage = new ArrayList<String>();
		expectedLanguage.add("Korean");
		expectedLanguage.add("English");
		assertEquals(expectedLanguage, siteData.language);
		
		//title
		assertEquals("5·18 Kinyŏm Chaedan May 18 Memorial Foundation", siteData.title);
		
		//alternateTitle
		ArrayList<String> expectedAlternateTitle = new ArrayList<String>();
		expectedAlternateTitle.add("May 18 Memorial Foundation");
		assertEquals(expectedAlternateTitle, siteData.alternativeTitle);
		
		//creatorName
		ArrayList<String> expectedCreatorName = new ArrayList<String>();
		expectedCreatorName.add("5.18 Kinyŏm Chaedan (Korea)");
		assertEquals(expectedCreatorName, siteData.creatorName);
		
		assertEquals("The May 18 Memorial Foundation is a non-profit organization "
				+ "established on August 30, 1994 by the surviving victims of the "
				+ "1980 Gwangju Democratic Uprising, the victims families, and the "
				+ "citizens of Gwangju. The foundation aims to commemorate as well "
				+ "as continue the spirit and struggle and solidarity of the May 18 "
				+ "Uprising; to contribute to the peaceful reunification of Korea; "
				+ "and to work towards peace and human rights throughout the world.", siteData.summary);
		
		assertEquals("7832247", siteData.bibId);
		
		assertTrue(siteData.isValid());
    }
	
	@Test
	public void siteDataValidation() {
		SiteData siteData = new SiteData();
		assertTrue( siteData.isValid() == false);
		
		ArrayList<String> expectedValidationErrors = new ArrayList<String>();
		expectedValidationErrors.add("Missing bibId.");
		expectedValidationErrors.add("Missing originalUrl.");
		expectedValidationErrors.add("Missing archivedUrl.");
		expectedValidationErrors.add("Missing hostString (derived from originalUrl).");
		expectedValidationErrors.add("Missing organizationType.");
		expectedValidationErrors.add("Missing subject.");
		expectedValidationErrors.add("Missing language.");
		expectedValidationErrors.add("Missing title.");
		expectedValidationErrors.add("Missing creatorName.");
		
		ArrayList<String> validationErrors = siteData.getValidationErrors();
		Collections.sort(expectedValidationErrors); //Sort so we don't have to worry about message order
		Collections.sort(validationErrors); //Sort so we don't have to worry about message order
		assertEquals(expectedValidationErrors, validationErrors);
	}
	
    
    
    @Test
    public void serializeSiteDataToElasticsearchJson() {
    	//SiteData siteData = new SiteData(this.getClass().getResourceAsStream("/sample-marcxml.xml"));

//		String generatedJson = null;
//		try {
//			generatedJson = siteData.toElasticsearchJsonBuilder().string();
//		} catch (IOException e) {
//			System.out.println("Unexpected IOException: " + e.getMessage());
//			e.printStackTrace();
//		}
//		
//		String expectedJson = "{" +
//			"\"originalUrl\":\"http://www.example.com\"," +
//			"\"hostString\":\"example.com\"," +
//			"\"archiveFileName\":\"some-archive-file-12345.warc.gz\"," +
//			"\"archiveFileOffset\":42," +
//			"\"contentLength\":56779," +
//			"\"crawlDate\":\"20150728\"," +
//			"\"fulltext\":\"This is the full text.\"," +
//			"\"mimetypeFromHeader\":\"text/plain\"," +
//			"\"detectedMimetype\":\"text/plain\"" +
//		"}";
//		
//		assertEquals(expectedJson, generatedJson);
    }

}
