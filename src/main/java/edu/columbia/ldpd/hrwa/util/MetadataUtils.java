package edu.columbia.ldpd.hrwa.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

import edu.columbia.ldpd.hrwa.HrwaManager;

public class MetadataUtils {
	
	public static Pattern wwwVariationPattern = Pattern.compile("\\Awww\\d*\\.");

	/**
	 * Extracts a hostString from a given url String, removing a leading "www", "www1", etc., and also removing the url path.
	 e.g. "http://www.example.com/abc" or "http://www1.example.com/abc" are converted into "example.com".  "http://test.example.com/abc" is converted into "text.example.com".
	 * @param url
	 * @return
	 */
	public static String extractHostString(String url) {
		if( ! url.startsWith("http://") && ! url.startsWith("https://") ) { url = "http://" + url; } //This is so that an hoststring (which has no protocol) can also be run through this method 
		
		URL uri;
		try {
			uri = new URL(url);
			String host = uri.getHost();
			if(host == null) {
				HrwaManager.logger.error("Unable to parse URI: " + url);
				return null;
			} else {
				return uri.getHost().replaceFirst("\\Awww\\d*\\.", "");
			}
		} catch (MalformedURLException e) {
			HrwaManager.logger.error("Unable to parse URL: " + url);
			return null;
		}
    }
	
	/**
	 * Extracts a urlPrefixString from a given url String, removing a leading "www", "www1", etc., but KEEPING the url path. If the path portion of the url only contains a "/", the "/" is removed.
	 e.g. "http://www.example.com/abc" or "http://www1.example.com/abc" are converted into "example.com/abc".  "http://test.example.com/abc" is converted into "text.example.com/abc".
	 * @param url
	 * @return
	 */
	public static String extractHostStringWithPath(String url) {
		if( ! url.startsWith("http://") && ! url.startsWith("https://") ) { url = "http://" + url; } //This is so that an hoststring (which has no protocol) can also be run through this method 
		
		URL uri;
		try {
			uri = new URL(url);
			String host = uri.getHost();
			if(host == null) {
				HrwaManager.logger.error("Unable to parse URI: " + url);
				return null;
			} else {
				String uriPath = uri.getPath();
				return uri.getHost().replaceFirst("\\Awww\\d*\\.", "") + (uriPath.equals("/") ? "" : uriPath);
			}
		} catch (MalformedURLException e) {
			HrwaManager.logger.error("Unable to parse URL: " + url);
			return null;
		}
    }

}