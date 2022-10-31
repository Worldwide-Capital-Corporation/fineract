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

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.fineract.farmersbank.kyc.data.shared.ClientBeneficiaryOwnerData;
import org.apache.fineract.farmersbank.kyc.service.ClientBeneficiariesReadPlatformService;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

@Path("/clients/{clientId}/beneficiaries")
@Component
@Scope("singleton")
@Tag(name = "Client Beneficiary Owners", description = "")
public class ClientBeneficiariesApiResources {

    private final String resourceNameForPermissions = "FamilyMembers";
    private final PlatformSecurityContext context;
    private final ClientBeneficiariesReadPlatformService readPlatformService;
    private final ToApiJsonSerializer<ClientBeneficiaryOwnerData> toApiJsonSerializer;

    @Autowired
    public ClientBeneficiariesApiResources(final PlatformSecurityContext context,
                                           final ClientBeneficiariesReadPlatformService readPlatformService,
                                           final ToApiJsonSerializer<ClientBeneficiaryOwnerData> toApiJsonSerializer
    ) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getFamilyMembers(@Context final UriInfo uriInfo, @PathParam("clientId") final long clientId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<ClientBeneficiaryOwnerData> beneficiaryOwners = this.readPlatformService.getBeneficiaryOwners(clientId);

        return this.toApiJsonSerializer.serialize(beneficiaryOwners);
    }

}

