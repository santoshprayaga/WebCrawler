package com.imaginea.task;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

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
			link.getWebLinks(pageURL, year, outFile, searchTag, searchAttr, connTryCount);
		} catch (Exception e) {
			LOG.error("Excpetion in Main Class is:: " + e.getMessage());
		}
	}
}