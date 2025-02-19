package com.example.crawler;

import java.io.*;
import java.util.*;

public class CrawlReport {

    private static final String DOMAIN = "https://www.usatoday.com";

    public static void main(String[] args) {
        try {
            File fetchFile = new File("fetch_USAToday.csv");
            File visitFile = new File("visit_USAToday.csv");
            File urlsFile = new File("urls_USAToday.csv");

            // Fetch statistics
            int fetchAttempted = 0;
            int fetchSucceeded = 0;
            int fetchFailed = 0;
            int status200 = 0;
            int status301 = 0;
            int status401 = 0;
            int status403 = 0;
            int status404 = 0;

            // Outgoing URLs
            int totalExtractedUrls = 0;
            Set<String> uniqueUrls = new HashSet<>();
            Set<String> uniqueUrlsWithinDomain = new HashSet<>();
            Set<String> uniqueUrlsOutsideDomain = new HashSet<>();

            // File sizes
            int sizeLessThan1KB = 0;
            int size1KBto10KB = 0;
            int size10KBto100KB = 0;
            int size100KBto1MB = 0;
            int sizeGreaterThan1MB = 0;

            // Content types
            int textHtmlCount = 0;
            int imageGifCount = 0;
            int imageJpegCount = 0;
            int imagePngCount = 0;
            int applicationPdfCount = 0;

            // Process fetch_USAToday.csv for fetch stats and status codes
            try (BufferedReader fetchReader = new BufferedReader(new FileReader(fetchFile))) {
                String line;
                fetchReader.readLine(); // Skip header
                while ((line = fetchReader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue; // Skip empty lines
                    fetchAttempted++;
                    String[] fields = line.split(",");
                    if (fields.length < 2) continue; // Skip malformed lines

                    int statusCode = Integer.parseInt(fields[1].trim());
                    if (statusCode == 200) status200++;
                    else if (statusCode == 301) status301++;
                    else if (statusCode == 401) status401++;
                    else if (statusCode == 403) status403++;
                    else if (statusCode == 404) status404++;

                    if (statusCode >= 200 && statusCode < 300) {
                        fetchSucceeded++;
                    } else {
                        fetchFailed++;
                    }
                }
            }

            // Process visit_USAToday.csv for file sizes, content types, and outgoing URLs
            try (BufferedReader visitReader = new BufferedReader(new FileReader(visitFile))) {
                String line;
                visitReader.readLine(); // Skip header
                while ((line = visitReader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue; // Skip empty lines
                    String[] fields = line.split(",");
                    if (fields.length < 4) continue; // Skip malformed lines

                    String url = fields[0].trim();
                    long size = Long.parseLong(fields[1].trim());
                    String contentType = fields[3].trim();

                    totalExtractedUrls++;
                    uniqueUrls.add(url);

                    // Categorize URLs within or outside the domain
                    if (url.startsWith(DOMAIN)) {
                        uniqueUrlsWithinDomain.add(url);
                    } else {
                        uniqueUrlsOutsideDomain.add(url);
                    }

                    // File sizes categorization
                    if (size < 1024) {
                        sizeLessThan1KB++;
                    } else if (size < 10240) {
                        size1KBto10KB++;
                    } else if (size < 102400) {
                        size10KBto100KB++;
                    } else if (size < 1024 * 1024) {
                        size100KBto1MB++;
                    } else {
                        sizeGreaterThan1MB++;
                    }

                    // Content type categorization
                    if (contentType.equals("text/html")) {
                        textHtmlCount++;
                    } else if (contentType.equals("image/gif")) {
                        imageGifCount++;
                    } else if (contentType.equals("image/jpeg")) {
                        imageJpegCount++;
                    } else if (contentType.equals("image/png")) {
                        imagePngCount++;
                    } else if (contentType.equals("application/pdf")) {
                        applicationPdfCount++;
                    }
                }
            }

            // Process urls_USAToday.csv for unique URLs
            try (BufferedReader urlsReader = new BufferedReader(new FileReader(urlsFile))) {
                String line;
                urlsReader.readLine(); // Skip header
                while ((line = urlsReader.readLine()) != null) {if (line.trim().isEmpty()) continue; // Skip empty lines
                    String[] fields = line.split(",");
                    if (fields.length < 1) continue; // Skip malformed lines

                    String url = fields[0].trim();

                    uniqueUrls.add(url);

                    if (url.startsWith(DOMAIN)) {
                        uniqueUrlsWithinDomain.add(url);
                    } else {
                        uniqueUrlsOutsideDomain.add(url);
                    }
                }
            }

            // Generate report
            generateReport(fetchAttempted, fetchSucceeded, fetchFailed, status200, status301, status401, status403, status404,
                    totalExtractedUrls, uniqueUrls.size(), uniqueUrlsWithinDomain.size(), uniqueUrlsOutsideDomain.size(),
                    sizeLessThan1KB, size1KBto10KB, size10KBto100KB, size100KBto1MB, sizeGreaterThan1MB,
                    textHtmlCount, imageGifCount, imageJpegCount, imagePngCount, applicationPdfCount);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateReport(int fetchAttempted, int fetchSucceeded, int fetchFailed,
                                       int status200, int status301, int status401, int status403, int status404,
                                       int totalExtractedUrls, int uniqueUrls, int uniqueWithinDomain,
                                       int uniqueOutsideDomain, int sizeLessThan1KB, int size1KBto10KB, int size10KBto100KB,
                                       int size100KBto1MB, int sizeGreaterThan1MB, int textHtmlCount, int imageGifCount,
                                       int imageJpegCount, int imagePngCount, int applicationPdfCount) {

        try (BufferedWriter reportWriter = new BufferedWriter(new FileWriter("CrawlReport_USAToday.txt"))) {

            // Header with name, ID, news site, and threads
            reportWriter.write("Name: Caroline Silva\n");
            reportWriter.write("USC ID: 3950043476\n");
            reportWriter.write("News site crawled: usatoday.com\n");
            reportWriter.write("Number of threads: 7\n\n");

            // Fetch Statistics
            reportWriter.write("Fetch Statistics\n");
            reportWriter.write("================\n");
            reportWriter.write("Fetches attempted: " + fetchAttempted + "\n");
            reportWriter.write("Fetches succeeded: " + fetchSucceeded + "\n");
            reportWriter.write("Fetches failed or aborted: " + fetchFailed + "\n\n");

            // Outgoing URLs
            reportWriter.write("Outgoing URLs:\n");
            reportWriter.write("==============\n");
            reportWriter.write("Total URLs extracted: " + totalExtractedUrls + "\n");
            reportWriter.write("Unique URLs extracted: " + uniqueUrls + "\n");
            reportWriter.write("Unique URLs within News Site: " + uniqueWithinDomain + "\n");
            reportWriter.write("Unique URLs outside News Site: " + uniqueOutsideDomain + "\n\n");

            // Status Codes
            reportWriter.write("Status Codes:\n");
            reportWriter.write("=============\n");
            reportWriter.write("200 OK: " + status200 + "\n");
            reportWriter.write("301 Moved Permanently: " + status301 + "\n");
            reportWriter.write("401 Unauthorized: " + status401 + "\n");
            reportWriter.write("403 Forbidden: " + status403 + "\n");
            reportWriter.write("404 Not Found: " + status404 + "\n\n");

            // File Sizes
            reportWriter.write("File Sizes:\n");
            reportWriter.write("===========\n");
            reportWriter.write("< 1KB: " + sizeLessThan1KB + "\n");
            reportWriter.write("1KB ~ <10KB: " + size1KBto10KB + "\n");
            reportWriter.write("10KB ~ <100KB: " + size10KBto100KB + "\n");
            reportWriter.write("100KB ~ <1MB: " + size100KBto1MB + "\n");
            reportWriter.write(">= 1MB: " + sizeGreaterThan1MB + "\n\n");

            // Content Types
            reportWriter.write("Content Types:\n");
            reportWriter.write("==============\n");
            reportWriter.write("text/html: " + textHtmlCount + "\n");
            reportWriter.write("image/gif: " + imageGifCount + "\n");
            reportWriter.write("image/jpeg: " + imageJpegCount + "\n");
            reportWriter.write("image/png: " + imagePngCount + "\n");
            reportWriter.write("application/pdf: " + applicationPdfCount + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
