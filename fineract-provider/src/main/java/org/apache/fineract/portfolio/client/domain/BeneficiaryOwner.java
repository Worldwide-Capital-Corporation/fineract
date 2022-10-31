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


package org.apache.fineract.portfolio.client.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Setter
@Getter
@Table(name = "m_client_entity_beneficiaries")
public class BeneficiaryOwner extends AbstractAuditableWithUTCDateTimeCustom {

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "id_number")
    private String idNumber;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "ownership")
    private float ownership;

    @Column(name = "tin")
    private String tin;

    @ManyToOne
    @JoinColumn(name = "source_of_funds_code_id")
    private CodeValue sourceOfFunds;

    @ManyToOne
    @JoinColumn(name = "source_of_wealth_code_id")
    private CodeValue sourceOfWealth;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private CodeValue country;

    @ManyToOne
    @JoinColumn(name = "state_id")
    private CodeValue state;

    @Column(name = "street")
    private String street;

    @Column(name = "town")
    private String town;

    @Column(name = "city")
    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    public BeneficiaryOwner(
            Client client,
            String idNumber,
            String firstName,
            String middleName,
            String lastName,
            float ownership,
            String tin,
            CodeValue sourceOfFunds,
            CodeValue sourceOfWealth,
            CodeValue country,
            CodeValue state,
            String street,
            String town,
            String city,
            String postalCode) {
        this.client = client;
        this.idNumber = idNumber;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.ownership = ownership;
        this.tin = tin;
        this.sourceOfFunds = sourceOfFunds;
        this.sourceOfWealth = sourceOfWealth;
        this.country = country;
        this.state = state;
        this.street = street;
        this.town = town;
        this.city = city;
        this.postalCode = postalCode;
    }

    protected BeneficiaryOwner() {}

    public static BeneficiaryOwner fromJson(
            final Client client,
            final CodeValue sourceOfFunds,
            final CodeValue sourceOfWealth,
            final CodeValue state,
            final CodeValue country,
            final String idNumber,
            final String firstName,
            final String middleName,
            final String lastName,
            final float ownership,
            final String tin,
            final String street,
            final String town,
            final String city,
            final String postalCode) {
        return new BeneficiaryOwner(client,
                idNumber,
                firstName,
                middleName,
                lastName,
                ownership,
                tin,
                sourceOfFunds,
                sourceOfWealth,
                country,
                state,
                street,
                town,
                city,
                postalCode
        );
    }
}
