package edu.columbia.ldpd.hrwa;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.junit.Test;

import edu.columbia.ldpd.hrwa.tasks.workers.ProcessPageDataWorker;

public class TikaExtractionTest {
	
	@Test
    public void canExtractMimeTypeAndFulltextFromPDF() throws IOException, TikaException {
		InputStream stream = this.getClass().getResourceAsStream("/test.pdf");
		
		Tika tika = new Tika();
		tika.setMaxStringLength(ProcessPageDataWorker.MAX_FULLTEXT_CHARS_TO_EXTRACT);
		
		String detectedMimetype = tika.detect(stream);
		//Extract text and compress whitespace to reduce storage requirements
		String fulltext = PageData.WHITESPACE_PATTERN.matcher(tika.parseToString(stream)).replaceAll(" ").trim();
		
		assertEquals("application/pdf", detectedMimetype);
		assertTrue(fulltext.startsWith("Artículo de prueba Martes"));
		assertTrue(fulltext.endsWith("Pedro 1 / 1"));
    }

}
