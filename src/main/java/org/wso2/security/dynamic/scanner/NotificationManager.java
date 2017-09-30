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
import org.wso2.security.dynamic.scanner.handlers.HttpRequestHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class NotificationManager {

    private final String NOTIFY = "/dynamicScanner/notify";
    private final String SERVER_STARTED = NOTIFY + "/serverStarted";
    private final String ZAP_SCAN_STATUS = NOTIFY + "/zapScanStatus";
    private final String REPORT_READY = NOTIFY + "/reportReady";

    private String myContainerId;
    private String automationManagerHost;
    private int automationManagerPort;

    public NotificationManager(String myContainerId, String automationManagerHost, int automationManagerPort) {
        this.myContainerId = myContainerId;
        this.automationManagerHost = automationManagerHost;
        this.automationManagerPort = automationManagerPort;
    }

    public void notifyServerStarted(boolean status) {
        try {
            URI uri = (new URIBuilder()).setHost(automationManagerHost).setPort(automationManagerPort).setScheme("http").setPath(SERVER_STARTED)
                    .addParameter("containerId", myContainerId)
                    .addParameter("status", String.valueOf(status))
                    .build();
            HttpRequestHandler.sendGetRequest(uri);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public void notifyZapScanStatus(String status, int progress) {
        try {
            URI uri = (new URIBuilder()).setHost(automationManagerHost).setPort(automationManagerPort).setScheme("http").setPath(ZAP_SCAN_STATUS)
                    .addParameter("containerId", myContainerId)
                    .addParameter("status", String.valueOf(status))
                    .addParameter("progress", String.valueOf(progress))
                    .build();
            HttpRequestHandler.sendGetRequest(uri);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public void notifyReportReady(boolean status) {
        try {
            URI uri = (new URIBuilder()).setHost(automationManagerHost).setPort(automationManagerPort).setScheme("http").setPath(REPORT_READY)
                    .addParameter("containerId", myContainerId)
                    .addParameter("status", String.valueOf(status))
                    .build();
            HttpRequestHandler.sendGetRequest(uri);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public String getMyContainerId() {
        return myContainerId;
    }

    public String getAutomationManagerHost() {
        return automationManagerHost;
    }

    public int getAutomationManagerPort() {
        return automationManagerPort;
    }
}
