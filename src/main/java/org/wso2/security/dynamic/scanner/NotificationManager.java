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

    public static boolean isConfigured() {
        return automationManagerHost != null && automationManagerPort != -1 && myContainerId != null;
    }
}
