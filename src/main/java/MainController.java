import org.apache.http.HttpResponse;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    private static String sessionName = "Login+Session";
    private static String logoutUrl = "";
    private static String contextName = "Authenticated+Context";
    private static String loginUrl = "https://localhost:9443/carbon/admin/login_action.jsp";
    private static String zapHost = "http://localhost:8500";
    private static String host = "https://localhost:9443/carbon";

    private static JSONObject jsonObject;
    private static String jsonString;

    public static String runZapScan(String zapHost, String host) throws Exception {

        //Save a new session
//        HttpRequestHandler.sendGetRequest(ZapClient.saveSession(zapHost, sessionPath));
//


        //Create empty session
        // HttpRequestHandler.sendGetRequest(ZapClient.createEmptySession(zapHost, host, sessionName));

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
        HttpResponse spiderHttpResponse = HttpRequestHandler.sendGetRequest(ZapClient.spider(zapHost, host));

        //Get ScanId of Spider
        String spiderScanId = extractJsonValue(spiderHttpResponse, "scan");

        //Check Spider Status
        HttpResponse spiderStatusHttpResponse = HttpRequestHandler.sendGetRequest(ZapClient.spiderStatus(zapHost, spiderScanId));
        String spiderStatus = extractJsonValue(spiderStatusHttpResponse, "status");

        while (Integer.parseInt(spiderStatus) < 100) {
            Thread.sleep(1500);

            spiderStatusHttpResponse = HttpRequestHandler.sendGetRequest(ZapClient.spiderStatus(zapHost, spiderScanId));
            spiderStatus = extractJsonValue(spiderStatusHttpResponse, "status");
        }

        //Run AJAX Spider
        HttpRequestHandler.sendGetRequest(ZapClient.ajaxSpider(zapHost, host));

        //Check AJAX Spider Status
        HttpResponse ajaxSpiderStatusHttpResponse = HttpRequestHandler.sendGetRequest(ZapClient.ajaxSpiderStatus(zapHost));
        String ajaxSpiderStatus = extractJsonValue(ajaxSpiderStatusHttpResponse, "status");

        while (!ajaxSpiderStatus.equals("stopped")) {
            Thread.sleep(2000);

            ajaxSpiderStatusHttpResponse = HttpRequestHandler.sendGetRequest(ZapClient.ajaxSpiderStatus(zapHost));
            ajaxSpiderStatus = extractJsonValue(ajaxSpiderStatusHttpResponse, "status");
        }

        //Run Active Scan
        HttpResponse activeScanHttpResponse = HttpRequestHandler.sendGetRequest(ZapClient.activeScan(zapHost, host));

        //Get ScanId of Active Scan
        String activeScanId = extractJsonValue(activeScanHttpResponse, "scan");

        //Check Active Scan Status
        HttpResponse activeScanStatusHttpResponse = HttpRequestHandler.sendGetRequest(ZapClient.activeScanStatus(zapHost, activeScanId));
        String activeScanStatus = extractJsonValue(activeScanStatusHttpResponse, "status");

        while (Integer.parseInt(activeScanStatus) < 100) {
            Thread.sleep(2000);

            activeScanStatusHttpResponse = HttpRequestHandler.sendGetRequest(ZapClient.activeScanStatus(zapHost, activeScanId));
            activeScanStatus = extractJsonValue(activeScanStatusHttpResponse, "status");
        }


        HttpResponse htmlFile = HttpRequestHandler.sendGetRequest(ZapClient.generateHtmlReport(zapHost));

        HttpRequestHandler.saveResponseToFile(htmlFile, new File("/home/deshani/Documents/new.html"));

        return htmlFile.toString();


    }

    private static String extractJsonValue(HttpResponse httpResponse, String key) throws IOException {
        jsonString = HttpRequestHandler.printResponse(httpResponse);

        jsonObject = new JSONObject(jsonString);

        return jsonObject.getString(key);
    }

    public static void main(String[] args) throws Exception {

        Map<String, String> props = new HashMap<>();
        props.put("Content-Type", "x-www-form-urlencoded");
        props.put("User-Agent", "Mozilla/5.0");


        Map<String, String> arguments = new HashMap<>();
        arguments.put("username", "admin");
        arguments.put("password", "admin");


       /*
        StringJoiner sj = new StringJoiner("&");
        for(Map.Entry<String,String> entry : arguments.entrySet())
            sj.add((entry.getKey()) + "="
                    + (URLEncoder.encode(entry.getValue(),"UTF-8")));
                    */

        //String r = HttpsHandler.sendRequest(loginUrl, props, arguments, "POST", null);
        //System.out.println(r);

        runZapScan(zapHost, host);

    }
}
