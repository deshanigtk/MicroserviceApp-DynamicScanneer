/*
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.dynamicscanner.observerables.Wso2ServerHandler;
import org.wso2.dynamicscanner.observerables.ZapScanner;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * API which exposes to outside world
 *
 * @author Deshani Geethika
 */
@Controller
@EnableAutoConfiguration
@PropertySource("classpath:global.properties")
@RequestMapping("dynamicScanner")
public class UserAPI {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Value("${report_file_path}")
    private String reportFilePath;

    @Value("${product_path}")
    private String productPath;

    @RequestMapping(value = "runZap", method = RequestMethod.GET)
    @ResponseBody
    public void runZapScan(@RequestParam String zapHost,
                           @RequestParam int zapPort,
                           @RequestParam String sessionName,
                           @RequestParam String productHostRelativeToZap,
                           @RequestParam String productHostRelativeToThis,
                           @RequestParam int productPort,
                           @RequestParam String urlListPath,
                           @RequestParam boolean isAuthenticatedScan) throws Exception {


        ZapScanner zapScanner = new ZapScanner(zapHost, zapPort, sessionName, productHostRelativeToZap, productHostRelativeToThis,
                productPort, urlListPath, isAuthenticatedScan);

        Observer zapObserver = new Observer() {
            String message;

            @Override
            public void update(Observable o, Object arg) {
                if (new File(reportFilePath).exists()) {
                    message = "success";
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

    @RequestMapping(value = "uploadZipFileExtractAndStartServer", method = RequestMethod.POST)
    @ResponseBody
    public void uploadZipFileExtractAndStartServer(@RequestParam("file") MultipartFile file,
                                               @RequestParam boolean replaceExisting) throws IOException {


        Wso2ServerHandler wso2ServerHandler = new Wso2ServerHandler(file, productPath, replaceExisting);
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
                }, 70000);


            }
        };
        wso2ServerHandler.addObserver(wso2ServerObserver);
        new Thread(wso2ServerHandler).start();
    }
}