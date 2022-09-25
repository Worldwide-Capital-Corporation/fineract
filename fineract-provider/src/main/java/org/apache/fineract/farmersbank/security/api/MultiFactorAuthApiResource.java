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
import org.apache.fineract.farmersbank.security.data.TwoFactorData;
import org.apache.fineract.farmersbank.service.AuthenticatorService;
import org.apache.fineract.infrastructure.core.exception.InvalidTwoFactorCodeException;
import org.apache.fineract.infrastructure.core.exception.PlatformInternalServerException;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;


@Path("/multifactor")
@Component
@ConditionalOnProperty("fineract.security.mfa.enabled")
@Scope("singleton")
@Tag(name = "Two Factor", description = "")
public class MultiFactorAuthApiResource {

    public static class MultiFactorAuthRequest {
        public String code;
        public boolean enrolled;
    }

    public static class MultiFactorAuthStatus {
        public String qrCode;
        public boolean enrolled;

        public MultiFactorAuthStatus(String qrCode, boolean enrolled) {
            this.qrCode = qrCode;
            this.enrolled = enrolled;
        }
    }

    private static final Logger logger
            = LoggerFactory.getLogger(MultiFactorAuthApiResource.class);

    private final ToApiJsonSerializer<MultiFactorAuthStatus> statusSerializer;
    private final ToApiJsonSerializer<TwoFactorData> verifySerializer;

    private final PlatformSecurityContext context;
    private final AppUserRepository repository;
    private final AuthenticatorService authenticatorService;

    @Autowired
    public MultiFactorAuthApiResource( final ToApiJsonSerializer<MultiFactorAuthStatus> statusSerializer,
                                       final ToApiJsonSerializer<TwoFactorData> verifySerializer,
                                       final PlatformSecurityContext context,
                                       final AppUserRepository repository,
                                       final AuthenticatorService authenticatorService) {

        this.statusSerializer = statusSerializer;
        this.verifySerializer = verifySerializer;
        this.context = context;
        this.repository = repository;
        this.authenticatorService = authenticatorService;
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public String multiFactorStatus(@Context final UriInfo uriInfo) {
        AppUser user = context.authenticatedUser();

        if (user.getIsAuthenticatorEnrolled()){
            return this.statusSerializer.serialize(new MultiFactorAuthStatus(null, true));
        } else {
            String secret = authenticatorService.generateSecret();
            user.setAuthenticatorSecret(secret);
            repository.save(user);
            try {
                String qrCodeImageString = authenticatorService.generateQRCode(user, secret);
                return this.statusSerializer.serialize(new MultiFactorAuthStatus(qrCodeImageString, false));
            } catch (Exception e) {
                logger.error("Error generating QR image data {}", e.getMessage());
                throw new PlatformInternalServerException("500", "Error generating authenticator QR image, please try again later");
            }
        }
    }


    @Path("validate")
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public String validate(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        MultiFactorAuthRequest request =
                new Gson().fromJson(apiRequestBodyAsJson, MultiFactorAuthRequest.class);

        if (request == null) {
            logger.error("Invalid request body to /multifactor/validate");
            throw new BadRequestException(
                    "Invalid request body");
        }
        if (request.code == null) {
            logger.error("Invalid request parameters to /multifactor/validate, code parameter missing");
            throw new BadRequestException(
                    "Code is required");
        }
        final AppUser user = context.authenticatedUser();

        if (authenticatorService.isAppCodeValid(user.getAuthenticatorSecret(), request.code)) {
            if (!request.enrolled) {
                user.setAuthenticatorEnrolled(true);
                repository.save(user);
            }
            return verifySerializer.serialize(new TwoFactorData(true));
        } else {
            throw new InvalidTwoFactorCodeException("Invalid code!");
        }
    }
}
