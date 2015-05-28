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
		System.out.println("Ran once");
		
//		System.out.println("Start: " + HrwaManager.getCurrentAppMemoryUsageMessage());
//		InputStream stream = this.getClass().getResourceAsStream("/ARCHIVEIT-1068-QUARTERLY-20748-20131004123919268-00808-wbgrp-crawl066.us.archive.org-6444.warc");
//		System.out.println("After open inputstream: " + HrwaManager.getCurrentAppMemoryUsageMessage());
//		Tika tika = new Tika();
//		tika.setMaxStringLength(ProcessPageDataWorker.MAX_FULLTEXT_CHARS_TO_EXTRACT);
//		
//		String detectedMimetype = tika.detect(stream);
//		System.out.println("After detect: " + HrwaManager.getCurrentAppMemoryUsageMessage());
//		//Extract text and compress whitespace to reduce storage requirements
//		String fulltext = PageData.whitespaceCompressorPattern.matcher(tika.parseToString(stream)).replaceAll(" ").trim();
//		System.out.println("After fulltext: " + HrwaManager.getCurrentAppMemoryUsageMessage());
		
		
		System.out.println("Start: " + HrwaManager.getCurrentAppMemoryUsageMessage());
		InputStream stream = this.getClass().getResourceAsStream("/ARCHIVEIT-1068-QUARTERLY-20748-20131004123919268-00808-wbgrp-crawl066.us.archive.org-6444.warc");
		System.out.println("After reading stream: " + HrwaManager.getCurrentAppMemoryUsageMessage());
		byte[] theData = IOUtils.toByteArray(stream);
		System.out.println("After saving as byte array: " + HrwaManager.getCurrentAppMemoryUsageMessage());
		InputStream newStream = new ByteArrayInputStream(theData);
		System.out.println("After creating new stream: " + HrwaManager.getCurrentAppMemoryUsageMessage());
		
		Tika tika = new Tika();
		tika.setMaxStringLength(ProcessPageDataWorker.MAX_FULLTEXT_CHARS_TO_EXTRACT);
		
		String detectedMimetype = tika.detect(newStream);
		System.out.println("After detecting mimetype: " + HrwaManager.getCurrentAppMemoryUsageMessage());
		//Extract text and compress whitespace to reduce storage requirements
		String fulltext = PageData.WHITESPACE_PATTERN.matcher(tika.parseToString(newStream)).replaceAll(" ").trim();
		System.out.println("After extracting fulltext: " + HrwaManager.getCurrentAppMemoryUsageMessage());
		
		
		assertEquals("application/pdf", detectedMimetype);
		assertTrue(fulltext.startsWith("Artículo de prueba Martes"));
		assertTrue(fulltext.endsWith("Pedro 1 / 1"));
    }

}
