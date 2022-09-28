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
import org.apache.fineract.farmersbank.kyc.data.response.LinkedCompanyResponse;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Entity
@Getter
@Setter
@Table(name = "m_client_screening_linked_company")
public class LinkedCompany extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "linked_company_id")
    public Long linkedCompanyId;

    @Column(name = "name")
    public String name;

    @Column(name = "category")
    public String category;

    @Column(name = "sub_categories")
    public String subcategories;

    @Column(name = "description")
    public String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "result_entity_id", nullable = false)
    private ScreeningResultEntity resultEntity;

    public LinkedCompany(Long linkedCompanyId, String name, String category, String subcategories, String description) {
        this.linkedCompanyId = linkedCompanyId;
        this.name = name;
        this.category = category;
        this.subcategories = subcategories;
        this.description = description;
    }

    protected LinkedCompany() {}

    public static Set<LinkedCompany> createNew(ArrayList<LinkedCompanyResponse> response, ScreeningResultEntity resultEntity) {
        Set<LinkedCompany> linkedCompanySet = new HashSet<>();
        for (LinkedCompanyResponse linkedCompany : response) {
            LinkedCompany entity = new LinkedCompany(
                    linkedCompany.id,
                    linkedCompany.name,
                    linkedCompany.category,
                    linkedCompany.subcategories,
                    linkedCompany.description
            );
            entity.setResultEntity(resultEntity);
            linkedCompanySet.add(entity);
        }
        return linkedCompanySet;
    }

}
