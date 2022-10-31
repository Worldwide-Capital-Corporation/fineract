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

package org.apache.fineract.farmersbank.kyc.data.shared;

public class ClientBeneficiaryOwnerData {
    private long id;
    private long clientId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String idNumber;
    private float ownership;
    private String tin;
    private String sourceOfFunds;
    private long sourceOfFundsId;
    private String sourceOfWealth;
    private long sourceOfWealthId;

    public ClientBeneficiaryOwnerData(long id, long clientId, String firstName, String middleName, String lastName, String idNumber, float ownership, String tin, String sourceOfFunds, long sourceOfFundsId, String sourceOfWealth, long sourceOfWealthId) {
        this.id = id;
        this.clientId = clientId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.idNumber = idNumber;
        this.ownership = ownership;
        this.tin = tin;
        this.sourceOfFunds = sourceOfFunds;
        this.sourceOfFundsId = sourceOfFundsId;
        this.sourceOfWealth = sourceOfWealth;
        this.sourceOfWealthId = sourceOfWealthId;
    }
}
