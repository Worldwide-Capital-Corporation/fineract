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

package org.apache.fineract.farmersbank.kyc.domain;

import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.farmersbank.kyc.data.response.ScanResponse;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.client.domain.Client;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Entity
@Table(name = "m_client_screening")
public class ClientScreening extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "scan_id")
    private Long scanId;

    @Column(name = "matched_number")
    private Long matchedNumber;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_match")
    private boolean isMatched;

    @Column(name = "is_verified_match")
    private boolean isVerifiedMatch;

    @Column(name = "is_pep")
    private boolean isPep;

    @Column(name = "is_sip")
    private boolean isSpecialInterestPerson;

    @Column(name = "is_sanctioned")
    private boolean isSanctioned;

    @Column(name = "is_involved_financial_crime")
    private boolean isInvolvedInFinancialCrime;

    @Column(name = "is_corrupt_bribery")
    private boolean isInvolvedInBriberyCorruption;

    @Column(name = "is_ter")
    private boolean isTerrorist;

    @Column(name = "is_rca")
    private boolean isRelativeAssociate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToMany(mappedBy="clientScreening", cascade = CascadeType.ALL)
    public Set<MatchedEntity> matchedEntities = new HashSet<>();

    @OneToMany(mappedBy="clientScreening", cascade = CascadeType.ALL)
    private Set<WebSearch> webSearchResults = new HashSet<>();


    public ClientScreening(
            Long scanId,
            Long matchedNumber,
            String imageUrl,
            boolean isMatched,
            boolean isVerifiedMatch,
            boolean isSanctioned,
            boolean isInvolvedInFinancialCrime,
            boolean isInvolvedInBriberyCorruption,
            boolean isPep,
            boolean isSpecialInterestPerson,
            boolean isTerrorist,
            boolean isRelativeAssociate
    ) {
        this.scanId = scanId;
        this.matchedNumber = matchedNumber;
        this.imageUrl = imageUrl;
        this.isMatched = isMatched;
        this.isSanctioned = isSanctioned;
        this.isInvolvedInFinancialCrime = isInvolvedInFinancialCrime;
        this.isInvolvedInBriberyCorruption = isInvolvedInBriberyCorruption;
        this.isVerifiedMatch = isVerifiedMatch;
        this.isPep = isPep;
        this.isSpecialInterestPerson = isSpecialInterestPerson;
        this.isTerrorist = isTerrorist;
        this.isRelativeAssociate = isRelativeAssociate;
    }

    protected ClientScreening() {}

    public static ClientScreening createNew(ScanResponse response) {
        ClientScreening screening = new ClientScreening(
                response.scanId,
                response.matchedNumber,
                response.getPictureImageUrl(),
                response.isExactMatch,
                false,
                response.isSanctioned(),
                response.isInvolvedInFinancialCrime(),
                response.isInvolvedInBriberyAndCorruption(),
                response.isPoliticalExposedPerson(),
                response.isSpecialInterestPerson(),
                response.isTerrorist(),
                response.isRelativeOrAssociate()
        );
        return screening;
    }

    public void addWebSearch(WebSearch webSearch) {
        webSearch.setClientScreening(this);
        webSearchResults.add(webSearch);
    }


    public static ClientScreening createFromNoMatch(ScanResponse response) {
        return new ClientScreening(
                response.scanId,
                response.matchedNumber,
                null,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        );
    }
}
