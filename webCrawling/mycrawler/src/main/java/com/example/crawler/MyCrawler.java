package com.example.crawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

public class MyCrawler extends WebCrawler {

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|json|xml|gif|jpg|png|mp3|mp4|zip|gz))$");
    private final static String DOMAIN = "https://www.nytimes.com";
    private final static String HARD_STOP = "HARD_STOP"; // Special marker to stop the threads

    // Thread-safe queues for each writer
    private static final BlockingQueue<String> fetchQueue = new LinkedBlockingQueue<>();
    private static final BlockingQueue<String> visitQueue = new LinkedBlockingQueue<>();
    private static final BlockingQueue<String> urlsQueue = new LinkedBlockingQueue<>();

    public MyCrawler() throws IOException {
        writeTitle("fetch_USAToday.csv", "URL, Status");
        writeTitle("visit_USAToday.csv", "URL, Size (Bytes), Outlinks, Content-Type");
        writeTitle("urls_USAToday.csv", "URL, Indicator");
        // Start writer threads for each output
        new Thread(new WriterTask("fetch_USAToday.csv", fetchQueue)).start();
        new Thread(new WriterTask("visit_USAToday.csv", visitQueue)).start();
        new Thread(new WriterTask("urls_USAToday.csv", urlsQueue)).start();
    }

    private void writeTitle(String filePath, String title) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))) {
            writer.write(title + "\n");
            writer.flush();
        }
    }

    // A writer task that processes the queue and writes to the corresponding file
    private static class WriterTask implements Runnable {
        private final String filePath;
        private final BlockingQueue<String> queue;
        private BufferedWriter writer;

        public WriterTask(String filePath, BlockingQueue<String> queue) throws IOException {
            this.filePath = filePath;
            this.queue = queue;
            this.writer = new BufferedWriter(new FileWriter(filePath, true));
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String record = queue.take();  // Retrieve data from the queue
                    if (record.equals(HARD_STOP)) {
                        break;
                    }
                    synchronized (writer) {
                        writer.write(record + "\n");
                        writer.flush();  // Ensure immediate write to the file
                    }
                }
                writer.close();  // Close writer after finishing
            } catch (InterruptedException | IOException e) {
                System.out.println("Error");
            }
        }
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL();
        boolean isInDomain = href.startsWith(DOMAIN);

        try {
            urlsQueue.put(href + ", " + (isInDomain ? "OK" : "N_OK"));  // Add URL data to the queue
        } catch (InterruptedException e) {
            System.out.println("Error");
        }

        // Allow visiting URLs with specific content types (HTML, PDF, images)
        return isInDomain && !FILTERS.matcher(href).matches();
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        int statusCode = page.getStatusCode();
        String contentType = page.getContentType().split(";")[0].toLowerCase();
        StringBuilder visitRecord = new StringBuilder();
        visitRecord.append(url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            long fileSize = page.getContentData().length;

            visitRecord.append(", ").append(fileSize)
                    .append(", ").append(links.size())
                    .append(", ").append(contentType);
        } else if (isAllowedFile(page)) {
            long fileSize = page.getContentData().length;
            visitRecord.append(", ").append(fileSize)
                    .append(", 0, ").append(contentType);
        }

        // Add visit data to the queue
        try {
            visitQueue.put(visitRecord.toString());
        } catch (InterruptedException e) {
            System.out.println("Error");
        }

        // Add fetch data to the queue
        try {
            fetchQueue.put(url + ", " + statusCode);
        } catch (InterruptedException e) {
            System.out.println("Error");
        }
    }

    // Method to check if a file type without extension is allowed
    private boolean isAllowedFile(Page page) {
        String contentType = page.getContentType();
        return contentType.contains("text/html") ||
                contentType.contains("application/pdf") ||
                contentType.contains("application/msword") ||
                contentType.contains("image/");
    }

    @Override
    public void onBeforeExit() {
        try {
            fetchQueue.put(HARD_STOP);
            visitQueue.put(HARD_STOP);
            urlsQueue.put(HARD_STOP);
        } catch (InterruptedException e) {
            System.out.println("Error");
        }
    }
}
