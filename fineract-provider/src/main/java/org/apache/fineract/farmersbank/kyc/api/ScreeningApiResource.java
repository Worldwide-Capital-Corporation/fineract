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

package org.apache.fineract.farmersbank.kyc.api;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.fineract.farmersbank.kyc.data.request.IndividualScanRequest;
import org.apache.fineract.farmersbank.kyc.data.response.ClientRiskRating;
import org.apache.fineract.farmersbank.kyc.data.response.ScanResponse;
import org.apache.fineract.farmersbank.kyc.service.MemberCheckScanService;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Component
@Scope("singleton")
@Path("/screening/result")
@Tag(
        name = "Scan customer PEP & Sanctions status",
        description =
                "An API capability that allows bank to verify if customers are PEP and are not sanctioned.")
public class ScreeningApiResource {

    private final ToApiJsonSerializer<ClientRiskRating> apiJsonSerializerService;
    private final MemberCheckScanService kycService;
    private final PlatformSecurityContext context;


    @Autowired
    public ScreeningApiResource(final ToApiJsonSerializer<ClientRiskRating> apiJsonSerializerService,
                                final MemberCheckScanService kycService,
                                final PlatformSecurityContext context) {
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.kycService = kycService;
        this.context = context;
    }

    @GET
    @Path("{clientId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Customer screening result",
            description =
                    "Get the latest customer screening result")
    @RequestBody(
            required = true,
            content =
            @Content(
                    schema =
                    @Schema(
                            implementation =
                                    IndividualScanRequest.class)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content =
                    @Content(
                            schema =
                            @Schema(
                                    implementation =
                                            ScanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Unauthenticated. Please login")
    })
    public String customerKycScreening(@Parameter(description = "clientId") @PathParam("clientId") final Long clientId) throws IOException {
        if (clientId == null) {
            throw new IllegalArgumentException(
                    "client id parameter required");
        }
        ClientRiskRating response = kycService.getScreeningHistory(clientId, 1);
        return this.apiJsonSerializerService.serialize(response);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Customer screening result",
            description =
                    "Mark matched result verified match")
    @RequestBody(
            required = true,
            content =
            @Content(
                    schema =
                    @Schema(
                            implementation =
                                    IndividualScanRequest.class)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content =
                    @Content(
                            schema =
                            @Schema(
                                    implementation =
                                            ScanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Unauthenticated. Please login")
    })
    public String markVerified(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        VerifyScreening request =
                new Gson().fromJson(apiRequestBodyAsJson, VerifyScreening.class);
        if (request == null) {
            throw new IllegalArgumentException(
                    "Invalid JSON in BODY  of POST to /screening/result");
        }
        if (request.matchId == 0 || request.clientId == 0 || request.screeningId == 0) {
            throw new IllegalArgumentException(
                    "matchId or clientId or screeningId is null in JSON of POST to /screening/result");
        }
        if (!this.context.authenticatedUser().hasSpecificPermissionTo(ClientApiConstants.RUN_KYC_SCREENING)) {
            final String authorizationMessage = "User has no authority to " + ClientApiConstants.RUN_KYC_SCREENING.toLowerCase() + "s";
            throw new NoAuthorizationException(authorizationMessage);
        }

        ClientRiskRating response = kycService.markVerifiedMatch(request);
        return this.apiJsonSerializerService.serialize(response);
    }

    public static class VerifyScreening {
        public long matchId;
        public long screeningId;
        public long clientId;
    }
}
