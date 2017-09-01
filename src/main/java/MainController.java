import org.apache.tomcat.util.http.fileupload.FileUtils;

import java.io.File;
import java.io.IOException;

class MainController {

    private static String wso2serverFileAbsolutePath;

    static void runShellScript(String[] command) {
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String extractZipFileAndReturnServerFile(String fileName, String productPath, boolean replaceExisting) throws IOException {
        if (new File(productPath).exists() && replaceExisting) {
            FileUtils.deleteDirectory(new File(productPath));
        }
        FileUtil.extractFolder(productPath + File.separator + fileName);

        String folderName = fileName.substring(0, fileName.length() - 4);
        findFile(new File(productPath + File.separator + folderName), "wso2server.sh");
        return wso2serverFileAbsolutePath;
    }


    private static void findFile(File parentDirectory, String fileToFind) {
        File[] files = parentDirectory.listFiles();
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

    public static void main(String[] args) throws IOException {
        String fileName = "wso2is-5.3.0.zip";
        String productPath = "/home/deshani/Documents/Product";
        boolean replaceExisting = false;
        System.out.println(extractZipFileAndReturnServerFile(fileName, productPath, replaceExisting));
    }
}
