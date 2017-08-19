public class ZapClient {

    private static String formMethodGet = "GET";
    private static String formMethodPost = "POST";


    //Save session
    public static String saveSession(String zap_host, String session_path) {
        return (zap_host + "/JSON/core/action/newSession/?zapapiformat=JSON&formMethod=" + formMethodGet +
                "&name=" + session_path + "&overwrite=");
    }

    //Remove Login Session
    public static String removeSession(String zap_host, String home_url, String session_name) {
        return (zap_host + "/JSON/httpSessions/action/removeSession/?zapapiformat=JSON&formMethod=" + formMethodGet +
                "&site=" + home_url + "&session=" + session_name);
    }

    //Create Empty Session
    public static String createEmptySession(String zap_host, String home_url, String session_name) {
        return (zap_host + "/JSON/httpSessions/action/createEmptySession/?zapapiformat=JSON&formMethod=" + formMethodGet +
                "&site=" + home_url + "&session=" + session_name);
    }

    //Set Session Token Value
    public static String setSessionTokenValue(String zap_host, String home_url, String session_name, String token_name, String token_value) {
        return (zap_host + "/JSON/httpSessions/action/setSessionTokenValue/?zapapiformat=JSON&formMethod=" + formMethodGet +
                "&site=" + home_url + "&session=" + session_name + "&sessionToken=" + token_name + "&tokenValue=" + token_value);
    }

    //Exclude URL from Spider
    public static String excludeFromSpider(String zap_host, String logout_url) {
        return (zap_host + "/JSON/spider/action/excludeFromScan/?zapapiformat=JSON&formMethod=" + formMethodGet + "&regex=" + logout_url);
    }

    //Create New Context
    public static String createNewContext(String zap_host, String context_name) {
        return (zap_host + "/JSON/context/action/newContext/?zapapiformat=JSON&formMethod=" + formMethodGet + "&contextName=" + context_name);
    }

    //Include In Context
    public static String includeInContext(String zap_host, String context_name, String home_url) {
        return (zap_host + "/JSON/context/action/includeInContext/?zapapiformat=JSON&formMethod=" + formMethodGet + "&contextName=" + context_name + "" +
                "&regex=" + home_url);
    }

    //Run Spider
    public static String spider(String zap_host, String url) {
        return (zap_host + "/JSON/spider/action/scan/?zapapiformat=JSON&formMethod=" + formMethodGet + "&url=" + url + "&maxChildren=&recurse=&contextName=&subtreeOnly=");
    }

    //Check Spider Status
    public static String spiderStatus(String zap_host, String scan_id) {
        return (zap_host + "/JSON/spider/view/status/?zapapiformat=JSON&formMethod=" + formMethodGet + "&scanId=" + scan_id);
    }

    //Run AJAX Spider
    public static String ajaxSpider(String zap_host, String url) {
        return (zap_host + "/JSON/ajaxSpider/action/scan/?zapapiformat=JSON&formMethod=" + formMethodGet + "&url=" + url + "&inScope=&contextName=&subtreeOnly=");
    }

    //Check AJAX Spider Status
    public static String ajaxSpiderStatus(String zap_host) {
        return (zap_host + "/JSON/ajaxSpider/view/status/?zapapiformat=JSON&formMethod=" + formMethodGet);
    }

    //Run Active Scan
    public static String activeScan(String zap_host, String home_url) {
        return (zap_host + "/JSON/ascan/action/scan/?zapapiformat=JSON&formMethod=" + formMethodGet + "&url=" + home_url + "&recurse=" +
                "&inScopeOnly=&scanPolicyName=&method=&postData=&contextId=");
    }

    //Check active Scan Status
    public static String activeScanStatus(String zap_host, String scanId) {
        return (zap_host + "/JSON/ascan/view/status/?zapapiformat=JSON&formMethod=" + formMethodGet + "&scanId="+scanId);
    }

    //Generate HTML Report
    public static String generateHtmlReport(String zap_host) {
        return (zap_host + "/OTHER/core/other/htmlreport/?formMethod=" + formMethodGet);
    }
}

