package edu.columbia.ldpd.hrwa;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import edu.columbia.ldpd.hrwa.util.MetadataUtils;

public class MetadataUtilsTest {

    @Test
    public void hostStringExtraction() throws IOException {
		assertEquals("example.com", MetadataUtils.extractHostString("http://www.example.com"));
		assertEquals("example.com", MetadataUtils.extractHostString("http://www1.example.com"));
    }

}
