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
import org.apache.fineract.farmersbank.kyc.data.response.IdNumberResponse;
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
@Getter
@Setter
@Table(name = "m_client_screening_id_numbers")
public class ScreeningIdNumbers extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "type")
    private String type;

    @Column(name = "id_notes")
    private String idNotes;

    @Column(name = "number")
    private String number;

    @ManyToOne
    @PrimaryKeyJoinColumn(name = "result_entity_id")
    private ScreeningResultEntity resultEntity;

    public ScreeningIdNumbers(String type, String idNotes, String number) {
        this.type = type;
        this.idNotes = idNotes;
        this.number = number;
    }

    public static Set<ScreeningIdNumbers> createNew(ArrayList<IdNumberResponse> response, ScreeningResultEntity resultEntity) {
        Set<ScreeningIdNumbers> idNumbersSet = new HashSet<>();
        for (IdNumberResponse idNumber: response){
            ScreeningIdNumbers entity = new ScreeningIdNumbers(
                    idNumber.type,
                    idNumber.idNotes,
                    idNumber.number
            );
            entity.setResultEntity(resultEntity);
            idNumbersSet.add(entity);
        }
        return idNumbersSet;
    }

    protected ScreeningIdNumbers() {}
}
