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


    private static void runSpider(String zapHost, int zapPort, String scheme, String urlFilePath) throws IOException, InterruptedException, URISyntaxException {

        ZapClient zapClient = new ZapClient(zapHost, zapPort, scheme);
        BufferedReader bufferedReader;
        ArrayList<String> spiderScanIds = new ArrayList<>();
        int i = 0;

        try {
            bufferedReader = new BufferedReader(new FileReader(urlFilePath));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                try {
                    HttpResponse spiderResponse = zapClient.spider(line, "", "", "", "", false);
                    String scanId = extractJsonValue(spiderResponse, "scan");
                    spiderScanIds.add(scanId);
                    System.out.println(i);
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
            while (Integer.parseInt(extractJsonValue(spiderStatusResponse, "status")) < 100) {
                spiderStatusResponse = zapClient.spiderStatus(scanId, false);
                Thread.sleep(1000);
            }
        }
    }

    private static void runAjaxSpider(String zapHost, int zapPort, String scheme, String urlFilePath) throws IOException, InterruptedException, URISyntaxException {

        ZapClient zapClient = new ZapClient(zapHost, zapPort, scheme);
        BufferedReader bufferedReader;
        int i = 0;

        try {
            bufferedReader = new BufferedReader(new FileReader(urlFilePath));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                try {
                    HttpResponse ajaxSpiderResponse = zapClient.ajaxSpider(line, "", "", "", false);

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        HttpResponse ajaxSpiderStatusResponse = zapClient.ajaxSpiderStatus(false);
        while (!extractJsonValue(ajaxSpiderStatusResponse, "status").equals("stopped")) {
            ajaxSpiderStatusResponse = zapClient.ajaxSpiderStatus(false);
            Thread.sleep(1000);
        }
    }

    private static void runActiveScan(String zapHost, int zapPort, String scheme, String urlFilePath) throws IOException, InterruptedException, URISyntaxException {

        ZapClient zapClient = new ZapClient(zapHost, zapPort, scheme);
        BufferedReader bufferedReader;
        ArrayList<String> activeScanIds = new ArrayList<>();
        int i = 0;

        try {
            bufferedReader = new BufferedReader(new FileReader(urlFilePath));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                try {
                    HttpResponse activeScanResponse = zapClient.activeScan(line, "", "", "", "", "", "", false);
                    String scanId = extractJsonValue(activeScanResponse, "scan");
                    activeScanIds.add(scanId);
                    i++;

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String scanId : activeScanIds) {
            HttpResponse activeScanStatusResponse = zapClient.activeScanStatus(scanId, false);
            while (Integer.parseInt(extractJsonValue(activeScanStatusResponse, "status")) < 100) {
                activeScanStatusResponse = zapClient.activeScanStatus(scanId, false);
                Thread.sleep(1000);
            }
        }
    }


    public static void runZapScan(String zapHost, int zapPort, String scheme, String urlListPath, String reportFilePath) throws Exception {
        ZapClient zapClient = new ZapClient(zapHost, zapPort, "http");

        runSpider(zapHost, zapPort, scheme, urlListPath);
        runAjaxSpider(zapHost, zapPort, scheme, urlListPath);
        runActiveScan(zapHost, zapPort, scheme, urlListPath);

        HttpResponse generatedHtmlReport = zapClient.generateHtmlReport(false);
        HttpRequestHandler.saveResponseToFile(generatedHtmlReport, new File(reportFilePath));

    }

    public static void login() throws Exception {

        Map<String, String> props = new HashMap<>();
        props.put("Content-Type", "text/plain");


        Map<String, Object> arguments = new HashMap<>();
        arguments.put("username", "admin");
        arguments.put("password", "admin");


        String r = HttpsRequestHandler.sendRequest("https://localhost:9443/carbon/admin/login_action.jsp", props, arguments, "POST", null);
        System.out.println(r);
    }
}
