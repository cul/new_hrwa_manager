package edu.columbia.ldpd.hrwa;

import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
//import org.archive.io.arc.ARCReaderFactory;
//import org.archive.io.warc.WARCReaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.ldpd.hrwa.tasks.AbstractTask;
import edu.columbia.ldpd.hrwa.tasks.ProcessPageDataTask;
import edu.columbia.ldpd.hrwa.tasks.ProcessSiteDataTask;
import edu.columbia.ldpd.hrwa.tasks.SiteDataToSolrTask;
import edu.columbia.ldpd.hrwa.util.ElasticsearchHelper;

public class HrwaManager {

	public static final int EXIT_CODE_SUCCESS = 0;
	public static final int EXIT_CODE_ERROR = 1;
	public static final int BYTES_IN_A_MEGABYTE = 1048576;
	
	public static final long APP_START_TIME_IN_MILLIS = System.currentTimeMillis();
	public static final long MAX_AVAILABLE_MEMORY_IN_BYTES = Runtime.getRuntime().maxMemory();
	
	public static final String 	ELASTICSEARCH_ARCHIVE_FILE_INDEX_NAME = "hrwa_archive_files";
	public static final int 	ELASTICSEARCH_ARCHIVE_FILE_INDEX_NUM_SHARDS = 1;
	public static final int 	ELASTICSEARCH_ARCHIVE_FILE_INDEX_NUM_REPLICAS = 0;
	public static final String 	ELASTICSEARCH_ARCHIVE_FILE_TYPE_NAME = "archive_file";
	public static final String 	ELASTICSEARCH_PAGE_INDEX_NAME = "hrwa_pages";
	public static final int 	ELASTICSEARCH_PAGE_INDEX_NUM_SHARDS = 1;
	public static final int 	ELASTICSEARCH_PAGE_INDEX_NUM_REPLICAS = 0;
	public static final String 	ELASTICSEARCH_PAGE_TYPE_NAME = "page";
	public static final String 	ELASTICSEARCH_SITE_INDEX_NAME = "hrwa_sites";
	public static final int 	ELASTICSEARCH_SITE_INDEX_NUM_SHARDS = 1;
	public static final int 	ELASTICSEARCH_SITE_INDEX_NUM_REPLICAS = 0;
	public static final String 	ELASTICSEARCH_SITE_TYPE_NAME = "site";
	
	public static final String MARC_DOWNLOAD_DIR = "temp/marcxml-download";
	public static final String HRWA_965_MARKER = "965hrportal";
	
	public static final Logger logger = LoggerFactory.getLogger(HrwaManager.class);

	// Options from command line
	public static String elasticsearchHostname;
	public static int elasticsearchPort;
	public static String sitesSolrUrl;
	public static String pagesSolrUrl;
	public static String archiveFileDirectory;
	public static String relatedHostsFile = "";
	public static boolean reuseLatestDownloadedVoyagerData;
	public static boolean runTaskProcessSiteData;
	public static boolean runTaskProcessPageData;
	public static boolean runTaskSiteDataToSolr;
	public static boolean runTaskPageDataToSolr;
	public static int maxNumberOfThreads;
	public static long minAvailableMemoryInBytesForNewProcess;

	public static void main(String[] args) {
		
		logger.info("Starting HRWA Manager run.");
		logger.debug("Logger running in debug mode."); //Only shows up when we're logging in debug mode
		
		setupAndParseCommandLineOptions(args);
		
		logger.info(
			"Run configuration:" + "\n" +
			"- Max number of threads: " + HrwaManager.maxNumberOfThreads + "\n" +
			"- Min available memory (in bytes) for new process: " + HrwaManager.minAvailableMemoryInBytesForNewProcess + " (" + (HrwaManager.minAvailableMemoryInBytesForNewProcess/BYTES_IN_A_MEGABYTE) + " MB)"
		);
		
		ArrayList<AbstractTask> tasksToRun = new ArrayList<AbstractTask>();
		
		//Proper task order defined below
		if(HrwaManager.runTaskProcessSiteData){ tasksToRun.add(new ProcessSiteDataTask()); }
		if(HrwaManager.runTaskProcessPageData){ tasksToRun.add(new ProcessPageDataTask()); }
		if(HrwaManager.runTaskSiteDataToSolr){ tasksToRun.add(new SiteDataToSolrTask()); }
		if(HrwaManager.runTaskPageDataToSolr){}
		
		
		//Connect to elasticsearch
		ElasticsearchHelper.openTransportClientConnection();
		
		for(AbstractTask task : tasksToRun) {
			task.runTask();
		}
		
		//Disconnect from elasticsearch
		ElasticsearchHelper.closeTransportClientConnection();

	}

	public static void setupAndParseCommandLineOptions(String[] args) {
		// create the Options
		Options options = new Options();

		// Boolean options
		options.addOption("help", false, "Usage information.");

		// Options with values
		options.addOption("elasticsearch_hostname", true, "Elasticsearch indexing/search server hostname.");
		options.addOption("elasticsearch_port", true, "Elasticsearch indexing/search server port.");
		options.addOption("sites_solr_url", true,
				"Solr url for the sites core.");
		options.addOption("pages_solr_url", true,
				"Solr url for the pages core.");
		options.addOption("archive_file_directory", true,
				"Directory where ARC/WARC files live.");
		options.addOption("related_hosts_file", true,
				"CSV file that contains a hosts-to-related hosts mapping.");
		options.addOption("reuse_latest_downloaded_marc_data", false,
				"Reuse latest copy of downloaded marc data rather than downloading the latest version.");
		// Task-related options
		options.addOption("run_task_process_site_data", false,
				"Process site data from Voyager (and related hosts file) and save it to the datastore.");
		options.addOption("run_task_process_page_data", false,
				"Process page data from ARC/WARC files and save it to the datastore.");
		options.addOption("run_task_site_data_to_solr", false,
				"Index SITE data from datastore into solr sites core.");
		options.addOption("run_task_pages_to_solr", false,
				"Index PAGE data from database into solr pages core.");
		options.addOption("max_number_of_threads", true,
				"Maximum number of threads to use for concurrent processing (when applicable).");
		options.addOption("min_available_memory_in_bytes_for_new_process", true,
				"Minimum number of free bytes of memory required for starting a new processing job (when applicable).");
		
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmdLine = parser.parse(options, args);

			if (args.length == 0 || cmdLine.hasOption("help")) {
				// Show help
				HelpFormatter hf = new HelpFormatter();
				hf.printHelp("hrwa_manager [options]", options);
				System.exit(HrwaManager.EXIT_CODE_SUCCESS);
			} else {
				// Handle actual options
				HrwaManager.elasticsearchHostname = cmdLine.getOptionValue("elasticsearch_hostname");
				HrwaManager.elasticsearchPort = Integer.parseInt(cmdLine.getOptionValue("elasticsearch_port"));
				HrwaManager.sitesSolrUrl = cmdLine.getOptionValue("sites_solr_url");
				HrwaManager.pagesSolrUrl = cmdLine.getOptionValue("pages_solr_url");
				HrwaManager.archiveFileDirectory = cmdLine.getOptionValue("archive_file_directory");
				HrwaManager.relatedHostsFile = cmdLine.getOptionValue("related_hosts_file");
				HrwaManager.reuseLatestDownloadedVoyagerData = cmdLine.hasOption("reuse_latest_downloaded_marc_data");
				HrwaManager.runTaskProcessSiteData = cmdLine.hasOption("run_task_process_site_data");
				HrwaManager.runTaskProcessPageData = cmdLine.hasOption("run_task_process_page_data");
				HrwaManager.runTaskSiteDataToSolr = cmdLine.hasOption("run_task_site_data_to_solr");
				HrwaManager.runTaskPageDataToSolr = cmdLine.hasOption("run_task_page_data_to_solr");
				HrwaManager.maxNumberOfThreads = Integer.parseInt(cmdLine.getOptionValue("max_number_of_threads", "1"));
				
				if(cmdLine.hasOption("min_available_memory_in_bytes_for_new_process")) {
					HrwaManager.minAvailableMemoryInBytesForNewProcess = Long.parseLong(cmdLine.getOptionValue("min_available_memory_in_bytes_for_new_process"));
				} else {
					HrwaManager.minAvailableMemoryInBytesForNewProcess = (MAX_AVAILABLE_MEMORY_IN_BYTES/4L);
				}
			}

		} catch (ParseException e) {
			logger.error("Error parsing command line args: " + e.getMessage());
			e.printStackTrace();
			System.exit(HrwaManager.EXIT_CODE_ERROR);
		}
	}
	
	public static String getCurrentAppMemoryUsageMessage() {
		return "Current memory usage: " + (getCurrentAppMemoryUsageInBytes()/BYTES_IN_A_MEGABYTE) + "/" + (MAX_AVAILABLE_MEMORY_IN_BYTES/BYTES_IN_A_MEGABYTE) + " MB";
	}
	
	public static String getCurrentAppRunTime() {
		return "Current run time: " + ((System.currentTimeMillis() - HrwaManager.APP_START_TIME_IN_MILLIS)/1000) + "seconds";
	}
	
	public static long getCurrentAppMemoryUsageInBytes() {
		return Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
	}
	
	public static long getFreeMemoryInBytes() {
		return Runtime.getRuntime().freeMemory();
	}

}
