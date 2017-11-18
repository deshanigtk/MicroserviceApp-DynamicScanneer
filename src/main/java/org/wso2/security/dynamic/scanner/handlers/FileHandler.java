package org.wso2.security.dynamic.scanner.handlers;/*
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
import org.wso2.security.dynamic.scanner.ProductManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileHandler {

    private static String wso2serverFileAbsolutePath;
    private final static Logger LOGGER = LoggerFactory.getLogger(ProductManager.class);

    public static void findFile(File parentDirectory, String fileToFind) {
        File[] files = parentDirectory.listFiles();
        if (files != null) {
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
    }

    public static boolean uploadFile(MultipartFile file, String fileUploadPath) {
        try {
            byte[] bytes = file.getBytes();
            BufferedOutputStream stream =
                    new BufferedOutputStream(new FileOutputStream(new File(fileUploadPath)));
            stream.write(bytes);
            stream.close();
            return true;

        } catch (IOException e) {
            LOGGER.error("File is not uploaded" + e.toString());
        }
        return false;
    }

    public static String extractFolder(String zipFile) {
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
        return null;
    }

    public static String getWso2serverFileAbsolutePath() {
        return wso2serverFileAbsolutePath;
    }
}
