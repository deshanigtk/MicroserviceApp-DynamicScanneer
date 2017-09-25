package org.wso2.security.dynamic.scanner.observerables;
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
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.dynamic.scanner.handlers.FileHandler;

import java.io.*;
import java.net.Socket;
import java.util.Observable;

/**
 * Methods to extract a zip file of wso2 product, run wso2server.sh, check a host is available
 *
 * @author Deshani Geethika
 */

public class Wso2ServerHandler extends Observable implements Runnable {

    private String wso2serverFileAbsolutePath;
    private MultipartFile file;
    private String productPath;
    private boolean replaceExisting;
    private final static Logger LOGGER = LoggerFactory.getLogger(Wso2ServerHandler.class);


    public Wso2ServerHandler(MultipartFile file, String productPath, boolean replaceExisting) {
        this.file = file;
        this.productPath = productPath;
        this.replaceExisting = replaceExisting;
    }

    @Override
    public void run() {
        try {
            uploadZipFileExtractAndStartServer();
            setChanged();
            notifyObservers(true);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
    }

    private void uploadZipFileExtractAndStartServer() throws IOException {
        boolean isProductPathCreated;
        if (new File(productPath).exists() && replaceExisting) {
            System.out.println("kkkkkkkkkkkkkkkk");
            FileUtils.deleteDirectory(new File(productPath));
            System.out.println("product path availability: " + new File(productPath).exists());
        }
        isProductPathCreated = new File(productPath).exists() || new File(productPath).mkdir();
        if (isProductPathCreated) {
            System.out.println("product path created");
            String fileName = uploadFile(file);
            if (fileName != null) {
                String folderName = FileHandler.extractFolder(productPath + File.separator + fileName);

                findFile(new File(productPath + File.separator + folderName), "wso2server.sh");

                if (wso2serverFileAbsolutePath != null) {
                    Runtime.getRuntime().exec(new String[]{"chmod", "777", wso2serverFileAbsolutePath});
                    runShellScript(new String[]{wso2serverFileAbsolutePath});
                }
            }
        } else {
            LOGGER.error("Product path is not available");
        }
    }

    private void findFile(File parentDirectory, String fileToFind) {
        File[] files = parentDirectory.listFiles();
        for (File file : files) {
            if (file.getName().equals(fileToFind)) {
                wso2serverFileAbsolutePath = file.getAbsolutePath();
                LOGGER.info("wso2server file absolute path: " + wso2serverFileAbsolutePath);
                break;
            }
            if (file.isDirectory()) {
                findFile(file, fileToFind);
            }
        }
    }

    private void runShellScript(String[] command) throws IOException {
        Process proc = Runtime.getRuntime().exec(command);

//        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
//
//        // read the output from the command
//        LOGGER.info("Here is the standard output of the command");
//        String s;
//
//        while ((s = stdInput.readLine()) != null) {
//            LOGGER.info(s);
//        }
//        // read any errors from the attempted command
//        LOGGER.info("Here is the standard error of the command (if any)");
//        while ((s = stdError.readLine()) != null) {
//            LOGGER.error(s);
//        }
//        stdInput.close();
//        stdError.close();
    }

    public static boolean hostAvailabilityCheck(String host, int port) {
        try (Socket s = new Socket(host, port)) {
            LOGGER.info("Success");
            return true;
        } catch (IOException e) {
            LOGGER.error(e.toString());
            return false;
        }
    }

    private String uploadFile(MultipartFile file) {
        if (!file.isEmpty()) {
            System.out.println("file not wmptyyyyy");
            String fileName = file.getOriginalFilename();
            if (fileName.endsWith(".zip")) {
                try {
                    byte[] bytes = file.getBytes();
                    BufferedOutputStream stream =
                            new BufferedOutputStream(new FileOutputStream(new File(productPath + File.separator + fileName)));
                    stream.write(bytes);
                    stream.close();
                    LOGGER.info("File successfully uploaded");
                    return fileName;

                } catch (IOException e) {
                    LOGGER.error("File is not uploaded" + e.toString());
                }

            } else {
                LOGGER.error("Not a zip file");
            }
        } else {
            System.out.println("file empty: " + file.isEmpty());
            LOGGER.error("No file");
        }
        return null;
    }
}
