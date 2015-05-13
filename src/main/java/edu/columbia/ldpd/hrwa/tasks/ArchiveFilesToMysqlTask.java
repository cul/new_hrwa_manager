package edu.columbia.ldpd.hrwa.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.FileUtils;

import edu.columbia.ldpd.hrwa.HrwaManager;
import edu.columbia.ldpd.hrwa.tasks.workers.ArchiveToMysqlWorker;

public class ArchiveFilesToMysqlTask extends AbstractTask {
	
	public static final int STATUS_POLLING_INTERVAL_IN_MILLIS = 10000; // How frequently we get status messages about progress during processing. 
	private static final String[] archiveFileExtensions = {"arc.gz", "warc.gz"};
	
	public ArchiveFilesToMysqlTask() {
		
	}

	@Override
	public void taskImpl() {
		
		//Get all archive files
		ArrayList<File> archiveFiles = getAlphabeticallySortedListOfArchiveFiles(HrwaManager.archiveFileDirectory);
		int numberOfFilesToProcess = archiveFiles.size();
		
		//For each archive file, queue up a worker
		ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(HrwaManager.maxNumberOfThreads);
		for(File archiveFile : archiveFiles) {
			Runnable worker = new ArchiveToMysqlWorker(archiveFile.getAbsolutePath());
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
	
	public ArrayList<File> getAlphabeticallySortedListOfArchiveFiles(String pathToDirectory) {
		
		ArrayList<File> files = new ArrayList<File>(FileUtils.listFiles(new File(pathToDirectory), archiveFileExtensions, true));
		
		Collections.sort(files, new Comparator<File>(){
			public int compare(File f1, File f2)
		    {
		        return (f1.getPath()).compareTo(f2.getPath());
		    }
		});
		
		return files;
	}
	
}
