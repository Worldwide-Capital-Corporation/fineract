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
import org.apache.fineract.farmersbank.kyc.data.response.GeneralInfoResponse;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Slf4j
@Entity
@Table(name = "m_client_screening_general_info")
public class ScreeningGeneralInfo extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "additional_prop1")
    private String additionalProp1;

    @Column(name = "additional_prop2")
    private String additionalProp2;

    @Column(name = "additional_prop3")
    private String additionalProp3;

    public ScreeningGeneralInfo(String additionalProp1, String additionalProp2, String additionalProp3) {
        this.additionalProp1 = additionalProp1;
        this.additionalProp2 = additionalProp2;
        this.additionalProp3 = additionalProp3;
    }

    public static ScreeningGeneralInfo createNew(GeneralInfoResponse response) {
        return new ScreeningGeneralInfo(response.additionalProp1, response.additionalProp2, response.additionalProp3);
    }

    protected ScreeningGeneralInfo() {}
}
