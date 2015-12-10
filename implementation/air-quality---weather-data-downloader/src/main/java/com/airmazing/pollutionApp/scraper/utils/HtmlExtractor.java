package com.airmazing.pollutionApp.scraper.utils;

import com.airmazing.pollutionApp.scraper.Main;
import com.airmazing.pollutionApp.scraper.objects.Entry;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by andrei on 27/10/15.
 */



public class HtmlExtractor {
    private static Map<String, String> sessionCookies = new HashMap<String, String>();

    private final static boolean cacheWebpages = true;
    private final static String CHARSET = "ISO-8859-1";
    private static void saveHtmlFile(StringBuilder htmlContent, Path localPath) throws IOException {
        Files.deleteIfExists(localPath);
        Files.createDirectories(localPath.getParent());
        Files.createFile(localPath);
        FileWriter fileWriter = new FileWriter(localPath.toString());
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(htmlContent.toString());
        bufferedWriter.close();
        if (Main.LOG_DOWNLOADS) {
            System.out.println("File downloaded succesfully!");
        }
    }

    private static StringBuilder fetchHtmlContent(String url) throws IOException {
        StringBuilder htmlContent = new StringBuilder();
        URLConnection conn = new URL(url).openConnection();
        setConnectionCookies(conn);
        Document document = Jsoup.parse(conn.getInputStream(), CHARSET, url);

        for (Element link : document.select("a")) {
            String urlText = link.attr("abs:href");
            link.attr("href", urlText);
        }
        for (Element link : document.select("img")) {
            String urlText = link.attr("abs:src");
            link.attr("src", urlText);
        }

        htmlContent.append(document.body().html());
        return htmlContent;
    }

    private static StringBuilder fetchHtmlContent(String url, String cssQuery) throws IOException {
        StringBuilder htmlContent = new StringBuilder();

        URLConnection conn = new URL(url).openConnection();
        setConnectionCookies(conn);

        Document document = Jsoup.parse(conn.getInputStream(), CHARSET, url);

        Elements content = document.select(cssQuery);

        for (Element link : content.select("a")) {
            String urlText = link.attr("abs:href");
            link.attr("href", urlText);
        }
        for (Element link : content.select("img")) {
            String urlText = link.attr("abs:src");
            link.attr("src", urlText);
        }
        for (Element element : content) {
            htmlContent.append(element.outerHtml());
        }

        htmlContent.append("\n<p id=\"page_link\">" + url + "</p>");
        return htmlContent;
    }

    private static StringBuilder fetchHtmlContent(String url, Entry postParam) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        setConnectionCookies(conn);

        conn.setRequestMethod("POST");

        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + CHARSET);

        conn.setDoInput(true);
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, CHARSET));

        List<String> vals = new ArrayList<String>();
        for(String key : postParam.getAttributes()) {
            vals.add(URLEncoder.encode(key, CHARSET) + "=" + URLEncoder.encode(postParam.getAttribute(key), CHARSET));
        }
        //writer.write(StringUtils.join(vals, "&"));
        writer.flush();
        writer.close();
        os.close();

        Document doc = Jsoup.parse(conn.getInputStream(), CHARSET, url);

        StringBuilder htmlContent = new StringBuilder();

        for (Element link : doc.select("a")) {
            String urlText = link.attr("abs:href");
            link.attr("href", urlText);
        }
        for (Element link : doc.select("img")) {
            String urlText = link.attr("abs:src");
            link.attr("src", urlText);
        }

        htmlContent.append(doc.body().html());
        return htmlContent;
    }

    private static StringBuilder fetchHtmlContent(String url, Entry postParam, Entry cookies, String cssQuery) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        setConnectionCookies(conn);

        conn.setRequestMethod("POST");

        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + CHARSET);

        conn.setDoInput(true);
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, CHARSET));

        List<String> vals = new ArrayList<String>();
        for(String key : postParam.getAttributes()) {
            vals.add(URLEncoder.encode(key, CHARSET) + "=" + URLEncoder.encode(postParam.getAttribute(key), CHARSET));
        }
       // writer.write(StringUtils.join(vals, "&"));
        writer.flush();
        writer.close();
        os.close();

        Document doc = Jsoup.parse(conn.getInputStream(), CHARSET, url);

        StringBuilder htmlContent = new StringBuilder();

        Elements content = doc.select(cssQuery);

        for (Element link : content.select("a")) {
            String urlText = link.attr("abs:href");
            link.attr("href", urlText);
        }
        for (Element link : content.select("img")) {
            String urlText = link.attr("abs:src");
            link.attr("src", urlText);
        }
        for (Element element : content) {
            htmlContent.append(element.outerHtml());
        }
        return htmlContent;
    }

    public static boolean downloadFile(String url, Path localPath) {
        if (Main.LOG_DOWNLOADS) {
            System.out.println("\nDownloading file: " + url);
        }

        try {
            StringBuilder htmlContent = HtmlExtractor.fetchHtmlContent(url);
            HtmlExtractor.saveHtmlFile(htmlContent, localPath);
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            if (Main.LOG_DOWNLOADS) {
                System.out.println("Could not download file!");
            }
            return false;
        }
    }

    public static boolean downloadFile(String url, Path localPath, String cssQuery) {
        if (Main.LOG_DOWNLOADS) {
            System.out.println("\nDownloading file: " + url);
        }

        try {
            StringBuilder htmlContent = HtmlExtractor.fetchHtmlContent(url, cssQuery);
            HtmlExtractor.saveHtmlFile(htmlContent, localPath);
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            if (Main.LOG_DOWNLOADS) {
                System.out.println("Could not download file! " + e.getMessage());
            }
            return false;
        }
    }

    public static boolean downloadFile(String url, Path localPath, Entry postParam) {
        if (Main.LOG_DOWNLOADS) {
            System.out.println("\nDownloading file with post: " + url);
        }

        try {
            StringBuilder htmlContent = HtmlExtractor.fetchHtmlContent(url, postParam);
            HtmlExtractor.saveHtmlFile(htmlContent, localPath);
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            if (Main.LOG_DOWNLOADS) {
                System.out.println("Could not download file!");
            }
            return false;
        }
    }

    public static boolean downloadFile(String url, Path localPath, Entry postParam, Entry cookies, String cssQuery) {
        if (Main.LOG_DOWNLOADS) {
            System.out.println("\nDownloading file with post: " + url);
        }

        try {
            StringBuilder htmlContent = HtmlExtractor.fetchHtmlContent(url, postParam, cookies, cssQuery);
            HtmlExtractor.saveHtmlFile(htmlContent, localPath);
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            if (Main.LOG_DOWNLOADS) {
                System.out.println("Could not download file!");
            }
            return false;
        }
    }

    public static Document getDocument(String url, File file) {

        if(!file.exists() || file.isDirectory()) {
            if (Main.LOG_DOWNLOADS) {
                System.out.println("Error oppening file!");
            }
            return null;
        }
        if (Main.LOG_DOWNLOADS) {
            System.out.println("Parsing from file...");
        }

        try {
            Document doc = Jsoup.parse(file, "UTF-8", url);
            if (Main.LOG_DOWNLOADS) {
                System.out.println("Done!");
            }
            return doc;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Document getDocument(String url, String fileLocation, String cssQuery) {

        try {
            fileLocation += URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return null;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }

        Path localPath = Paths.get(fileLocation);

        if (Files.exists(localPath)) {
            if (Main.LOG_DOWNLOADS) {
                System.out.println("\nFound file: " + fileLocation);
            }
        } else {
            if (!HtmlExtractor.downloadFile(url, localPath, cssQuery)) {
                return null;
            }
        }

        Document doc = getDocument(url, new File(fileLocation));

        return doc;

    }

    public static Document getDocument(String url, String filePath, Entry postParam) {

        Path localPath = Paths.get(filePath);

        if (Files.exists(localPath)) {
            if (Main.LOG_DOWNLOADS) {
                System.out.println("\nFound file: " + filePath);
            }
        } else {
            if (!HtmlExtractor.downloadFile(url, localPath, postParam)) {
                return null;
            }
        }

        Document doc = getDocument(url, new File(filePath));

        return doc;
    }

    public static Document getDocument(String url, String filePath, Entry postParam, Entry cookies, String cssQuery) {

        Path localPath = Paths.get(filePath);

        if (Files.exists(localPath)) {
            if (Main.LOG_DOWNLOADS) {
                System.out.println("\nFound file: " + filePath);
            }
        } else {
            if (!HtmlExtractor.downloadFile(url, localPath, postParam, cookies, cssQuery)) {
                return null;
            }
        }

        Document doc = getDocument(url, new File(filePath));

        return doc;
    }

    public static Document getPage(String url, String cssQuery) {
        //if filepath null
        //store retrive as cache
        //else as other

        String filename = null;
        Document page = null;

        // Encode the URL to be used as a filename for the local version of the web page
        try {
            filename = "cached_pages/" + URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // If a file with that filename exists, use the local version
        File file = new File(filename);
        if (cacheWebpages && file.exists() && !file.isDirectory()) {
            try {
                if (Main.LOG_DOWNLOADS) {
                    System.out.println("LOADING CACHED WEBPAGE: " + url);
                }
                page = Jsoup.parse(file, "UTF-8", "");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            return page;
        }

        try {
            if (Main.LOG_DOWNLOADS) {
                System.out.println("DOWNLOADING WEBPAGE: " + url);
            }
            page = Jsoup.connect(url)
                    .timeout(70000)
                    .maxBodySize(1024*1024*10) // 10 MB size limit to accommodate big web pages
                    .get();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if (cacheWebpages) {
            saveToFile(filename, page.toString());
        }

        return page;
    }

    public static List<Document> getDocs (String myDirectoryPath) {
        File dir = new File(myDirectoryPath);
        File[] directoryListing = dir.listFiles();

        List<Document> documents = new ArrayList<Document>();

        if (directoryListing != null) {
            for (File file : directoryListing) {

                String filename = file.getName();

                String url = null;
                try {
                    url = URLDecoder.decode(filename, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (url != null) {
                    if (Main.LOG_DOWNLOADS) {
                        System.out.println(url);
                    }
                    Document doc = HtmlExtractor.getDocument(url, file);
                    if (doc != null) {
                        documents.add(doc);
                    }
                }
            }
        } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
        }
        if (documents.isEmpty()) {
            return null;
        }
        return documents;
    }

    // Save a string to a text file
    public static void saveToFile(String filename, String contents) {
        File file = new File(filename);
        file.getAbsoluteFile().getParentFile().mkdirs();

        PrintWriter out = null;
        try {
            out = new PrintWriter(file);
            out.println(contents);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }

    public static String getUrlCookies(String url) {
        try {
            Connection.Response resp = Jsoup.connect(url)
                    .method(Connection.Method.GET)
                    .execute();
            getConnectionCookies(resp);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void setConnectionCookies(Connection connection) {
        for (Map.Entry<String, String> cookie : sessionCookies.entrySet()) {
            connection.cookie(cookie.getKey(), cookie.getValue());
        }
    }
    private static void setConnectionCookies(URLConnection connection) {

        List<String> vals = new ArrayList<String>();

        for (Map.Entry<String, String> cookie : sessionCookies.entrySet()) {
            String cookieVal = cookie.getKey() + "=" + cookie.getValue();
            vals.add(cookieVal);
        }
        //connection.setRequestProperty("Cookie", StringUtils.join(vals, "; "));
    }
    private static void getConnectionCookies(Connection.Response response){
        sessionCookies.putAll(response.cookies());
    }

}
