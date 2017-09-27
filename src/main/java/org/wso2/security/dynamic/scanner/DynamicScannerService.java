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
import org.wso2.security.dynamic.scanner.observerables.Wso2ServerHandler;
import org.wso2.security.dynamic.scanner.observerables.ZapScanner;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

public class DynamicScannerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicScannerService.class);

    public static void runZapScan(String zapHost, int zapPort, String sessionName,
                                  String productHostRelativeToZap, String productHostRelativeToThis, int productPort,
                                  String urlListPath, boolean isAuthenticatedScan) throws Exception {


        ZapScanner zapScanner = new ZapScanner(zapHost, zapPort, sessionName, productHostRelativeToZap, productHostRelativeToThis,
                productPort, urlListPath, isAuthenticatedScan);

        Observer zapObserver = new Observer() {
            String message;

            @Override
            public void update(Observable o, Object arg) {
                if (new File(Constants.REPORT_FILE_PATH).exists()) {
                    message = "ZAP scan successfully completed";
                } else {
                    message = "scan failed";
                }
                LOGGER.info("Zap scan status: " + message);
            }
        };
        zapScanner.addObserver(zapObserver);
        if (Wso2ServerHandler.hostAvailabilityCheck(productHostRelativeToThis, productPort)) {
            if (Wso2ServerHandler.hostAvailabilityCheck(zapHost, zapPort)) {
                new Thread(zapScanner).start();
            } else {
                LOGGER.error("ZAP is not in running status");
            }
        } else {
            LOGGER.error("Wso2 server is not in running status");
        }
    }

    public static void uploadZipFileExtractAndStartServer(MultipartFile file, boolean replaceExisting) throws IOException {
        Wso2ServerHandler wso2ServerHandler = new Wso2ServerHandler(file, Constants.PRODUCT_PATH, replaceExisting);
        Observer wso2ServerObserver = new Observer() {

            String message;

            @Override
            public void update(Observable o, Object arg) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (Wso2ServerHandler.hostAvailabilityCheck("localhost", 9443)) {
                            message = "Successfully started";
                        } else {
                            message = "Failed to start the server";
                        }
                        LOGGER.info("WSO2 server status: " + message);
                    }
                }, 120000);
            }
        };
        wso2ServerHandler.addObserver(wso2ServerObserver);
        new Thread(wso2ServerHandler).start();
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
}
