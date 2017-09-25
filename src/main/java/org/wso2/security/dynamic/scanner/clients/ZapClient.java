package org.wso2.security.dynamic.scanner.clients;
/*
*  Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Methods to run API calls to ZAP
 *
 * @author Deshani Geethika
 */
@SuppressWarnings("unused")
public class ZapClient {

    private final String SAVE_SESSION_PATH = "/JSON/core/action/newSession/";
    private final String REMOVE_SESSION_PATH = "/JSON/httpSessions/action/removeSession/";
    private final String CREATE_EMPTY_SESSION = "/JSON/httpSessions/action/createEmptySession/";
    private final String SET_SESSION_TOKEN_VALUE = "/JSON/httpSessions/action/setSessionTokenValue/";
    private final String EXCLUDE_FROM_SCAN = "/JSON/spider/action/excludeFromScan/";
    private final String NEW_CONTEXT = "/JSON/context/action/newContext/";
    private final String INCLUDE_IN_CONTEXT = "/JSON/context/action/includeInContext/";
    private final String SPIDER_SCAN = "/JSON/spider/action/scan/";
    private final String SPIDER_STATUS = "/JSON/spider/view/status/";
    private final String AJAX_SPIDER_SCAN = "/JSON/ajaxSpider/action/scan/";
    private final String AJAX_SPIDER_STATUS = "/JSON/ajaxSpider/view/status/";
    private final String ACTIVE_SCAN = "/JSON/ascan/action/scan/";
    private final String ACTIVE_SCAN_STATUS = "/JSON/ascan/view/status/";
    private final String HTML_REPORT = "/OTHER/core/other/htmlreport/";

    private final HttpClient httpClient;
    private final String host;
    private final int port;
    private final String scheme;

    private final static String GET = "GET";
    private final static String POST = "POST";

    private final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public ZapClient(String host, int port, String scheme) {
        httpClient = HttpClientBuilder.create().build();
        this.host = host;
        this.port = port;
        this.scheme = scheme;
        LOGGER.info("ZapClient is initialized");
    }

    public HttpResponse saveSession(String name, boolean overwrite, Boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(SAVE_SESSION_PATH)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("name", name)
                .addParameter("overwrite", overwrite ? "true" : "false")
                .build();

        LOGGER.info("URI to run SaveSession: " + uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse removeSession(String site, String session, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(REMOVE_SESSION_PATH)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("site", site)
                .addParameter("session", session)
                .build();

        LOGGER.info("URI to run RemoveSession: " + uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse createEmptySession(String site, String session, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(CREATE_EMPTY_SESSION)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("site", site)
                .addParameter("session", session)
                .build();

        LOGGER.info("URI to run CreateEmptySession: " + uri);
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

        LOGGER.info("URI to run SetSessionTokenValue: " + uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse excludeFromSpider(String regex, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(EXCLUDE_FROM_SCAN)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("regex", regex)
                .build();

        LOGGER.info("URI to ExcludeFromSpider: " + uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse createNewContext(String contextName, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(NEW_CONTEXT)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("contextName", contextName)
                .build();

        LOGGER.info("URI to CreateNewContext: " + uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse includeInContext(String contextName, String regex, boolean post)
            throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(INCLUDE_IN_CONTEXT)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("contextName", contextName)
                .addParameter("regex", regex)
                .build();

        LOGGER.info("URI to run IncludeInContext: " + uri);
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

        LOGGER.info("URI to run Spider" + uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse spiderStatus(String scanId, boolean post) throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(SPIDER_STATUS)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("scanId", scanId)
                .build();

        LOGGER.info("URI to run SpiderStatus: " + uri);
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

        LOGGER.info("URI to run AjaxSpider: " + uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse ajaxSpiderStatus(boolean post) throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(AJAX_SPIDER_STATUS)
                .addParameter("formMethod", post ? POST : GET)
                .build();

        LOGGER.info("URI to check Ajax Spider Status: " + uri);
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

        LOGGER.info("URI to run ActiveScan: " + uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse activeScanStatus(String scanId, boolean post) throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(ACTIVE_SCAN_STATUS)
                .addParameter("formMethod", post ? POST : GET)
                .addParameter("scanId", scanId)
                .build();

        LOGGER.info("URI to check ActiveScanStatus: " + uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }

    public HttpResponse generateHtmlReport(boolean post) throws IOException, URISyntaxException {

        URI uri = (new URIBuilder()).setHost(this.host).setPort(this.port).setScheme(this.scheme).setPath(HTML_REPORT)
                .addParameter("formMethod", post ? POST : GET)
                .build();

        LOGGER.info("URI to generate HTML report: " + uri);
        return httpClient.execute(post ? new HttpPost(uri) : new HttpGet(uri));
    }
}