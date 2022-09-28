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
import org.apache.fineract.farmersbank.kyc.data.response.LinkedIndividualResponse;
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
@Table(name = "m_client_screening_linked_individual")
public class LinkedIndividual extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "linked_individual_id")
    private Long linkedIndividualId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "category")
    private String category;

    @Column(name = "sub_categories")
    private String subcategories;

    @Column(name = "description")
    private String description;

    @Column(name = "monitored_old_entity_id")
    private Long monitoredOldEntityId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "result_entity_id", nullable = false)
    private ScreeningResultEntity resultEntity;

  public LinkedIndividual(
      Long linkedIndividualId,
      String firstName,
      String middleName,
      String lastName,
      String category,
      String subcategories,
      String description) {
        this.linkedIndividualId = linkedIndividualId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.category = category;
        this.subcategories = subcategories;
        this.description = description;
  }

    public static Set<LinkedIndividual> createNew(ArrayList<LinkedIndividualResponse> response, ScreeningResultEntity resultEntity) {
        Set<LinkedIndividual> linkedIndividualSet = new HashSet<>();
        for (LinkedIndividualResponse linkedIndividual : response) {
            LinkedIndividual entity = new LinkedIndividual(
                    linkedIndividual.id,
                    linkedIndividual.firstName,
                    linkedIndividual.middleName,
                    linkedIndividual.lastName,
                    linkedIndividual.category,
                    linkedIndividual.subcategories,
                    linkedIndividual.description
            );
            entity.setResultEntity(resultEntity);
            linkedIndividualSet.add(entity);
        }
        return linkedIndividualSet;
    }

    protected LinkedIndividual() {}
}
