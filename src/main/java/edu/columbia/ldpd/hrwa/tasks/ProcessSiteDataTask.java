package edu.columbia.ldpd.hrwa.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import edu.columbia.ldpd.hrwa.HrwaManager;
import edu.columbia.ldpd.hrwa.SiteData;
import edu.columbia.ldpd.hrwa.tasks.workers.ProcessPageDataWorker;
import edu.columbia.ldpd.hrwa.util.ElasticsearchHelper;
import edu.columbia.ldpd.hrwa.util.MysqlHelper;
import edu.columbia.ldpd.marc.z3950.MARCFetcher;

public class ProcessSiteDataTask extends AbstractTask {
	
	public ProcessSiteDataTask() {
		
	}

	@Override
	public void taskImpl() {
		
		//Create Elasticsearch index if it doesn't already exist
		SiteData.creatElastisearchIndexIfNotExist();
		
		//Get site data from Voyager MARC records
		
		//Download latest version (unless we're reusing the latest downloaded set)
		if( HrwaManager.reuseLatestDownloadedVoyagerData ) {
			System.out.println("Reusing already-downloaded MARC files (instead of re-downloading the latest set).");
		} else {
			System.out.println("Clearing old MARC download...");
			clearDownloadedVoyagerContent();
			
			// Create download path on filesystem
			new File(HrwaManager.MARC_DOWNLOAD_DIR).mkdirs();
			
			// Perform download
			System.out.println("Fetching new MARC records with 965 marker: " + HrwaManager.HRWA_965_MARKER);
			MARCFetcher marcFetcher = new MARCFetcher(new File(HrwaManager.MARC_DOWNLOAD_DIR));
			marcFetcher.fetch(1, 9000, HrwaManager.HRWA_965_MARKER);
		}
		
		// Collect list of marcXmlFiles in File array   
		File dir = new File(HrwaManager.MARC_DOWNLOAD_DIR);
		File[] marcXmlFiles = dir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".xml");
		    }
		});
		int numberOfFilesToProcess = marcXmlFiles.length;
		System.out.println("Found " + numberOfFilesToProcess + " files.");
		
		ArrayList<SiteData> siteDataRecords = new ArrayList<SiteData>(); 
		
		//Process records
		int counter = 1;
		for(File marcXmlFile : marcXmlFiles) {
			try {
				FileInputStream fis = new FileInputStream(marcXmlFile);
				SiteData siteData = new SiteData(fis);
				fis.close();
				siteDataRecords.add(siteData);
			} catch (FileNotFoundException e) {
				HrwaManager.logger.error(
					"Could not find file: " + marcXmlFile.getAbsolutePath() + "\n" +
					"Message: " + e.getMessage()
				);
			} catch (IOException e) {
				HrwaManager.logger.error(
					"IOException encountered while processing file: " + marcXmlFile.getAbsolutePath() + "\n" +
					"Message: " + e.getMessage()
				);
			}
			System.out.println("Processed " + counter + " of " + numberOfFilesToProcess + " archive files.");
			counter++;
		}
		
		//Validate records
		boolean foundAtLeastOneError = false;
		ArrayList<String> errors = new ArrayList<String>(); 
		for(SiteData siteDataRecord : siteDataRecords) {
			if( ! siteDataRecord.isValid() ) {
				foundAtLeastOneError = true;
				errors.add("Could not update site record with bib id: " + siteDataRecord.bibId + " due to the following error(s): " + StringUtils.join(siteDataRecord.getValidationErrors()));
			}
		}
		
		if(foundAtLeastOneError) {
			HrwaManager.logger.error("One or more errors were found during " + this.getClass().getName() + " run. These must be fixed before the process can continue:\n----------\n" + StringUtils.join(errors, "\n") + "\n----------\n\nRecords were NOT updated.");
		} else {
			//No errors found!  Let's save these records.
			
			TransportClient elasticsearchClient = new TransportClient();
			elasticsearchClient.addTransportAddress(new InetSocketTransportAddress(HrwaManager.elasticsearchHostname, HrwaManager.elasticsearchPort));
			
			for(SiteData siteDataRecord : siteDataRecords) {
				try {
					siteDataRecord.sendToElasticsearch(elasticsearchClient);
				} catch (ElasticsearchException e) {
					HrwaManager.logger.error("ElasticsearchException encountered while sending SiteData to Elasticsearch. Bib ID: " + siteDataRecord.bibId + ", Error Message: " + e.getMessage());
				} catch (IOException e) {
					HrwaManager.logger.error("IOException encountered while sending PageData to Elasticsearch. Bib ID: " + siteDataRecord.bibId + ", Error Message: " + e.getMessage());
				}
				
			}
			
			//Be sure to close the connection when we're done
			elasticsearchClient.close();
		}
		
		System.out.println("Flushing Elasticsearch updates...");
		//Flush elasticsearch changes, otherwise all index changes won't necessarily be up to date in time for the next task to run.
		ElasticsearchHelper.flushIndexChanges(HrwaManager.ELASTICSEARCH_SITE_INDEX_NAME);
		System.out.println("Updates have been flushed.");
        
        System.out.println("Done.");
	}
	
	public void clearDownloadedVoyagerContent() {
		try {
			FileUtils.deleteDirectory(new File(HrwaManager.MARC_DOWNLOAD_DIR));
		} catch (IOException e) {
			System.out.println("Could not delete all downloaded voyager content for some reason. Tried to delete directory: " + HrwaManager.MARC_DOWNLOAD_DIR);
			e.printStackTrace();
		}
	}
	
}
