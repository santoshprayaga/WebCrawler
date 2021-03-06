package com.imaginea.task;

import java.io.File;
import java.io.FileOutputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Santosh
 *
 */

public class CrawlLinks {

	private Logger LOG = Logger.getLogger(CrawlLinks.class);

	TreeSet<String> arrLink = new TreeSet<String>();
	String baseURL;
	HashMap<String, Integer> linkLevels = new HashMap<String, Integer>();
	HashMap<String, String> parentChild = new HashMap<String, String>();
	int connFailCount = 1;

	public void getWebLinks(String pageURL, String year, File outFile, String searchTag, String searchAttr,
			Integer connTryCount) {
		LOG.info("--getWebLinks() called--");
		try {
			int level = 1;
			String initialUrl = "";
			initialUrl = pageURL.substring(pageURL.indexOf("//"));
			int start = initialUrl.indexOf("//") + 2;
			initialUrl = initialUrl.substring(initialUrl.indexOf("//") + 2);
			int end = initialUrl.indexOf("/");
			LOG.debug("In getWebLinks() Intial URL to crawl is: " + initialUrl + " " + start + " " + end);
			if (end > 0) {
				baseURL = initialUrl.substring(0, end);
			} else {
				baseURL = initialUrl;
			}
			LOG.debug("--Connecting to the Page URL: " + pageURL + " via JSOUP --");
			Document doc = Jsoup.connect(pageURL).get();
			Elements links = doc.select(searchTag);
			level++;

			fetchHrefLinks(links, pageURL, level, year, outFile, searchAttr, searchTag);
			LOG.info("Fetching Hrefs Finished");
			level--;
			linkLevels.put(pageURL, new Integer(level));
			arrLink.clear();
		} catch (UnknownHostException ue) {
			if (connFailCount < connTryCount) {
				connFailCount++;
				getWebLinks(pageURL, year, outFile, searchTag, searchAttr, connTryCount);
			} else {
				LOG.error("Please check with your Internet Connection..We tried for " + connTryCount + " times");
				ue.printStackTrace();
			}
		} catch (Exception e) {
			LOG.error("Excpetion Occurred is: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void fetchHrefLinks(Elements links, String pageURL, int level, String year, File outFile, String searchAttr,
			String searchTag) {
		for (Element link : links) {
			String lnk = link.attr(searchAttr);
			LOG.debug("Lnk for Fetching is:: " + lnk);
			if (lnk.contains(year)) {
				LOG.debug("--Selected href link is: " + lnk);
				String lnkChk = "";
				if (lnk.indexOf("#") > 0) {
					lnkChk = lnk.substring(0, lnk.indexOf("#"));
				} else {
					lnkChk = lnk;
				}
				if (!lnk.contains("http://")) {
					// lnkChk = "";
					lnkChk = pageURL + lnk;
				}
				arrLink.add(lnkChk);
				LOG.debug("--Passing linkCheck is: " + lnkChk + " and Level is: " + level);
				if (linkLevels.get(lnkChk) != null) {
					int cLevel = Integer.parseInt(linkLevels.get(new String(lnkChk)).toString());
					if (level < cLevel) {
						linkLevels.put(lnkChk, new Integer(level));
						parentChild.put(lnkChk, pageURL);
						LOG.info("--Calling getPageLinks() from IF of fetchHrefLinks--");
						getPageLinks(lnkChk, level, year, outFile, searchTag);
					}
				} else {
					linkLevels.put(lnkChk, new Integer(level));
					parentChild.put(lnkChk, pageURL);
					LOG.info("--Calling getPageLinks() from ELSE of fetchHrefLinks--");
					getPageLinks(lnkChk, level, year, outFile, searchTag);
				}
			}
		}
	}

	public void getPageLinks(String pageLink, int level, String year, File outFile, String searchTag) {
		LOG.info("--getPageLinks() Called--");
		try {
			if (pageLink.contains(year)) {
				level++;
				LOG.debug("--Connecting to the Page Link: " + pageLink
						+ " via JSOUP and the Path in which the Mails are downloading is: " + outFile.getPath()
						+ " --");
				Document doc = Jsoup.connect(pageLink).get();
				Elements links = doc.select(searchTag);
				for (Element link : links) {
					LOG.info("--In the Outer Loop of getPageLinks()--");
					String lnk = link.attr("abs:href");
					if (lnk.indexOf(baseURL) >= 0) {
						String lnkChk = "";
						if (lnk.indexOf("#") > 0) {
							lnkChk = lnk.substring(0, lnk.indexOf("#"));
						} else {
							lnkChk = lnk;
						}
						if (arrLink.contains(new String(lnkChk))) {
							if (linkLevels.get(lnkChk) != null) {
								int curLevel = Integer.parseInt(linkLevels.get(new String(lnkChk)).toString());
								if (level < curLevel) {
									linkLevels.put(lnkChk, new Integer(level));
									String extension = "";
									if (lnkChk.lastIndexOf('.') > 0) {
										extension = lnkChk.substring(lnkChk.lastIndexOf('.') + 1);
										if (extension.indexOf("#") > 0) {
											extension = extension.substring(0, extension.indexOf("#"));
										}
									}
									if (extension.equalsIgnoreCase("htm") || extension.equalsIgnoreCase("html")
											|| extension.equalsIgnoreCase("aspx")
											|| extension.equalsIgnoreCase("jsp")) {
										parentChild.put(lnkChk, pageLink);
										LOG.info("Recurrence of getPageLinks() occurred as Link has Extension");
										getPageLinks(lnkChk, level, year, outFile, searchTag);
									}
								}
							} else {
								parentChild.put(lnkChk, pageLink);
								linkLevels.put(lnkChk, new Integer(level));
							}
							continue;
						} else {
							String path = lnk.substring((baseURL.length() + (lnk.indexOf(baseURL))));
							String extension = "";
							if (path != null) {
								if (path.indexOf('.') > 0) {
									extension = path.substring(path.lastIndexOf('.') + 1);
									if (extension.indexOf("#") > 0) {
										extension = extension.substring(0, extension.indexOf("#"));
									}
								}
								if (path.lastIndexOf('/') >= 0 && path.indexOf('.') > 0) {
									path = path.substring(0, path.lastIndexOf('/'));
									path = baseURL + path;
								}

								arrLink.add(lnkChk);

								fetchMailContent(lnkChk, year, outFile);

								LOG.info("Returned!!!!");

								if (linkLevels.get(lnkChk) != null) {
									int cLevel = Integer.parseInt(linkLevels.get(new String(lnkChk)).toString());
									if (level < cLevel) {
										linkLevels.put(lnkChk, new Integer(level));
										parentChild.put(lnkChk, pageLink);
										LOG.info("Recurrence of getPageLinks() occurred");
										getPageLinks(lnkChk, level, year, outFile, searchTag);
									}
								} else {
									linkLevels.put(lnkChk, new Integer(level));
									parentChild.put(lnkChk, pageLink);
									LOG.info("Recurrence of getPageLinks() occurred");
									getPageLinks(lnkChk, level, year, outFile, searchTag);
								}
							}
						}
					}
				}
			}
		} catch (SocketTimeoutException ste) {
			LOG.error("Exception Occurred is: " + ste.getMessage());
			LOG.info("Recurrence of getPageLinks() occurred");
			getPageLinks(pageLink, level, year, outFile, searchTag);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Exception Occurred is: " + e.getMessage());
		}
	}

	private void fetchMailContent(String lnkChk, String year, File outFile) {

		String day = "";
		String date = "";
		String month = "";
		String time = "";
		String textYear = "";
		try {
			if (lnkChk.contains(year)) {
				LOG.info("Fetching Mail Content");
				Document mailContentDoc = Jsoup.connect(lnkChk).get();
				String mailContent = mailContentDoc.html();
				String mailTextContent = mailContentDoc.text();
				if (mailContent.contains("Received") && mailContent.contains("Delivered-To")
						&& mailContent.contains("Reply-To") && mailContent.contains("Subject")
						&& mailContent.contains("To") && mailContent.contains("From") && mailContent.contains("Date")) {

					Elements mailContentElements = mailContentDoc.getAllElements();
					for (Element mailContentElement : mailContentElements) {
						if (mailContentElement.text().contains("Date:")) {
							String dateIndexString = mailContentElement.text()
									.substring(mailContentElement.text().indexOf("Date: "));
							int dateIndex = dateIndexString.indexOf("Date: ") + 6;
							int timeIndex = dateIndexString.indexOf("Date: ") + 31;
							LOG.debug("Date and Time Indexes are: " + dateIndex + " == " + timeIndex
									+ " dateIndexString is: " + dateIndexString);
							String dateString = dateIndexString.substring(dateIndex, timeIndex);
							String dateArray[] = dateString.split(" ");
							day = dateArray[0].split(",")[0];
							date = dateArray[1];
							month = dateArray[2];
							textYear = dateArray[3];
							time = dateArray[4];
							break;
						}
					}
					if (Integer.parseInt(textYear) == Integer.parseInt(year)) {
						File outputDirectory = new File(
								outFile.getPath() + "/" + textYear + "/" + month + "/" + date + "/" + day);
						if (!outputDirectory.exists())
							outputDirectory.mkdirs();
						File fileName = new File(outputDirectory.getPath() + "/" + time);
						FileOutputStream fos = new FileOutputStream(fileName);
						fos.write(mailTextContent.getBytes());
						fos.close();
						LOG.info("FOS is closed after writing the file");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage()
					.contains("Unhandled content type. Must be text/*, application/xml, or application/xhtml+xml")) {
				LOG.error("Exception Occurred and Returning Back Again");
				return;
			}
			LOG.error("Exception Occurred is: " + e.getMessage());
		}
	}
}