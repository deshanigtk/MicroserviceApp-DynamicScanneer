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
import org.wso2.security.dynamic.scanner.scanners.MainScanner;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Service
public class DynamicScannerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicScannerService.class);
    private static boolean isServerStarted;


    private boolean configureNotificationManager(String automationManagerHost, int automationManagerPort, String myContainerId) {
        NotificationManager.config(automationManagerHost, automationManagerPort, myContainerId);
        return NotificationManager.isConfigured();
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


//    public void doWholeProcess(String automationManagerHost, int automationManagerPort, String containerId, boolean isFileUpload, MultipartFile zipFile,
//                               MultipartFile urlListFile, String zapHost, int zapPort, String productHostRelativeToZap, String productHostRelativeToThis, int productPort,
//                               boolean isAuthenticatedScan, boolean isUnauthenticatedScan) {
//        try {
//            //Configure notification manager
//            if (configureNotificationManager(automationManagerHost, automationManagerPort, containerId)) {
//                //Upload URL list file
//                if (Wso2ServerHandler.uploadFile(urlListFile, Constants.URL_LIST_PATH)) {
//                    //Product upload by uploading zip file or give an already running server address
//                    if ((isFileUpload && zipFile != null && uploadZipFileExtractAndStartServer(zipFile)) ||
//                            (!isFileUpload && Wso2ServerHandler.hostAvailabilityCheck(productHostRelativeToThis, productPort))) {
//
//                        if (isFileUpload) {
//                            Thread.sleep(120000);
//                            if (!isServerStarted) {
//                                NotificationManager.notifyMessage("WSO2 server not started");
//                            }
//                        }
//                        //Check whether zap container is running
//                        if (Wso2ServerHandler.hostAvailabilityCheck(zapHost, zapPort)) {
//                            runZapScan(zapHost, zapPort, productHostRelativeToZap, productHostRelativeToThis,
//                                    productPort, isAuthenticatedScan, isUnauthenticatedScan);
//
//                        } else {
//                            NotificationManager.notifyMessage("ZAP is not in running status");
//                        }
//                    } else {
//                        NotificationManager.notifyMessage("WSO2 server not found");
//                    }
//                } else {
//                    NotificationManager.notifyMessage("URL file not found");
//                }
//            } else {
//                LOGGER.error("Notification Manager not configured");
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            NotificationManager.notifyMessage(e.toString());
//        }
//    }


    public String doWholeProcess(String automationManagerHost, int automationManagerPort, String containerId, boolean isFileUpload, MultipartFile zipFile,
                                 MultipartFile urlListFile, String zapHost, int zapPort, String productHostRelativeToZap, String productHostRelativeToThis, int productPort,
                                 boolean isAuthenticatedScan, boolean isUnauthenticatedScan) {

        //Configure notification manager
        if (configureNotificationManager(automationManagerHost, automationManagerPort, containerId)) {
            //Upload URL list file

            if (urlListFile != null) {
                if (isFileUpload) {
                    if (zipFile == null || !zipFile.getOriginalFilename().endsWith(".zip")) {
                        return "Please upload a zip file";
                    }
                }
            } else {
                return "Please upload url list file";
            }
        } else {
            return "Please configure Notification Manager";
        }

        MainScanner mainScanner = new MainScanner(isFileUpload,zipFile, urlListFile, zapHost, zapPort, productHostRelativeToZap, productHostRelativeToThis, productPort,isAuthenticatedScan, isUnauthenticatedScan);
        new Thread(mainScanner).start();
        return "Ok";
    }

//    @Retryable(value = IOException.class, maxAttempts = 10, backoff = @Backoff(delay = 30000))
//    public boolean isServerReady() throws IOException {
//        boolean status;
//        status = Wso2ServerHandler.hostAvailabilityCheck("localhost", 9443);
//        LOGGER.info("WSO2 server is ready: " + status);
//        return status;
//    }

}
