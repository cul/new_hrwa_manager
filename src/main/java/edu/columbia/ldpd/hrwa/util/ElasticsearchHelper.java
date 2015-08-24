package edu.columbia.ldpd.hrwa.util;

import java.io.IOException;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.flush.FlushResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;

import edu.columbia.ldpd.hrwa.HrwaManager;

public class ElasticsearchHelper {
	
	private static TransportClient elasticsearchClient;
	
	public static TransportClient getTransportClient() {
		return ElasticsearchHelper.elasticsearchClient;
	}
	
	public static void openTransportClientConnection() {
		//Note: Client is thread-safe
		elasticsearchClient = new TransportClient();
		elasticsearchClient.addTransportAddress(new InetSocketTransportAddress(HrwaManager.elasticsearchHostname, HrwaManager.elasticsearchPort));
		
		//Sleep for 2 seconds to allow the connection to be established before work begins
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
		
		//Sleep for 2 seconds to allow the index to be fully created before future code works with it
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
