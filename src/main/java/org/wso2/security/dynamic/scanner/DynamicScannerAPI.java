package org.wso2.security.dynamic.scanner;/*
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
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * API which exposes to outside world
 *
 * @author Deshani Geethika
 */
@Controller
@EnableAutoConfiguration
@RequestMapping("dynamicScanner")
public class DynamicScannerAPI {

    @RequestMapping(value = "configureNotificationManager", method = RequestMethod.GET)
    @ResponseBody
    public boolean configureNotificationManager(@RequestParam String automationManagerHost, @RequestParam int automationManagerPort, @RequestParam String myContainerId) throws IOException {
        return DynamicScannerService.configureNotificationManager(automationManagerHost, automationManagerPort, myContainerId);
    }


    @RequestMapping(value = "uploadZipFileExtractAndStartServer", method = RequestMethod.POST)
    @ResponseBody
    public boolean uploadZipFileExtractAndStartServer(@RequestParam MultipartFile file) throws IOException {

        return DynamicScannerService.uploadZipFileExtractAndStartServer(file);
    }

    @RequestMapping(value = "runZapScan", method = RequestMethod.GET)
    @ResponseBody
    public void runZapScan(@RequestParam String zapHost,
                           @RequestParam int zapPort,
                           @RequestParam String contextName,
                           @RequestParam String sessionName,
                           @RequestParam String productHostRelativeToZap,
                           @RequestParam String productHostRelativeToThis,
                           @RequestParam int productPort,
                           @RequestParam String urlListPath,
                           @RequestParam boolean isAuthenticatedScan) throws Exception {

//        return DynamicScannerService.runZapScan(zapHost, zapPort, contextName, sessionName, productHostRelativeToZap, productHostRelativeToThis, productPort, urlListPath, isAuthenticatedScan);
    }


    @RequestMapping(value = "getReport", method = RequestMethod.GET, produces = "application/octet-stream")
    @ResponseBody
    public HttpResponse getReport(HttpServletResponse response) {
        return DynamicScannerService.getReport(response);
    }

    @RequestMapping(value = "test", method = RequestMethod.POST)
    @ResponseBody
    public void test(@RequestParam String automationManagerHost, @RequestParam int automationManagerPort,
                     @RequestParam String containerId, @RequestParam boolean isFileUpload,
                     @RequestParam(required = false) MultipartFile zipFile, @RequestParam MultipartFile urlListFile,
                     @RequestParam String zapHost,
                     @RequestParam int zapPort,
                     @RequestParam String contextName,
                     @RequestParam String sessionName,
                     @RequestParam String productHostRelativeToZap,
                     @RequestParam String productHostRelativeToThis,
                     @RequestParam int productPort,
                     @RequestParam boolean isAuthenticatedScan,
                     @RequestParam boolean isUnauthenticatedScan) {

        DynamicScannerService.doWholeProcess(automationManagerHost, automationManagerPort, containerId, isFileUpload, zipFile, urlListFile, zapHost, zapPort, contextName, sessionName, productHostRelativeToZap, productHostRelativeToThis, productPort, isAuthenticatedScan, isUnauthenticatedScan);
//        return DynamicScannerService.runZapScan(zapHost, zapPort, contextName, sessionName, productHostRelativeToZap, productHostRelativeToThis, productPort, urlListPath, isAuthenticatedScan);
    }


}