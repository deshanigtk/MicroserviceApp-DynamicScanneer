import com.sun.media.jfxmedia.logging.Logger;
import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.wso2.dynamicscanner.clients.ZapClient;
import org.wso2.dynamicscanner.handlers.FileHandler;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.wso2.dynamicscanner.handlers.HttpRequestHandler;
import org.wso2.dynamicscanner.handlers.HttpsRequestHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class MainController {

    private static String wso2serverFileAbsolutePath;

    static void runShellScript(String[] command) {
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//
//    static String extractZipFileAndReturnServerFile(String fileName, String productPath, boolean replaceExisting) throws IOException {
//        if (new File(productPath).exists() && replaceExisting) {
//            FileUtils.deleteDirectory(new File(productPath));
//        }
//        FileHandler.extractFolder(productPath + File.separator + fileName);
//
//        String folderName = fileName.substring(0, fileName.length() - 4);
//        findFile(new File(productPath + File.separator + folderName), "wso2server.sh");
//        return wso2serverFileAbsolutePath;
//    }
//
//    private static void findFile(File parentDirectory, String fileToFind) {
//        File[] files = parentDirectory.listFiles();
//        for (File file : files) {
//            if (file.getName().equals(fileToFind)) {
//                wso2serverFileAbsolutePath = file.getAbsolutePath();
//                break;
//            }
//            if (file.isDirectory()) {
//                findFile(file, fileToFind);
//            }
//        }
//    }

//    public static void main(String[] args) throws Exception {
//
//        Map<String, String> props = new HashMap<>();
//        props.put("Content-Type", "text/plain");
//
//
//        Map<String, Object> arguments = new HashMap<>();
//        arguments.put("username", "admin");
//        arguments.put("password", "admin");
//
//
//        //String r = HttpsRequestHandler.sendRequest("https://localhost:9443/carbon/admin/login_action.jsp", props, arguments, "POST", null);
//        //System.out.println(r);
//
//
//    }
}
