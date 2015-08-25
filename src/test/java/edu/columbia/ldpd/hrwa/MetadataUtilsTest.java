package edu.columbia.ldpd.hrwa;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import edu.columbia.ldpd.hrwa.util.MetadataUtils;

public class MetadataUtilsTest {

    @Test
    public void hostStringExtraction() throws IOException {
    	assertEquals("example.com", MetadataUtils.extractHostString("http://example.com/abc"));
		assertEquals("example.com", MetadataUtils.extractHostString("http://www.example.com/123"));
		assertEquals("example.com", MetadataUtils.extractHostString("https://www1.example.com/456"));
    }
    
    @Test
    public void hostStringWithPathExtraction() throws IOException {
    	assertEquals("example.com/abc", MetadataUtils.extractHostStringWithPath("http://example.com/abc"));
		assertEquals("example.com/123", MetadataUtils.extractHostStringWithPath("http://www.example.com/123"));
		assertEquals("example.com/456", MetadataUtils.extractHostStringWithPath("https://www1.example.com/456"));
		
		//And it removes a standalone trailing slash
		assertEquals("example.com", MetadataUtils.extractHostStringWithPath("https://www1.example.com/"));
    }

}
