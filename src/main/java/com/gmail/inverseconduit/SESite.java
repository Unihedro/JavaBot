// CLASS CREATED 2014/10/19 AT 5:16:59 P.M.
// SEChat.java by Unihedron
package com.gmail.inverseconduit;

import java.net.URL;

/**
 * Generates locations to the destinated address.<br>
 * SEChat @ com.gmail.inverseconduit
 * 
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 */
public enum SESite {
	STACK_OVERFLOW("stackoverflow"), 
	STACK_EXCHANGE("stackexchange"), 
	META_STACK_EXCHANGE("meta." + STACK_EXCHANGE.domain), 
	CODE_REVIEW("codereview", STACK_EXCHANGE.domain);

	private final String domain;
	private final String rootUrl;
	private final String loginUrl;
	private final String subdomain;

	SESite(final String domain) {
		this("", domain);
	}

	SESite(String subdomain, String dir) {
		this.subdomain = subdomain;
		this.domain = dir;
		this.rootUrl = "https://" + dir + ".com/";
		this.loginUrl = subdomain.isEmpty() ? rootUrl + "users/login"
				: "https://" + subdomain + "." + domain + ".com/users/login";
	}

	public String urlToRoom(int id) throws IllegalArgumentException {
		if (id <= 0)
			throw new IllegalArgumentException("id must be a positive number.");
		return "http://chat." + domain + ".com/rooms/" + id;
	}

	public String getRootUrl() {
		return rootUrl;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public static SESite fromUrl(URL url) {
		for (SESite site : SESite.values()) {
			if (url.toString().contains(site.domain))
				return site;
		}
		return null;
	}

	public String getDomain() {
		return domain;
	}
}
