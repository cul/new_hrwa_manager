package edu.columbia.ldpd.hrwa.tasks;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import edu.columbia.ldpd.hrwa.HrwaManager;
import edu.columbia.ldpd.hrwa.SiteData;

public class SiteDataToSolrTask extends AbstractTask {
	
	public SiteDataToSolrTask() {
		
	}

	@Override
	public void taskImpl() {
		
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
			
			//Ensure that there is at least one record to update (for safety, so that we don't delete all records and have no records to update)
			if(siteDataRecords.size() > 0) {
				//Establish connection to solr
				SolrClient solrClient = new HttpSolrClient(HrwaManager.sitesSolrUrl);
				
				//Delete current records
				try {
					solrClient.deleteByQuery("*:*");
				} catch (SolrServerException | IOException e) {
					HrwaManager.logger.error("Exception encountered while deleting existing site solr docs in " + this.getClass().getName() + ".  Message: " + e.getMessage());
				}
				
				//Update index with new records
				int i = 0;
				int total = siteDataRecords.size();
				System.out.println("Sending sites to solr...");
				for(SiteData siteData : siteDataRecords) {
					siteData.sendToSolr(solrClient);
					i++;
					System.out.println("Sent " + i + " of " + total);
				}
				
				// Commit changes
				try {
					solrClient.commit();
					System.out.println("Updates committed.");
				} catch (SolrServerException | IOException e) {
					HrwaManager.logger.error("Exception encountered while committing changes to solr in " + this.getClass().getName() + ".  Message: " + e.getMessage());
				}

				//Be sure to close solr client connection
				try {
					solrClient.close();
				} catch (IOException e) {
					HrwaManager.logger.error("IOException encountered while attempting to close connection to solr after " + this.getClass().getName() + " run. Message: " + e.getMessage());
				}
			}
			
		}
        
        System.out.println("Done.");
	}
	
}
