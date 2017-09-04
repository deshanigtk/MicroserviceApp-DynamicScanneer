import ch.qos.logback.core.util.FileUtil;
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


    static String extractZipFileAndReturnServerFile(String fileName, String productPath, boolean replaceExisting) throws IOException {
        if (new File(productPath).exists() && replaceExisting) {
            FileUtils.deleteDirectory(new File(productPath));
        }
        FileHandler.extractFolder(productPath + File.separator + fileName);

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

    private static String extractJsonValue(HttpResponse httpResponse, String key) throws IOException {
        String jsonString = HttpRequestHandler.printResponse(httpResponse);
        JSONObject jsonObject = new JSONObject(jsonString);
        return jsonObject.getString(key);
    }


    private static void runSpider(String zapHost, int zapPort, String urlFilePath) throws IOException, InterruptedException, URISyntaxException {

        ZapClient zapClient = new ZapClient(zapHost, zapPort);
        BufferedReader bufferedReader;
        String[] spiderScanIds = new String[]{};
        int i = 0;

        try {
            bufferedReader = new BufferedReader(new FileReader(urlFilePath));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                try {
                    HttpResponse spiderResponse = zapClient.spider(line, "", "", "", "", false);
                    String scanId = extractJsonValue(spiderResponse, "scan");
                    spiderScanIds[i] = scanId;
                    i++;

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String scanId : spiderScanIds) {
            HttpResponse spiderStatusResponse = zapClient.spiderStatus(scanId, false);
            while (Integer.parseInt(extractJsonValue(spiderStatusResponse, scanId)) < 100) {
                spiderStatusResponse = zapClient.spiderStatus(scanId, false);
                Thread.sleep(1000);
            }
        }
    }

    public static void main2(String[] args) throws Exception {

        Map<String, String> props = new HashMap<>();
        props.put("Content-Type", "text/plain");


        Map<String, Object> arguments = new HashMap<>();
        arguments.put("username", "admin");
        arguments.put("password", "admin");


        String r = HttpsRequestHandler.sendRequest("https://localhost:9443/carbon/admin/login_action.jsp", props, arguments, "POST", null);
        System.out.println(r);
    }

    public static void main(String[] args) throws InterruptedException, IOException, URISyntaxException {
        String zapHost = "localhost";
        int zapPort = 8500;
        String urlFilePath = "/home/deshani/Documents/urlList";

        runSpider(zapHost, zapPort, urlFilePath);
    }
}
