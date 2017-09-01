import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class MainController_OLD {


    static String runZapScan(String zapHost, String host, String reportPath, String sessionPath) throws Exception {

        //Save a new session
//        HttpRequestHandler.sendGetRequest(ZapClient.saveSession(zapHost, sessionPath));
//
//
//        // Create empty session
//        HttpRequestHandler.sendGetRequest(ZapClient.createEmptySession(zapHost, host, sessionName));

//        //Login
//        String login = "";
//
//        //Set JSESSIONID
//        //HttpRequestHandler.sendGetRequest(ZapClient.setSessionTokenValue(zapHost, host, sessionName, "JSESSIONID", ""));
//
//        //Logout
//
//
//        //Exclude Logout
//        //HttpRequestHandler.sendGetRequest(ZapClient.excludeFromSpider(zapHost, host + "/admin/logout_action.jsp"));
//
//        //Remove Login Session
//        HttpRequestHandler.sendGetRequest(ZapClient.removeSession(zapHost, host, sessionName));
//
//        //Create empty session
//        HttpRequestHandler.sendGetRequest(ZapClient.createEmptySession(zapHost, host, sessionName));
//
//        //Create a new Context
//        HttpRequestHandler.sendGetRequest(ZapClient.createNewContext(zapHost, contextName));
//
//        //Include in Authenticated Context
//        HttpRequestHandler.sendGetRequest(ZapClient.includeInContext(zapHost,contextName, host+"/carbon"));
//        HttpRequestHandler.sendGetRequest(ZapClient.includeInContext(zapHost,contextName, host+".*"));
//
//        //Login and get Jsessionid
//
//        //Set JSESSIONID
//        //HttpRequestHandler.sendGetRequest(ZapClient.setSessionTokenValue(zapHost, host, sessionName, "JSESSIONID", ""));
//

        //Run Spider
        boolean isSpiderSuccess = runSpider(zapHost, host);

        System.out.println(isSpiderSuccess);
        if (isSpiderSuccess) {
            boolean isAjaxSpiderSuccess = runAjaxSpider(zapHost, host);

            //Run AjaxSpider
            if (isAjaxSpiderSuccess) {
                boolean isActiveScanSuccess = runActiveScan(zapHost, host);

                if (isActiveScanSuccess) {
                    HttpResponse htmlFile = HttpRequestHandler.sendGetRequest(ZapClient.generateHtmlReport(zapHost));

                    if (htmlFile.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        boolean isHtmlReportSaved = HttpRequestHandler.saveResponseToFile(htmlFile, new File(reportPath));

                        if (isHtmlReportSaved) {
                            return "success";
                        } else {
                            return "Report Not Saved";
                        }
                    }
                } else {
                    return "Active Scan Failed";
                }
            } else {
                return "Ajax Spider Failed";
            }
        } else {
            return "Spider Failed";
        }

        return zapHost;
    }

    private static boolean runSpider(String zapHost, String host) throws IOException, InterruptedException {
        //Run Spider
        HttpResponse spiderHttpResponse = HttpRequestHandler.sendGetRequest(ZapClient.spider(zapHost, host));

        if (spiderHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            //Get ScanId of Spider
            String spiderScanId = extractJsonValue(spiderHttpResponse, Constant.SCAN);
            //Check Spider Status
            HttpResponse spiderStatusHttpResponse = HttpRequestHandler.sendGetRequest(ZapClient.spiderStatus(zapHost, spiderScanId));

            if (spiderStatusHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String spiderStatus = extractJsonValue(spiderStatusHttpResponse, Constant.STATUS);

                while (Integer.parseInt(spiderStatus) < 100) {
                    Thread.sleep(1500);

                    spiderStatusHttpResponse = HttpRequestHandler.sendGetRequest(ZapClient.spiderStatus(zapHost, spiderScanId));
                    spiderStatus = extractJsonValue(spiderStatusHttpResponse, Constant.STATUS);
                }
                return true;
            }
        }
        return false;
    }

    private static boolean runAjaxSpider(String zapHost, String host) throws IOException, InterruptedException {
        //Run AJAX Spider
        HttpResponse ajaxSpiderHttpResponse = HttpRequestHandler.sendGetRequest(ZapClient.ajaxSpider(zapHost, host));

        if (ajaxSpiderHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            //Check AJAX Spider Status
            HttpResponse ajaxSpiderStatusHttpResponse = HttpRequestHandler.sendGetRequest(ZapClient.ajaxSpiderStatus(zapHost));

            if (ajaxSpiderStatusHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String ajaxSpiderStatus = extractJsonValue(ajaxSpiderStatusHttpResponse, Constant.STATUS);

                while (!ajaxSpiderStatus.equals(Constant.STOPPED_STATE)) {
                    Thread.sleep(2000);

                    ajaxSpiderStatusHttpResponse = HttpRequestHandler.sendGetRequest(ZapClient.ajaxSpiderStatus(zapHost));
                    ajaxSpiderStatus = extractJsonValue(ajaxSpiderStatusHttpResponse, Constant.STATUS);
                }
                return true;
            }
        }
        return false;
    }

    private static boolean runActiveScan(String zapHost, String host) throws IOException, InterruptedException {
        //Run Active Scan
        HttpResponse activeScanHttpResponse = HttpRequestHandler.sendGetRequest(ZapClient.activeScan(zapHost, host));

        if (activeScanHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            //Get ScanId of Active Scan
            String activeScanId = extractJsonValue(activeScanHttpResponse, Constant.SCAN);
            //Check Active Scan Status
            HttpResponse activeScanStatusHttpResponse = HttpRequestHandler.sendGetRequest(ZapClient.activeScanStatus(zapHost, activeScanId));

            if (activeScanStatusHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String activeScanStatus = extractJsonValue(activeScanStatusHttpResponse, Constant.STATUS);

                while (Integer.parseInt(activeScanStatus) < 100) {
                    Thread.sleep(2000);
                    activeScanStatusHttpResponse = HttpRequestHandler.sendGetRequest(ZapClient.activeScanStatus(zapHost, activeScanId));
                    activeScanStatus = extractJsonValue(activeScanStatusHttpResponse, Constant.STATUS);
                }
                return true;
            }
        }
        return false;
    }

    private static String extractJsonValue(HttpResponse httpResponse, String key) throws IOException {
        String jsonString = HttpRequestHandler.printResponse(httpResponse);
        JSONObject jsonObject = new JSONObject(jsonString);
        return jsonObject.getString(key);
    }


    public static void main1(String[] args) throws Exception {

//        Map<String, String> props = new HashMap<>();
//        props.put("Content-Type", "x-www-form-urlencoded");
//        props.put("User-Agent", "Mozilla/5.0");
//
//
//        Map<String, String> arguments = new HashMap<>();
//        arguments.put("username", "admin");
//        arguments.put("password", "admin");
//
//
//        StringJoiner sj = new StringJoiner("&");
//        for (Map.Entry<String, String> entry : arguments.entrySet())
//            sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
//                    + URLEncoder.encode(entry.getValue(), "UTF-8"));
//
//
//        String r = HttpsHandler.sendRequest(loginUrl, props, arguments, "POST", sj.toString());
//        System.out.println(r);

        String[] cmd = {"/home/deshani/Documents/myshell.sh",
                "http://0.0.0.0:8500",
                "https://172.17.0.1:9443/admin/carbon", "/home/deshani/Documents/newSession",
                "Login+Session",
                "Authenticated+context"};
        Runtime.getRuntime().exec(cmd);


    }
}
