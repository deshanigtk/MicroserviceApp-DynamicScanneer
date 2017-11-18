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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.dynamic.scanner.handlers.FileHandler;

import java.io.*;

@Service
public class ProductManagerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductManagerService.class);

    private boolean configureNotificationManager(String automationManagerHost, int automationManagerPort, String myContainerId) {
        NotificationManager.config(automationManagerHost, automationManagerPort, myContainerId);
        return NotificationManager.isConfigured();
    }

    public String startServer(String automationManagerHost, int automationManagerPort, String containerId, MultipartFile zipFile) {

        String zipFileName;
        //Configure notification manager
        if (configureNotificationManager(automationManagerHost, automationManagerPort, containerId)) {
            if (!zipFile.getOriginalFilename().endsWith(".zip")) {
                return "Please upload a zip file";
            } else {
                zipFileName = zipFile.getOriginalFilename();
                if (new File(Constants.PRODUCT_PATH).exists() || new File(Constants.PRODUCT_PATH).mkdir()) {
                    String fileUploadPath = Constants.PRODUCT_PATH + File.separator + zipFile.getOriginalFilename();
                    if (FileHandler.uploadFile(zipFile, fileUploadPath)) {
                        LOGGER.info("File successfully uploaded");
                        NotificationManager.notifyFileUploaded(true);
                    } else {
                        return "Error occurred while uploading zip file";
                    }
                }
            }
        } else {
            return "Please configure Notification Manager";
        }
        ProductManager productManager = new ProductManager(zipFileName);
        new Thread(productManager).start();
        return "Ok";
    }
}
