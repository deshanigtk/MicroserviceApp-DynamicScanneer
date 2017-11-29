/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.security.tools.product.manager.exception;

/**
 * The class {@code ProductManagerException} wraps the Exceptions and return a new {@link Exception} with the
 * type of {@code ProductManagerException}
 *
 * @see Exception
 */
@SuppressWarnings({"unused"})
public class ProductManagerException extends Exception {
    /**
     * Constructs a new exception with {@code null} as its detail message.
     */
    public ProductManagerException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message Message for the exception
     */
    public ProductManagerException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     */
    public ProductManagerException(String message, Throwable e) {
        super(message, e);
    }
}
