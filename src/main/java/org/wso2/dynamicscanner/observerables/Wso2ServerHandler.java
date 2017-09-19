package org.wso2.dynamicscanner.observerables;
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

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.dynamicscanner.handlers.FileHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Observable;

public class Wso2ServerHandler extends Observable implements Runnable {

    private String wso2serverFileAbsolutePath;
    private String fileName;
    private String productPath;
    private boolean replaceExisting;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());


    public Wso2ServerHandler(String fileName, String productPath, boolean replaceExisting) {
        this.fileName = fileName;
        this.productPath = productPath;
        this.replaceExisting = replaceExisting;
    }

    @Override
    public void run() {
        try {
            extractZipFileAndStartServer();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
    }

    private void extractZipFileAndStartServer() throws IOException {
        if (new File(productPath).exists() && replaceExisting) {
            FileUtils.deleteDirectory(new File(productPath));
        }
        FileHandler.extractFolder(productPath + File.separator + fileName);

        String folderName = fileName.substring(0, fileName.length() - 4);
        findFile(new File(productPath + File.separator + folderName), "wso2server.sh");

        if (wso2serverFileAbsolutePath != null) {
            Runtime.getRuntime().exec(new String[]{"chmod", "777", wso2serverFileAbsolutePath});
            runShellScript(new String[]{wso2serverFileAbsolutePath});
        }
    }

    private void findFile(File parentDirectory, String fileToFind) {
        File[] files = parentDirectory.listFiles();
        for (File file : files) {
            if (file.getName().equals(fileToFind)) {
                wso2serverFileAbsolutePath = file.getAbsolutePath();
                LOGGER.info("wso2server file absolute path" + wso2serverFileAbsolutePath);
                break;
            }
            if (file.isDirectory()) {
                findFile(file, fileToFind);
            }
        }
    }

    private void runShellScript(String[] command) throws IOException {
        Process proc = Runtime.getRuntime().exec(command);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        // read the output from the command
        LOGGER.info("Here is the standard output of the command");
        String s;

        while ((s = stdInput.readLine()) != null) {
            LOGGER.info(s);
        }
        // read any errors from the attempted command
        LOGGER.info("Here is the standard error of the command (if any)");
        while ((s = stdError.readLine()) != null) {
            LOGGER.error(s);
        }
    }

}
