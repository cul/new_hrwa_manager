package edu.columbia.ldpd.hrwa.util;

import java.io.IOException;

import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.flush.FlushResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import edu.columbia.ldpd.hrwa.HrwaManager;
import edu.columbia.ldpd.hrwa.SiteData;

public class SolrHelper {
	
	private static HttpSolrClient sitesSolrClient;
	private static HttpSolrClient pagesSolrClient;
	
	public static HttpSolrClient getSitesSolrClient() {
		return SolrHelper.sitesSolrClient;
	}
	
	public static HttpSolrClient getPagesSolrClient() {
		return SolrHelper.pagesSolrClient;
	}
	
	public static void openSitesSolrClientConnection() {
		//Note: Client is thread-safe
		sitesSolrClient = new HttpSolrClient(HrwaManager.sitesSolrUrl);
		
		//Sleep for 2 seconds to allow the connection to be established before work begins
		try { Thread.sleep(2000); }
		catch (InterruptedException e) { e.printStackTrace(); }
	}
	
	public static void openPagesSolrClientConnection() {
		//Note: Client is thread-safe
		pagesSolrClient = new HttpSolrClient(HrwaManager.pagesSolrUrl);
		
		//Sleep for 2 seconds to allow the connection to be established before work begins
		try { Thread.sleep(2000); }
		catch (InterruptedException e) { e.printStackTrace(); }
	}
	
	public static void closeSitesClientConnection() {
		try { sitesSolrClient.close(); }
		catch (IOException e) { HrwaManager.logger.error("IOException encountered while attempting to close connection to sites Solr core. Message: " + e.getMessage()); }
	}
	
	public static void closePagesClientConnection() {
		try { pagesSolrClient.close(); }
		catch (IOException e) { HrwaManager.logger.error("IOException encountered while attempting to close connection to pages Solr core. Message: " + e.getMessage()); }
	}
	
}
