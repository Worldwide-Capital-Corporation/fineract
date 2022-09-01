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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.fineract.farmersbank.security.data.FBAuthenticatedUserData;
import org.apache.fineract.farmersbank.security.data.FBJwtTokenData;
import org.apache.fineract.farmersbank.service.JwtUtil;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.api.AuthenticationApiResourceSwagger;
import org.apache.fineract.infrastructure.security.constants.TwoFactorConstants;
import org.apache.fineract.infrastructure.security.data.AuthenticatedUserData;
import org.apache.fineract.infrastructure.security.service.SpringSecurityPlatformSecurityContext;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.useradministration.data.RoleData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.apache.fineract.useradministration.domain.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
@Path("/authentication")
@Tag(
    name = "Authentication HTTP Basic",
    description =
        "An API capability that allows client applications to verify authentication details using HTTP Basic Authentication.")
public class AuthenticationApiResource {

  @Value("${fineract.security.2fa.enabled}")
  private boolean twoFactorEnabled;

  public static class AuthenticateRequest {

    public String username;
    public String password;
  }

  private final DaoAuthenticationProvider customAuthenticationProvider;
  private final ToApiJsonSerializer<AuthenticatedUserData> apiJsonSerializerService;
  private final SpringSecurityPlatformSecurityContext springSecurityPlatformSecurityContext;
  private final ClientReadPlatformService clientReadPlatformService;
  private final AppUserRepository repository;
  private final JwtUtil jwtUtil;

  @Autowired
  public AuthenticationApiResource(
      @Qualifier("customAuthenticationProvider")
          final DaoAuthenticationProvider customAuthenticationProvider,
      final ToApiJsonSerializer<AuthenticatedUserData> apiJsonSerializerService,
      final SpringSecurityPlatformSecurityContext springSecurityPlatformSecurityContext,
      final AppUserRepository repository,
      final JwtUtil jwtUtil,
      ClientReadPlatformService aClientReadPlatformService) {
    this.customAuthenticationProvider = customAuthenticationProvider;
    this.apiJsonSerializerService = apiJsonSerializerService;
    this.springSecurityPlatformSecurityContext = springSecurityPlatformSecurityContext;
    this.repository = repository;
    this.jwtUtil = jwtUtil;
    clientReadPlatformService = aClientReadPlatformService;
  }

  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(
      summary = "Verify authentication",
      description =
          "Authenticates the credentials provided and returns the set roles and permissions allowed.")
  @RequestBody(
      required = true,
      content =
          @Content(
              schema =
                  @Schema(
                      implementation =
                          AuthenticationApiResourceSwagger.PostAuthenticationRequest.class)))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content =
            @Content(
                schema =
                    @Schema(
                        implementation =
                            AuthenticationApiResourceSwagger.PostAuthenticationResponse.class))),
    @ApiResponse(responseCode = "400", description = "Unauthenticated. Please login")
  })
  public String authenticate(
      @Parameter(hidden = true) final String apiRequestBodyAsJson,
      @QueryParam("returnClientList") @DefaultValue("false") boolean returnClientList) {
    AuthenticateRequest request =
        new Gson().fromJson(apiRequestBodyAsJson, AuthenticateRequest.class);
    if (request == null) {
      throw new IllegalArgumentException(
          "Invalid JSON in BODY  of POST to /authentication");
    }
    if (request.username == null || request.password == null) {
      throw new IllegalArgumentException(
          "Username or Password is null in JSON of POST to /authentication");
    }

    final Authentication authentication =
        new UsernamePasswordAuthenticationToken(request.username, request.password);
    final Authentication authenticationCheck =
        this.customAuthenticationProvider.authenticate(authentication);

    final Collection<String> permissions = new ArrayList<>();
    FBAuthenticatedUserData authenticatedUserData =
        new FBAuthenticatedUserData(request.username, permissions);

    if (authenticationCheck.isAuthenticated()) {
      final Collection<GrantedAuthority> authorities =
          new ArrayList<>(authenticationCheck.getAuthorities());
      for (final GrantedAuthority grantedAuthority : authorities) {
        permissions.add(grantedAuthority.getAuthority());
      }

      final AppUser principal = (AppUser) authenticationCheck.getPrincipal();
      FBJwtTokenData accessTokenData = jwtUtil.generate(principal);
      FBJwtTokenData refreshTokenData = jwtUtil.refreshToken(principal, accessTokenData);
      final Collection<RoleData> roles = new ArrayList<>();
      final Set<Role> userRoles = principal.getRoles();
      for (final Role role : userRoles) {
        roles.add(role.toData());
      }

      final Long officeId = principal.getOffice().getId();
      final String officeName = principal.getOffice().getName();

      final Long staffId = principal.getStaffId();
      final String staffDisplayName = principal.getStaffDisplayName();

      final EnumOptionData organisationalRole = principal.organisationalRoleData();

      boolean isTwoFactorRequired =
          this.twoFactorEnabled
              && !principal.hasSpecificPermissionTo(
                  TwoFactorConstants.BYPASS_TWO_FACTOR_PERMISSION);
      Long userId = principal.getId();
      principal.setAccessTokenUuid(accessTokenData.getUuid());
      repository.save(principal);
      if (this.springSecurityPlatformSecurityContext.doesPasswordHasToBeRenewed(principal)) {
        authenticatedUserData =
            new FBAuthenticatedUserData(
                request.username,
                userId,
                accessTokenData.getToken(),
                refreshTokenData.getToken(),
                accessTokenData.getExpireIn(),
                refreshTokenData.getExpireIn(),
                isTwoFactorRequired);
      } else {

        authenticatedUserData =
            new FBAuthenticatedUserData(
                request.username,
                officeId,
                officeName,
                staffId,
                staffDisplayName,
                organisationalRole,
                roles,
                permissions,
                principal.getId(),
                accessTokenData.getToken(),
                refreshTokenData.getToken(),
                accessTokenData.getExpireIn(),
                refreshTokenData.getExpireIn(),
                isTwoFactorRequired,
                returnClientList ? clientReadPlatformService.retrieveUserClients(userId) : null);
      }
    }

    return this.apiJsonSerializerService.serialize(authenticatedUserData);
  }
}
