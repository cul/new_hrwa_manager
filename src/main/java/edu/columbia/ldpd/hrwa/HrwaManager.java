package edu.columbia.ldpd.hrwa;

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

public class HrwaManager {

	public static final int EXIT_CODE_SUCCESS = 0;
	public static final int EXIT_CODE_FAILURE = 1;
	public static Logger logger = LoggerFactory.getLogger(HrwaManager.class);

	// Options from command line
	public static String mysqlUrl;
	public static String mysqlDatabase;
	public static String mysqlUsername;
	public static String mysqlPassword;
	public static String sitesSolrUrl;
	public static String pagesSolrUrl;
	public static String archiveFileDirectory;
	public static boolean runTaskVoyagerToMysql;
	public static boolean runTaskRelatedHostsFileToMysql;
	public static boolean runTaskArchiveFilesToMysql;
	public static boolean runTaskMysqlSitesToSolr;
	public static boolean runTaskMysqlPagesToSolr;

	public static void main(String[] args) {
		logger.info("Starting HRWA Manager run.");
		setupAndParseCommandLineOptions(args);
		
		if(HrwaManager.runTaskVoyagerToMysql){}
		if(HrwaManager.runTaskRelatedHostsFileToMysql){}
		if(HrwaManager.runTaskArchiveFilesToMysql){}
		if(HrwaManager.runTaskMysqlPagesToSolr){}
		if(HrwaManager.runTaskMysqlPagesToSolr){}

		// Continue here with the two lines below
		// WARCReaderFactory.get(file)
		// ARCReaderFactory.get(file)

		// Iterator<ArchiveRecord> archIt = WARCReaderFactory.get(new
		// File(args[0])).iterator();
		// while (archIt.hasNext()) {
		// handleRecord(archIt.next());
		// }
	}

	public static void setupAndParseCommandLineOptions(String[] args) {
		// create the Options
		Options options = new Options();

		// Boolean options
		options.addOption("help", false, "Usage information.");

		// Options with values
		options.addOption("mysql_url", true, "MySQL database url.");
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
		options.addOption("run_task_voyager_to_mysql", false,
				"Retrieve site data from Voyager and save it in the database.");
		options.addOption("run_task_related_hosts_file_to_mysql", false,
				"Extract data from related hosts file and save it in the database.");
		options.addOption("run_task_archive_files_to_mysql", false,
				"Extract data from ARC/WARC files and save it in the database.");
		options.addOption("run_task_mysql_sites_to_solr", false,
				"Index SITE data from database into solr sites core.");
		options.addOption("run_task_mysql_pages_to_solr", false,
				"Index PAGE data from database into solr pages core.");

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
				HrwaManager.mysqlUrl = cmdLine.getOptionValue("mysql_url");
				HrwaManager.mysqlDatabase = cmdLine.getOptionValue("mysql_database");
				HrwaManager.mysqlUsername = cmdLine.getOptionValue("mysql_username");
				HrwaManager.mysqlPassword = cmdLine.getOptionValue("mysql_password");
				HrwaManager.sitesSolrUrl = cmdLine.getOptionValue("sites_solr_url");
				HrwaManager.pagesSolrUrl = cmdLine.getOptionValue("pages_solr_url");
				HrwaManager.archiveFileDirectory = cmdLine.getOptionValue("archive_file_directory");
				HrwaManager.runTaskVoyagerToMysql = cmdLine.hasOption("run_task_voyager_to_mysql");
				HrwaManager.runTaskRelatedHostsFileToMysql = cmdLine.hasOption("run_task_related_hosts_file_to_mysql");
				HrwaManager.runTaskArchiveFilesToMysql = cmdLine.hasOption("run_task_archive_files_to_mysql");
				HrwaManager.runTaskMysqlPagesToSolr = cmdLine.hasOption("run_task_mysql_sites_to_solr");
				HrwaManager.runTaskMysqlPagesToSolr = cmdLine.hasOption("run_task_mysql_pages_to_solr");				
			}

		} catch (ParseException e) {
			logger.error("Error parsing command line args: " + e.getMessage());
			e.printStackTrace();
			System.exit(HrwaManager.EXIT_CODE_FAILURE);
		}
	}

}
