package org.wso2.dynamicscanner.handlers;

import sun.security.pkcs11.wrapper.Constants;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utility methods for file handling
 *
 * @author Deshani Geethika
 */
public class FileHandler {

    private static final int BUFFER = 2048;

    public static void extractFileAtLocation(String zipFilePath) throws IOException {

        File file = new File(zipFilePath);

        ZipFile zipFile = new ZipFile(file);
        String extractParent = file.getParent();

        Enumeration zipFileEntries = zipFile.entries();

        while (zipFileEntries.hasMoreElements()) {

            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String name = entry.getName();
            File destFile = new File(extractParent, name);

            if (destFile.getParentFile().mkdirs()) {
                if (!entry.isDirectory()) {
                    try (BufferedInputStream inputStream =
                                 new BufferedInputStream(zipFile.getInputStream(entry));
                         BufferedOutputStream outputStream =
                                 new BufferedOutputStream(new FileOutputStream(destFile), BUFFER)) {

                        byte data[] = new byte[BUFFER];
                        int currentByte;

                        while ((currentByte = inputStream.read(data, 0, BUFFER)) != -1) {
                            outputStream.write(data, 0, currentByte);
                        }
                        outputStream.flush();
                    }
                }
            }
        }
    }


    public static String extractFolder(String zipFile) throws IOException {
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
        //FileUtils.deleteDirectory(new File(zipFile));
        return fileName.substring(0, fileName.length() - 4);
    }
}