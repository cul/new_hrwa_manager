package edu.columbia.ldpd.hrwa.util;

import java.net.URI;
import java.net.URISyntaxException;
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
		URI uri;
		try {
			uri = new URI(url);
			String host = uri.getHost();
			if(host == null) {
				HrwaManager.logger.error("Unable to parse URI: " + url);
				return null;
			} else {
				return uri.getHost().replaceFirst("\\Awww\\d*\\.", "");
			}
		} catch (URISyntaxException e) {
			HrwaManager.logger.error("Unable to parse URI: " + url);
			return null;
		}
    }

}