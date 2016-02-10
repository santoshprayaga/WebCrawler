package com.imaginea.task;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;

/**
 * @author Santosh
 *
 */

public class ExecutableClass {

	private static Logger LOG = Logger.getLogger(ExecutableClass.class);

	private static ResourceBundle bundle = ResourceBundle.getBundle("webcrawler",Locale.ENGLISH);

	public static void main(String[] args) {
		try {
			String pageURL = bundle.getString("pageURL");
			String year = bundle.getString("year");
			File outFile = new File(bundle.getString("filePath"));
			String searchTag = bundle.getString("searchTag");
			String searchAttr = bundle.getString("searchAttr");
			int connTryCount = Integer.parseInt(bundle.getString("connTryCount"));

			LOG.info("--Given Page URL: " + pageURL + " and year to fetch mails is: " + year+" --");
			CrawlLinks link = new CrawlLinks();
			LOG.info("--Getting links by calling the method getWebLinks()--");
			try {
				Jsoup.connect("http://mail-archives.apache.org/mod_mbox/maven-users/")
						.userAgent(
								"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
						.timeout(10000).execute();
				link.getWebLinks(pageURL, year, outFile, searchTag, searchAttr, connTryCount);
			} catch (IOException e) {
				LOG.error("Cannot Connect to the URL: " + pageURL + "... Please check the Internet Connection");
			}
		} catch (Exception e) {
			LOG.error("Excpetion in Main Class is:: " + e.getMessage());
		}
	}
}