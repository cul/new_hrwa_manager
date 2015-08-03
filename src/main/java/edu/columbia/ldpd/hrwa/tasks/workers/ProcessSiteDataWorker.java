package edu.columbia.ldpd.hrwa.tasks.workers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.jwat.arc.ArcReader;
import org.jwat.arc.ArcReaderFactory;
import org.jwat.arc.ArcRecordBase;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

import edu.columbia.ldpd.hrwa.ArchiveFileInfoRecord;
import edu.columbia.ldpd.hrwa.ArchiveFileInfoRecord.MissingArchiveHeaderValueException;
import edu.columbia.ldpd.hrwa.ArchiveFileInfoRecord.UnexpectedRecordTypeException;
import edu.columbia.ldpd.hrwa.BetterMarcRecord;
import edu.columbia.ldpd.hrwa.HrwaManager;
import edu.columbia.ldpd.hrwa.PageData;
import edu.columbia.ldpd.hrwa.SiteData;
import edu.columbia.ldpd.hrwa.util.MysqlHelper;

public class ProcessSiteDataWorker implements Runnable {
	
	private File marcXmlFile;
	private TransportClient elasticsearchClient;
	private SiteData siteData;
	
	public ProcessSiteDataWorker(String pathToMarcXmlFile) {
		this.marcXmlFile = new File(pathToMarcXmlFile);
		this.siteData = new SiteData();
	}

	@Override
	public void run() {
		
		//Get a connection for this worker
		//this.conn = MysqlHelper.getNewDBConnection();
		//System.out.println("Connect at: " + HrwaManager.elasticsearchHostname + ", " + HrwaManager.elasticsearchPort);
		//elasticsearchClient = new TransportClient();
		//elasticsearchClient.addTransportAddress(new InetSocketTransportAddress(HrwaManager.elasticsearchHostname, HrwaManager.elasticsearchPort));
		
		processMarcXmlFile(this.marcXmlFile);
		
		//Be sure to close the connection when we're done
		//MysqlHelper.closeConnection(conn);
		//elasticsearchClient.close();
	}
	
	public void processMarcXmlFile(File paramMarcXmlFile) {
		
		try {
			FileInputStream fis = new FileInputStream(paramMarcXmlFile);
			this.siteData = new SiteData(fis);
			
			if( ! this.siteData.isValid() ) {
				HrwaManager.logger.error("Could not update site record with bib id: " + this.siteData.bibId + " due to the following error(s): " + StringUtils.join(this.siteData.getValidationErrors()));
				return;
			}
		
		} catch (FileNotFoundException e) {
			HrwaManager.logger.error(
				"Could not find file: " + paramMarcXmlFile.getAbsolutePath() + "\n" +
				"Message: " + e.getMessage()
			);
			return;
		}
		
	}
	
	public void markArchiveFileAsProcessed() throws IOException {
		XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()
		    .startObject()
		    	.field("processed", true)
		        .field("processDate", new Date())
		    .endObject();
		
		IndexResponse response = elasticsearchClient.prepareIndex(HrwaManager.ELASTICSEARCH_ARCHIVE_FILE_INDEX, HrwaManager.ELASTICSEARCH_ARCHIVE_FILE_TYPE, this.marcXmlFile.getName())
	        .setSource(jsonBuilder)
	        .execute()
	        .actionGet();
	}
	
	public boolean hasArchiveFileBeenProcessed(String archiveFileName) throws IOException {
		try {
			SearchResponse response = elasticsearchClient.prepareSearch(HrwaManager.ELASTICSEARCH_ARCHIVE_FILE_INDEX)
			        .setTypes(HrwaManager.ELASTICSEARCH_ARCHIVE_FILE_TYPE)
			        .setQuery(QueryBuilders.termQuery("_id", archiveFileName))
			        .setFrom(0).setSize(1)
			        .execute()
			        .actionGet();
			
			SearchHits searchHits = response.getHits();
			if(searchHits.getTotalHits() == 1) {
				return (Boolean)(searchHits.getAt(0).sourceAsMap().get("processed"));
			}
			
			return false;
		} catch (IndexMissingException e) {
			//If this index hasn't been created yet, then none of the archive files have been processed
			return false;
		}
	}
	
	public void processWarcRecord(WarcRecord warcRecord, String archiveFileName) {
		PageData pageData = new PageData(warcRecord, archiveFileName);
		processPageData(pageData);
	}
	
	public void processArcRecord(ArcRecordBase arcRecord, String archiveFileName) {
		PageData pageData = new PageData(arcRecord, archiveFileName);
		processPageData(pageData);
	}
	
	public void processPageData(PageData pageData) {
		try {
			pageData.sendToElasticsearch(this.elasticsearchClient);
		} catch (ElasticsearchException e) {
			HrwaManager.logger.error("ElasticsearchException encountered while sending PageData to Elasticsearch. File: " + pageData.archiveFileName + ", Byte Offset: " + pageData.archiveFileOffset + ", Error Message: " + e.getMessage());
		} catch (IOException e) {
			HrwaManager.logger.error("IOException encountered while sending PageData to Elasticsearch. File: " + pageData.archiveFileName + ", Byte Offset: " + pageData.archiveFileOffset + ", Error Message: " + e.getMessage());
		}
	}

}
