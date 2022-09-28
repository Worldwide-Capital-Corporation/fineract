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
import org.apache.fineract.farmersbank.kyc.data.response.DescriptionResponse;
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
@Table(name = "m_client_screening_description")
public class Description extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "description1")
    private String description1;

    @Column(name = "description2")
    private String description2;
    
    @Column(name = "description3")
    private String description3;

    @ManyToOne(optional = false)
    @JoinColumn(name = "result_entity_id", nullable = false)
    private ScreeningResultEntity resultEntity;

    public Description(
      String description1,
      String description2,
      String description3) {
        this.description1 = description1;
        this.description2 = description2;
        this.description3 = description3;
    }

    public static Set<Description> createNew(ArrayList<DescriptionResponse> response, ScreeningResultEntity resultEntity) {
        Set<Description> descriptionSet = new HashSet<>();
        for(DescriptionResponse description: response) {
            Description entity = new Description(
                    description.description1,
                    description.description2,
                    description.description3);
            entity.setResultEntity(resultEntity);
            descriptionSet.add(entity);
        }
        return descriptionSet;
    }

    protected Description() {}
}
