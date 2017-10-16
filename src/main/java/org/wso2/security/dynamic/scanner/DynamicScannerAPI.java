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
import org.springframework.web.bind.annotation.*;
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

    @GetMapping(value = "configureNotificationManager")
    @ResponseBody
    public boolean configureNotificationManager(@RequestParam String automationManagerHost, @RequestParam int automationManagerPort, @RequestParam String myContainerId) throws IOException {
        return DynamicScannerService.configureNotificationManager(automationManagerHost, automationManagerPort, myContainerId);
    }


    @PostMapping(value = "uploadZipFileExtractAndStartServer")
    @ResponseBody
    public boolean uploadZipFileExtractAndStartServer(@RequestParam MultipartFile file) throws IOException {

        return DynamicScannerService.uploadZipFileExtractAndStartServer(file);
    }

    @GetMapping(value = "runZapScan")
    @ResponseBody
    public void runZapScan(@RequestParam String zapHost,
                           @RequestParam int zapPort,
                           @RequestParam String contextName,
                           @RequestParam String sessionName,
                           @RequestParam String productHostRelativeToZap,
                           @RequestParam String productHostRelativeToThis,
                           @RequestParam int productPort,
                           @RequestParam boolean isAuthenticatedScan) throws Exception {

        DynamicScannerService.runZapScan(zapHost, zapPort, contextName, sessionName, productHostRelativeToZap, productHostRelativeToThis, productPort, isAuthenticatedScan);
    }


    @GetMapping(value = "getReport", produces = "application/octet-stream")
    @ResponseBody
    public HttpResponse getReport(HttpServletResponse response) {
        return DynamicScannerService.getReport(response);
    }

    @PostMapping(value = "scan")
    @ResponseBody
    public String scan(@RequestParam String automationManagerHost,
                       @RequestParam int automationManagerPort,
                       @RequestParam String myContainerId,
                       @RequestParam boolean isFileUpload,
                       @RequestParam(required = false) MultipartFile zipFile,
                       @RequestParam MultipartFile urlListFile,
                       @RequestParam String zapHost,
                       @RequestParam int zapPort,
                       @RequestParam String productHostRelativeToZap,
                       @RequestParam String productHostRelativeToThis,
                       @RequestParam int productPort,
                       @RequestParam boolean isAuthenticatedScan,
                       @RequestParam boolean isUnauthenticatedScan) {

        return DynamicScannerService.doWholeProcess(automationManagerHost, automationManagerPort, myContainerId, isFileUpload, zipFile,
                urlListFile, zapHost, zapPort, productHostRelativeToZap, productHostRelativeToThis, productPort, isAuthenticatedScan, isUnauthenticatedScan);
    }

    @GetMapping(value = "isReady")
    @ResponseBody
    public boolean isReady() {
        return true;
    }

}