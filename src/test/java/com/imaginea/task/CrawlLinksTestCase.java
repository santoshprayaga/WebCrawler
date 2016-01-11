package com.imaginea.task;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

public class CrawlLinksTestCase {

	private CrawlLinks link;
	private ResourceBundle bundle = ResourceBundle.getBundle("webcrawler", Locale.ENGLISH);

	String pageURL;
	String year;
	File outFile;
	int level;
	Elements links;
	String samplePageLink ;

	@Before
	public void getCrawlClassInstance() {
		try {
			link = new CrawlLinks();
			pageURL = bundle.getString("pageURL");
			year = bundle.getString("year");
			outFile = new File(bundle.getString("filePath"));
			level = 5;
			links = Jsoup.connect(pageURL).get().getAllElements();
			samplePageLink = bundle.getString("samplePageLink");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetWebLinks() {
		link.getWebLinks(pageURL, year, outFile);
	}

	@Test
	public void testFetchHrefLinks() {
		link.fetchHrefLinks(links, pageURL, level, year, outFile);
	}

	@Test
	public void testGetPageLinks() {
		link.getPageLinks(samplePageLink, level, year, outFile);
	}

}
