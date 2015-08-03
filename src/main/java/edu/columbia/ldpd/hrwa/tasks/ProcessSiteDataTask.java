package edu.columbia.ldpd.hrwa.tasks;

import java.io.File;
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

import edu.columbia.ldpd.hrwa.HrwaManager;
import edu.columbia.ldpd.hrwa.tasks.workers.ProcessPageDataWorker;
import edu.columbia.ldpd.hrwa.tasks.workers.ProcessSiteDataWorker;
import edu.columbia.ldpd.hrwa.util.MysqlHelper;
import edu.columbia.ldpd.marc.z3950.MARCFetcher;

public class ProcessSiteDataTask extends AbstractTask {
	
	public static final int STATUS_POLLING_INTERVAL_IN_MILLIS = 10000; // How frequently we get status messages about progress during processing.
	
	public ProcessSiteDataTask() {
		
	}

	@Override
	public void taskImpl() {
		
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
			new MARCFetcher(new File(HrwaManager.MARC_DOWNLOAD_DIR)).fetch(1, 9000, HrwaManager.HRWA_965_MARKER);
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
		
		//For each site, queue up a worker
		ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(HrwaManager.maxNumberOfThreads);
		for(File marcXmlFile : marcXmlFiles) {
			Runnable worker = new ProcessSiteDataWorker(marcXmlFile.getAbsolutePath());
			executor.execute(worker);
		}
        executor.shutdown();
        
        while (!executor.isTerminated()) {
        	System.out.println(
        		"Processed " + executor.getCompletedTaskCount() + " of " + numberOfFilesToProcess + " archive files." + "\n" +
        		HrwaManager.getCurrentAppMemoryUsageMessage()
        	);
        	try { Thread.sleep(STATUS_POLLING_INTERVAL_IN_MILLIS); } catch (InterruptedException e) { e.printStackTrace(); }
        }
        
        System.out.println(
    		"Processed " + numberOfFilesToProcess + " of " + numberOfFilesToProcess + " archive files." + "\n" +
    		HrwaManager.getCurrentAppMemoryUsageMessage()
    	);
        
        System.out.println("Done.");
	}
	
	public void clearDownloadedVoyagerContent() {
		try {
			FileUtils.deleteDirectory(new File(HrwaManager.MARC_DOWNLOAD_DIR));
		} catch (IOException e) {
			System.out.println("Could not delete all downloaded voyager content for some reason.");
			e.printStackTrace();
		}
	}
	
}
