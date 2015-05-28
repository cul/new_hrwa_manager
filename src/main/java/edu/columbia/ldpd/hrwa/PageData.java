package edu.columbia.ldpd.hrwa;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.jwat.arc.ArcRecordBase;
import org.jwat.common.HttpHeader;
import org.jwat.common.Payload;
import org.jwat.warc.WarcRecord;

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
	
	
	/**
	 * Creates an empty PageData record. Generally used when creating a PageData record from a non-ARC/WARC file data source.  Each publicly-accessible field can be set manually.
	 * @param archiveRecord
	 */
	public PageData() {
		
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
