package org.wso2.security.dynamic.scanner.observable;
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
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.dynamic.scanner.NotificationManager;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Observable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Methods to extract a zip file of wso2 product, run wso2server.sh, check a host is available
 *
 * @author Deshani Geethika
 */

public class Wso2ServerHandler extends Observable implements Runnable {

    private String wso2serverFileAbsolutePath;
    private MultipartFile file;
    private String productPath;
    private final static Logger LOGGER = LoggerFactory.getLogger(Wso2ServerHandler.class);


    public Wso2ServerHandler(MultipartFile file, String productPath) {
        this.file = file;
        this.productPath = productPath;
    }

    @Override
    public void run() {
        try {
            uploadZipFileExtractAndStartServer();
            setChanged();
            notifyObservers(true);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
    }

    private void uploadZipFileExtractAndStartServer() throws IOException, InterruptedException {
        boolean isProductPathCreated = new File(productPath).exists() || new File(productPath).mkdir();
        if (isProductPathCreated) {
            String fileName = uploadFile(file);
            if (fileName != null) {
                String time = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
                NotificationManager.notifyFileUploaded(true, time);

                String folderName = extractFolder(productPath + File.separator + fileName);
                time = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss").format(new Date());
                NotificationManager.notifyFileExtracted(true, time);

                findFile(new File(productPath + File.separator + folderName), "wso2server.sh");

                if (wso2serverFileAbsolutePath != null) {
                    Runtime.getRuntime().exec(new String[]{"chmod", "+x", wso2serverFileAbsolutePath});
                    Thread.sleep(500);
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
        Runtime.getRuntime().exec(command);
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

    private String extractFolder(String zipFile) throws IOException {
        int BUFFER = 2048;
        File file = new File(zipFile);

        ZipFile zip = new ZipFile(file);
        String newPath = file.getParent();

        String fileName = file.getName();

        Enumeration zipFileEntries = zip.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(newPath, currentEntry);

            File destinationParent = destFile.getParentFile();
            // create the parent directory structure if needed
            destinationParent.mkdirs();

            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(zip
                        .getInputStream(entry));
                int currentByte;
                // establish buffer for writing file
                byte data[] = new byte[BUFFER];

                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos,
                        BUFFER);

                // read and write until last byte is encountered
                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }

            if (currentEntry.endsWith(".zip")) {
                // found a zip file, try to open
                extractFolder(destFile.getAbsolutePath());
            }
        }

        return fileName.substring(0, fileName.length() - 4);
    }
}
