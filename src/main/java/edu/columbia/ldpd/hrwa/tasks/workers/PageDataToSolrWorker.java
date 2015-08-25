package edu.columbia.ldpd.hrwa.tasks.workers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.jwat.arc.ArcReader;
import org.jwat.arc.ArcReaderFactory;
import org.jwat.arc.ArcRecordBase;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

import edu.columbia.ldpd.hrwa.ArchiveFileInfoRecord;
import edu.columbia.ldpd.hrwa.ArchiveFileInfoRecord.MissingArchiveHeaderValueException;
import edu.columbia.ldpd.hrwa.ArchiveFileInfoRecord.UnexpectedRecordTypeException;
import edu.columbia.ldpd.hrwa.SiteData;
import edu.columbia.ldpd.hrwa.util.ElasticsearchHelper;
import edu.columbia.ldpd.hrwa.util.SolrHelper;
import edu.columbia.ldpd.hrwa.HrwaManager;
import edu.columbia.ldpd.hrwa.PageData;

public class PageDataToSolrWorker implements Runnable {
	
	public static final int HIGH_MEMORY_USAGE_POLLING_DELAY_IN_MILLIS = 10000; // While waiting in times of high memory usage, check back every X milliseconds to see if a new job can start.
	public static final int NUM_MILLIS_OF_WAIT_TIME_BEFORE_LOGGING_WARNING = 120000; // If we wait for too long, this should be logged so that the user can tweak memory limits.
	
	private SiteData siteData;
	
	public PageDataToSolrWorker(String siteDataBibId) {
		siteData = SiteData.getRecordByBibId(siteDataBibId);
	}

	@Override
	public void run() {
		
		//Don't start the processing yet if current free memory is lower than our minimum threshold for creating new processes
		//boolean alreadyLoggedUnusuallyLongWaitTimeMessage = false;
		long startTime = System.currentTimeMillis();
		int attempts = 0;
		while(HrwaManager.getFreeMemoryInBytes() < HrwaManager.minAvailableMemoryInBytesForNewProcess) {			
			if( (System.currentTimeMillis() - startTime) > NUM_MILLIS_OF_WAIT_TIME_BEFORE_LOGGING_WARNING ) {
				HrwaManager.logger.error("A new " + this.getClass().getSimpleName() + " process has been waiting for at least " + (NUM_MILLIS_OF_WAIT_TIME_BEFORE_LOGGING_WARNING/1000) + " seconds to run.  If you see a lot of messages like this, you may want to allocate more memory to this application, or lower the value of the minAvailableMemoryInBytesForNewProcess command line option.");
				//alreadyLoggedUnusuallyLongWaitTimeMessage = true;
			}
			if((attempts+1) % 4 == 0) {
				//And pass a hint that the garbage collector should run just in case.
				System.gc();
				HrwaManager.logger.error("Ran garbage collector in an effort to free up memory for additional workers.");
			}
			try {
				Thread.sleep(HIGH_MEMORY_USAGE_POLLING_DELAY_IN_MILLIS);
			} catch (InterruptedException e) { e.printStackTrace(); }
			attempts++;
		}
		
		if( siteData.status.equals(SiteData.STATUS_DELETED) ) {
			//Perform deletion
			deleteSolrPagesForSiteDataAndRemoveSiteFromElasticsearchIndex(this.siteData);
		} else if( siteData.status.equals(SiteData.STATUS_UPDATED) ) {
			//Perform update
			updateSolrPagesForSiteData(this.siteData);
		}
	}
	
	public void updateSolrPagesForSiteData(SiteData siteData) {
		
		//1) Get associated Pages for this site (scroll through Elasticsearch results)
		//2) Index those Pages into Solr using pageData.sendToSolr(SolrHelper.getPagesSolrClient(), siteData)
		
		BoolQueryBuilder prefixQuery = QueryBuilders.boolQuery();
		
		//Handle host string matching
		for(String hostStringWithPath : siteData.hostStringsWithPath) {
			if(hostStringWithPath.contains("/")) {
				//If this hostStringWithPath contains a slash, then we must do a prefix match because we need to match on a subdirectory
				prefixQuery.should(QueryBuilders.prefixQuery("hostStringWithPath", hostStringWithPath));
			} else {
				//This hostStringWithPath does not contain a slash, so we can do an exact match on hostString (which is more efficient)
				prefixQuery.should(QueryBuilders.termQuery("hostString", hostStringWithPath));
			}
		}
		
		//Handle matching for related hosts
		for(String relatedUrlWithPathString : siteData.relatedHostStringsWithPath) {
			if(relatedUrlWithPathString.contains("/")) {
				//If this relatedUrlWithPathString contains a slash, then we must do a prefix match because we need to match on a subdirectory
				prefixQuery.should(QueryBuilders.prefixQuery("hostStringWithPath", relatedUrlWithPathString));
			} else {
				//This relatedUrlWithPathString does not contain a slash, so we can do an exact match on hostString (which is more efficient)
				prefixQuery.should(QueryBuilders.termQuery("hostString", relatedUrlWithPathString));
			}
		}
		
		SearchResponse scrollResp = ElasticsearchHelper.getTransportClient().prepareSearch(HrwaManager.ELASTICSEARCH_PAGE_INDEX_NAME)
			.setTypes(HrwaManager.ELASTICSEARCH_PAGE_TYPE_NAME)
			.setQuery(prefixQuery)
	        .setSearchType(SearchType.SCAN)
	        .setScroll(new TimeValue(60_000)) //60 seconds should be enough time to process EACH scroll batch
	        .setSize(300).execute().actionGet(); //X hits per shard will be returned for each scroll
		//Scroll until no hits are returned
		while (true) {
		    for (SearchHit hit : scrollResp.getHits().getHits()) {
		        PageData pageData = PageData.getPageDataFromElasticsearchHit(hit);
		        pageData.sendToSolr(SolrHelper.getPagesSolrClient(), siteData);
		    }
		    scrollResp = ElasticsearchHelper.getTransportClient().prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
		    //Break condition: No hits are returned
		    if (scrollResp.getHits().getHits().length == 0) {
		        break;
		    }
		}
		
		//If we finished processing the site and didn't encounter any errors, mark this site file as processed by clearing the status 
		try {
			//TODO: Change line below to blank status instead of STATUS_UPDATED
			this.siteData.status = SiteData.STATUS_UPDATED;
			this.siteData.sendToElasticsearch(ElasticsearchHelper.getTransportClient());
			ElasticsearchHelper.flushIndexChanges(HrwaManager.ELASTICSEARCH_SITE_INDEX_NAME);
		} catch (IOException e) {
			HrwaManager.logger.error("An IOException occurred while trying to mark an Elasticsearch Site record as processed (by clearing its status). Site bib id: " + this.siteData.bibId);
		}

	}
	
	public void deleteSolrPagesForSiteDataAndRemoveSiteFromElasticsearchIndex(SiteData siteData) {
		
		//TODO: Processing
		//1) Get associated Pages for this site (liked by bib_id) (and then scroll through Elasticsearch results)
		//2) Delete those Solr docs
		
		
		
		
		
		//If we finished processing the site and didn't encounter any errors, delete this site from Elasticsearch 
		try {
			siteData.deleteFromElasticsearch(ElasticsearchHelper.getTransportClient());
			ElasticsearchHelper.flushIndexChanges(HrwaManager.ELASTICSEARCH_SITE_INDEX_NAME);
		} catch (IOException e) {
			HrwaManager.logger.error("An IOException occurred while trying to delete an Elasticsearch Site record. Site bib id: " + this.siteData.bibId);
		}
	}

}
