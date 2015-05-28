package edu.columbia.ldpd.hrwa;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
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
