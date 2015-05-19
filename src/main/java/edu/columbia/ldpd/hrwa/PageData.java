package edu.columbia.ldpd.hrwa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.AutoDetectParser;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.arc.ARCRecord;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.warc.WARCRecord;
import org.jwat.warc.WarcReader;

import edu.columbia.ldpd.hrwa.tasks.workers.ProcessPageDataWorker;
import edu.columbia.ldpd.hrwa.util.MetadataUtils;

public class PageData {
	
	public String 		originalUrl; //Original url of this crawled record.
	public String 		archiveFileName; // Name of the archive file (warc/arc) that this record came from.
	public long 		archiveFileOffset; // Byte offset address of the record in the archive file.
	public long			contentLength; // Size of the content returned in the HTTP response in bytes. Largest will probably be video.
	public String 		crawlDate; // Crawl date for this record.  Format: 
	public String		mimetypeFromHeader; // Mimetype supplied by the archive file header.
	public String		recordIdentifier; // Unique identifier for this record.  Of the format: record_date/url.
	public String		readerIdentifier; // Path to archive file on the filesystem at time of data extraction.
	public int			statusCode; // HTTP response status code at record crawl time.
	
	public String 		hostString; //Truncated url, only including hostname and removing www, www1, www2, etc. if present.
	public String 		detectedMimetype; // Mimetype detected by the HRWA Manager application.  null if mimetype could not be detected.
	public String		fulltext; // Full text extracted from record content.
	
	public boolean		forceSkipThisRecord = false; // If this flag is set to true, then this record will always appear as skippable
	
	public static Pattern whitespaceCompressorPattern = Pattern.compile("\\s+");
	
	public PageData() {
		
	}

	/**
	 * Creates a new PageData record from an ArchiveRecord
	 * @param archiveRecord
	 */
	public PageData(ArchiveRecord archiveRecord) {
		
		ArchiveRecordHeader header = archiveRecord.getHeader();
		
		this.originalUrl = header.getUrl();
		this.archiveFileName = header.getReaderIdentifier();
		this.archiveFileOffset = header.getOffset();
		this.contentLength = header.getContentLength();
		this.crawlDate = header.getDate();
		
		this.mimetypeFromHeader = header.getMimetype();
		
		//Some fields work differently for ARC vs WARC files
		if (archiveRecord instanceof ARCRecord) {
			//Record identifier
			this.recordIdentifier = header.getRecordIdentifier();
			
			//Status code
			ARCRecord arcRecord = (ARCRecord)archiveRecord;
			this.statusCode = arcRecord.getStatusCode();
			//Ignore anything with a non-200 status because it's not useful for us
			if(this.statusCode != 200) {
				this.forceSkipThisRecord = true;
			}
			
		} else if (archiveRecord instanceof WARCRecord) {
			//Record identifier
			this.recordIdentifier = header.getHeaderValue(WARCRecord.HEADER_KEY_ID).toString();
			
			// WARC Files say what their WARC type is, so we can use this to ignore irrelevant ones.
			if( ! header.getHeaderValue(WARCRecord.HEADER_KEY_TYPE).toString().equals("response") ) {
				//We only want to process WARC files of type "response"
				this.forceSkipThisRecord = true;
			}
			
		}
		
		this.readerIdentifier = header.getReaderIdentifier();
		//statusCode = header.getHeaderFields() 
		
		
		//System.out.println(header.getHeaderFields());
		//System.out.println("\n\n");
		
		
		
		
		
		
		
		// Don't do heavier mimetype and fulltext extraction work if we're going to skip this record 
		if( shouldBeSkipped() ) {
			return;
		}
		
		// Derive hostString from originalUrl when applicable (only runs if shouldBeSkipped() passes, since we check for the presence of an originalUrl value)
		this.hostString = MetadataUtils.extractHostString(this.originalUrl);
		
		//Use Tika for mimetype detection and text extraction
		//Extract from file
		//Note: If we need to do this in chunks due to memory constraints, there's a how-to example here: https://tika.apache.org/1.8/examples.html
		try {
			//Note: Closing a ByteArrayOutputStream has no effect.  See: https://docs.oracle.com/javase/7/docs/api/java/io/ByteArrayOutputStream.html
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
			archiveRecord.dump(byteArrayOutputStream);
			
			InputStream stream = null;
			try {
				stream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
				
				Tika tika = new Tika();
				tika.setMaxStringLength(ProcessPageDataWorker.MAX_FULLTEXT_CHARS_TO_EXTRACT);
				//Detect the mimetype
				// Note: detect() only reads part of the file, does not close the stream, and actually rewinds the stream.
				this.detectedMimetype = tika.detect(stream);
				//Extract text and compress whitespace to reduce storage requirements
				this.fulltext = whitespaceCompressorPattern.matcher(tika.parseToString(stream)).replaceAll(" ");
				
				
				
				
				//Alternate method of parsing, using an AutoDetectParser
//				AutoDetectParser parser = new AutoDetectParser();
//			    BodyContentHandler handler = new BodyContentHandler(ProcessPageDataWorker.MAX_FULLTEXT_CHARS_TO_EXTRACT);
//			    Metadata metadata = new Metadata();
//				//Detect the mimetype
//				// Note: detect() only reads part of the file, does not close the stream, and actually rewinds the stream.
//				this.detectedMimetype = parser.getDetector().detect(stream, metadata).getType();
//				parser.parse(stream, handler, metadata);
//				//Extract text and compress whitespace to reduce storage requirements
//				this.fulltext = whitespaceCompressorPattern.matcher(handler.toString()).replaceAll(" ");
				
				
				
			} catch (TikaException e) {
				HrwaManager.logger.error("TikaException encountered while parsing content with Tika.  Message: " + e.getMessage());
			} finally {
			    stream.close();
			}
			
		} catch (IOException e) {
			HrwaManager.logger.error("IOException encountered while parsing content with Tika.  Message: " + e.getMessage());
		} finally {
			//We're done with this archive record.  Make sure to close it.
			try {
				archiveRecord.close();
			} catch (IOException e) {
				HrwaManager.logger.error("Error encountered while closing ArchiveRecord.  Message: " + e.getMessage());				
			}
		}
	}
	
	/**
	 * Returns true if this record should be skipped.
	 * (e.g. If this is a "filedesc:" or "dns:" record)
	 * @return
	 */
	public boolean shouldBeSkipped() {
		if(forceSkipThisRecord) { return true; }
		if(skipRecordBasedOnOriginalUrl()) { return true; }
		if(this.recordIdentifier == null) { return true; }
		return false;
	}
	
	public boolean skipRecordBasedOnOriginalUrl() {
		if(originalUrl == null) { return true; }
		if(originalUrl.startsWith("dns:")) { return true; }
		if(originalUrl.startsWith("filedesc:")) { return true; }
		
		return false;
	}
	
	
}
