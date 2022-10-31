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

package org.apache.fineract.portfolio.client.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.BeneficiaryOwner;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientBeneficiariesRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.handler.ClientBeneficiaryWritePlatformService;
import org.apache.fineract.portfolio.client.serialization.ClientFamilyMemberCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientBeneficiaryWritePlatformServiceImpl implements ClientBeneficiaryWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(ClientBeneficiaryWritePlatformServiceImpl.class);
    private final PlatformSecurityContext context;
    private final CodeValueRepository codeValueRepository;
    private final ClientBeneficiariesRepository clientBeneficiariesRepository;
    private final ClientRepositoryWrapper clientRepositoryWrapper;

    @Autowired
    public ClientBeneficiaryWritePlatformServiceImpl(final PlatformSecurityContext context,
                                                     final CodeValueRepository codeValueRepository,
                                                     final ClientBeneficiariesRepository clientBeneficiariesRepository,
                                                     final ClientRepositoryWrapper clientRepositoryWrapper,
                                                     final ClientFamilyMemberCommandFromApiJsonDeserializer apiJsonDeserializer) {
        this.context = context;
        this.codeValueRepository = codeValueRepository;
        this.clientBeneficiariesRepository = clientBeneficiariesRepository;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
    }

    @Override
    public CommandProcessingResult addBeneficiary(final Client client, final JsonCommand command) {

        Long sourceOfFundsId = null;
        CodeValue sourceOfFunds = null;
        CodeValue sourceOfWealth = null;
        Long sourceOfWealthId = null;
        Long stateId = null;
        CodeValue state = null;
        Long countryId = null;
        CodeValue country = null;
        String idNumber = "";
        String firstName = "";
        String middleName = "";
        String lastName = "";
        float ownership = 0;
        String tin = "";
        String street = null;
        String town = null;
        String city = null;
        String postalCode = null;

        this.context.authenticatedUser();

        JsonArray beneficiaries = command.arrayOfParameterNamed("beneficiaries");

        for (JsonElement beneficiary : beneficiaries) {

            JsonObject member = beneficiary.getAsJsonObject();

            if (member.get("idNumber") != null) {
                idNumber = member.get("idNumber").getAsString();
            }

            if (member.get("firstName") != null) {
                firstName = member.get("firstName").getAsString();
            }

            if (member.get("middleName") != null) {
                middleName = member.get("middleName").getAsString();
            }

            if (member.get("lastName") != null) {
                lastName = member.get("lastName").getAsString();
            }

            if (member.get("ownership") != null) {
                ownership = member.get("ownership").getAsFloat();
            }

            if (member.get("tin") != null) {
                tin = member.get("tin").getAsString();
            }

            if (!(member.get("street") instanceof JsonNull) && member.get("street") != null) {
                street = member.get("street").getAsString();
            }

            if (member.get("town") != null) {
                town = member.get("town").getAsString();
            }

            if (member.get("city") != null) {
                city = member.get("city").getAsString();
            }

            if (!(member.get("postalCode") instanceof JsonNull) && member.get("postalCode") != null) {
                postalCode = member.get("postalCode").getAsString();
            }

            if (member.get("sourceOfFundsId") != null) {
                sourceOfFundsId = member.get("sourceOfFundsId").getAsLong();
                sourceOfFunds = this.codeValueRepository.getReferenceById(sourceOfFundsId);
            }

            if (member.get("sourceOfWealthId") != null) {
                sourceOfWealthId = member.get("sourceOfWealthId").getAsLong();
                sourceOfWealth = this.codeValueRepository.getReferenceById(sourceOfWealthId);
            }

            if (member.get("stateId") != null) {
                stateId = member.get("stateId").getAsLong();
                state = this.codeValueRepository.getReferenceById(stateId);
            }

            if (member.get("countryId") != null) {
                countryId = member.get("countryId").getAsLong();
                country = this.codeValueRepository.getReferenceById(countryId);
            }

            BeneficiaryOwner beneficiaryOwner = BeneficiaryOwner.fromJson(
                    client,
                    sourceOfFunds,
                    sourceOfWealth,
                    state,
                    country,
                    idNumber,
                    firstName,
                    middleName,
                    lastName,
                    ownership,
                    tin,
                    street,
                    town,
                    city,
                    postalCode
                    );

            this.clientBeneficiariesRepository.saveAndFlush(beneficiaryOwner);
        }
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).build();
    }
}

