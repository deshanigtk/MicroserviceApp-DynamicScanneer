package org.wso2.dynamicscanner.scanners;/*
*  Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.http.HttpResponse;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.dynamicscanner.clients.ZapClient;
import org.wso2.dynamicscanner.handlers.FileHandler;
import org.wso2.dynamicscanner.handlers.HttpRequestHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Observable;

public class ZapScanner extends Observable implements Runnable {

    private String wso2serverFileAbsolutePath;
    private String zapHost;
    private int zapPort;
    private String urlListPath;
    private String reportFilePath;

    private final String DEFAULT_SCHEME = "http";

    private String scheme = DEFAULT_SCHEME;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run() {
        try {
            System.out.println("start scan before");
            startScan(zapHost, zapPort, scheme, urlListPath, reportFilePath);
            System.out.println("start scan after");
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info(e.toString(), e);
        }
    }

    public ZapScanner(String zapHost, int zapPort, String scheme, String urlListPath, String reportFilePath) {
        this.zapHost = zapHost;
        this.zapPort = zapPort;
        this.scheme = scheme;
        this.urlListPath = urlListPath;
        this.reportFilePath = reportFilePath;
    }

    private void startScan(String zapHost, int zapPort, String scheme, String urlListPath, String reportFilePath) throws Exception {
        LOGGER.info("Starting the Scanning Process ");
        ZapClient zapClient = new ZapClient(zapHost, zapPort, scheme);

        runSpider(zapHost, zapPort, scheme, urlListPath);
        runAjaxSpider(zapHost, zapPort, scheme, urlListPath);
        runActiveScan(zapHost, zapPort, scheme, urlListPath);

        HttpResponse generatedHtmlReport = zapClient.generateHtmlReport(false);
        HttpRequestHandler.saveResponseToFile(generatedHtmlReport, new File(reportFilePath));

    }

    private void runSpider(String zapHost, int zapPort, String scheme, String urlFilePath) throws IOException, InterruptedException, URISyntaxException {
        System.out.println("spiderrr");
        ZapClient zapClient = new ZapClient(zapHost, zapPort, scheme);
        BufferedReader bufferedReader;
        ArrayList<String> spiderScanIds = new ArrayList<>();
        int i = 0;

        try {
            bufferedReader = new BufferedReader(new FileReader(urlFilePath));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                LOGGER.info("Reading URL list of wso2 product: ", line);
                try {
                    HttpResponse spiderResponse = zapClient.spider(line, "", "", "", "", false);
                    LOGGER.info("Spider HTTP Response", spiderResponse.getEntity().getContent());
                    System.out.println(spiderResponse.getEntity().getContent());
                    String scanId = extractJsonValue(spiderResponse, "scan");
                    spiderScanIds.add(scanId);
                    LOGGER.info("Adding ScanIds of Spider Scans to array: ", scanId);
                    i++;

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    LOGGER.info( e.getReason(), e);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.info( e.toString(), e);
        }
        for (String scanId : spiderScanIds) {
            HttpResponse spiderStatusResponse = zapClient.spiderStatus(scanId, false);
            LOGGER.info("Spider HTTP Response", spiderStatusResponse.getEntity().getContent());

            while (Integer.parseInt(extractJsonValue(spiderStatusResponse, "status")) < 100) {
                spiderStatusResponse = zapClient.spiderStatus(scanId, false);
                LOGGER.info("Spider HTTP Response", spiderStatusResponse.getEntity().getContent());
                Thread.sleep(1000);
            }
        }
    }

    private void runAjaxSpider(String zapHost, int zapPort, String scheme, String urlFilePath) throws IOException, InterruptedException, URISyntaxException {
        ZapClient zapClient = new ZapClient(zapHost, zapPort, scheme);
        BufferedReader bufferedReader;
        int i = 0;

        try {
            bufferedReader = new BufferedReader(new FileReader(urlFilePath));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                try {
                    HttpResponse ajaxSpiderResponse = zapClient.ajaxSpider(line, "", "", "", false);
                    LOGGER.info("AJAX Spider HTTP Response", ajaxSpiderResponse.getEntity().getContent());

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    LOGGER.info(e.toString(), e);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.info( e.toString(), e);
        }

        HttpResponse ajaxSpiderStatusResponse = zapClient.ajaxSpiderStatus(false);
        LOGGER.info("AJAX Spider HTTP Response", ajaxSpiderStatusResponse.getEntity().getContent());

        while (!extractJsonValue(ajaxSpiderStatusResponse, "status").equals("stopped")) {
            ajaxSpiderStatusResponse = zapClient.ajaxSpiderStatus(false);
            LOGGER.info( "AJAX Spider HTTP Response", ajaxSpiderStatusResponse.getEntity().getContent());
            Thread.sleep(1000);
        }
    }

    private void runActiveScan(String zapHost, int zapPort, String scheme, String urlFilePath) throws IOException, InterruptedException, URISyntaxException {
        ZapClient zapClient = new ZapClient(zapHost, zapPort, scheme);
        BufferedReader bufferedReader;
        ArrayList<String> activeScanIds = new ArrayList<>();
        int i = 0;

        try {
            bufferedReader = new BufferedReader(new FileReader(urlFilePath));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                LOGGER.info("Reading URL list of wso2 product: ", line);
                try {
                    HttpResponse activeScanResponse = zapClient.activeScan(line, "", "", "", "", "", "", false);
                    LOGGER.info("Active Scan Response", activeScanResponse.getEntity().getContent());
                    String scanId = extractJsonValue(activeScanResponse, "scan");
                    activeScanIds.add(scanId);
                    LOGGER.info("Adding ScanIds of Active Scans to array: ", scanId);
                    i++;

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    LOGGER.info( e.toString(), e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.info( e.toString(), e);
        }

        for (String scanId : activeScanIds) {
            HttpResponse activeScanStatusResponse = zapClient.activeScanStatus(scanId, false);
            LOGGER.info("Active Scan Status Response", activeScanStatusResponse.getEntity().getContent());

            while (Integer.parseInt(extractJsonValue(activeScanStatusResponse, "status")) < 100) {
                activeScanStatusResponse = zapClient.activeScanStatus(scanId, false);
                LOGGER.info( "Active Scan Status Response", activeScanStatusResponse.getEntity().getContent());
                Thread.sleep(1000);
            }
        }
    }

    String extractZipFileAndReturnServerFile(String fileName, String productPath, boolean replaceExisting) throws IOException {
        if (new File(productPath).exists() && replaceExisting) {
            FileUtils.deleteDirectory(new File(productPath));
        }
        FileHandler.extractFolder(productPath + File.separator + fileName);

        String folderName = fileName.substring(0, fileName.length() - 4);
        findFile(new File(productPath + File.separator + folderName), "wso2server.sh");
        return wso2serverFileAbsolutePath;
    }

    void findFile(File parentDirectory, String fileToFind) {
        File[] files = parentDirectory.listFiles();
        for (File file : files) {
            if (file.getName().equals(fileToFind)) {
                wso2serverFileAbsolutePath = file.getAbsolutePath();
                break;
            }
            if (file.isDirectory()) {
                findFile(file, fileToFind);
            }
        }
    }

    private String extractJsonValue(HttpResponse httpResponse, String key) throws IOException {
        String jsonString = HttpRequestHandler.printResponse(httpResponse);
        JSONObject jsonObject = new JSONObject(jsonString);
        return jsonObject.getString(key);
    }


}
