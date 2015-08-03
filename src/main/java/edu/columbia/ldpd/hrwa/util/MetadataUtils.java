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
	 * Expexts input of the format http://www.example.com
	 * @param url
	 * @return
	 */
	public static String extractHostString(String url) {
		if( ! url.startsWith("http://") ) { url = "http://" + url; } //This is so that an hoststring (which has no protocol) can also be run through this method 
		
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

}