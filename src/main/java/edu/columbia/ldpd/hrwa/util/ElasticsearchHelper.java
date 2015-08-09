package edu.columbia.ldpd.hrwa.util;

import java.io.IOException;

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

public class ElasticsearchHelper {
	
	private static TransportClient elasticsearchClient;
	
	public static TransportClient getTransportClient() {
		return ElasticsearchHelper.elasticsearchClient;
	}
	
	public static void openTransportClientConnection() {
		//Note: Client is thread-safe
		elasticsearchClient = new TransportClient();
		elasticsearchClient.addTransportAddress(new InetSocketTransportAddress(HrwaManager.elasticsearchHostname, HrwaManager.elasticsearchPort));
	}
	
	public static void closeTransportClientConnection() {
		elasticsearchClient.close();
	}
	
	public static void flushIndexChanges(String indexName) {
		final FlushResponse res = ElasticsearchHelper.getTransportClient().admin().indices().prepareFlush(indexName).execute().actionGet();
	}
	
	public static void createElasticsearchIndexIfNotExists(String indexName, int numShards, int numReplicas, String typeName, XContentBuilder mappingBuilderForType) {
		// Check if index exists already. If so, return.
        final IndicesExistsResponse res = ElasticsearchHelper.getTransportClient().admin().indices().prepareExists(indexName).execute().actionGet();
        if (res.isExists()) {
        	return;
//        	System.out.println("DELETING OLD INDEX"); //TODO: Stop deleting old index after testing is complete
//        	DeleteIndexResponse delete = elasticsearchClient.admin().indices().delete(new DeleteIndexRequest(HrwaManager.ELASTICSEARCH_SITE_INDEX)).actionGet();
        }
        
        // Create the index with appropriate settings
		Settings indexSettings = ImmutableSettings.settingsBuilder()
                .put("number_of_shards", numShards)
                .put("number_of_replicas", numReplicas)
                .build();
		CreateIndexRequest indexRequest = new CreateIndexRequest(indexName, indexSettings);
		
		//Set Mapping
		indexRequest.mapping(typeName, mappingBuilderForType);
		
		CreateIndexResponse indexResponse = ElasticsearchHelper.getTransportClient().admin().indices().create(indexRequest).actionGet();
	}
	
}
