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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.farmersbank.kyc.data.response.MatchedEntityResponse;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Entity
@Getter
@Setter
@Table(name = "m_client_screening_matched_entity")
public class MatchedEntity extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "result_id")
    private Long resultId;

    @Column(name = "unique_id")
    private Long uniqueId;

    @Column(name = "monitoring_status")
    private String monitoringStatus;

    @Column(name = "matched_fields")
    private String matchedFields;

    @Column(name = "category")
    private String category;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "matched_rate")
    private Long matchRate;

    @Column(name = "dob")
    private String dob;

    @Column(name = "primary_location")
    private String primaryLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties("matchedEntities")
    @JoinColumn(name = "screening_id")
    private ClientScreening clientScreening;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonIgnoreProperties("decisionDetail")
    @JoinColumn(name = "decision_detail_id")
    private DecisionDetail decisionDetail;

    @OneToOne(cascade = CascadeType.ALL)
    @JsonIgnoreProperties("resultEntity")
    @JoinColumn(name = "result_entity_id")
    private ScreeningResultEntity resultEntity;

    public MatchedEntity(
            Long resultId,
            Long uniqueId,
            String monitoringStatus,
            String matchedFields,
            String category,
            String firstName,
            String middleName,
            String lastName,
            Long matchRate,
            String dob,
            String primaryLocation) {
        this.resultId = resultId;
        this.uniqueId = uniqueId;
        this.monitoringStatus = monitoringStatus;
        this.matchedFields = matchedFields;
        this.category = category;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.matchRate = matchRate;
        this.dob = dob;
        this.primaryLocation = primaryLocation;
    }

    protected MatchedEntity() {}

    public static Set<MatchedEntity> createNew(ArrayList<MatchedEntityResponse> response, ClientScreening screening) {
        Set<MatchedEntity> matchedEntitySet = new HashSet<>();
        for (MatchedEntityResponse matchedEntityResponse : response) {
            MatchedEntity entity = new MatchedEntity(
                    matchedEntityResponse.resultId,
                    matchedEntityResponse.uniqueId,
                    matchedEntityResponse.monitoringStatus,
                    matchedEntityResponse.matchedFields,
                    matchedEntityResponse.category,
                    matchedEntityResponse.firstName,
                    matchedEntityResponse.middleName,
                    matchedEntityResponse.lastName,
                    matchedEntityResponse.matchRate,
                    matchedEntityResponse.dob,
                    matchedEntityResponse.primaryLocation
            );
            entity.setClientScreening(screening);
            if (matchedEntityResponse.decisionDetail != null)
                entity.setDecisionDetail(DecisionDetail.creatNewFrom(matchedEntityResponse.decisionDetail));
            if (matchedEntityResponse.resultEntity != null)
                entity.setResultEntity(ScreeningResultEntity.createNewFrom(matchedEntityResponse.resultEntity));
            matchedEntitySet.add(entity);
        }
        return matchedEntitySet;
    }

  public static MatchedEntity createNew(MatchedEntityResponse response, ClientScreening screening, ScreeningResultEntity resultEntity) {
        MatchedEntity entity =
            new MatchedEntity(
                response.resultId,
                response.uniqueId,
                response.monitoringStatus,
                response.matchedFields,
                response.category,
                response.firstName,
                response.middleName,
                response.lastName,
                response.matchRate,
                response.dob,
                response.primaryLocation);
        entity.setClientScreening(screening);
        entity.setResultEntity(resultEntity);
        return entity;
    }
}

