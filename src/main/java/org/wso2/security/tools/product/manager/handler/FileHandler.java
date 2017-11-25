/*
*  Copyright (c) ${2017}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.security.tools.product.manager.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.wso2.security.tools.product.manager.Constants;
import org.wso2.security.tools.product.manager.ProductManagerExecutor;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utility methods for file handling
 */
public class FileHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ProductManagerExecutor.class);
    private static String wso2serverFileAbsolutePath;

    /**
     * Find a path of a file in a given directory
     *
     * @param parentDirectory Parent directory the file belongs to
     * @param fileToFind      File to find
     */
    public static void findFile(File parentDirectory, String fileToFind) {
        File[] files = parentDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals(fileToFind)) {
                    wso2serverFileAbsolutePath = file.getAbsolutePath();
                    break;
                }
                if (file.isDirectory()) {
                    findFile(file, fileToFind);
                }
            }
        }
    }

    /**
     * Extract a zip file and returns name of the extracted folder
     *
     * @param zipFilePath ZIP file path
     * @return Extracted folder name
     * @throws IOException If an error occurs while I/O operations
     */
    public static String extractZipFile(String zipFilePath) throws IOException {
        int BUFFER = 2048;
        File file = new File(zipFilePath);
        ZipFile zip = new ZipFile(file);
        String newPath = file.getParent();
        String fileName = file.getName();
        Enumeration zipFileEntries = zip.entries();

        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(newPath, currentEntry);
            File destinationParent = destFile.getParentFile();
            // create the parent directory structure if needed
            destinationParent.mkdirs();

            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                int currentByte;
                // establish buffer for writing file
                byte data[] = new byte[BUFFER];
                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
                // read and write until last byte is encountered
                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }
            if (currentEntry.endsWith(Constants.ZIP_FILE_EXTENSION)) {
                // found a zip file, try to open
                extractZipFile(destFile.getAbsolutePath());
            }
        }
        zip.close();
        return fileName.substring(0, fileName.length() - 4);
    }

    /**
     * Upload a {@code MultipartFile} to a specified location
     *
     * @param file            File to be uploaded
     * @param destinationPath Destination path to upload the file
     * @throws IOException If an error occurs while I/O operations
     */
    public static void uploadFile(MultipartFile file, String destinationPath) throws IOException {
        byte[] bytes = file.getBytes();
        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(destinationPath)));
        stream.write(bytes);
        stream.close();
    }

    /**
     * Get the absolute file path of wso2server.sh file
     *
     * @return Absolute path of wso2server,sh
     */
    public static String getWso2serverFileAbsolutePath() {
        return wso2serverFileAbsolutePath;
    }
}
