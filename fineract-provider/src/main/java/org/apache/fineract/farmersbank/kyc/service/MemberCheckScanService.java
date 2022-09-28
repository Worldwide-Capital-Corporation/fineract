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

package org.apache.fineract.farmersbank.kyc.service;

import org.apache.fineract.farmersbank.kyc.client.ScanClient;
import org.apache.fineract.farmersbank.kyc.configs.KYCConfiguration;
import org.apache.fineract.farmersbank.kyc.data.request.IndividualScanRequest;
import org.apache.fineract.farmersbank.kyc.data.request.OrganisationScanRequest;
import org.apache.fineract.farmersbank.kyc.data.response.IdNumberResponse;
import org.apache.fineract.farmersbank.kyc.data.response.MatchedEntityResponse;
import org.apache.fineract.farmersbank.kyc.data.response.ScanResponse;
import org.apache.fineract.farmersbank.kyc.domain.ClientScreening;
import org.apache.fineract.farmersbank.kyc.domain.Description;
import org.apache.fineract.farmersbank.kyc.domain.LinkedCompany;
import org.apache.fineract.farmersbank.kyc.domain.LinkedIndividual;
import org.apache.fineract.farmersbank.kyc.domain.MatchedEntity;
import org.apache.fineract.farmersbank.kyc.domain.ScreeningIdNumbers;
import org.apache.fineract.farmersbank.kyc.domain.ScreeningJobHistory;
import org.apache.fineract.farmersbank.kyc.domain.ScreeningResultEntity;
import org.apache.fineract.farmersbank.kyc.domain.WebSearch;
import org.apache.fineract.farmersbank.kyc.domain.repositories.CustomerScreeningRepository;
import org.apache.fineract.farmersbank.kyc.domain.repositories.DescriptionRepository;
import org.apache.fineract.farmersbank.kyc.domain.repositories.IdNumbersRepository;
import org.apache.fineract.farmersbank.kyc.domain.repositories.JobHistoryRepository;
import org.apache.fineract.farmersbank.kyc.domain.repositories.LinkedCompaniesRepository;
import org.apache.fineract.farmersbank.kyc.domain.repositories.LinkedIndividualsRepository;
import org.apache.fineract.farmersbank.kyc.domain.repositories.MatchedEntityRepository;
import org.apache.fineract.farmersbank.kyc.domain.repositories.ResultEntityRepository;
import org.apache.fineract.farmersbank.kyc.domain.repositories.WebSearchRepository;
import org.apache.fineract.farmersbank.utils.SearchUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Set;

@Service
public class MemberCheckScanService implements KYCConfiguration {

    private ScanClient scanClient;
    private final CustomerScreeningRepository repository;
    private final WebSearchRepository webSearchRepository;
    private final MatchedEntityRepository matchedEntityRepository;
    private final DescriptionRepository descriptionRepository;
    private final IdNumbersRepository idNumbersRepository;
    private final JobHistoryRepository jobHistoryRepository;
    private final ResultEntityRepository resultEntityRepository;
    private final LinkedIndividualsRepository linkedIndividualsRepository;
    private final LinkedCompaniesRepository linkedCompaniesRepository;

    @Autowired
    public MemberCheckScanService(
            final CustomerScreeningRepository repository,
            final WebSearchRepository webSearchRepository,
            final MatchedEntityRepository matchedEntityRepository,
            final DescriptionRepository descriptionRepository,
            final IdNumbersRepository idNumbersRepository,
            final JobHistoryRepository jobHistoryRepository,
            final ResultEntityRepository resultEntityRepository,
            final LinkedIndividualsRepository linkedIndividualsRepository,
            final LinkedCompaniesRepository linkedCompaniesRepository
    ) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.scanClient = retrofit.create(ScanClient.class);
        this.repository = repository;
        this.webSearchRepository = webSearchRepository;
        this.matchedEntityRepository = matchedEntityRepository;
        this.descriptionRepository = descriptionRepository;
        this.idNumbersRepository = idNumbersRepository;
        this.jobHistoryRepository = jobHistoryRepository;
        this.resultEntityRepository = resultEntityRepository;
        this.linkedIndividualsRepository = linkedIndividualsRepository;
        this.linkedCompaniesRepository = linkedCompaniesRepository;
    }

    public ClientScreening individualScan(IndividualScanRequest request) throws IOException {
        Call<ScanResponse> retrofitCall = scanClient.individualScan(API_KEY, request);
        Response<ScanResponse> response = retrofitCall.execute();

        if (!response.isSuccessful()) {
            throw new IOException(response.errorBody() != null
                    ? response.errorBody().string() : "Unknown error");
        }

        ScanResponse scanResponse = response.body();

        if (scanResponse.matchedNumber != 0){
            ClientScreening kycScreening = repository.save(ClientScreening.createNew(scanResponse));
            Set<WebSearch> webSearch = WebSearch.createNew(scanResponse.webSearchResults, kycScreening);
            webSearchRepository.saveAll(webSearch);
            for (MatchedEntityResponse matchedEntityResponse : scanResponse.matchedEntities) {
                ScreeningResultEntity resultEntity = ScreeningResultEntity.createNewFrom(matchedEntityResponse.resultEntity);
                matchedEntityRepository.save(MatchedEntity.createNew(matchedEntityResponse, kycScreening, resultEntity));
                if (matchedEntityResponse.resultEntity.descriptions != null)
                    descriptionRepository.saveAll(Description.createNew(matchedEntityResponse.resultEntity.descriptions, resultEntity));
                if (matchedEntityResponse.resultEntity.roles != null)
                    jobHistoryRepository.saveAll(ScreeningJobHistory.createNew(matchedEntityResponse.resultEntity.roles, resultEntity));
                if (matchedEntityResponse.resultEntity.idNumbers != null)
                    idNumbersRepository.saveAll(ScreeningIdNumbers.createNew(matchedEntityResponse.resultEntity.idNumbers, resultEntity));
                if (matchedEntityResponse.resultEntity.linkedIndividuals != null)
                    linkedIndividualsRepository.saveAll(LinkedIndividual.createNew(matchedEntityResponse.resultEntity.linkedIndividuals, resultEntity));
                if (matchedEntityResponse.resultEntity.linkedCompanies != null)
                    linkedCompaniesRepository.saveAll(LinkedCompany.createNew(matchedEntityResponse.resultEntity.linkedCompanies, resultEntity));
            }
            return kycScreening;
        } else if(scanResponse.matchedNumber == 0){
            ClientScreening kycScreening = ClientScreening.createFromNoMatch(scanResponse);
            return repository.save(kycScreening);
        }
        return null;
    }

    public ScanResponse organisationScan(OrganisationScanRequest request) throws IOException {
        Call<ScanResponse> retrofitCall = scanClient.organisationScan(API_KEY, request);

        Response<ScanResponse> response = retrofitCall.execute();

        if (!response.isSuccessful()) {
            throw new IOException(response.errorBody() != null
                    ? response.errorBody().string() : "Unknown error");
        }
        return response.body();
    }

    @SuppressWarnings("unused")
    private ScanResponse getPossibleMatch(IndividualScanRequest request, ScanResponse response){
        if (isExactMatch(request, response)){
            response.isExactMatch = true;
            return response;
        } else if (isPositiveMatch(request, response)){
            response.isExactMatch = false;
            return response;
        } else {
            return response;
        }
    }

    //exact match is done with an ID
    private boolean isExactMatch(IndividualScanRequest request, ScanResponse response){
        for (MatchedEntityResponse entityResponse : response.matchedEntities) {
            IdNumberResponse idNumberMatch = SearchUtils.findByProperty(
                    entityResponse.resultEntity.idNumbers, id -> request.idvParam.IdNumber.equals(id.number)
            );
            return idNumberMatch != null;
        }
        return false;
    }

    private boolean isPositiveMatch(IndividualScanRequest request, ScanResponse response){
        // check for matching names
        // check for matching dob
        // check for matching picture
        return true;
    }
}
