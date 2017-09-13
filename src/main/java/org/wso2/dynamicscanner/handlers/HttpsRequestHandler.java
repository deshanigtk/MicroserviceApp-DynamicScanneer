package org.wso2.dynamicscanner.handlers;

import org.apache.http.impl.execchain.RequestAbortedException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HttpsRequestHandler {

    private static final String trustStoreType = "JKS";
    private static final String trustManagerType = "SunX509";
    private static final String protocol = "TLSv1.2";
    private static final String trustStorePath = "org/wso2/dynamicscanner/truststore.jks";
    private static final String trustStorePassword = "wso2carbon";

    private static KeyStore trustStore;
    private static HttpsURLConnection httpsURLConnection;
    private static SSLSocketFactory sslSocketFactory;

    private static boolean isInitialized = false;

    private static void init() {
        try {
            trustStore = KeyStore.getInstance(trustStoreType);
            InputStream inputStream = HttpsRequestHandler.class.getClassLoader().getResourceAsStream(trustStorePath);
            assert inputStream != null;
            trustStore.load(inputStream, trustStorePassword.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(trustManagerType);
            trustManagerFactory.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance(protocol);

            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            sslSocketFactory = sslContext.getSocketFactory();

            isInitialized = true;


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String sendRequest(String link, Map<String, String> requestHeaders, Map<String, Object> requestParams,
                                     String method, String data) throws RequestAbortedException, UnsupportedEncodingException {

        if (!isInitialized) {
            init();
        }

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> param : requestParams.entrySet()) {
            if (postData.length() != 0) {
                postData.append('&');
            }
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }

        try {
            URL url = new URL(link + "?" + postData.toString());

            httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setSSLSocketFactory(sslSocketFactory);
            httpsURLConnection.setRequestMethod(method);
//            httpsURLConnection.setInstanceFollowRedirects(true);
//            httpsURLConnection.setUseCaches(false);

            if (requestHeaders != null) {
                for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                    httpsURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            StringBuilder builder = new StringBuilder();
            builder.append(httpsURLConnection.getResponseCode())
                    .append(" ")
                    .append(httpsURLConnection.getResponseMessage())
                    .append("\n");

            Map<String, List<String>> headerFields = httpsURLConnection.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
                if (entry.getKey() == null)
                    continue;
                builder.append(entry.getKey())
                        .append(": ");

                List<String> headerValues = entry.getValue();

                Iterator<String> it = headerValues.iterator();
                if (it.hasNext()) {
                    builder.append(it.next());

                    while (it.hasNext()) {
                        builder.append(", ")
                                .append(it.next());
                    }
                }
                builder.append("\n");
            }

            return builder.toString();

        } catch (IOException e) {
            throw new RequestAbortedException("Https request aborted");
        }
    }
}