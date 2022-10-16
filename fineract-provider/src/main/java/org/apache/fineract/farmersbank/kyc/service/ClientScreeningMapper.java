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

import org.apache.fineract.farmersbank.kyc.data.response.ClientKycScreeningData;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public final class ClientScreeningMapper implements RowMapper<ClientKycScreeningData> {

    public String schema() {
        return "cl.legal_form_enum as clientType, cs.id as id, cs.client_id as clientId, cs.matched_number as matches, cs.image_url as imageUrl, cs.is_verified_match as isVerifiedMatch, cs.is_pep AS isPep, cs.is_sip AS isSip, cs.is_sanctioned AS isSanctioned, cs.is_involved_financial_crime AS financialCrime,"
                + "cs.is_corrupt_bribery AS briberyAndCorrupt, cs.is_rca AS isRca, cs.is_ter as isTerrorist, cs.risk_rating as riskRating, cs.created_on_utc as screeningDate"
                + " FROM m_client_screening cs join m_client cl on cl.id = cs.client_id";
    }

    @Override
    public ClientKycScreeningData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
        final long id = rs.getLong("id");
        final long clientId = rs.getLong("clientId");
        final long matches = rs.getLong("matches");
        final long clientType = rs.getLong("clientType");
        final boolean isPep = rs.getBoolean("isPep");
        final boolean isSip = rs.getBoolean("isSip");
        final boolean isSanctioned = rs.getBoolean("isSanctioned");
        final boolean financialCrime = rs.getBoolean("financialCrime");
        final boolean briberyAndCorrupt = rs.getBoolean("briberyAndCorrupt");
        final boolean isVerifiedMatch = rs.getBoolean("isVerifiedMatch");
        final boolean isRca = rs.getBoolean("isRca");
        final boolean isTerrorist = rs.getBoolean("isTerrorist");
        final String riskRating = rs.getString("riskRating");
        final String imageUrl = rs.getString("imageUrl");
        final Timestamp screeningDate = rs.getTimestamp("screeningDate");

        return new ClientKycScreeningData(
                id,
                clientId,
                clientType,
                isPep,
                isSip,
                isSanctioned,
                financialCrime,
                briberyAndCorrupt,
                isRca,
                isTerrorist,
                riskRating,
                screeningDate.getTime(),
                matches,
                isVerifiedMatch,
                imageUrl
        );
    }
}
