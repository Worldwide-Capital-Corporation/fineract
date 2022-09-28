package org.apache.fineract.farmersbank.kyc.data.response;

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

import java.util.ArrayList;

public class ResultEntityResponse {
    public Long uniqueId;
    public String category;
    public String categories;
    public String subcategory;
    public String gender;
    public String deceased;
    public String primaryFirstName;
    public String primaryMiddleName;
    public String primaryLastName;
    public String title;
    public String position;
    public String dateOfBirth;
    public String deceasedDate;
    public String placeOfBirth;
    public String primaryLocation;
    public String image;
    public GeneralInfoResponse generalInfo;
    public String furtherInformation;
    public String enterDate;
    public String lastReviewed;
    public ArrayList<DescriptionResponse> descriptions;
    public ArrayList<NameDetailResponse> nameDetails;
    public ArrayList<String> originalScriptNames;
    public ArrayList<ScanRoleResponse> roles;
    public ArrayList<ImportantDateResponse> importantDates;
    public ArrayList<LocationResponse> locations;
    public ArrayList<CountryResponse> countries;
    public ArrayList<OfficialListResponse> officialLists;
    public ArrayList<IdNumberResponse> idNumbers;
    public ArrayList<SourceResponse> sources;
    public ArrayList<LinkedIndividualResponse> linkedIndividuals;
    public ArrayList<LinkedCompanyResponse> linkedCompanies;
}
