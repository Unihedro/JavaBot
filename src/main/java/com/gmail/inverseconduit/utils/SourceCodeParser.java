package com.gmail.inverseconduit.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a websites source code and returns a List of matches of a search string
 * @author Michel v. Varendorff
 *
 */
public class SourceCodeParser {

	String website;
	String searchRegex;

	public SourceCodeParser(String website, String searchRegex){
		this.website = website;
		this.searchRegex = searchRegex;
	}
	
	public SourceCodeParser() {	}
	
	/**
	 * Sets the website to search through
	 * @param website URL of the website
	 */
	public void setWebsite(String website) {
		this.website = website;
	}

	/**
	 * Sets a regex that will be looked for in the websites DOM
	 * @param searchRegex Regex to search matches for
	 */
	public void setSearch(String searchRegex) {
		this.searchRegex = searchRegex;
	}
	
	/**
	 * Loads the websites code and returns the matches
	 * @return List of all matches of the search
	 */
	public List<String> parse(){
		String websiteCode = readWebsiteCode();
		return getAllMatches(websiteCode);
	}

	/**
	 * Reads the code of the website into one single String
	 * @return Websites DOM as String
	 */
	private String readWebsiteCode(){
		InputStream is = null;
		BufferedReader br = null;
		
		String htmlCode = "";
		
		try {
			String line;
			
			URL url = new URL(website);
			is = url.openStream();
			br = new BufferedReader(new InputStreamReader(is));
			
			while ( (line = br.readLine()) != null) {
				htmlCode += line;
			}
		} catch (MalformedURLException mue){
			mue.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (is != null) is.close();
				if (br != null) br.close();
			} catch (IOException ioe) {
				
			}
		}
		return htmlCode;
	}
	
	/**
	 * Finds all matches of the search regex in the input String
	 * @param htmlCode Code to look for matches
	 * @return List of all Matches in the String
	 */
	private List<String> getAllMatches(String htmlCode) {
		List<String> matches = new ArrayList<>();
		
		Matcher m = Pattern.compile(searchRegex).matcher(htmlCode);
		
		while (m.find()) {
			matches.add(m.group(0));
		}
		
		return matches;
		
	}
	
	
}
