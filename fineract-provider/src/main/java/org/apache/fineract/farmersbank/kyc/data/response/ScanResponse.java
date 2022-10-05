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

package org.apache.fineract.farmersbank.kyc.data.response;

import java.util.ArrayList;

public class ScanResponse {
    // Categories
    private static final String SIP = "SIP"; // Special interest person
    private static final String PEP = "PEP";
    private static final String TER = "TER"; // Terrorist
    private static final String RCA = "RCA"; // Relatives and Close Associates

    public boolean isExactMatch;
    public MetadataResponse metadata;
    public Long scanId;
    public String resultUrl;
    public Long matchedNumber;
    public ArrayList<MatchedEntityResponse> matchedEntities;
    public ArrayList<WebSearchResultResponse> webSearchResults;

    public String getPictureImageUrl(){
        for (MatchedEntityResponse entityResponse : matchedEntities) {
            return entityResponse.resultEntity.image;
        }
        return null;
    }

    public boolean isPoliticalExposedPerson(){
        for (MatchedEntityResponse entityResponse : matchedEntities) {
            if (entityResponse.category.contains(PEP)) {
                return true;
            }
            for (DescriptionResponse descriptionResponse : entityResponse.resultEntity.descriptions){
                if (descriptionResponse.description2.contains(PEP)) {
                    return true;
                }
            }
            for (SourceResponse sourceResponse : entityResponse.resultEntity.sources){
                if (sourceResponse.categories.contains(PEP)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSanctioned(){
        for (MatchedEntityResponse entityResponse : matchedEntities) {
            for (DescriptionResponse descriptionResponse : entityResponse.resultEntity.descriptions){
                if (descriptionResponse.description2.contains("Sanction")) {
                    return true;
                }
            }
            for (SourceResponse sourceResponse : entityResponse.resultEntity.sources){
                if (sourceResponse.categories.contains("Sanction")) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInvolvedInFinancialCrime(){
        for (MatchedEntityResponse entityResponse : matchedEntities) {
            for (DescriptionResponse descriptionResponse : entityResponse.resultEntity.descriptions){
                if (descriptionResponse.description2.contains("Financial Crime") ||
                        descriptionResponse.description2.contains("Law Enforcement")) {
                    return true;
                }
            }
            for (SourceResponse sourceResponse : entityResponse.resultEntity.sources){
                if (sourceResponse.categories.contains("Financial Crime") ||
                        sourceResponse.categories.contains("Law Enforcement")) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInvolvedInBriberyAndCorruption(){
        for (MatchedEntityResponse entityResponse : matchedEntities) {
            for (DescriptionResponse descriptionResponse : entityResponse.resultEntity.descriptions){
                if (descriptionResponse.description2.contains("Bribery") ||
                        descriptionResponse.description2.contains("Corruption")) {
                    return true;
                }
            }
            for (SourceResponse sourceResponse : entityResponse.resultEntity.sources){
                if (sourceResponse.categories.contains("Bribery") ||
                        sourceResponse.categories.contains("Corruption")) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSpecialInterestPerson(){
        for (MatchedEntityResponse entityResponse : matchedEntities) {
            return entityResponse.category.contains(SIP);
        }
        return false;
    }

    public boolean isTerrorist(){
        for (MatchedEntityResponse entityResponse : matchedEntities) {
            return entityResponse.category.contains(TER);
        }
        return false;
    }

    public boolean isRelativeOrAssociate(){
        for (MatchedEntityResponse entityResponse : matchedEntities) {
            return entityResponse.category.contains(RCA);
        }
        return false;
    }

    public String riskRating() {
        if (matchedNumber == 0){
            return "LOW";
        }
        
        if (isTerrorist() || isInvolvedInFinancialCrime() || isSanctioned()){
            return "HIGH";
        }

        if(isPoliticalExposedPerson() || isRelativeOrAssociate() || isInvolvedInBriberyAndCorruption()) {
            return "MEDIUM";
        }

        return "LOW";
    }
}

