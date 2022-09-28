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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.farmersbank.kyc.data.response.ScanRoleResponse;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Entity
@Setter
@Getter
@Table(name = "m_client_screening_job_history")
public class ScreeningJobHistory  extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "title")
    private String title;
    @Column(name = "type")
    private String type;
    @Column(name = "status")
    private String status;
    @Column(name = "country")
    private String country;
    @Column(name = "from")
    private String from;
    @Column(name = "to")
    private String to;

    @ManyToOne
    @PrimaryKeyJoinColumn(name = "result_entity_id")
    private ScreeningResultEntity resultEntity;

    public ScreeningJobHistory(
            String title,
            String type,
            String status,
            String country,
            String from,
            String to) {
        this.title = title;
        this.type = type;
        this.status = status;
        this.country = country;
        this.from = from;
        this.to = to;
    }

    public static Set<ScreeningJobHistory> createNew(ArrayList<ScanRoleResponse> response, ScreeningResultEntity resultEntity) {
        Set<ScreeningJobHistory> rolesSet = new HashSet<>();
        for (ScanRoleResponse role: response){
            ScreeningJobHistory entity = new ScreeningJobHistory(
                    role.title,
                    role.type,
                    role.status,
                    role.country,
                    role.from,
                    role.to
            );
            entity.setResultEntity(resultEntity);
            rolesSet.add(entity);
        }
        return rolesSet;
    }

    protected ScreeningJobHistory() {}
}

