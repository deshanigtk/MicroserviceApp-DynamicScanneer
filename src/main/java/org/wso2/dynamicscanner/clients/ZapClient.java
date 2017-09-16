package org.wso2.dynamicscanner.clients;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class ZapClient {

    private static final String SAVE_SESSION_PATH = "/JSON/core/action/newSession/";
    private static final String REMOVE_SESSION_PATH = "/JSON/httpSessions/action/removeSession/";
    private static final String CREATE_EMPTY_SESSION = "/JSON/httpSessions/action/createEmptySession/";
    private static final String SET_SESSION_TOKEN_VALUE = "/JSON/httpSessions/action/setSessionTokenValue/";
    private static final String EXCLUDE_FROM_SCAN = "/JSON/spider/action/excludeFromScan/";
    private static final String NEW_CONTEXT = "/JSON/context/action/newContext/";
    private static final String INCLUDE_IN_CONTEXT = "/JSON/context/action/includeInContext/";
    private static final String SPIDER_SCAN = "/JSON/spider/action/scan/";
    private static final String SPIDER_STATUS = "/JSON/spider/view/status/";
    private static final String AJAX_SPIDER_SCAN = "/JSON/ajaxSpider/action/scan/";
    private static final String AJAX_SPIDER_STATUS = "/JSON/ajaxSpider/view/status/";
    private static final String ACTIVE_SCAN = "/JSON/ascan/action/scan/";
    private static final String ACTIVE_SCAN_STATUS = "/JSON/ascan/view/status/";
    private static final String HTML_REPORT = "/OTHER/core/other/htmlreport/";

    private final HttpClient httpClient;
    private final String host;
    private final int port;
    private final String scheme;

    private final static String GET = "GET";
    private final static String POST = "POST";

    private static final Logger LOGGER = Logger.getLogger(ZapClient.class.getName());

    public ZapClient(String host, int port, String scheme) {
        httpClient = HttpClientBuilder.create().build();
        this.host = host;
        this.port = port;
        this.scheme = scheme;
        LOGGER.log(Level.FINE, "ZapClient is initialized", new String[]{host, String.valueOf(port), scheme});
    }

    public HttpResponse saveSession(String name, boolean overwrite, Boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(SAVE_SESSION_PATH)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("name", name)
                .addParameter("overwrite", overwrite ? "true" : "false")
                .build();

        LOGGER.log(Level.FINE, "URI to run SaveSession", uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse removeSession(String site, String session, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(REMOVE_SESSION_PATH)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("site", site)
                .addParameter("session", session)
                .build();

        LOGGER.log(Level.FINE, "URI to run RemoveSession", uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse createEmptySession(String site, String session, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(CREATE_EMPTY_SESSION)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("site", site)
                .addParameter("session", session)
                .build();

        LOGGER.log(Level.FINE, "URI to run CreateEmptySession", uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse setSessionTokenValue(String site, String session, String sessionToken, String tokenValue,
                                             boolean post) throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(SET_SESSION_TOKEN_VALUE)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("site", site)
                .addParameter("session", session)
                .addParameter("sessionToken", sessionToken)
                .addParameter("tokenValue", tokenValue)
                .build();

        LOGGER.log(Level.FINE, "URI to run SetSessionTokenValue", uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse excludeFromSpider(String regex, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(EXCLUDE_FROM_SCAN)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("regex", regex)
                .build();

        LOGGER.log(Level.FINE, "URI to ExcludeFromSpider", uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse createNewContext(String contextName, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(NEW_CONTEXT)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("contextName", contextName)
                .build();

        LOGGER.log(Level.FINE, "URI to CreateNewContext", uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse includeInContext(String contextName, String regex, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(INCLUDE_IN_CONTEXT)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("contextName", contextName)
                .addParameter("regex", regex)
                .build();

        LOGGER.log(Level.FINE, "URI to run IncludeInContext", uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse spider(String url, String maxChildren, String recurse, String contextName, String subtreeOnly,
                               boolean post) throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(scheme).setPath(SPIDER_SCAN)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("url", url)
                .addParameter("maxChildren", maxChildren)
                .addParameter("recurse", recurse)
                .addParameter("contextName", contextName)
                .addParameter("subtreeOnly", subtreeOnly)
                .build();

        LOGGER.log(Level.FINE, "URI to run Spider", uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse spiderStatus(String scanId, boolean post) throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(SPIDER_STATUS)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("scanId", scanId)
                .build();

        LOGGER.log(Level.FINE, "URI to run SpiderStatus", uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse ajaxSpider(String url, String inScope, String contextName, String subtreeOnly, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(AJAX_SPIDER_SCAN)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("url", url)
                .addParameter("inScope", inScope)
                .addParameter("contextName", contextName)
                .addParameter("subtreeOnly", subtreeOnly)
                .build();

        LOGGER.log(Level.FINE, "URI to run AjaxSpider", uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse ajaxSpiderStatus(boolean post) throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(AJAX_SPIDER_STATUS)
                .addParameter("formMethod", post ? POST : GET)
                .build();
        System.out.println(uri.toString());

        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse activeScan(String url, String recurse, String inScopeOnly, String scanPolicyName, String method,
                                   String postData, String contextId, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(ACTIVE_SCAN)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("url", url)
                .addParameter("recurse", recurse)
                .addParameter("inScopeOnly", inScopeOnly)
                .addParameter("scanPolicyName", scanPolicyName)
                .addParameter("method", method)
                .addParameter("postData", postData)
                .addParameter("contextId", contextId)
                .build();

        LOGGER.log(Level.FINE, "URI to run ActiveScan", uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse activeScanStatus(String scanId, boolean post) throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(ACTIVE_SCAN_STATUS)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("scanId", scanId)
                .build();

        LOGGER.log(Level.FINE, "URI to check ActiveScanStatus", uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse generateHtmlReport(boolean post) throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(HTML_REPORT)
                .addParameter("formMethod", post ? POST : GET)
                .build();

        LOGGER.log(Level.FINE, "URI to generate HTML report", uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }
}