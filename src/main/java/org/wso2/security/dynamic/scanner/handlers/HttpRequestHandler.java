package org.wso2.security.dynamic.scanner.handlers;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for HTTP request handling
 *
 * @author Deshani Geethika
 */
public class HttpRequestHandler {
    //    private static CloseableHttpClient httpClient = HttpClients.createDefault();

    private static List<NameValuePair> urlParameters = new ArrayList<>();

    public static HttpResponse sendGetRequest(URI request) {
        try {
            HttpClientBuilder clientBuilder = HttpClients.custom();
            HttpClient httpClient = clientBuilder.setRetryHandler(new
                    DefaultHttpRequestRetryHandler(3, false)).build();
            HttpGet httpGetRequest = new HttpGet(request);
            return httpClient.execute(httpGetRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HttpResponse sendPostrequest(String request, ArrayList<NameValuePair> parameters) {
        try {
            HttpClientBuilder clientBuilder = HttpClients.custom();
            HttpClient httpClient = clientBuilder.setRetryHandler(new
                    DefaultHttpRequestRetryHandler(3, false)).build();
            HttpPost httpPostRequest = new HttpPost(request);

            for (NameValuePair parameter : parameters) {
                urlParameters.add(new BasicNameValuePair(parameter.getName(), parameter.getValue()));
            }

            httpPostRequest.setEntity(new UrlEncodedFormEntity(urlParameters));
            return httpClient.execute(httpPostRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String printResponse(HttpResponse response) {
        try {
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean saveResponseToFile(HttpResponse response, File destinationFile) {
        try {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = entity.getContent();
                FileOutputStream output = new FileOutputStream(destinationFile);
                int l;
                byte[] tmp = new byte[2048];
                while ((l = inputStream.read(tmp)) != -1) {
                    output.write(tmp, 0, l);
                }
                output.close();
                inputStream.close();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
