package org.wso2.security.dynamic.scanner.scanners;/*
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.dynamic.scanner.NotificationManager;
import org.wso2.security.dynamic.scanner.handlers.Wso2ServerHandler;

import java.util.Observable;

@Service
public class MainScanner extends Observable implements Runnable {

    private boolean isFileUpload;
    private MultipartFile zipFile;
    private MultipartFile urlListFile;
    private String zapHost;
    private int zapPort;
    private String productHostRelativeToZap;
    private String productHostRelativeToThis;
    private int productPort;
    private boolean isAuthenticatedScan;
    private boolean isUnauthenticatedScan;

    private static final Logger LOGGER = LoggerFactory.getLogger(MainScanner.class);

    @Override
    public void run() {
        startScan();
    }

    public MainScanner() {
    }


    public MainScanner(boolean isFileUpload, MultipartFile zipFile, MultipartFile urlListFile, String zapHost, int zapPort, String productHostRelativeToZap, String productHostRelativeToThis, int productPort,
                       boolean isAuthenticatedScan, boolean isUnauthenticatedScan) {
        this.isFileUpload = isFileUpload;
        this.zipFile = zipFile;
        this.urlListFile = urlListFile;
        this.zapHost = zapHost;
        this.zapPort = zapPort;
        this.productHostRelativeToZap = productHostRelativeToZap;
        this.productHostRelativeToThis = productHostRelativeToThis;
        this.productPort = productPort;
        this.isAuthenticatedScan = isAuthenticatedScan;
        this.isUnauthenticatedScan = isUnauthenticatedScan;
    }

    private void startScan() {
        try {
            if (Wso2ServerHandler.hostAvailabilityCheck(zapHost, zapPort)) {
                if (isFileUpload) {
                    if (Wso2ServerHandler.uploadZipFileExtractAndStartServer(zipFile)) {
                        Thread.sleep(120000);

                        if (Wso2ServerHandler.hostAvailabilityCheck("localhost", 9443)) {
                            NotificationManager.notifyServerStarted(true);
                            ZapScanner zapScanner = new ZapScanner(zapHost, zapPort, productHostRelativeToZap, productHostRelativeToThis, productPort, isAuthenticatedScan, isUnauthenticatedScan);
                            zapScanner.startScan(isAuthenticatedScan);
                        }
                    }
                } else {
                    if (Wso2ServerHandler.hostAvailabilityCheck(productHostRelativeToThis, productPort)) {
                        ZapScanner zapScanner = new ZapScanner(zapHost, zapPort, productHostRelativeToZap, productHostRelativeToThis, productPort, isAuthenticatedScan, isUnauthenticatedScan);
                        zapScanner.startScan(isAuthenticatedScan);
                    } else {
                        LOGGER.error("WSO2 server not available");
                    }
                }

            } else {
                LOGGER.error("ZAP is not in running state");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
