/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.farmersbank.security.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Created by Innocent Magagula on 11/9/22.
 */
final class TokenApiResourceSwagger {

    private TokenApiResourceSwagger() {

    }

    @Schema(description = "PostTokenRequest")
    public static final class PostTokenRequest {

        private PostTokenRequest() {

        }
    }

    @Schema(description = "PostTokenResponse")
    public static final class PostTokenResponse {

        private PostTokenResponse() {

        }

        @Schema(example = "bWlmb3M6cGFzc3dvcmQ")
        public String accessToken;
        @Schema(example = "120")
        public Long expiresIn;
        @Schema(example = "bWlmb3M6cGFzc3dvcmQ")
        public String refreshToken;
    }
}
