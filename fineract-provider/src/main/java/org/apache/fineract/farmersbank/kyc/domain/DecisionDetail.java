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
import org.apache.fineract.farmersbank.kyc.data.response.DecisionDetailResponse;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Slf4j
@Entity
@Table(name = "m_client_screening_decision_detail")
public class DecisionDetail extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "decision_text")
    public String text;
    @Column(name = "match_decision")
    public String matchDecision;
    @Column(name = "assessed_risk")
    public String assessedRisk;
    @Column(name = "comment")
    public String comment;

    protected DecisionDetail() {}

    public DecisionDetail(
            String text,
            String matchDecision,
            String assessedRisk,
            String comment) {
        this.text = text;
        this.matchDecision = matchDecision;
        this.assessedRisk = assessedRisk;
        this.comment = comment;
    }

    public static DecisionDetail creatNewFrom(DecisionDetailResponse response) {
        return new DecisionDetail(
                response.text,
                response.matchDecision,
                response.assessedRisk,
                response.comment
        );
    }
}
