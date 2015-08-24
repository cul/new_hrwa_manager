package edu.columbia.ldpd.hrwa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.tika.Tika;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.SearchHit;
import org.jwat.arc.ArcRecordBase;
import org.jwat.common.HttpHeader;
import org.jwat.common.Payload;
import org.jwat.warc.WarcRecord;
import org.xml.sax.SAXException;

import edu.columbia.ldpd.hrwa.tasks.workers.PageDataToSolrWorker;
import edu.columbia.ldpd.hrwa.tasks.workers.ProcessPageDataWorker;
import edu.columbia.ldpd.hrwa.util.ElasticsearchHelper;
import edu.columbia.ldpd.hrwa.util.MetadataUtils;

public class PageData {
	
	public static final int MAX_FULLTEXT_CHARS_TO_EXTRACT =   2000000; // 2000000 == 2 MB.  Higher numbers will result in higher memory usage for larger files.
	
	// Archive File Fields
	public String 		originalUrl; //Original url of this crawled record.
	public String 		archiveFileName; // Name of the archive file (warc/arc) that this record came from.
	public long 		archiveFileOffset; // Byte offset address of the record in the archive file.
	public long			contentLength; // Size of the content returned in the HTTP response in bytes. Largest will probably be video.
	public String 		crawlDate; // Crawl date for this record.  Format: 
	public String		mimetypeFromHeader; // Mimetype supplied by the archive file header.
	
	// Tika-Generated Fields
	public String		title; //Title extracted and placed into Metadata() object during detection.
	public String 		detectedMimetype; // Mimetype detected by the HRWA Manager application.  null if mimetype could not be detected.
	public String		fulltext; // Full text extracted from record content.
	
	//Derived Fields
	public String 		originalUrlWithoutProtocol; // Version of the original URL without the protocol portion 
	public String 		hostString; //Truncated url, only including hostname (without path), removing www, www1, www2, etc. if present.
	
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
			this.originalUrlWithoutProtocol = MetadataUtils.removeProtocolFromUrlString(this.originalUrl);
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
			this.originalUrlWithoutProtocol = MetadataUtils.removeProtocolFromUrlString(this.originalUrl);
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
		
		//Handle the payload (i.e. actual crawled resource data)
		Payload payload = warcRecord.getPayload();
		this.contentLength = payload.getTotalLength();
		
		extractMimeTypeAndFulltextfromPayload(payload);
	}
	
	public static void creatElastisearchIndexIfNotExist() {
		try {
			
			XContentBuilder mappingBuilder = XContentFactory.jsonBuilder().startObject()
				.startObject(HrwaManager.ELASTICSEARCH_PAGE_TYPE_NAME)
					.startObject("_source")
						.field("enabled", true) //keep the source for now.  possibly disable later if not necessary
					.endObject()
					.startObject("properties")
			    		.startObject("originalUrl")					.field("type", "string").field("store", false).field("index", "not_analyzed").endObject() //do not analyze (indexed as is)
						.startObject("originalUrlWithoutProtocol")	.field("type", "string").field("store", false).field("index", "not_analyzed").endObject() //do not analyze (indexed as is)
						.startObject("hostString")					.field("type", "string").field("store", false).field("index", "not_analyzed").endObject() //do not analyze (indexed as is)
						.startObject("archiveFileName")				.field("type", "string").field("store", false).field("index", "not_analyzed").endObject() //do not analyze (indexed as is)
						.startObject("archiveFileOffset")			.field("type", "string").field("store", false).field("index", "not_analyzed").endObject() //do not analyze (indexed as is)
						.startObject("contentLength")				.field("type", "string").field("store", false).field("index", "not_analyzed").endObject() //do not analyze (indexed as is)
						.startObject("crawlDate")					.field("type", "string").field("store", false).field("index", "not_analyzed").endObject() //do not analyze (indexed as is)
						.startObject("fulltext")					.field("type", "string").field("store", false).field("index", "no").endObject() //do not index at all (only store)
						.startObject("mimetypeFromHeader")			.field("type", "string").field("store", false).field("index", "not_analyzed").endObject() //do not analyze (indexed as is)
						.startObject("detectedMimetype")			.field("type", "string").field("store", false).field("index", "not_analyzed").endObject() //do not analyze (indexed as is)
						.startObject("title")						.field("type", "string").field("store", false).field("index", "not_analyzed").endObject() //do not analyze (indexed as is)
		    		.endObject()
			    .endObject()
			.endObject();
			
			ElasticsearchHelper.createElasticsearchIndexIfNotExists(HrwaManager.ELASTICSEARCH_PAGE_INDEX_NAME, HrwaManager.ELASTICSEARCH_PAGE_INDEX_NUM_SHARDS, HrwaManager.ELASTICSEARCH_PAGE_INDEX_NUM_REPLICAS, HrwaManager.ELASTICSEARCH_PAGE_TYPE_NAME, mappingBuilder);
			
		} catch (IOException e) {
			HrwaManager.logger.error("IOException encountered while creating mapping for Elasticsearch" + HrwaManager.ELASTICSEARCH_PAGE_INDEX_NAME + " index.  Message: " + e.getMessage());
		}
	}
	
	public static void creatArchiveFileElastisearchIndexIfNotExist() {
		try {
			
			XContentBuilder mappingBuilder = XContentFactory.jsonBuilder().startObject()
				.startObject(HrwaManager.ELASTICSEARCH_ARCHIVE_FILE_TYPE_NAME)
					.startObject("_source")
						.field("enabled", true) //keep the source for now.  possibly disable later if not necessary
					.endObject()
					.startObject("properties")
			    		//There are no custom fields to be defined.  Only the _id field is used.
		    		.endObject()
			    .endObject()
			.endObject();
			
			ElasticsearchHelper.createElasticsearchIndexIfNotExists(HrwaManager.ELASTICSEARCH_ARCHIVE_FILE_INDEX_NAME, HrwaManager.ELASTICSEARCH_ARCHIVE_FILE_INDEX_NUM_SHARDS, HrwaManager.ELASTICSEARCH_ARCHIVE_FILE_INDEX_NUM_REPLICAS, HrwaManager.ELASTICSEARCH_ARCHIVE_FILE_TYPE_NAME, mappingBuilder);
			
		} catch (IOException e) {
			HrwaManager.logger.error("IOException encountered while creating mapping for Elasticsearch" + HrwaManager.ELASTICSEARCH_ARCHIVE_FILE_INDEX_NAME + " index.  Message: " + e.getMessage());
		}
	}

	
	public XContentBuilder toElasticsearchJsonBuilder() throws IOException {
		
		XContentBuilder builder = XContentFactory.jsonBuilder()
		    .startObject()
		    	.field("originalUrl", this.originalUrl)
		    	.field("originalUrlWithoutProtocol", this.originalUrlWithoutProtocol)
				.field("hostString", this.hostString)
				.field("archiveFileName", this.archiveFileName)
				.field("archiveFileOffset", this.archiveFileOffset)
				.field("contentLength", this.contentLength)
				.field("crawlDate", this.crawlDate)
				.field("fulltext", this.fulltext)
				.field("mimetypeFromHeader", this.mimetypeFromHeader)
				.field("detectedMimetype", this.detectedMimetype)
				.field("title", this.title)
		    .endObject();
		
		return builder;
	}
	
	public void sendToElasticsearch(Client client) throws ElasticsearchException, IOException {
		IndexResponse response = client.prepareIndex(HrwaManager.ELASTICSEARCH_PAGE_INDEX_NAME, HrwaManager.ELASTICSEARCH_PAGE_TYPE_NAME, this.getUniqueIdForRecord())
	        .setSource(this.toElasticsearchJsonBuilder())
	        .execute()
	        .actionGet();
	}
	
	public String getUniqueIdForRecord() {
		return this.archiveFileName + "---" + this.archiveFileOffset;
	}
	
	public static PageData getPageDataFromElasticsearchHit(SearchHit hit) {
		PageData pageData = new PageData();
		
		Map<String, Object> sourceAsMap = hit.sourceAsMap();

		//originalUrl
		if(sourceAsMap.containsKey("originalUrl")) {
			pageData.originalUrl = (String)sourceAsMap.get("originalUrl");
		}
    	
    	//originalUrlWithoutProtocol
		if(sourceAsMap.containsKey("originalUrlWithoutProtocol")) {
			pageData.originalUrlWithoutProtocol = (String)sourceAsMap.get("originalUrlWithoutProtocol");
		}
		
		//hostString
		if(sourceAsMap.containsKey("hostString")) {
			pageData.hostString = (String)sourceAsMap.get("hostString");
		}
		
		//archiveFileName
		if(sourceAsMap.containsKey("archiveFileName")) {
			pageData.archiveFileName = (String)sourceAsMap.get("archiveFileName");
		}
		
		//archiveFileOffset
		if(sourceAsMap.containsKey("archiveFileOffset")) {
			pageData.archiveFileOffset = (Integer)sourceAsMap.get("archiveFileOffset");
		}
		
		//contentLength
		if(sourceAsMap.containsKey("contentLength")) {
			pageData.contentLength = (Integer)sourceAsMap.get("contentLength");
		}
		
		//crawlDate
		if(sourceAsMap.containsKey("crawlDate")) {
			pageData.crawlDate = (String)sourceAsMap.get("crawlDate");
		}
		
		//fulltext
		if(sourceAsMap.containsKey("fulltext")) {
			pageData.fulltext = (String)sourceAsMap.get("fulltext");
		}
		
		//mimetypeFromHeader
		if(sourceAsMap.containsKey("mimetypeFromHeader")) {
			pageData.mimetypeFromHeader = (String)sourceAsMap.get("mimetypeFromHeader");
		}
		
		//detectedMimetype
		if(sourceAsMap.containsKey("detectedMimetype")) {
			pageData.detectedMimetype = (String)sourceAsMap.get("detectedMimetype");
		}
		
		//title
		if(sourceAsMap.containsKey("title")) {
			pageData.title = (String)sourceAsMap.get("title");
		}
		
		return pageData;
	}
	
	public void sendToSolr(SolrClient solrClient, SiteData associatedSiteData) {
		
		SolrInputDocument document = new SolrInputDocument();
		
		document.addField("bib_key", associatedSiteData.bibId);
		
		document.addField("creator_name", associatedSiteData.creatorName);
		document.addField("organization_type", associatedSiteData.organizationType);
		document.addField("organization_based_in", associatedSiteData.organizationBasedIn);
		document.addField("geographic_focus", associatedSiteData.geographicFocus);
		document.addField("language", associatedSiteData.language);
		document.addField("website_original_urls", associatedSiteData.hostStrings);
		
		document.addField("domain", this.hostString);
		
		document.addField("title", this.title);
		
		document.addField("archived_url", HrwaManager.waybackUrlPrefix + this.originalUrl); 
		document.addField("date_of_capture_yyyy", this.crawlDate.substring(0, 4));
		document.addField("date_of_capture_yyyymm", this.crawlDate.substring(0, 6)); 
		document.addField("date_of_capture_yyyymmdd", this.crawlDate.substring(0, 8));
		document.addField("length", this.contentLength);
		document.addField("original_url", this.originalUrl);
		document.addField("mimetype", this.detectedMimetype == null ? this.mimetypeFromHeader : this.detectedMimetype); 
		document.addField("mimetype_code", this.detectedMimetype);	//TODO: Create mapping for codes instead of using raw mimetype 
		document.addField("record_date", this.crawlDate);
		document.addField("record_identifier", this.getUniqueIdForRecord());   
		document.addField("contents", this.fulltext);
		
		try {
			UpdateResponse response = solrClient.add(document);
		} catch (SolrServerException | IOException e) {
			HrwaManager.logger.error("Exception encountered while sending " + this.getClass().getName() + " to solr.  Unique id for record : " + this.getUniqueIdForRecord() + ". Message: " + e.getMessage());
		}
		
	}
	
	public void extractMimeTypeAndFulltextfromPayload(Payload payload) {
		InputStream stream = payload.getInputStream();
		
		// Convert the payload stream above into a byte[], and then to a ByteArrayInputStream because
		// Tika doesn't seem to be able to handle the ByteCountingPushBackInputStream
		InputStream byteArrayInputStream = null;
		try {
			byte[] recordBytes = IOUtils.toByteArray(stream);
			byteArrayInputStream = new ByteArrayInputStream(recordBytes);
			extractMimeTypeAndFulltextfromInputStream(byteArrayInputStream);
			stream.close();
			byteArrayInputStream.close();
		} catch (IOException e) {
			HrwaManager.logger.info("IOException encountered while converting payload InputStream to byte array (or possibly while closing InputStream).  Unable to parse record at byte " + this.archiveFileOffset + " in " + this.archiveFileName + ".  Message: " + e.getMessage());
		}
	}
	
	public void extractMimeTypeAndFulltextfromInputStream(InputStream stream) {
		//Use Tika for mimetype detection and text extraction
		//Extract from file
		//Note: If we need to do this in chunks due to memory constraints, there's a how-to example here: https://tika.apache.org/1.8/examples.html
		
		Parser parser = new AutoDetectParser();
	    Metadata metadata = new Metadata();
	    BodyContentHandler handler = new BodyContentHandler(PageData.MAX_FULLTEXT_CHARS_TO_EXTRACT);
	    
	    try {
	        parser.parse(stream, handler, metadata, new ParseContext());
	        this.title = metadata.get(TikaCoreProperties.TITLE);
	        this.fulltext = PageData.WHITESPACE_PATTERN.matcher(handler.toString()).replaceAll(" ").trim();
		    
		    DefaultDetector detector = new DefaultDetector();
		    this.detectedMimetype = detector.detect(stream, metadata).toString();
		    
		    //Do not try to extract fulltext from images, audio or video
		    //if(this.detectedMimetype.startsWith("image") || this.detectedMimetype.startsWith("audio") || this.detectedMimetype.startsWith("video")) {
		    //	this.fulltext = "";
			//}
		    
	    } catch (TikaException e) {
	    	//This is a debug-level message because there's generally nothing that we can do about TikaExceptions.
			//A TikaException generally comes with a message that says "Unable to parse record", so putting them in
			//a higher level than DEBUG just fills up logs.
			HrwaManager.logger.debug("TikaException encountered while parsing record content with Tika.  Unable to parse record at byte " + this.archiveFileOffset + " in " + this.archiveFileName + ".  Message: " + e.getMessage());
			this.fulltext = "";
	    } catch (IOException e) {
	    	HrwaManager.logger.error("IOException encountered while parsing record content with Tika.  Unable to parse record at byte " + this.archiveFileOffset + " in " + this.archiveFileName + ".  Message: " + e.getMessage());
		} catch (SAXException e) {
			HrwaManager.logger.error("SAXException encountered while parsing record content with Tika.  Unable to parse record at byte " + this.archiveFileOffset + " in " + this.archiveFileName + ".  Message: " + e.getMessage());
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
