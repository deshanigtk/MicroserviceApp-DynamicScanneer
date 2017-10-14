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

public class DynamicScannerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicScannerService.class);
    private static boolean isServerStarted;


    public static boolean configureNotificationManager(String automationManagerHost, int automationManagerPort, String containerId) {
        NotificationManager.setAutomationManagerHost(automationManagerHost);
        NotificationManager.setAutomationManagerPort(automationManagerPort);
        NotificationManager.setMyContainerId(containerId);
        return NotificationManager.isConfigured();
    }

    public static boolean uploadZipFileExtractAndStartServer(MultipartFile file) {
        if (NotificationManager.isConfigured()) {
            Wso2ServerHandler.uploadZipFileExtractAndStartServer(file);

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
            }, 120000);
        }
        return isServerStarted;
    }


    public static String runZapScan(String zapHost, int zapPort, String contextName, String sessionName,
                                    String productHostRelativeToZap, String productHostRelativeToThis, int productPort,
                                    boolean isAuthenticatedScan) {
        if (NotificationManager.isConfigured()) {

            ZapScanner zapScanner = new ZapScanner(zapHost, zapPort, contextName, sessionName, productHostRelativeToZap, productHostRelativeToThis,
                    productPort, isAuthenticatedScan);

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
            final String message;
            zapScanner.addObserver(zapObserver);
            if (Wso2ServerHandler.hostAvailabilityCheck(productHostRelativeToThis, productPort)) {
                if (Wso2ServerHandler.hostAvailabilityCheck(zapHost, zapPort)) {
                    new Thread(zapScanner).start();
                    return "Ok";
                } else {
                    message = "ZAP is not in running status";
                    LOGGER.error(message);
                    return message;
                }
            } else {
                message = "Wso2 server is not in running status";
                LOGGER.error(message);
                return message;
            }
        } else {
            return "Please configure notification manager";
        }
    }

    public static HttpResponse getReport(HttpServletResponse response) {
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

    public static void doWholeProcess(String automationManagerHost, int automationManagerPort, String containerId, boolean isFileUpload,
                                      MultipartFile zipFile, MultipartFile urlListFile, String zapHost, int zapPort, String contextName,
                                      String sessionName, String productHostRelativeToZap, String productHostRelativeToThis, int productPort,
                                      boolean isAuthenticatedScan, boolean isUnauthenticatedScan) {
        if (configureNotificationManager(automationManagerHost, automationManagerPort, containerId)) {
            if (Wso2ServerHandler.uploadFile(urlListFile, Constants.URL_LIST_PATH)) {
                if ((isFileUpload && zipFile != null && uploadZipFileExtractAndStartServer(zipFile)) ||
                        (!isFileUpload && Wso2ServerHandler.hostAvailabilityCheck(productHostRelativeToThis, productPort))) {

                    if (isAuthenticatedScan) {
                        runZapScan(zapHost, zapPort, contextName, sessionName, productHostRelativeToZap, productHostRelativeToThis,
                                productPort, true);
                    }
                    if (isUnauthenticatedScan) {
                        runZapScan(zapHost, zapPort, contextName, sessionName, productHostRelativeToZap, productHostRelativeToThis,
                                productPort, false);
                    }
                } else {
                    LOGGER.error("WSO2 server not found");
                }
            } else {
                LOGGER.error("URL file not found");
            }
        }
    }
}
