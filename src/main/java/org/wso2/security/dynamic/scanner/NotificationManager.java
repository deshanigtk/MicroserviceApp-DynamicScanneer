package org.wso2.security.dynamic.scanner;/*
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

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.security.dynamic.scanner.handlers.HttpRequestHandler;

import java.net.URI;
import java.net.URISyntaxException;

public class NotificationManager {

    private final static String NOTIFY = "automationManager/dynamicScanner/notify";
    private final static String FILE_UPLOADED = NOTIFY + "/fileUploaded";
    private final static String FILE_EXTRACTED = NOTIFY + "/fileExtracted";
    private final static String SERVER_STARTED = NOTIFY + "/serverStarted";
    private final static String ZAP_SCAN_STATUS = NOTIFY + "/zapScanStatus";
    private final static String REPORT_READY = NOTIFY + "/reportReady";
    private final static String MESSAGE = NOTIFY + "/message";

    private static String myContainerId;
    private static String automationManagerHost;
    private static int automationManagerPort;

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationManager.class);

    public static void config(String automationManagerHost, int automationManagerPort, String myContainerId) {
        NotificationManager.automationManagerHost = automationManagerHost;
        NotificationManager.automationManagerPort = automationManagerPort;
        NotificationManager.myContainerId = myContainerId;
    }

    public static void notifyFileUploaded(boolean status) {
        try {
            URI uri = (new URIBuilder()).setHost(automationManagerHost).setPort(automationManagerPort).setScheme("http").setPath(FILE_UPLOADED)
                    .addParameter("containerId", myContainerId)
                    .addParameter("status", String.valueOf(status))
                    .build();

            LOGGER.info("Notifying file uploaded " + uri);
            HttpRequestHandler.sendGetRequest(uri);
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static void notifyFileExtracted(boolean status) {
        try {
            URI uri = (new URIBuilder()).setHost(automationManagerHost).setPort(automationManagerPort).setScheme("http").setPath(FILE_EXTRACTED)
                    .addParameter("containerId", myContainerId)
                    .addParameter("status", String.valueOf(status))
                    .build();

            LOGGER.info("Notifying file extracted" + uri);
            HttpRequestHandler.sendGetRequest(uri);
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage());

        }
    }

    public static void notifyServerStarted(boolean status) {
        try {
            URI uri = (new URIBuilder()).setHost(automationManagerHost).setPort(automationManagerPort).setScheme("http").setPath(SERVER_STARTED)
                    .addParameter("containerId", myContainerId)
                    .addParameter("status", String.valueOf(status))
                    .build();
            HttpRequestHandler.sendGetRequest(uri);
            LOGGER.info("Notifying WSO2 server started" + uri);
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static void notifyZapScanStatus(String status, int progress) {
        try {
            URI uri = (new URIBuilder()).setHost(automationManagerHost).setPort(automationManagerPort).setScheme("http").setPath(ZAP_SCAN_STATUS)
                    .addParameter("containerId", myContainerId)
                    .addParameter("status", String.valueOf(status))
                    .addParameter("progress", String.valueOf(progress))
                    .build();
            LOGGER.info("Notifying ZAP scan status" + uri);
            HttpRequestHandler.sendGetRequest(uri);
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static void notifyReportReady(boolean status) {
        try {
            URI uri = (new URIBuilder()).setHost(automationManagerHost).setPort(automationManagerPort).setScheme("http").setPath(REPORT_READY)
                    .addParameter("containerId", myContainerId)
                    .addParameter("status", String.valueOf(status))
                    .build();
            LOGGER.info("Notifying report is ready");
            HttpRequestHandler.sendGetRequest(uri);
        } catch (URISyntaxException e) {
            LOGGER.error(e.toString());
        }
    }

    public static void notifyMessage(String message) {
        try {
            URI uri = (new URIBuilder()).setHost(automationManagerHost).setPort(automationManagerPort).setScheme("http").setPath(MESSAGE)
                    .addParameter("containerId", myContainerId)
                    .addParameter("message", message)
                    .build();
            LOGGER.info("Notifying message");
            HttpRequestHandler.sendGetRequest(uri);
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static boolean isConfigured() {
        return automationManagerHost != null && automationManagerPort != -1 && myContainerId != null;
    }
}
