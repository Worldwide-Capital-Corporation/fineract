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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.fineract.farmersbank.kyc.data.request.IndividualScanRequest;
import org.apache.fineract.farmersbank.kyc.data.response.ScanResponse;
import org.apache.fineract.farmersbank.kyc.domain.ClientScreening;
import org.apache.fineract.farmersbank.kyc.service.MemberCheckScanService;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Component
@Scope("singleton")
@Path("/screening")
@Tag(
        name = "Scan customer PEP & Sanctions status",
        description =
                "An API capability that allows bank to verify if customers are PEP and are not sanctioned.")
public class KycScreeningApiResource {

    private final ToApiJsonSerializer<ClientScreening> apiJsonSerializerService;
    private final MemberCheckScanService kycService;
    private final ClientRepository clientRepository;


    @Autowired
    public KycScreeningApiResource(final ToApiJsonSerializer<ClientScreening> apiJsonSerializerService,
                                   final MemberCheckScanService kycService,
                                   final ClientRepository clientRepository) {
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.kycService = kycService;
        this.clientRepository = clientRepository;
    }

    @POST
    @Path("{clientId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Scan an individual PEP and sanction status",
            description =
                    "Perform a customer check to verify if a customer is a PEP or is sanctioned.")
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
                    responseCode = "201",
                    description = "OK",
                    content =
                    @Content(
                            schema =
                            @Schema(
                                    implementation =
                                            ScanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Unauthenticated. Please login")
    })
    public String individualScan(@Parameter(description = "clientId") @PathParam("clientId") final Long clientId) throws IOException {
        if (clientId == null) {
            throw new IllegalArgumentException(
                    "clientId parameter required");
        }

        ClientScreening response = kycService.kycScreening(clientId);
        return this.apiJsonSerializerService.serialize(response);
    }

}
