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

import org.apache.http.HttpResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.dynamic.scanner.observable.Wso2ServerHandler;
import org.wso2.security.dynamic.scanner.observable.ZapScanner;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DynamicScannerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicScannerService.class);
    private static boolean isServerStarted;


    private boolean configureNotificationManager(String automationManagerHost, int automationManagerPort, String myContainerId) {
        NotificationManager.setAutomationManagerHost(automationManagerHost);
        NotificationManager.setAutomationManagerPort(automationManagerPort);
        NotificationManager.setMyContainerId(myContainerId);
        return NotificationManager.isConfigured();
    }

    private boolean uploadZipFileExtractAndStartServer(MultipartFile file) {
        if (NotificationManager.isConfigured()) {
            if (Wso2ServerHandler.uploadZipFileExtractAndStartServer(file)) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    String message;

                    @Override
                    public void run() {
                        if (Wso2ServerHandler.hostAvailabilityCheck("localhost", 9443)) {
                            message = "Successfully started";
                            isServerStarted = true;
                            NotificationManager.notifyServerStarted(true, new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date()));
                        } else {
                            message = "Failed to start the server";
                        }
                        LOGGER.info("WSO2 server status: " + message);
                    }
                }, 110000);
                return true;
            }
        }
        return false;
    }


    private String runZapScan(String zapHost, int zapPort, String productHostRelativeToZap, String productHostRelativeToThis, int productPort,
                              boolean isAuthenticatedScan, boolean isUnauthenticatedScan) {
        ZapScanner zapScanner = new ZapScanner(zapHost, zapPort, productHostRelativeToZap, productHostRelativeToThis,
                productPort, isAuthenticatedScan, isUnauthenticatedScan);

        Observer zapObserver = new Observer() {
            String message;

            @Override
            public void update(Observable o, Object arg) {
                if (new File(Constants.REPORT_FILE_PATH).exists()) {
                    message = "ZAP scan successfully completed";
                    String time = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
                    NotificationManager.notifyReportReady(true, time);
                } else {
                    message = "scan failed";
                }
                LOGGER.info("Zap scan status: " + message);
            }
        };
        zapScanner.addObserver(zapObserver);
        new Thread(zapScanner).start();
        return "Ok";
    }


    public HttpResponse getReport(HttpServletResponse response) {
        if (new File(Constants.REPORT_FILE_PATH).exists()) {
            try {
                InputStream inputStream = new FileInputStream(Constants.REPORT_FILE_PATH);
                IOUtils.copy(inputStream, response.getOutputStream());
                response.flushBuffer();
                LOGGER.info("Successfully write to output stream");
                return (HttpResponse) response;
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error(e.toString());
            }
        } else {
            LOGGER.error("Report is not found");
        }
        return null;
    }

    public void doWholeProcess(String automationManagerHost, int automationManagerPort, String containerId, boolean isFileUpload, MultipartFile zipFile,
                               MultipartFile urlListFile, String zapHost, int zapPort, String productHostRelativeToZap, String productHostRelativeToThis, int productPort,
                               boolean isAuthenticatedScan, boolean isUnauthenticatedScan) {
        try {
            //Configure notification manager
            if (configureNotificationManager(automationManagerHost, automationManagerPort, containerId)) {
                //Upload URL list file
                if (Wso2ServerHandler.uploadFile(urlListFile, Constants.URL_LIST_PATH)) {
                    //Product upload by uploading zip file or give an already running server address
                    if ((isFileUpload && zipFile != null && uploadZipFileExtractAndStartServer(zipFile)) ||
                            (!isFileUpload && Wso2ServerHandler.hostAvailabilityCheck(productHostRelativeToThis, productPort))) {

                        if (isFileUpload) {
                            Thread.sleep(120000);
                            if (!isServerStarted) {
                                NotificationManager.notifyMessage("WSO2 server not started");
                            }
                        }
                        //Check whether zap container is running
                        if (Wso2ServerHandler.hostAvailabilityCheck(zapHost, zapPort)) {
                            runZapScan(zapHost, zapPort, productHostRelativeToZap, productHostRelativeToThis,
                                    productPort, isAuthenticatedScan, isUnauthenticatedScan);

                        } else {
                            NotificationManager.notifyMessage("ZAP is not in running status");
                        }
                    } else {
                        NotificationManager.notifyMessage("WSO2 server not found");
                    }
                } else {
                    NotificationManager.notifyMessage("URL file not found");
                }
            } else {
                LOGGER.error("Notification Manager not configured");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            NotificationManager.notifyMessage(e.toString());
        }
    }
}
