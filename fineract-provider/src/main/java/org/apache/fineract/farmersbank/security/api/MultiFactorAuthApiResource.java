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

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.fineract.farmersbank.security.data.MultiFactorToken;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("/multifactor")
@Component
@ConditionalOnProperty("fineract.security.2fa.enabled")
@Scope("singleton")
@Tag(name = "Two Factor", description = "")
public class MultiFactorAuthApiResource {

    public static class MultiFactorAuthRequest {
        public String code;
        public boolean enrolled;
    }

    private final ToApiJsonSerializer<MultiFactorToken> accessTokenSerializer;

    private final PlatformSecurityContext context;
    private final AppUserRepository repository;

    @Autowired
    public MultiFactorAuthApiResource( ToApiJsonSerializer<MultiFactorToken> accessTokenSerializer,
                                       PlatformSecurityContext context,
                                       AppUserRepository repository) {

        this.accessTokenSerializer = accessTokenSerializer;
        this.context = context;
        this.repository = repository;
    }


    @Path("validate")
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public String validate(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final AppUser user = context.authenticatedUser();

        MultiFactorAuthRequest request =
                new Gson().fromJson(apiRequestBodyAsJson, MultiFactorAuthRequest.class);

        if (!request.enrolled) {
            user.setAuthenticatorEnrolled(true);
            repository.save(user);
        }

        return accessTokenSerializer.serialize(new MultiFactorToken("App code validated successfully"));
    }
}
