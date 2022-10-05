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

package org.apache.fineract.farmersbank.kyc.data.request;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class IndividualScanRequest {
    public String matchType;
    public Long closeMatchRateThreshold;
    public String whitelist;
    public String residence;
    public String blankAddress;
    public String pepJurisdiction;
    public String excludeDeceasedPersons;
    public String memberNumber;
    public String clientId;
    public String firstName;
    public String middleName;
    public String lastName;
    public String scriptNameFullName;
    public String gender;
    public String dob;
    public String address;
    public String includeResultEntities;
    public String updateMonitoringList;
    public String includeWebSearch;
    public IdvParam idvParam;

    public static IndividualScanRequest createNew(
            String firstName,
            String middleName,
            String lastName,
            String gender,
            String dob) {
        return new IndividualScanRequest(
                "Close",
                80L,
                "Apply",
                "Ignore",
                "ApplyResidenceCountry",
                "Apply",
                "No",
                "",
                "",
                firstName,
                middleName,
                lastName,
                "",
                gender,
                dob,
                "",
                "Yes",
                "No",
                "Yes",
                null
        );
    }
}

