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


package org.apache.fineract.farmersbank.clients;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

public class EntityClientsApiResourceSwagger {

    private EntityClientsApiResourceSwagger() {}

    @Schema(description = "PostClientsRequest")
    public static final class PostClientsRequest {

        private PostClientsRequest() {}

        static final class PostClientsDatatable {

            private PostClientsDatatable() {}

            @Schema(example = "Client Beneficiary information")
            public String registeredTableName;
            @Schema(example = "data")
            public HashMap<String, Object> data;

        }

        static final class PostClientsAddressRequest {

            @Schema(example = "Ipca")
            public String street;
            @Schema(example = "Kandivali")
            public String addressLine1;
            @Schema(example = "plot47")
            public String addressLine2;
            @Schema(example = "charkop")
            public String addressLine3;
            @Schema(example = "Mumbai")
            public String city;
            @Schema(example = "800")
            public Integer stateProvinceId;
            @Schema(example = "802")
            public Integer countryId;
            @Schema(example = "400064")
            public Long postalCode;
            @Schema(example = "1")
            public Long addressTypeId;
            @Schema(example = "true")
            public Boolean isActive;
        }

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer legalFormId;
        @Schema(example = "Client of group")
        public String fullname;
        @Schema(example = "Client_FirstName")
        public String firstname;
        @Schema(example = "123")
        public String externalId;
        @Schema(example = "Client_LastName")
        public String lastname;
        @Schema(example = "[2013, 1, 1]")
        public LocalDate dateOfBirth;
        @Schema(example = "1")
        public Integer groupId;
        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "true")
        public Boolean active;
        @Schema(example = "04 March 2009")
        public String activationDate;
        @Schema(example = "+353851239876")
        public String mobileNo;
        @Schema(description = "List of PostClientsDatatable")
        public List<EntityClientsApiResourceSwagger.PostClientsRequest.PostClientsDatatable> datatables;
        @Schema(description = "Address requests")
        public List<EntityClientsApiResourceSwagger.PostClientsRequest.PostClientsAddressRequest> address;
        @Schema(example = "test@test.com")
        public String emailAddress;
    }

    @Schema(description = "PostClientsResponse")
    public static final class PostClientsResponse {

        private PostClientsResponse() {}

        @Schema(example = "1")
        public Integer officeId;
        @Schema(example = "1")
        public Integer groupId;
        @Schema(example = "2")
        public Long clientId;
        @Schema(example = "2")
        public Integer resourceId;
    }

}
