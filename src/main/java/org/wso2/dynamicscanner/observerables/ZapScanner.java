package org.wso2.dynamicscanner.observerables;
/*
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
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.wso2.dynamicscanner.clients.ZapClient;
import org.wso2.dynamicscanner.handlers.HttpRequestHandler;
import org.wso2.dynamicscanner.handlers.HttpsRequestHandler;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class ZapScanner extends Observable implements Runnable {

    private String urlListPath;
    private final String HTTP_SCHEME = "http";
    private final String HTTPS_SCHEME = "https";
    private final String POST = "POST";

    private String sessionName;
    private String productHostRelativeToZap;
    private String productHostRelativeToThis;
    private int productPort;

    private Map<String, Object> loginCredentials;
    private ZapClient zapClient;
    private URI productUri;
    private boolean isAuthenticatedScan;

    private String keyUsername = "username";
    private String valueUserName = "admin";
    private String keyPassword = "password";
    private String valuePassword = "admin";
    private String loginUrl="/carbon/admin/login_action.jsp";
    private String logoutUrl="/carbon/admin/logout_action.jsp";
    private String reportFilePath="/home/NewReport.html";

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run() {
        try {
            startScan();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
    }

    public ZapScanner(String zapHost, int zapPort, String sessionName, String productHostRelativeToZap, String productHostRelativeToThis, int productPort,
                      String urlListPath, boolean isAuthenticatedScan) throws URISyntaxException {

        this.sessionName = sessionName;
        this.productHostRelativeToZap = productHostRelativeToZap;
        this.productHostRelativeToThis = productHostRelativeToThis;
        this.productPort = productPort;

        this.urlListPath = urlListPath;
        this.zapClient = new ZapClient(zapHost, zapPort, HTTP_SCHEME);
        this.isAuthenticatedScan = isAuthenticatedScan;

        productUri = (new URIBuilder()).setHost(productHostRelativeToZap).setPort(productPort).setScheme(HTTPS_SCHEME).build();
        loginCredentials = new HashMap<>();
        loginCredentials.put(keyUsername, valueUserName);
        loginCredentials.put(keyPassword, valuePassword);
    }

    private void startScan() throws Exception {
        LOGGER.info("Starting ZAP scanning process ");

        //Create an empty session
        HttpResponse createEmptySessionResponse = zapClient.createEmptySession(productUri.toString(), sessionName, false);
        LOGGER.info("Creating empty session " + HttpRequestHandler.printResponse(createEmptySessionResponse));

        if (isAuthenticatedScan) {
            //login to wso2 server
            Map<String, String> props = new HashMap<>();
            props.put("Content-Type", "text/plain");
            System.out.println(keyPassword);
            System.out.println(keyUsername);
            System.out.println(valuePassword);
            System.out.println(valueUserName);
            System.out.println(loginUrl);
            System.out.println(logoutUrl);

            URI loginUri = (new URIBuilder()).setHost(productHostRelativeToThis).setPort(productPort).setScheme("https").setPath(loginUrl).build();
            LOGGER.info("URI to login to wso2server: " + loginUri.toString());
            HttpsURLConnection httpsURLConnection = HttpsRequestHandler.sendRequest(loginUri.toString(), props, loginCredentials, POST);
            List<String> setCookieResponseList = HttpsRequestHandler.getResponseValue("Set-Cookie", httpsURLConnection);

            assert setCookieResponseList != null;
            String setCookieResponse = setCookieResponseList.get(0);
            String jsessionId = setCookieResponse.substring(setCookieResponse.indexOf("=") + 1, setCookieResponse.indexOf(";"));

            HttpResponse setSessionTokenResponse = zapClient.setSessionTokenValue(productUri.toString(), sessionName, "JSESSIONID", jsessionId, false);
            LOGGER.info("Setting JSESSIONID to the newly created session: " + HttpRequestHandler.printResponse(setSessionTokenResponse));

            //Exclude logout url from spider
            URI logoutUri = (new URIBuilder()).setHost(productHostRelativeToThis).setPort(productPort).setScheme("https").setPath(logoutUrl)
                    .build();
            LOGGER.info("Logout URI: " + logoutUri.toString());
            HttpsURLConnection httpsURLConnectionLogout = HttpsRequestHandler.sendRequest(logoutUri.toString(), props, null, POST);
            LOGGER.info("Response of sending logout request to server: " + HttpsRequestHandler.printResponse(httpsURLConnectionLogout));

            HttpResponse excludeFromSpiderResponse = zapClient.excludeFromSpider(logoutUri.toString(), false);
            LOGGER.info("Exclude logout from spider response: " + HttpRequestHandler.printResponse(excludeFromSpiderResponse));

            //Remove previously created session
            HttpResponse removeSessionResponse = zapClient.removeSession(productUri.toString(), sessionName, false);
            LOGGER.info("Remove Session Response: " + HttpRequestHandler.printResponse(removeSessionResponse));

            //Create an empty session
            createEmptySessionResponse = zapClient.createEmptySession(productUri.toString(), sessionName, false);
            LOGGER.info("Creating empty session: " + HttpRequestHandler.printResponse(createEmptySessionResponse));

            httpsURLConnection = HttpsRequestHandler.sendRequest(loginUri.toString(), props, loginCredentials, POST);
            setCookieResponseList = HttpsRequestHandler.getResponseValue("Set-Cookie", httpsURLConnection);

            assert setCookieResponseList != null;
            setCookieResponse = setCookieResponseList.get(0);
            jsessionId = setCookieResponse.substring(setCookieResponse.indexOf("=") + 1, setCookieResponse.indexOf(";"));

            setSessionTokenResponse = zapClient.setSessionTokenValue(productUri.toString(), sessionName, "JSESSIONID", jsessionId, false);
            LOGGER.info("Setting JSESSIONID to the newly created session: " + HttpRequestHandler.printResponse(setSessionTokenResponse));
        }
        runSpider();
        runAjaxSpider();
        runActiveScan();

        HttpResponse generatedHtmlReport = zapClient.generateHtmlReport(false);
        HttpRequestHandler.saveResponseToFile(generatedHtmlReport, new File(reportFilePath));

    }

    private void runSpider() throws IOException, InterruptedException, URISyntaxException {
        BufferedReader bufferedReader;
        ArrayList<String> spiderScanIds = new ArrayList<>();

        try {
            bufferedReader = new BufferedReader(new FileReader(urlListPath));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                LOGGER.info("Reading URL list of wso2 product: " + line);
                try {
                    HttpResponse spiderResponse = zapClient.spider(productUri.toString() + line, "", "", "", "", false);
                    LOGGER.info("Spider HTTP Response");

                    String scanId = extractJsonValue(spiderResponse, "scan");
                    spiderScanIds.add(scanId);
                    LOGGER.info("Adding ScanIds of Spider Scans to array: " + scanId);

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    LOGGER.info(e.getReason(), e);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.info(e.toString(), e);
        }
        for (String scanId : spiderScanIds) {
            HttpResponse spiderStatusResponse = zapClient.spiderStatus(scanId, false);
            LOGGER.info("Sending request to check spider status");

            while (Integer.parseInt(extractJsonValue(spiderStatusResponse, "status")) < 100) {
                spiderStatusResponse = zapClient.spiderStatus(scanId, false);
                LOGGER.info("Sending request to check spider status: " + spiderStatusResponse);
                Thread.sleep(1000);
            }
        }
    }

    private void runAjaxSpider() throws IOException, InterruptedException, URISyntaxException {
        URI productUri = (new URIBuilder()).setHost(productHostRelativeToZap).setPort(productPort).setScheme(HTTPS_SCHEME).build();
        try {
            HttpResponse ajaxSpiderResponse = zapClient.ajaxSpider(productUri.toString(), "", "", "", false);
            LOGGER.info("Starting Ajax spider: " + ajaxSpiderResponse);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            LOGGER.error(e.toString(), e);
        }

        HttpResponse ajaxSpiderStatusResponse = zapClient.ajaxSpiderStatus(false);
        LOGGER.info("Ajax spider status: " + ajaxSpiderStatusResponse);

        while (!extractJsonValue(ajaxSpiderStatusResponse, "status").equals("stopped")) {
            ajaxSpiderStatusResponse = zapClient.ajaxSpiderStatus(false);
            LOGGER.info("Ajax spider status: " + ajaxSpiderStatusResponse);
            Thread.sleep(3000);
        }
    }

    private void runActiveScan() throws IOException, InterruptedException, URISyntaxException {
        BufferedReader bufferedReader;
        ArrayList<String> activeScanIds = new ArrayList<>();

        try {
            bufferedReader = new BufferedReader(new FileReader(urlListPath));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                LOGGER.info("Reading URL list of wso2 product: " + line);
                try {
                    HttpResponse activeScanResponse = zapClient.activeScan(productUri.toString() + line, "", "", "", "", "", "", false);
                    String scanId = extractJsonValue(activeScanResponse, "scan");
                    activeScanIds.add(scanId);
                    LOGGER.info("Adding ScanIds of Active Scans to array: " + scanId);
                    Thread.sleep(500);

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    LOGGER.info(e.toString(), e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.info(e.toString(), e);
        }

        for (String scanId : activeScanIds) {
            HttpResponse activeScanStatusResponse = zapClient.activeScanStatus(scanId, false);

            while (Integer.parseInt(extractJsonValue(activeScanStatusResponse, "status")) < 100) {
                activeScanStatusResponse = zapClient.activeScanStatus(scanId, false);
                Thread.sleep(1000);
            }
        }
    }

    private String extractJsonValue(HttpResponse httpResponse, String key) throws IOException {
        String jsonString = HttpRequestHandler.printResponse(httpResponse);
        JSONObject jsonObject = new JSONObject(jsonString);
        return jsonObject.getString(key);
    }

}
