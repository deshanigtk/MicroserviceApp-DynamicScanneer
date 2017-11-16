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
import org.wso2.security.dynamic.scanner.Constants;
import org.wso2.security.dynamic.scanner.NotificationManager;
import org.wso2.security.dynamic.scanner.handlers.Wso2ServerHandler;

import java.util.Observable;

public class MainScanner extends Observable implements Runnable {

    private boolean isFileUpload;
    private String zipFileName;
    private String zapHost;
    private int zapPort;
    private String productHostRelativeToZap;
    private String productHostRelativeToThis;
    private int productPort;
    private String message;

    private static final Logger LOGGER = LoggerFactory.getLogger(MainScanner.class);

    @Override
    public void run() {
        startScan();
        setChanged();
        notifyObservers(true);
    }

    public MainScanner(boolean isFileUpload, String zipFileName, String zapHost, int zapPort, String productHostRelativeToZap, String productHostRelativeToThis, int productPort) {
        this.isFileUpload = isFileUpload;
        this.zipFileName = zipFileName;
        this.zapHost = zapHost;
        this.zapPort = zapPort;
        this.productHostRelativeToZap = productHostRelativeToZap;
        this.productHostRelativeToThis = productHostRelativeToThis;
        this.productPort = productPort;
    }

    private void startScan() {
        if (Wso2ServerHandler.hostAvailabilityCheck(zapHost, zapPort, 4)) {
            if (isFileUpload) {
                if (Wso2ServerHandler.uploadZipFileExtractAndStartServer(zipFileName)) {
                    if (Wso2ServerHandler.hostAvailabilityCheck("localhost", 9443 + Constants.PORT_OFFSET, 12 * 5)) {
                        NotificationManager.notifyServerStarted(true);
                        ZapScanner zapScanner = new ZapScanner(zapHost, zapPort, productHostRelativeToZap, productHostRelativeToThis, productPort);
                        zapScanner.startScan();
                    }
                }
            } else {
                if (Wso2ServerHandler.hostAvailabilityCheck(productHostRelativeToThis, productPort, 4)) {
                    ZapScanner zapScanner = new ZapScanner(zapHost, zapPort, productHostRelativeToZap, productHostRelativeToThis, productPort);
                    zapScanner.startScan();
                } else {
                    message = "WSO2 server not available";
                }
            }

        } else {
            message = "ZAP is not in running state";
        }

        LOGGER.error(message);
        NotificationManager.notifyMessage(message);
    }
}
