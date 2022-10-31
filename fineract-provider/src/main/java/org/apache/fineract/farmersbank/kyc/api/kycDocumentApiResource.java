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
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.apache.fineract.infrastructure.core.exception.ResourceNotFoundException;
import org.apache.fineract.portfolio.client.api.ClientIdentifiersApiResource;
import org.apache.fineract.portfolio.client.domain.ClientIdentifier;
import org.apache.fineract.portfolio.client.domain.ClientIdentifierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Component
@Scope("singleton")
@Path("/screening/kyc-document")
@Tag(
        name = "Update KYC document status",
        description =
                "An API capability that allows bank to update details about customer kyc documents.")
public class kycDocumentApiResource {

    private final ConfigurationReadPlatformService configurationReadPlatformService;
    private final ClientIdentifierRepository clientIdentifierRepository;
    private final ClientIdentifiersApiResource clientIdentifiersApiResource;

    @Autowired
    public kycDocumentApiResource(final ConfigurationReadPlatformService configurationReadPlatformService,
                                  final ClientIdentifierRepository clientIdentifierRepository,
                                  final ClientIdentifiersApiResource clientIdentifiersApiResource) {
        this.configurationReadPlatformService = configurationReadPlatformService;
        this.clientIdentifierRepository = clientIdentifierRepository;
        this.clientIdentifiersApiResource = clientIdentifiersApiResource;
    }

    @POST
    @Path("{identifierId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Update customer kyc document",
            description =
                    "Update customer document status after uploading a new document")
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
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public String updateIdentifierDocument(@Context final UriInfo uriInfo, @Parameter(description = "identifierId") @PathParam("identifierId") final Long identifierId) {

        if (identifierId == null) {
            throw new IllegalArgumentException(
                    "Identifier id of POST to /screening/kyc-document is null");
        }
        final ClientIdentifier clientIdentifier =
            this.clientIdentifierRepository
                .findById(identifierId)
                .orElseThrow(() -> new ResourceNotFoundException("Client identifier does not exist"));
        final GlobalConfigurationPropertyData configuration = this.configurationReadPlatformService
                .retrieveGlobalConfiguration("kyc-documents-validity-period");
        clientIdentifier.setUploadedDate(OffsetDateTime.now(ZoneId.of("Africa/Harare")));
        if (clientIdentifier.getDocumentType().getCodeScore() != 1) {
          clientIdentifier.setExpiryDate(
              OffsetDateTime.now(ZoneId.of("Africa/Harare")).plusMonths(configuration.getValue()));
        }
        clientIdentifier.setStatus(200);
        this.clientIdentifierRepository.saveAndFlush(clientIdentifier);
        return clientIdentifiersApiResource.retrieveClientIdentifiers(clientIdentifier.getClient().getId(), clientIdentifier.getId(), uriInfo);
    }
}
