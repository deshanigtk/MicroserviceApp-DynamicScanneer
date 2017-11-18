package org.wso2.security.dynamic.scanner;
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
import org.wso2.security.dynamic.scanner.handlers.FileHandler;

import java.io.*;
import java.net.Socket;

/**
 * Methods to extract a zip file of wso2 product, run wso2server.sh, check a host is available
 *
 * @author Deshani Geethika
 */

public class ProductManager implements Runnable {

    private String productPath = Constants.PRODUCT_PATH;
    private final static Logger LOGGER = LoggerFactory.getLogger(ProductManager.class);

    private String zipFileName;

    public ProductManager(String zipFileName) {
        this.zipFileName = zipFileName;
    }

    @Override
    public void run() {
        if(extractZipFileAndStartServer()){

        }
    }

    private boolean extractZipFileAndStartServer() {
        try {
            String folderName = FileHandler.extractFolder(productPath + File.separator + zipFileName);
            NotificationManager.notifyFileExtracted(true);
            FileHandler.findFile(new File(productPath + File.separator + folderName), "wso2server.sh");

            if (FileHandler.getWso2serverFileAbsolutePath() != null) {
                Runtime.getRuntime().exec(new String[]{"chmod", "+x", FileHandler.getWso2serverFileAbsolutePath()});
                Thread.sleep(1000);
                runShellScript(new String[]{FileHandler.getWso2serverFileAbsolutePath(), "-DportOffset=" + String.valueOf(Constants.PORT_OFFSET)});
                return hostAvailabilityCheck("localhost", 9443, 12 * 5);

            } else {
                LOGGER.error("Cannot find wso2server.sh file");
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
        return false;
    }

    private void runShellScript(String[] command) {
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
    }

    private boolean hostAvailabilityCheck(String host, int port, int times) {
        int i = 0;
        while (i < times) {
            LOGGER.info("Checking host availability...");
            try (Socket s = new Socket(host, port)) {
                LOGGER.info(host + ":" + port + " is available");
                return true;
            } catch (IOException e) {
                LOGGER.error(e.toString());
                try {
                    Thread.sleep(5000);
                    i++;
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }


}
