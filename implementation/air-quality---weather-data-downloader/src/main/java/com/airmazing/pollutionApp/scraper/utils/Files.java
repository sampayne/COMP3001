package com.airmazing.pollutionApp.scraper.utils;

import com.airmazing.pollutionApp.scraper.Main;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by andrei on 02/11/15.
 */
public class Files {
    public static List<String> getFileNames (String myDirectoryPath) {
        File dir = new File(myDirectoryPath);
        File[] directoryListing = dir.listFiles();

        List<String> fileNames = new ArrayList<String>();

        if (directoryListing != null) {
            for (File file : directoryListing) {

                String filename = file.getName();
                fileNames.add(filename);
            }
        } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
        }
        if (fileNames.isEmpty()) {
            return null;
        }
        return fileNames;
    }

    public static JSONObject getJsonObject(File file) {
        FileReader fileReader;
        JSONObject object = null;
        try {
            fileReader = new FileReader(file);
            BufferedReader rd = new BufferedReader(fileReader);
            String line;

            while ((line = rd.readLine()) != null ) {
                object = new JSONObject(line);
                break;
            }
        } catch (IOException e) {
        }
        return object;
    }

    public static List<JSONObject> getJsonObjects(File file) {
        FileReader fileReader;
        List<JSONObject> objects = new ArrayList<JSONObject>();
        try {
            fileReader = new FileReader(file);
            BufferedReader rd = new BufferedReader(fileReader);
            String line;

            while ((line = rd.readLine()) != null ) {
                JSONObject o = new JSONObject(line);
                objects.add(o);

            }
        } catch (IOException e) {
        }
        return objects;
    }

    public static List<Object> getKeyValues(JSONObject list[], String key) {

        List<Object> keyValues = new ArrayList<Object>();
        for (int i = 0; i < list.length; i++) {
            keyValues.add(list[i].get(key));
        }

        return keyValues;
    }

    public static void downloadFile (String url, String localPath) throws IOException {
        downloadFile(url, localPath, false, 0);
    }

    public static void downloadFile (String url, String localPath, int milliseconds) throws IOException {
        downloadFile(url, localPath, false, milliseconds);
    }

    public static void downloadFile (String url, String localPath, Boolean replace, int milliseconds) throws IOException {

        if (java.nio.file.Files.exists(Paths.get(localPath)) && !replace) {
            if (Main.LOG_DOWNLOADS) {
                System.out.println("Found file: " + localPath);
            }
        } else {

            System.out.println("Downloading... " + url);
            FileUtils.copyURLToFile(
                    new URL(url),
                    new File(localPath)
            );
            if (milliseconds > 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(milliseconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
