//import org.wso2.dynamicscanner.handlers.FileUtil;
//import org.wso2.dynamicscanner.handlers.HttpsHandler;
//import org.apache.http.HttpResponse;
//import org.apache.http.HttpStatus;
//import org.json.JSONObject;
//
//import java.io.*;
//
//import java.net.URLEncoder;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.StringJoiner;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipFile;
//
//public class MainController_OLD {
//    private static final int BUFFER = 2048;
//
//
//    static String runZapScan(String zapHost, String host, String reportPath, String sessionPath) throws Exception {
//
//        //Save a new session
////        org.wso2.dynamicscanner.handlers.FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.saveSession(zapHost, sessionPath));
////
////
////        // Create empty session
////        org.wso2.dynamicscanner.handlers.FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.createEmptySession(zapHost, host, sessionName));
//
////        //Login
////        String login = "";
////
////        //Set JSESSIONID
////        //org.wso2.dynamicscanner.handlers.FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.setSessionTokenValue(zapHost, host, sessionName, "JSESSIONID", ""));
////
////        //Logout
////
////
////        //Exclude Logout
////        //org.wso2.dynamicscanner.handlers.FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.excludeFromSpider(zapHost, host + "/admin/logout_action.jsp"));
////
////        //Remove Login Session
////        org.wso2.dynamicscanner.handlers.FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.removeSession(zapHost, host, sessionName));
////
////        //Create empty session
////        org.wso2.dynamicscanner.handlers.FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.createEmptySession(zapHost, host, sessionName));
////
////        //Create a new Context
////        org.wso2.dynamicscanner.handlers.FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.createNewContext(zapHost, contextName));
////
////        //Include in Authenticated Context
////        org.wso2.dynamicscanner.handlers.FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.includeInContext(zapHost,contextName, host+"/carbon"));
////        org.wso2.dynamicscanner.handlers.FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.includeInContext(zapHost,contextName, host+".*"));
////
////        //Login and get Jsessionid
////
////        //Set JSESSIONID
////        //org.wso2.dynamicscanner.handlers.FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.setSessionTokenValue(zapHost, host, sessionName, "JSESSIONID", ""));
////
//
//        //Run Spider
//        boolean isSpiderSuccess = runSpider(zapHost, host);
//
//        System.out.println(isSpiderSuccess);
//        if (isSpiderSuccess) {
//            boolean isAjaxSpiderSuccess = runAjaxSpider(zapHost, host);
//
//            //Run AjaxSpider
//            if (isAjaxSpiderSuccess) {
//                boolean isActiveScanSuccess = runActiveScan(zapHost, host);
//
//                if (isActiveScanSuccess) {
//                    HttpResponse htmlFile = FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.generateHtmlReport(zapHost));
//
//                    if (htmlFile.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                        boolean isHtmlReportSaved = FileUtil.HttpRequestHandler.saveResponseToFile(htmlFile, new File(reportPath));
//
//                        if (isHtmlReportSaved) {
//                            return "success";
//                        } else {
//                            return "Report Not Saved";
//                        }
//                    }
//                } else {
//                    return "Active Scan Failed";
//                }
//            } else {
//                return "Ajax Spider Failed";
//            }
//        } else {
//            return "Spider Failed";
//        }
//
//        return zapHost;
//    }
//
//    private static boolean runSpider(String zapHost, String host) throws IOException, InterruptedException {
//        //Run Spider
//        HttpResponse spiderHttpResponse = FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.spider(zapHost, host));
//
//        if (spiderHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//            //Get ScanId of Spider
//            String spiderScanId = extractJsonValue(spiderHttpResponse, Constants.SCAN);
//            //Check Spider Status
//            HttpResponse spiderStatusHttpResponse = FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.spiderStatus(zapHost, spiderScanId));
//
//            if (spiderStatusHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                String spiderStatus = extractJsonValue(spiderStatusHttpResponse, Constants.STATUS);
//
//                while (Integer.parseInt(spiderStatus) < 100) {
//                    Thread.sleep(1500);
//
//                    spiderStatusHttpResponse = FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.spiderStatus(zapHost, spiderScanId));
//                    spiderStatus = extractJsonValue(spiderStatusHttpResponse, Constants.STATUS);
//                }
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private static boolean runAjaxSpider(String zapHost, String host) throws IOException, InterruptedException {
//        //Run AJAX Spider
//        HttpResponse ajaxSpiderHttpResponse = FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.ajaxSpider(zapHost, host));
//
//        if (ajaxSpiderHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//            //Check AJAX Spider Status
//            HttpResponse ajaxSpiderStatusHttpResponse = FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.ajaxSpiderStatus(zapHost));
//
//            if (ajaxSpiderStatusHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                String ajaxSpiderStatus = extractJsonValue(ajaxSpiderStatusHttpResponse, Constants.STATUS);
//
//                while (!ajaxSpiderStatus.equals(Constants.STOPPED_STATE)) {
//                    Thread.sleep(2000);
//
//                    ajaxSpiderStatusHttpResponse = FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.ajaxSpiderStatus(zapHost));
//                    ajaxSpiderStatus = extractJsonValue(ajaxSpiderStatusHttpResponse, Constants.STATUS);
//                }
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private static boolean runActiveScan(String zapHost, String host) throws IOException, InterruptedException {
//        //Run Active Scan
//        HttpResponse activeScanHttpResponse = FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.activeScan(zapHost, host));
//
//        if (activeScanHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//            //Get ScanId of Active Scan
//            String activeScanId = extractJsonValue(activeScanHttpResponse, Constants.SCAN);
//            //Check Active Scan Status
//            HttpResponse activeScanStatusHttpResponse = FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.activeScanStatus(zapHost, activeScanId));
//
//            if (activeScanStatusHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                String activeScanStatus = extractJsonValue(activeScanStatusHttpResponse, Constants.STATUS);
//
//                while (Integer.parseInt(activeScanStatus) < 100) {
//                    Thread.sleep(2000);
//                    activeScanStatusHttpResponse = FileUtil.HttpRequestHandler.sendGetRequest(ZapClientOld.activeScanStatus(zapHost, activeScanId));
//                    activeScanStatus = extractJsonValue(activeScanStatusHttpResponse, Constants.STATUS);
//                }
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private static String extractJsonValue(HttpResponse httpResponse, String key) throws IOException {
//        String jsonString = FileUtil.HttpRequestHandler.printResponse(httpResponse);
//        JSONObject jsonObject = new JSONObject(jsonString);
//        return jsonObject.getString(key);
//    }
//
//
