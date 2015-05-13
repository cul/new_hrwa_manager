package edu.columbia.ldpd.hrwa.tasks.workers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.Iterator;

import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.warc.WARCReaderFactory;

import edu.columbia.ldpd.hrwa.HrwaManager;
import edu.columbia.ldpd.hrwa.util.MysqlHelper;

public class ArchiveToMysqlWorker implements Runnable {
	
	private Connection conn;
	private File archiveFile;
	
	public ArchiveToMysqlWorker(String pathToArchiveFile) {
		archiveFile = new File(pathToArchiveFile);
	}

	@Override
	public void run() {
		//Get a MySQL connection for this worker
		this.conn = MysqlHelper.getNewDBConnection();
		
		try {
			processArchiveFile();
		} catch (IOException e) {
			HrwaManager.logger.error(
				"IOException while processing file " + archiveFile.getAbsolutePath() + "\n" +
				"Message: " + e.getMessage()
			);
		}
		
		//Be sure to close the connection when we're done
		MysqlHelper.closeConnection(conn);
	}
	
	public void processArchiveFile() throws IOException {
		
		Iterator<ArchiveRecord> archiveRecordIterator;
		
		if(archiveFile.getName().endsWith(".warc.gz")) {
			archiveRecordIterator = WARCReaderFactory.get(archiveFile).iterator();
		}
		else if(archiveFile.getName().endsWith(".arc.gz")) {
			archiveRecordIterator = ARCReaderFactory.get(archiveFile).iterator();
		} else {
			HrwaManager.logger.error("Skipping archive file with unexpected extension: " + archiveFile.getAbsolutePath());
			return;
		}
		
		while (archiveRecordIterator.hasNext()) {
			ArchiveRecord archiveRecord = archiveRecordIterator.next();
			
			ArchiveRecordHeader header = archiveRecord.getHeader();
		}
	}

}
