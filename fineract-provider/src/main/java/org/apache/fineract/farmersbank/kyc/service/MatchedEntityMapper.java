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

import org.apache.fineract.farmersbank.kyc.data.response.MatchedEntityData;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class MatchedEntityMapper implements RowMapper<MatchedEntityData> {

  public String schema() {
    return "me.id as id,"+
            "me.screening_id as screeningId,"+
            "me.result_entity_id as resultEntityId," +
            "me.result_id as resultId," +
            "me.unique_id as uniqueId," +
            "me.monitoring_status as monitoringStatus," +
            "me.matched_fields as matchedFields," +
            "me.category AS category," +
            "me.first_name AS firstName," +
            "me.middle_name AS middleName," +
            "me.last_name AS lastName," +
            "me.matched_rate AS matchRate," +
            "me.dob AS dob," +
            "me.primary_location as primaryLocation," +
            "me.is_verified_match as isVerifiedMatch," +
            "re.categories as categories," +
            "re.gender as gender," +
            "re.primary_location as location," +
            "re.image as imageUrl" +
            " FROM m_client_screening_matched_entity me JOIN m_client_screening_result_entity re ON me.result_entity_id = re.id";
  }

    public String update() {
        return "m_client_screening_matched_entity";
    }

    @Override
    public MatchedEntityData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
        final long id = rs.getLong("id");
        final long screeningId = rs.getLong("screeningId");
        final long resultEntityId = rs.getLong("resultEntityId");
        final long resultId = rs.getLong("resultId");
        final long uniqueId = rs.getLong("uniqueId");
        final String monitoringStatus = rs.getString("monitoringStatus");
        final String matchedFields = rs.getString("matchedFields");
        final String category = rs.getString("category");
        final String firstName = rs.getString("firstName");
        final String middleName = rs.getString("middleName");
        final String lastName = rs.getString("lastName");
        final long matchRate = rs.getLong("matchRate");
        final String dob = rs.getString("dob");
        final String primaryLocation = rs.getString("primaryLocation");
        final String categories = rs.getString("categories");
        final String gender = rs.getString("gender");
        final String location = rs.getString("location");
        final String imageUrl = rs.getString("imageUrl");
        final boolean isVerifiedMatch = rs.getBoolean("isVerifiedMatch");
        return new MatchedEntityData(
                id,
                screeningId,
                resultEntityId,
                resultId,
                uniqueId,
                monitoringStatus,
                matchedFields,
                category,
                firstName,
                middleName,
                lastName,
                matchRate,
                dob,
                primaryLocation,
                categories,
                gender,
                location,
                imageUrl,
                isVerifiedMatch
        );
    }
}
