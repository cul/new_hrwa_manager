package edu.columbia.ldpd.hrwa.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;

import edu.columbia.ldpd.hrwa.HrwaManager;
import edu.columbia.ldpd.hrwa.SiteData;
import edu.columbia.ldpd.hrwa.util.ElasticsearchHelper;
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
			System.out.println("Processed " + counter + " of " + numberOfFilesToProcess + " Site records.");
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
			//No errors found!  Let's save/update these records.
			
			//To be safe, we're only going to perform deletion comparisons and updates if there is at least one record to update
			if(siteDataRecords.size() == 0) {
				HrwaManager.logger.error("-- Zero source MARC site records were found.  This is suspicious, so no updates will be performed. --");
			} else {
				
				//Get list of existing records so we can determine what changed and what needs to be marked as new or deleted
				ArrayList<SiteData> existingRecords = SiteData.getAllRecords();
				
				//Get list of bib ids of existing records (for later comparison)
				ArrayList<String> bibIdsOfExistingRecords = new ArrayList<String>();
				for(SiteData siteData : existingRecords) {
					bibIdsOfExistingRecords.add(siteData.bibId);
				}
				
				//Get list of bib ids of latest set of records (for later comparison)
				ArrayList<String> bibIdsOfLatestSetOfRecords = new ArrayList<String>();
				for(SiteData siteDataRecord : siteDataRecords) {
					bibIdsOfLatestSetOfRecords.add(siteDataRecord.bibId);
				}
				
				//Generate mapping of bibId values to existing SiteData records
				HashMap<String, SiteData> bibIdsToExistingSiteDataRecords = new HashMap<String, SiteData>();
				for(SiteData siteDataRecord : existingRecords) {
					bibIdsToExistingSiteDataRecords.put(siteDataRecord.bibId, siteDataRecord);
				}
				
				//Generate mapping of bibId values to latest SiteData records
				HashMap<String, SiteData> bibIdsToLatestSiteDataRecords = new HashMap<String, SiteData>();
				for(SiteData siteDataRecord : siteDataRecords) {
					bibIdsToLatestSiteDataRecords.put(siteDataRecord.bibId, siteDataRecord);
				}
				
				//Generate list of records to be marked as new
				HashSet<String> bibIdsOfNewRecords = new HashSet<String>(bibIdsOfLatestSetOfRecords);
				bibIdsOfNewRecords.removeAll(bibIdsOfExistingRecords);
				
				//Generate list of records to be deleted
				HashSet<String> bibIdsOfDeletedRecords = new HashSet<String>(bibIdsOfExistingRecords);
				bibIdsOfDeletedRecords.removeAll(bibIdsOfLatestSetOfRecords);
				
				ArrayList<SiteData> changedRecords = new ArrayList<SiteData>(); 
				int numberOfDeletedRecords = 0;
				int numberOfNewRecords = 0;
				int numberOfUpdatedExistingRecords = 0;
				
				//Go through and mark records appropriately, and then send them for updates
				
				//Mark deleted records as changed and add them to the list of changed records that need updating 
				for(String bibId : bibIdsOfDeletedRecords) {
					SiteData siteData = bibIdsToExistingSiteDataRecords.get(bibId); //MUST select deleted records from EXISTING set of records, not the LATEST set
					siteData.status = SiteData.STATUS_DELETED;
					changedRecords.add(siteData);
					numberOfDeletedRecords++;
				}
				//Also add new records to the list of changed records that need updating,
				//along with existing records that require updating because of marc 005 field changes.
				//Select new/updated records from LATEST set of records, not the EXISTING set (otherwise new records wouldn't be present).
				for(SiteData siteDataRecord : siteDataRecords) {
					if(bibIdsOfNewRecords.contains(siteDataRecord.bibId)) {
						//Mark new sites as updated
						siteDataRecord.status = SiteData.STATUS_UPDATED;
						changedRecords.add(siteDataRecord);
						numberOfNewRecords++;
					} else if( ! bibIdsToExistingSiteDataRecords.get(siteDataRecord.bibId).marc005LastModified.equals(bibIdsToLatestSiteDataRecords.get(siteDataRecord.bibId).marc005LastModified))  {
						System.out.println("Found existing record to UPDATE!");
						//Mark updated records as updated if their marc 005 value changed
						siteDataRecord.status = SiteData.STATUS_UPDATED;
						changedRecords.add(siteDataRecord);
						numberOfUpdatedExistingRecords++;
					}
				}
				
				System.out.println("Will mark " + numberOfDeletedRecords + " removed " + (numberOfDeletedRecords == 1 ? "Site" : "Sites") + " as: " + SiteData.STATUS_DELETED);
				System.out.println("Will mark " + numberOfNewRecords + " new " + (numberOfNewRecords == 1 ? "Site" : "Sites") + " as: " + SiteData.STATUS_UPDATED);
				System.out.println("Will mark " + numberOfUpdatedExistingRecords + " existing " + (numberOfUpdatedExistingRecords == 1 ? "Site" : "Sites") + " as: " + SiteData.STATUS_UPDATED);
				
				int changedRecordCounter = 1;
				int numberOfChangedRecords = changedRecords.size();
				System.out.println("--> " + numberOfChangedRecords + " " + (numberOfChangedRecords == 1 ? "record has" : "records have") + " changed.");
				for(SiteData siteDataRecord : changedRecords) {
					try {
						siteDataRecord.sendToElasticsearch(ElasticsearchHelper.getTransportClient());
						System.out.println("Sent record " + changedRecordCounter + " of " + numberOfChangedRecords + " to Elasticsearch.");
					} catch (ElasticsearchException e) {
						HrwaManager.logger.error("ElasticsearchException encountered while sending SiteData to Elasticsearch. Bib ID: " + siteDataRecord.bibId + ", Error Message: " + e.getMessage());
					} catch (IOException e) {
						HrwaManager.logger.error("IOException encountered while sending PageData to Elasticsearch. Bib ID: " + siteDataRecord.bibId + ", Error Message: " + e.getMessage());
					}
					changedRecordCounter++;
				}
			}
			
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
