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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class NotificationManager {

    private final static String NOTIFY = "/dynamicScanner/notify";
    private final static String FILE_UPLOADED = NOTIFY + "/fileUploaded";
    private final static String FILE_EXTRACTED = NOTIFY + "/fileExtracted";
    private final static String SERVER_STARTED = NOTIFY + "/serverStarted";
    private final static String ZAP_SCAN_STATUS = NOTIFY + "/zapScanStatus";
    private final static String REPORT_READY = NOTIFY + "/reportReady";

    private static String myContainerId;
    private static String automationManagerHost;
    private static int automationManagerPort;

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationManager.class);

    public static void setMyContainerId(String myContainerId) {
        NotificationManager.myContainerId = myContainerId;
    }


    public static void setAutomationManagerHost(String automationManagerHost) {
        NotificationManager.automationManagerHost = automationManagerHost;
    }

    public static void setAutomationManagerPort(int automationManagerPort) {
        NotificationManager.automationManagerPort = automationManagerPort;
    }

    public static void notifyFileUploaded(boolean status, String time) {
        try {
            URI uri = (new URIBuilder()).setHost(automationManagerHost).setPort(automationManagerPort).setScheme("http").setPath(FILE_UPLOADED)
                    .addParameter("containerId", myContainerId)
                    .addParameter("status", String.valueOf(status))
                    .addParameter("time", time)
                    .build();

            LOGGER.info("URI to notify file uploaded: " + uri);
            HttpRequestHandler.sendGetRequest(uri);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
    }

    public static void notifyFileExtracted(boolean status, String time) {
        try {
            URI uri = (new URIBuilder()).setHost(automationManagerHost).setPort(automationManagerPort).setScheme("http").setPath(FILE_EXTRACTED)
                    .addParameter("containerId", myContainerId)
                    .addParameter("status", String.valueOf(status))
                    .addParameter("time", time)
                    .build();

            LOGGER.info("URI to notify file extracted: " + uri);
            HttpRequestHandler.sendGetRequest(uri);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());

        }
    }

    public static void notifyServerStarted(boolean status, String time) {
        try {
            URI uri = (new URIBuilder()).setHost(automationManagerHost).setPort(automationManagerPort).setScheme("http").setPath(SERVER_STARTED)
                    .addParameter("containerId", myContainerId)
                    .addParameter("status", String.valueOf(status))
                    .addParameter("time", time)
                    .build();
            HttpRequestHandler.sendGetRequest(uri);
            LOGGER.info("URI to notify server started: " + uri);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
    }

    public static void notifyZapScanStatus(String status, int progress, String time) {
        try {
            URI uri = (new URIBuilder()).setHost(automationManagerHost).setPort(automationManagerPort).setScheme("http").setPath(ZAP_SCAN_STATUS)
                    .addParameter("containerId", myContainerId)
                    .addParameter("status", String.valueOf(status))
                    .addParameter("progress", String.valueOf(progress))
                    .addParameter("time", time)
                    .build();
            LOGGER.info("URI to notify zap scan status: " + uri);
            HttpRequestHandler.sendGetRequest(uri);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
    }

    public static void notifyReportReady(boolean status, String time) {
        try {
            URI uri = (new URIBuilder()).setHost(automationManagerHost).setPort(automationManagerPort).setScheme("http").setPath(REPORT_READY)
                    .addParameter("containerId", myContainerId)
                    .addParameter("status", String.valueOf(status))
                    .addParameter("time", time)
                    .build();
            LOGGER.info("URI to notify report is ready: " + uri);
            HttpRequestHandler.sendGetRequest(uri);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
    }

    public static boolean isConfigured() {
        return automationManagerHost != null && automationManagerPort != 0 && myContainerId != null;
    }

    public static String getMyContainerId() {
        return myContainerId;
    }

    public static String getAutomationManagerHost() {
        return automationManagerHost;
    }

    public static int getAutomationManagerPort() {
        return automationManagerPort;
    }
}
