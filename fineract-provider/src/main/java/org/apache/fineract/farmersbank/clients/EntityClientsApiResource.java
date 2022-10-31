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

package org.apache.fineract.farmersbank.clients;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.farmersbank.kyc.data.shared.ClientIdentifier;
import org.apache.fineract.farmersbank.kyc.service.MemberCheckScanService;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.data.ClientIdentifierData;
import org.apache.fineract.portfolio.client.service.ClientIdentifierReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.UUID;

@Path("/clients/entity")
@Component
@Scope("singleton")
@Tag(name = "Client", description = "Clients are people and businesses that have applied (or may apply) to an MFI for loans.\n" + "\n"
        + "Clients can be created in Pending or straight into Active state.")
@RequiredArgsConstructor
public class EntityClientsApiResource {

        private final ToApiJsonSerializer<ClientData> toApiJsonSerializer;
        private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
        private final MemberCheckScanService kycScreeningService;
        private final ClientIdentifierReadPlatformService clientIdentifierReadPlatformService;
        private final ConfigurationReadPlatformService configurationReadPlatformService;
        private final CodeValueReadPlatformService codeValueReadPlatformService;
        private final ToApiJsonSerializer<ClientIdentifier> clientIdentifierToApiJsonSerializer;

        private static final Logger logger
                = LoggerFactory.getLogger(EntityClientsApiResource.class);


        @POST
        @Consumes({ MediaType.APPLICATION_JSON })
        @Produces({ MediaType.APPLICATION_JSON })
        @Operation(summary = "Create a Client", description = "Note:\n\n"
                + "1. You can enter fullname - for a business or organisation (or person known by one name).\n"
                + "\n" + "2.Additional field called address has to be passed.\n\n"
                + "\n" + "3.Additional field called beneficiary owners has to be passed.")
        @RequestBody(required = true, content = @Content(schema = @Schema(implementation = EntityClientsApiResourceSwagger.PostClientsRequest.class)))
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = EntityClientsApiResourceSwagger.PostClientsResponse.class))) })
        public String create(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

            // create client
            // add address
            // add beneficiary owners

            final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                    .createClient() //
                    .withJson(apiRequestBodyAsJson) //
                    .build(); //

            final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            addClientDocuments(result);
            final GlobalConfigurationPropertyData configuration = this.configurationReadPlatformService
                    .retrieveGlobalConfiguration("enable-kyc-screening");
            if (configuration.isEnabled()) {
                try {
                    kycScreeningService.kycScreening(result.getClientId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            return this.toApiJsonSerializer.serialize(result);
        }

        private void addClientDocuments(final CommandProcessingResult result) {
            final Collection<CodeValueData> codeValues = this.codeValueReadPlatformService.retrieveCodeValuesByCode("Entity Identifier");
            final ClientIdentifierData clientIdentifierData = ClientIdentifierData.template(codeValues);
            for (CodeValueData data : clientIdentifierData.getAllowedDocumentTypes()) {
                String identifierJson = clientIdentifierToApiJsonSerializer.serialize(new ClientIdentifier(
                        data.getId(),
                        "Pending",
                        UUID.randomUUID().toString()
                ));
                final CommandWrapper command = new CommandWrapperBuilder().createClientIdentifier(result.getClientId())
                        .withJson(identifierJson).build();
                this.commandsSourceWritePlatformService.logCommandSource(command);
            }
        }
}
