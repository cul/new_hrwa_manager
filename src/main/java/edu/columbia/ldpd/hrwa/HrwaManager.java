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

public class HrwaManager {

	public static final int EXIT_CODE_SUCCESS = 0;
	public static final int EXIT_CODE_ERROR = 1;
	public static final int BYTES_IN_A_MEGABYTE = 1048576;
	
	public static final long APP_START_TIME_IN_MILLIS = System.currentTimeMillis();
	public static final long MAX_AVAILABLE_MEMORY_IN_BYTES = Runtime.getRuntime().maxMemory();
	
	public static Logger logger = LoggerFactory.getLogger(HrwaManager.class);

	// Options from command line
	public static String mysqlHostname;
	public static int mysqlPort;
	public static String mysqlDatabase;
	public static String mysqlUsername;
	public static String mysqlPassword;
	public static String sitesSolrUrl;
	public static String pagesSolrUrl;
	public static String archiveFileDirectory;
	public static boolean runTaskProcessSiteData;
	public static boolean runTaskProcessPageData;
	public static boolean runTaskSiteDataToSolr;
	public static boolean runTaskPageDataToSolr;
	public static int maxNumberOfThreads;
	public static long minAvailableMemoryInBytesForNewProcess;

	public static void main(String[] args) {
		
		logger.info("Starting HRWA Manager run.");
		
		setupAndParseCommandLineOptions(args);
		
		logger.info(
			"Run configuration:" + "\n" +
			"- Max number of threads: " + HrwaManager.maxNumberOfThreads + "\n" +
			"- Min available memory (in bytes) for new process: " + HrwaManager.minAvailableMemoryInBytesForNewProcess + " (" + (HrwaManager.minAvailableMemoryInBytesForNewProcess/BYTES_IN_A_MEGABYTE) + " MB)"
		);
		
		ArrayList<AbstractTask> tasksToRun = new ArrayList<AbstractTask>();
		
		//Proper task order defined below
		if(HrwaManager.runTaskProcessSiteData){}
		if(HrwaManager.runTaskProcessPageData){ tasksToRun.add(new ProcessPageDataTask()); }
		if(HrwaManager.runTaskPageDataToSolr){}
		if(HrwaManager.runTaskPageDataToSolr){}
		
		for(AbstractTask task : tasksToRun) {
			task.runTask();
		}

	}

	public static void setupAndParseCommandLineOptions(String[] args) {
		// create the Options
		Options options = new Options();

		// Boolean options
		options.addOption("help", false, "Usage information.");

		// Options with values
		options.addOption("mysql_hostname", true, "MySQL database hostname.");
		options.addOption("mysql_port", true, "MySQL database port (default = 3306).");
		options.addOption("mysql_database", true, "MySQL database name.");
		options.addOption("mysql_username", true, "MySQL database username.");
		options.addOption("mysql_password", true, "MySQL database password.");
		options.addOption("sites_solr_url", true,
				"Solr url for the sites core.");
		options.addOption("pages_solr_url", true,
				"Solr url for the pages core.");
		options.addOption("archive_file_directory", true,
				"Directory where ARC/WARC files live.");
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
//				HrwaManager.mysqlHostname = cmdLine.getOptionValue("mysql_hostname");
//				HrwaManager.mysqlPort = Integer.parseInt(cmdLine.getOptionValue("mysql_port", "3306"));
//				HrwaManager.mysqlDatabase = cmdLine.getOptionValue("mysql_database");
//				HrwaManager.mysqlUsername = cmdLine.getOptionValue("mysql_username");
//				HrwaManager.mysqlPassword = cmdLine.getOptionValue("mysql_password");
				HrwaManager.sitesSolrUrl = cmdLine.getOptionValue("sites_solr_url");
				HrwaManager.pagesSolrUrl = cmdLine.getOptionValue("pages_solr_url");
				HrwaManager.archiveFileDirectory = cmdLine.getOptionValue("archive_file_directory");
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
