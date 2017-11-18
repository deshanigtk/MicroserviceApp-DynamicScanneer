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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * API which exposes to outside world
 *
 * @author Deshani Geethika
 */
@Controller
@RequestMapping("productManager")
public class ProductManagerAPI {

    private final ProductManagerService productManagerService;

    @Autowired
    public ProductManagerAPI(ProductManagerService productManagerService) {
        this.productManagerService = productManagerService;
    }

    @GetMapping(value = "isReady")
    @ResponseBody
    public boolean isReady() {
        return true;
    }

    @PostMapping(value = "startServer")
    @ResponseBody
    public String startServer(@RequestParam String automationManagerHost,
                              @RequestParam int automationManagerPort,
                              @RequestParam String myContainerId,
                              @RequestParam MultipartFile zipFile) {

        return productManagerService.startServer(automationManagerHost, automationManagerPort, myContainerId, zipFile);
    }
}