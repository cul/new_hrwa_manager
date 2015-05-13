package edu.columbia.ldpd.hrwa.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import edu.columbia.ldpd.hrwa.HrwaManager;

public class MysqlHelper {
	
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
	
}
