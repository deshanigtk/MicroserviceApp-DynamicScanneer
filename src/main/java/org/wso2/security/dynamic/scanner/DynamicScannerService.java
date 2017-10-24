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
import java.util.Observer;

@Service
public class DynamicScannerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicScannerService.class);

    private boolean configureNotificationManager(String automationManagerHost, int automationManagerPort, String myContainerId) {
        NotificationManager.config(automationManagerHost, automationManagerPort, myContainerId);
        return NotificationManager.isConfigured();
    }


    public String startScan(String automationManagerHost, int automationManagerPort, String containerId, boolean isFileUpload, MultipartFile zipFile,
                            MultipartFile urlListFile, String zapHost, int zapPort, String productHostRelativeToZap, String productHostRelativeToThis, int productPort,
                            boolean isAuthenticatedScan) {

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

        Observer observer = (o, arg) -> {
            if (new File(Constants.REPORT_FILE_PATH).exists()) {
                LOGGER.info("Dynamic Scanner report is ready");
                NotificationManager.notifyReportReady(true);
            }
        };

        MainScanner mainScanner = new MainScanner(isFileUpload, zipFile, urlListFile, zapHost, zapPort, productHostRelativeToZap,
                productHostRelativeToThis, productPort, isAuthenticatedScan);
        mainScanner.addObserver(observer);
        new Thread(mainScanner).start();
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

}
