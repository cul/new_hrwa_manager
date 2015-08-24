package edu.columbia.ldpd.hrwa.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import edu.columbia.ldpd.hrwa.HrwaManager;
import edu.columbia.ldpd.hrwa.PageData;
import edu.columbia.ldpd.hrwa.SiteData;
import edu.columbia.ldpd.hrwa.tasks.workers.PageDataToSolrWorker;
import edu.columbia.ldpd.hrwa.tasks.workers.ProcessPageDataWorker;
import edu.columbia.ldpd.hrwa.util.ElasticsearchHelper;
import edu.columbia.ldpd.hrwa.util.SolrHelper;

public class PageDataToSolrTask extends AbstractTask {
	
	public static final int STATUS_POLLING_INTERVAL_IN_MILLIS = 3000; // How frequently we get status messages about progress during processing.
	
	public PageDataToSolrTask() {
		
	}

	@Override
	public void taskImpl() {
		
		long startTime = System.currentTimeMillis();
		
		//Get site data from Elasticsearch
		ArrayList<SiteData> siteDataRecords = SiteData.getAllRecords();
		
		//Validate all records
		boolean foundAtLeastOneError = false;
		ArrayList<String> errors = new ArrayList<String>();
		
		for(SiteData siteData : siteDataRecords) {
			if( ! siteData.isValid() ) {
				foundAtLeastOneError = true;
				errors.add("Could not update site record with bib id: " + siteData.bibId + " due to the following error(s): " + StringUtils.join(siteData.getValidationErrors()));
			}
		}
		
		//Update all records if no errors were found
		if(foundAtLeastOneError) {
			HrwaManager.logger.error("One or more errors were found during " + this.getClass().getName() + " run. These must be fixed before the process can continue:\n----------\n" + StringUtils.join(errors, "\n") + "\n----------\n\nRecords were NOT updated.");
		} else {
			
			//Open connection to Solr
			SolrHelper.openPagesSolrClientConnection();
			
			int numberOfSitesToProcess = siteDataRecords.size();
			
			//For each site, queue up a worker
			ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(HrwaManager.maxNumberOfThreads);
			for(SiteData siteData : siteDataRecords) {
				Runnable worker = new PageDataToSolrWorker(siteData.bibId);
				executor.execute(worker);
			}
	        executor.shutdown();
	        
	        while (!executor.isTerminated()) {
	        	System.out.println(
	        		"Processed associated Pages for " + executor.getCompletedTaskCount() + " of " + numberOfSitesToProcess + " sites." + "\n" +
	        		HrwaManager.getCurrentAppMemoryUsageMessage() + "\n" +
	        		this.getClass().getSimpleName() + " run time: " + ((System.currentTimeMillis()-startTime)/1000) + " seconds"
	        	);
	        	try { Thread.sleep(STATUS_POLLING_INTERVAL_IN_MILLIS); } catch (InterruptedException e) { e.printStackTrace(); }
	        }
	        
	        System.out.println(
	    		"Processed " + numberOfSitesToProcess + " of " + numberOfSitesToProcess + " archive files." + "\n" +
	    		HrwaManager.getCurrentAppMemoryUsageMessage()
	    	);
	        
	        //Commit solr changes
			try {
				System.out.println("Committing Solr updates...");
				SolrHelper.getPagesSolrClient().commit();
				System.out.println("Updates committed.");
			} catch (SolrServerException | IOException e) {
				HrwaManager.logger.error("Exception encountered while committing changes to solr in " + this.getClass().getName() + ".  Message: " + e.getMessage());
			}

			//Be sure to close solr client connection
			SolrHelper.closePagesClientConnection();
		}
		
        System.out.println("Done.");
	}
	
}
