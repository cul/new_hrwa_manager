package edu.columbia.ldpd.hrwa;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.tika.Tika;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.MagicDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.junit.Test;
import org.xml.sax.SAXException;

import edu.columbia.ldpd.hrwa.tasks.workers.ProcessPageDataWorker;

public class TikaExtractionTest {
	
	@Test
    public void canExtractTitleAndFulltextAndMimetypeFromPDF() throws IOException, TikaException {
		InputStream stream = this.getClass().getResourceAsStream("/test.pdf");
		
		Parser parser = new AutoDetectParser();
	    Metadata metadata = new Metadata();
	    BodyContentHandler handler = new BodyContentHandler();
	    
	    try {
	        parser.parse(stream, handler, metadata, new ParseContext());
	        String title = metadata.get(TikaCoreProperties.TITLE);
		    String fulltext = PageData.WHITESPACE_PATTERN.matcher(handler.toString()).replaceAll(" ").trim();
		    
		    DefaultDetector detector = new DefaultDetector();
		    String detectedMimetype = detector.detect(stream, metadata).toString();
		    
		    assertEquals("application/pdf", detectedMimetype);
			assertEquals("Artículo de prueba", title);
			assertTrue(fulltext.startsWith("Artículo de prueba Martes"));
			assertTrue(fulltext.endsWith("Pedro 1 / 1"));
		    
	    } catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    finally {
	        stream.close();
	    }
	}
	
	@Test
    public void canExtractTitleFromPDFUsingPageDataObject() throws IOException, TikaException {
		PageData pageData = new PageData();
		InputStream stream = this.getClass().getResourceAsStream("/test.pdf");
		pageData.extractMimeTypeAndFulltextfromInputStream(stream);
		
		assertEquals("Artículo de prueba", pageData.title);
		assertEquals("application/pdf", pageData.detectedMimetype);
		assertTrue(pageData.fulltext.startsWith("Artículo de prueba Martes"));
		assertTrue(pageData.fulltext.endsWith("Pedro 1 / 1"));
	}
	
}
