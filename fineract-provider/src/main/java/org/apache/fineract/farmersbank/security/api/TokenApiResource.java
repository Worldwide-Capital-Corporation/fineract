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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.fineract.farmersbank.security.data.JwtTokenData;
import org.apache.fineract.farmersbank.security.data.RefreshTokenResponse;
import org.apache.fineract.farmersbank.security.utils.TokenProvider;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.data.AuthenticatedUserData;
import org.apache.fineract.infrastructure.security.service.SpringSecurityPlatformSecurityContext;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Component
@ConditionalOnProperty("fineract.security.oauth.enabled")
@Scope("singleton")
@Path("/oauth/token")
@Tag(
        name = "Refresh access token",
        description =
                "An API capability that allows client applications to request for a new authentication token using HTTP Bearer Authentication.")
public class TokenApiResource {

    private final DaoAuthenticationProvider customAuthenticationProvider;
    private final ToApiJsonSerializer<AuthenticatedUserData> apiJsonSerializerService;
    private final SpringSecurityPlatformSecurityContext springSecurityPlatformSecurityContext;
    private final ClientReadPlatformService clientReadPlatformService;
    private final TokenProvider tokenProvider;
    private final AppUserRepository repository;

    @Autowired
    public TokenApiResource(
            @Qualifier("customAuthenticationProvider")
            final DaoAuthenticationProvider customAuthenticationProvider,
            final ToApiJsonSerializer<AuthenticatedUserData> apiJsonSerializerService,
            final SpringSecurityPlatformSecurityContext springSecurityPlatformSecurityContext,
            final AppUserRepository repository,
            final TokenProvider tokenProvider,
            ClientReadPlatformService aClientReadPlatformService) {
        this.customAuthenticationProvider = customAuthenticationProvider;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.springSecurityPlatformSecurityContext = springSecurityPlatformSecurityContext;
        this.repository = repository;
        this.tokenProvider = tokenProvider;
        clientReadPlatformService = aClientReadPlatformService;
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Refresh token",
            description =
                    "Invalidate old access token and generate new access and refresh token.")
    @RequestBody(
            required = true,
            content =
            @Content(
                    schema =
                    @Schema(
                            implementation =
                                    TokenApiResourceSwagger.PostTokenRequest.class)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content =
                    @Content(
                            schema =
                            @Schema(
                                    implementation =
                                            TokenApiResourceSwagger.PostTokenResponse.class))),
            @ApiResponse(responseCode = "400", description = "Unauthenticated. Please login")
    })
    public String refreshToken() {
        String token = ThreadLocalContextUtil.getAuthToken();
        if (token == null) {
            throw new IllegalArgumentException("Refresh token is null in request header /refreshtoken");
        }
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final AppUser appUser = (AppUser) authentication.getPrincipal();

        if (!validateRefreshToken(token, appUser)) {
            throw new AccessDeniedException("Token validation checks failed");
        }

        JwtTokenData accessTokenData = tokenProvider.generate(appUser);
        JwtTokenData refreshTokenData = tokenProvider.refreshToken(appUser, accessTokenData);
        appUser.setAccessTokenUuid(accessTokenData.getUuid());
        repository.save(appUser);
        return this.apiJsonSerializerService.serialize(
                new RefreshTokenResponse(
                        accessTokenData.getToken(),
                        refreshTokenData.getToken(),
                        accessTokenData.getExpireIn()));
    }

    private boolean validateRefreshToken(String token, AppUser user) {
        return tokenProvider.isRefreshToken(token) && tokenProvider.validateTokenUuid(token, user);
    }
}
