package org.wso2.security.dynamic.scanner.observable;
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
import org.apache.http.impl.execchain.RequestAbortedException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.dynamic.scanner.Constants;
import org.wso2.security.dynamic.scanner.NotificationManager;
import org.wso2.security.dynamic.scanner.clients.ZapClient;
import org.wso2.security.dynamic.scanner.handlers.HttpRequestHandler;
import org.wso2.security.dynamic.scanner.handlers.HttpsRequestHandler;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Main ZAP scanning methods
 *
 * @author Deshani Geethika
 */
public class ZapScanner extends Observable implements Runnable {

    private final String HTTP_SCHEME = "http";
    private final String HTTPS_SCHEME = "https";
    private final String POST = "POST";

    private String contextName;
    private String contextId;
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
    private String loginUrl = "/carbon/admin/login_action.jsp";
    private String logoutUrl = "/carbon/admin/logout_action.jsp";

    private final static Logger LOGGER = LoggerFactory.getLogger(ZapScanner.class);

    @Override
    public void run() {
        try {
            startScan();
            setChanged();
            notifyObservers(true);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
    }

    public ZapScanner(String zapHost, int zapPort, String contextName, String sessionName, String productHostRelativeToZap, String productHostRelativeToThis, int productPort,
                      boolean isAuthenticatedScan) {
        try {
            this.contextName = contextName;
            this.sessionName = sessionName;
            this.productHostRelativeToZap = productHostRelativeToZap;
            this.productHostRelativeToThis = productHostRelativeToThis;
            this.productPort = productPort;

            this.zapClient = new ZapClient(zapHost, zapPort, HTTP_SCHEME);
            this.isAuthenticatedScan = isAuthenticatedScan;

            productUri = (new URIBuilder()).setHost(productHostRelativeToZap).setPort(productPort).setScheme(HTTPS_SCHEME).build();
            loginCredentials = new HashMap<>();
            loginCredentials.put(keyUsername, valueUserName);
            loginCredentials.put(keyPassword, valuePassword);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void startScan() {
        try {
            LOGGER.info("Starting ZAP scanning process...");

            //Create new context
            HttpResponse createNewContextResponse = zapClient.createNewContext(contextName, false);
            contextId = extractJsonValue(createNewContextResponse, "contextId");

            HttpResponse includeInContextResponse = zapClient.includeInContext(contextName, "\\Q" + productUri.toString() + "\\E.*", false);
            LOGGER.info("Include in context response: " + HttpRequestHandler.printResponse(includeInContextResponse));

            //Create an empty session
            HttpResponse createEmptySessionResponse = zapClient.createEmptySession(productUri.toString(), sessionName, false);
            LOGGER.info("Creating empty session " + HttpRequestHandler.printResponse(createEmptySessionResponse));

            if (isAuthenticatedScan) {
                //login to wso2 server
                Map<String, String> props = new HashMap<>();
                props.put("Content-Type", "text/plain");

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
            HttpRequestHandler.saveResponseToFile(generatedHtmlReport, new File(Constants.REPORT_FILE_PATH));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void runSpider() {
        try {
            BufferedReader bufferedReader;
            ArrayList<String> spiderScanIds = new ArrayList<>();

            bufferedReader = new BufferedReader(new FileReader(Constants.URL_LIST_PATH));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                LOGGER.info("Reading URL list of wso2 product: " + line);

                HttpResponse spiderResponse = zapClient.spider(productUri.toString() + line, "", "", "", "", false);
                LOGGER.info("Spider HTTP Response");

                String scanId = extractJsonValue(spiderResponse, "scan");
                spiderScanIds.add(scanId);
                LOGGER.info("Adding ScanIds of Spider Scans to array: " + scanId);
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
        } catch (InterruptedException | URISyntaxException | IOException e) {
            e.printStackTrace();
        }

    }

    private void runAjaxSpider() {
        try {
            HttpResponse ajaxSpiderResponse = zapClient.ajaxSpider(productUri.toString(), "", "", "", false);
            LOGGER.info("Starting Ajax spider: " + ajaxSpiderResponse);

            HttpResponse ajaxSpiderStatusResponse = zapClient.ajaxSpiderStatus(false);
            LOGGER.info("Ajax spider status: " + ajaxSpiderStatusResponse);

            while (!extractJsonValue(ajaxSpiderStatusResponse, "status").equals("stopped")) {
                ajaxSpiderStatusResponse = zapClient.ajaxSpiderStatus(false);
                LOGGER.info("Ajax spider status: " + ajaxSpiderStatusResponse);
                Thread.sleep(3000);
            }
        } catch (InterruptedException | IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void runActiveScan() {
        try {
            HttpResponse activeScanResponse = zapClient.activeScan(productUri.toString(), "", "", "", "", "", contextId, false);
            String activeScanId = extractJsonValue(activeScanResponse, "scan");

            LOGGER.info("Scan Id of active scan: " + activeScanId);
            Thread.sleep(500);

            HttpResponse activeScanStatusResponse = zapClient.activeScanStatus(activeScanId, false);
            int progress = Integer.parseInt(extractJsonValue(activeScanStatusResponse, "status"));

            while (progress < 100) {
                activeScanStatusResponse = zapClient.activeScanStatus(activeScanId, false);
                progress = Integer.parseInt(extractJsonValue(activeScanStatusResponse, "status"));

                String time = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
                NotificationManager.notifyZapScanStatus("running", progress, time);
                Thread.sleep(1000 * 60);
            }
            if (progress == 100) {

                String time = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
                NotificationManager.notifyZapScanStatus("completed", progress, time);
            }
        } catch (InterruptedException | IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }


    private String extractJsonValue(HttpResponse httpResponse, String key) {
        String jsonString = HttpRequestHandler.printResponse(httpResponse);
        JSONObject jsonObject = new JSONObject(jsonString);
        return jsonObject.getString(key);
    }

//    public static void main(String[] args) throws IOException {
//
//        String loginUrl = "/carbon/admin/login_action.jsp";
//        Map<String, String> props = new HashMap<>();
//        props.put("Content-Type", "text/x-www-form-urlencoded");
//
//        URI loginUri = null;
//        try {
//            loginUri = (new URIBuilder()).setHost("localhost").setPort(9443).setScheme("https").setPath(loginUrl).build();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//
//        Map<String, Object> loginCredentials = new HashMap<>();
//        loginCredentials.put("username", "admin");
//        loginCredentials.put("password", "admin");
//
//        LOGGER.info("URI to login to wso2server: " + loginUri.toString());
//         HttpsRequestHandler.sendRequest(loginUri.toString(), props, loginCredentials, "POST");
////        System.out.println(HttpsRequestHandler.printResponse(httpsURLConnection));
//
////        List<String> setCookieResponseList = HttpsRequestHandler.getResponseValue("Set-Cookie", httpsURLConnection);
//
//    }
}
