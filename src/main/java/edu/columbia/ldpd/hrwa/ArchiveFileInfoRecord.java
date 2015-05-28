package edu.columbia.ldpd.hrwa;

import org.jwat.arc.ArcRecordBase;
import org.jwat.warc.WarcRecord;

public class ArchiveFileInfoRecord {
	
	public String archiveFileName;
	
	public ArchiveFileInfoRecord(WarcRecord warcRecord) throws UnexpectedRecordTypeException, MissingArchiveHeaderValueException {
		if(warcRecord.getHeader("WARC-Type") != null && warcRecord.getHeader("WARC-Type").value.equals("warcinfo")) {			
			if(warcRecord.getHeader("WARC-Filename") != null) {
				archiveFileName = warcRecord.getHeader("WARC-Filename").value;
			} else {
				throw new MissingArchiveHeaderValueException("WarcRecord is missing WARC-Filename header.");				
			}
		} else {
			throw new UnexpectedRecordTypeException("Invalid WarcRecord provided.  Must be of WARC-Type: warcinfo");
		}
	}
	
	public ArchiveFileInfoRecord(ArcRecordBase arcRecord) throws UnexpectedRecordTypeException {
		String urlString = arcRecord.getUrlStr();
		if( urlString.startsWith("filedesc://") ) {			
			this.archiveFileName = urlString.substring(11); //substring(11) to chop off leading "filedesc://"
		} else {
			throw new UnexpectedRecordTypeException("Invalid ArcRecordBase provided.  Must have URL that starts with filedesc://");
		}
	}
	
	
	
	
	
	
	
	public class UnexpectedRecordTypeException extends Exception {
		private static final long serialVersionUID = 4470547310887930773L;
		
		public UnexpectedRecordTypeException(String message) {
			super(message);
		}
	}
	
	public class MissingArchiveHeaderValueException extends Exception {
		private static final long serialVersionUID = 9139949953462480862L;

		public MissingArchiveHeaderValueException(String message) {
			super(message);
		}
	}
}
