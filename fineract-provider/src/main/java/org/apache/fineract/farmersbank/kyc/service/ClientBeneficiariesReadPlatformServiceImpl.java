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


package org.apache.fineract.farmersbank.kyc.service;

import org.apache.fineract.farmersbank.kyc.data.shared.ClientBeneficiaryOwnerData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Service
public class ClientBeneficiariesReadPlatformServiceImpl implements ClientBeneficiariesReadPlatformService {
    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    @Autowired
    public ClientBeneficiariesReadPlatformServiceImpl(final PlatformSecurityContext context, final JdbcTemplate jdbcTemplate,
                                                      final CodeValueReadPlatformService codeValueReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
        this.codeValueReadPlatformService = codeValueReadPlatformService;

    }

    private static final class ClientBeneficiariesMapper implements RowMapper<ClientBeneficiaryOwnerData> {

        public String schema() {
            return "fmb.id AS id, fmb.client_id AS clientId, fmb.first_name AS firstName, fmb.middle_name AS middleName,"
                    + "fmb.last_name AS lastName,fmb.id_number AS idNumber,fmb.ownership as ownership,fmb.tin as tin, cv.code_value AS sourceOfFunds,fmb.source_of_funds_code_id AS sourceOfFundsId,"
                    + "c.code_value AS sourceOfWealth,fmb.source_of_wealth_code_id AS sourceOfWealthId"
                    + " FROM m_client_entity_beneficiaries fmb" + " LEFT JOIN m_code_value cv ON fmb.source_of_funds_code_id=cv.id"
                    + " LEFT JOIN m_code_value c ON fmb.source_of_wealth_code_id=c.id";
        }

        @Override
        public ClientBeneficiaryOwnerData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final long id = rs.getLong("id");
            final long clientId = rs.getLong("clientId");
            final String firstName = rs.getString("firstName");
            final String middleName = rs.getString("middleName");
            final String lastName = rs.getString("lastName");
            final String idNumber = rs.getString("idNumber");
            final float ownership = rs.getFloat("ownership");
            final String tin = rs.getString("tin");
            final String sourceOfFunds = rs.getString("sourceOfFunds");
            final long sourceOfFundsId = rs.getLong("sourceOfFundsId");
            final String sourceOfWealth = rs.getString("sourceOfWealth");
            final long sourceOfWealthId = rs.getLong("sourceOfWealthId");

            return new ClientBeneficiaryOwnerData(
                    id,
                    clientId,
                    firstName,
                    middleName,
                    lastName,
                    idNumber,
                    ownership,
                    tin,
                    sourceOfFunds,
                    sourceOfFundsId,
                    sourceOfWealth,
                    sourceOfWealthId
            );

        }
    }

    @Override
    public Collection<ClientBeneficiaryOwnerData> getBeneficiaryOwners(long clientId) {

        this.context.authenticatedUser();

        final ClientBeneficiariesReadPlatformServiceImpl.ClientBeneficiariesMapper rm = new ClientBeneficiariesReadPlatformServiceImpl.ClientBeneficiariesMapper();
        final String sql = "select " + rm.schema() + " where fmb.client_id=?";

        return this.jdbcTemplate.query(sql, rm, clientId); // NOSONAR
    }

}
