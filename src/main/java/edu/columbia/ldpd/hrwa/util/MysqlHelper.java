package edu.columbia.ldpd.hrwa.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import edu.columbia.ldpd.hrwa.HrwaManager;

public class MysqlHelper {
	
	public static final String PAGES_TABLE_NAME = "pages";
	public static MysqlDataSource dataSource;
	
	static {
		dataSource = new MysqlDataSource();
		dataSource.setUser(HrwaManager.mysqlUsername);
		dataSource.setPassword(HrwaManager.mysqlPassword);
		dataSource.setServerName(HrwaManager.mysqlHostname);
		dataSource.setPort(HrwaManager.mysqlPort);
		dataSource.setDatabaseName(HrwaManager.mysqlDatabase);
	}
	
	public static Connection getNewDBConnection() {
		
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			HrwaManager.logger.error("Could not connect to MySQL at url:" + HrwaManager.mysqlHostname + ":" + HrwaManager.mysqlPort + "\n" +
			    "SQLException: " + e.getMessage() + "\n" +
			    "SQLState: " + e.getSQLState()  + "\n" +
			    "VendorError: " + e.getErrorCode()
			);
			return null;
		}
	}
	
	/**
	 * A clean way of closing a MySQL connection and logging any encountered errors.
	 * @param conn
	 */
	public static void closeConnection(Connection conn) {
		try {
			conn.close();
		} catch (SQLException e) {
			HrwaManager.logger.error("Error encountered while attempting to close MySQL connection." + "\n" +
			    "SQLException: " + e.getMessage() + "\n" +
			    "SQLState: " + e.getSQLState()  + "\n" +
			    "VendorError: " + e.getErrorCode()
			);
		}
	}
	
	
	
	
	
	
	public static void createPagesTableIfItDoesNotExist() throws SQLException {
	
		Connection conn = MysqlHelper.getNewDBConnection();
	
		PreparedStatement pstmt0 = conn.prepareStatement(
			"CREATE TABLE IF NOT EXISTS `" + MysqlHelper.PAGES_TABLE_NAME + "` (" +
			"  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto-incremented unique numeric identifier for MySQL convenience.'," +
			"  `original_url` varchar(2100) NOT NULL COMMENT 'Original url of this crawled record.'," +
			"  `hoststring` varchar(255) DEFAULT NULL COMMENT 'Truncated url, only including hostname and removing www, www1, www2, etc. if present.'," +
			"  `archive_file` varchar(255) NOT NULL COMMENT 'Name of the archive file (warc/arc) that this record came from.'," +
			"  `offset_in_archive_file` bigint(20) unsigned NOT NULL COMMENT 'This is the byte offset address of the record in the archive file.'," +
			"  `content_length` bigint(20) unsigned NOT NULL COMMENT 'Size of the content returned in the HTTP response in bytes. Largest will probably be video.'," +
			"  `crawl_date` char(14) NOT NULL COMMENT 'Crawl date for this record.'," +
			"  `fulltext` text DEFAULT NULL COMMENT 'Full text extracted from record content.'," +
			"  `mimetype_from_header` varchar(100) DEFAULT NULL COMMENT 'Mimetype supplied by the archive file header.'," +
			"  `mimetype_detected` varchar(100) DEFAULT NULL COMMENT 'Mimetype detected by the HRWA Manager application.  NULL if mimetype could not be detected.'," +
			"  `record_identifier` varchar(2300) NOT NULL COMMENT 'Unique identifier for this record.  Of the format: record_date/url'," +
			"  `status_code` int(3) NOT NULL COMMENT 'HTTP response status code at record crawl time.'," +
			"  PRIMARY KEY (`id`)" +
			//"  KEY `mimetype_from_header` (`mimetype_from_header`)," +
			//"  KEY `detected_mimetype` (`detected_mimetype`)," +
			//"  KEY `hoststring` (`hoststring`)," +
			//"  KEY `site_id` (`site_id`)," +
			//"  KEY `record_date` (`record_date`)," +
			//"  KEY `archive_file` (`archive_file`)" +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;"
		);
		pstmt0.execute();
		pstmt0.close();
		
		MysqlHelper.closeConnection(conn);
		
	}
	
}
