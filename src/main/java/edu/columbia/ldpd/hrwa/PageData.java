package edu.columbia.ldpd.hrwa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.arc.ARCRecord;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.warc.WARCRecord;
import org.archive.io.warc.WARCRecordInfo;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jwat.arc.ArcRecordBase;
import org.jwat.common.HttpHeader;
import org.jwat.common.Payload;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcRecord;
import org.xml.sax.SAXException;

import edu.columbia.ldpd.hrwa.tasks.workers.ProcessPageDataWorker;
import edu.columbia.ldpd.hrwa.util.MetadataUtils;

public class PageData {
	
	// Archive File Fields
	public String 		originalUrl; //Original url of this crawled record.
	public String 		archiveFileName; // Name of the archive file (warc/arc) that this record came from.
	public long 		archiveFileOffset; // Byte offset address of the record in the archive file.
	public long			contentLength; // Size of the content returned in the HTTP response in bytes. Largest will probably be video.
	public String 		crawlDate; // Crawl date for this record.  Format: 
	public String		mimetypeFromHeader; // Mimetype supplied by the archive file header.
	
	// Tika-Generated Fields
	public String 		detectedMimetype; // Mimetype detected by the HRWA Manager application.  null if mimetype could not be detected.
	public String		fulltext; // Full text extracted from record content.
	
	//Derived Fields
	public String 		hostString; //Truncated url, only including hostname and removing www, www1, www2, etc. if present.
	
	//Force skip
	public boolean		forceSkipThisRecord = false; // If this flag is set to true, then this record will always appear as skippable
	
	//Constants
	public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
	
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
			//this.recordIdentifier = header.getRecordIdentifier();
			
			//Status code
			ARCRecord arcRecord = (ARCRecord)archiveRecord;
			int statusCode = arcRecord.getStatusCode();
			//Ignore anything with a non-200 status because it's not useful for us
			if(statusCode != 200) {
				this.forceSkipThisRecord = true;
			}
			
		} else if (archiveRecord instanceof WARCRecord) {
			
			WARCRecord warcRecord = (WARCRecord)archiveRecord;
			
			//Record identifier
			//this.recordIdentifier = header.getHeaderValue(WARCRecord.HEADER_KEY_ID).toString();
			// WARC Files say what their WARC type is, so we can use this to ignore irrelevant ones.
			if( ! header.getHeaderValue(WARCRecord.HEADER_KEY_TYPE).toString().equals("response") ) {
				//We only want to process WARC files of type "response"
				this.forceSkipThisRecord = true;
			}
			
		}
		
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
				this.fulltext = WHITESPACE_PATTERN.matcher(tika.parseToString(stream)).replaceAll(" ").trim();
				
				//System.out.println("Fulltext length: " + this.fulltext.length());
//				if(this.fulltext.length() == 0) {
//					System.out.println("Length is 0 for: " + this.originalUrl);
//				} else {
//					System.out.println("Fulltext starts with: " + this.fulltext.substring(0, 100));
//				}
				
				
				
				
				//Alternate method of parsing, using an AutoDetectParser
//				AutoDetectParser parser = new AutoDetectParser();
//			    BodyContentHandler handler = new BodyContentHandler(ProcessPageDataWorker.MAX_FULLTEXT_CHARS_TO_EXTRACT);
//			    Metadata metadata = new Metadata();
//				//Detect the mimetype
//				// Note: detect() only reads part of the file, does not close the stream, and actually rewinds the stream.
//				this.detectedMimetype = parser.getDetector().detect(stream, metadata).getType();
//				parser.parse(stream, handler, metadata);
//				//Extract text and compress whitespace to reduce storage requirements
//				this.fulltext = whitespaceCompressorPattern.matcher(handler.toString()).replaceAll(" ").trim();
				
				
				
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
	 * Creates a new PageData record from a WarcRecord
	 * @param archiveRecord
	 */
	public PageData(ArcRecordBase arcRecord, String nameOfArchiveFile) {
		
		this.originalUrl = arcRecord.getUrlStr();
		
		//Skip all records that don't start with http/https (like "dns:" records) 
		if( ! this.originalUrl.startsWith("http") && ! this.originalUrl.startsWith("https")) {
			forceSkipThisRecord = true;
			return;
		} else {
			this.hostString = MetadataUtils.extractHostString(this.originalUrl); 
		}
		
		HttpHeader httpHeader = arcRecord.getHttpHeader();
		//Get http headers for crawled content
		
		//Skip all records that don't have a 200 status
		if( httpHeader.statusCode != 200 ) {
			forceSkipThisRecord = true;
			return;
		}
		
		this.archiveFileName = nameOfArchiveFile;
		this.archiveFileOffset = arcRecord.getStartOffset();
		this.contentLength = arcRecord.getArchiveLength();
		this.crawlDate = arcRecord.getArchiveDateStr();
		this.mimetypeFromHeader = arcRecord.getContentTypeStr();
		
		//Handle the payload (i.e. actual crawled resource data)
		Payload payload = arcRecord.getPayload();
		
		extractMimeTypeAndFulltextfromPayload(payload);
	}
	
	/**
	 * Creates a new PageData record from a WarcRecord
	 * @param archiveRecord
	 */
	public PageData(WarcRecord warcRecord, String nameOfArchiveFile) {
		
		//Skip all records that aren't of type "response"
		if( warcRecord.getHeader("WARC-Type") == null || ! warcRecord.getHeader("WARC-Type").value.equals("response") ) {
			forceSkipThisRecord = true;
			return;
		}
		
		
		//Skip all records that have a url that doesn't start with http or https 
		if( warcRecord.getHeader("WARC-Target-URI") != null &&
			(
				warcRecord.getHeader("WARC-Target-URI").value.startsWith("http")
				||
				warcRecord.getHeader("WARC-Target-URI").value.startsWith("https"))
			)
		{
			this.originalUrl = warcRecord.getHeader("WARC-Target-URI").value;
			this.hostString = MetadataUtils.extractHostString(this.originalUrl);
		} else {
			forceSkipThisRecord = true;
			return;
		}
		
		
		//Get http headers for crawled content
		HttpHeader httpHeader = warcRecord.getHttpHeader();
		
		//Skip all records that don't have a 200 status
		if( httpHeader.statusCode != 200 ) {
			forceSkipThisRecord = true;
			return;
		}
		
		
		this.archiveFileName = nameOfArchiveFile;
		
		this.mimetypeFromHeader = httpHeader.contentType;
		this.archiveFileOffset = warcRecord.getStartOffset();
		
		if(warcRecord.getHeader("WARC-Date") != null) {
			this.crawlDate = PageData.warcDateFormatToArcDateFormat(warcRecord.getHeader("WARC-Date").value);
		}
		if(this.crawlDate == null) {
			forceSkipThisRecord = true;
			HrwaManager.logger.error("Unable to parse crawl date for record: " + this.originalUrl + " in archive file " + this.archiveFileName + ".");
			return;
		}
		
		// Derive hostString from originalUrl when applicable (only runs if shouldBeSkipped() passes, since we check for the presence of an originalUrl value)
		this.hostString = MetadataUtils.extractHostString(this.originalUrl);
		
		//Handle the payload (i.e. actual crawled resource data)
		Payload payload = warcRecord.getPayload();
		this.contentLength = payload.getTotalLength();
		
		extractMimeTypeAndFulltextfromPayload(payload);
	}
	
	public void extractMimeTypeAndFulltextfromPayload(Payload payload) {
		
		String[] mimeTypeAndFulltext = new String[2];
		
		//Use Tika for mimetype detection and text extraction
		//Extract from file
		//Note: If we need to do this in chunks due to memory constraints, there's a how-to example here: https://tika.apache.org/1.8/examples.html
		try {
			
			InputStream stream = null;
			InputStream byteArrayInputStream = null;
			try {
				stream = payload.getInputStream();
				// Convert the stream above into a byte[], and then to a ByteArrayInputStream because
				// Tika doesn't seem to be able to handle the ByteCountingPushBackInputStream
				byte[] recordBytes = IOUtils.toByteArray(stream);
				byteArrayInputStream = new ByteArrayInputStream(recordBytes);
				
				Tika tika = new Tika();
				tika.setMaxStringLength(ProcessPageDataWorker.MAX_FULLTEXT_CHARS_TO_EXTRACT);
				//Detect the mimetype
				// Note: detect() only reads part of the file, does not close the stream, and actually rewinds the stream.
				this.detectedMimetype = tika.detect(byteArrayInputStream);
				
				//Do not try to extract fulltext from images, audio or video
				if(this.detectedMimetype.startsWith("image") || this.detectedMimetype.startsWith("audio") || this.detectedMimetype.startsWith("video")) {
					this.fulltext = "";
				} else {
					//Extract text and compress whitespace to reduce storage requirements
					this.fulltext = WHITESPACE_PATTERN.matcher(tika.parseToString(byteArrayInputStream)).replaceAll(" ").trim();
				}
				
			} catch (TikaException e) {
				HrwaManager.logger.info("TikaException encountered while parsing record content with Tika.  Unable to parse record at byte " + this.archiveFileOffset + " in " + this.archiveFileName + ".  Message: " + e.getMessage());
				this.fulltext = "";
			} finally {
				if(stream != null) {
					stream.close();
				}
				if(byteArrayInputStream != null) {
					byteArrayInputStream.close();
				}
			}
		} catch (IOException e) {
			HrwaManager.logger.info("IOException encountered while parsing record content with Tika.  Unable to parse record at byte " + this.archiveFileOffset + " in " + this.archiveFileName + ".  Message: " + e.getMessage());
		}
	}
	
	
	/**
	 * Returns true if this record should be skipped.
	 * (e.g. If this is a "filedesc:" or "dns:" record)
	 * @return
	 */
	public boolean shouldBeSkipped() {
		if(forceSkipThisRecord) { return true; }
		return false;
	}
	
	public static String warcDateFormatToArcDateFormat(String warcDateTime) {
	    StringBuilder convertedDateString = new StringBuilder();
	    convertedDateString.append(warcDateTime, 0,  4);
	    convertedDateString.append(warcDateTime, 5,  7);
	    convertedDateString.append(warcDateTime, 8,  10);
	    convertedDateString.append(warcDateTime, 11, 13);
	    convertedDateString.append(warcDateTime, 14, 16);
	    convertedDateString.append(warcDateTime, 17, 19);
	    return convertedDateString.toString();
	}
	
}
