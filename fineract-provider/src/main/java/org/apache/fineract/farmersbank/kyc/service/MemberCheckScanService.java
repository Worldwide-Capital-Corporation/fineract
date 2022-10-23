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

import org.apache.fineract.farmersbank.kyc.api.ScreeningApiResource;
import org.apache.fineract.farmersbank.kyc.client.ScanClient;
import org.apache.fineract.farmersbank.kyc.configs.KYCConfiguration;
import org.apache.fineract.farmersbank.kyc.data.request.IndividualScanRequest;
import org.apache.fineract.farmersbank.kyc.data.request.OrganisationScanRequest;
import org.apache.fineract.farmersbank.kyc.data.response.ClientKycScreeningData;
import org.apache.fineract.farmersbank.kyc.data.response.ClientRiskRating;
import org.apache.fineract.farmersbank.kyc.data.response.IdNumberResponse;
import org.apache.fineract.farmersbank.kyc.data.response.MatchedEntityData;
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
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.exception.InvalidKycTokenException;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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
    private final ClientRepository clientRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final Logger logger
            = LoggerFactory.getLogger(MemberCheckScanService.class);

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
            final LinkedCompaniesRepository linkedCompaniesRepository,
            final ClientRepository clientRepository,
            final JdbcTemplate jdbcTemplate
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
        this.clientRepository = clientRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

      public ClientScreening kycScreening(Long clientId) throws Exception {
        Client client = clientRepository.getReferenceById(clientId);
        if (client.getLegalForm() == 1) {
          return individualScan(
              IndividualScanRequest.createNew(
                  client.getFirstname(),
                  Optional.ofNullable(client.getMiddlename()).orElse(""),
                  client.getLastname(),
                  Optional.ofNullable(client.gender()).map(CodeValue::label).orElse(""),
                  Optional.ofNullable(client.dateOfBirth())
                      .map(date -> getFormattedDateString(date))
                      .orElse("")),
              client);
        }
        return organisationScan(
            OrganisationScanRequest.createNew(client.getDisplayName(), "", "", ""), client);
      }

    private String getFormattedDateString(LocalDate date) {
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatters);
    }

    public ClientScreening individualScan(IndividualScanRequest request, Client client) throws IOException {
        Call<ScanResponse> retrofitCall = scanClient.individualScan(API_KEY, request);
        Response<ScanResponse> response = retrofitCall.execute();

        if (!response.isSuccessful()) {
            String message = response.errorBody() != null
                    ? response.errorBody().string() : "Unknown error";
            throw new InvalidKycTokenException(message);
        }

        ScanResponse scanResponse = response.body();

        if (scanResponse.matchedNumber != 0){
            return saveKycScreeningResults(scanResponse, client, false);
        } else if(scanResponse.matchedNumber == 0){
            return saveNoMatchKycResults(scanResponse, client);
        }
        return null;
    }

    public ClientScreening organisationScan(OrganisationScanRequest request, Client client) throws IOException {
        Call<ScanResponse> retrofitCall = scanClient.organisationScan(API_KEY, request);

        Response<ScanResponse> response = retrofitCall.execute();

        if (!response.isSuccessful()) {
            String message = response.errorBody() != null
                    ? response.errorBody().string() : "Unknown error";
            throw new InvalidKycTokenException(message);
        }

        ScanResponse scanResponse = response.body();

        if (scanResponse.matchedNumber != 0){
            return saveKycScreeningResults(scanResponse, client, true);
        } else if(scanResponse.matchedNumber == 0){
            return saveNoMatchKycResults(scanResponse, client);
        }
        return null;
    }

    private ClientScreening saveKycScreeningResults(ScanResponse scanResponse, Client client, boolean isOrganisation) {
        ClientScreening kycScreening = repository.save(ClientScreening.createNew(scanResponse, client));
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
    }

    private ClientScreening saveNoMatchKycResults(ScanResponse scanResponse, Client client) {
        ClientScreening kycScreening = ClientScreening.createFromNoMatch(scanResponse, client);
        return repository.save(kycScreening);
    }

    @SuppressWarnings("unused")
    private ScanResponse getPossibleMatch(IndividualScanRequest request, ScanResponse response){
        if (isExactMatch(request, response)){
            response.isExactMatch = true;
            return response;
        } else {
            response.isExactMatch = false;
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

    public ClientKycScreeningData getLatestScreening(Long clientId) {

        final ClientScreeningMapper rm = new ClientScreeningMapper();
        final String sql = "select " + rm.schema() + " where cs.client_id=? ORDER BY cs.id DESC";
        List<ClientKycScreeningData> results =  this.jdbcTemplate.query(sql, rm, clientId); // NOSONAR
        if (results != null && results.size() > 0) {
            return results.get(0);
        }
        return null;
    }

    public ClientRiskRating getScreeningHistory(Long clientId, int limit) {
        final ClientScreeningMapper rm = new ClientScreeningMapper();
        final String sql = "select " + rm.schema() + " where cs.client_id=? ORDER BY cs.id DESC LIMIT "+limit;
        List<ClientKycScreeningData> results =  this.jdbcTemplate.query(sql, rm, clientId); // NOSONAR
        if (results != null && results.size() > 0) {
            ClientKycScreeningData screeningData = results.get(0);
            final MatchedEntityMapper matchedEntityMapper = new MatchedEntityMapper();
            final String matchSql = "select " + matchedEntityMapper.schema() + " where me.screening_id=? ";
            List<MatchedEntityData> matches = this.jdbcTemplate.query(matchSql, matchedEntityMapper, screeningData.getId()); // NOSONAR
            if (matches != null && matches.size() > 0) {
                return new ClientRiskRating(results.get(0), matches);
            }
            return new ClientRiskRating(results.get(0), null);
        }
        return null;
    }

    public ClientRiskRating markVerifiedMatch(ScreeningApiResource.VerifyScreening request) {
        final String updateMatchedEntitySql = "UPDATE m_client_screening SET is_verified_match=true where id="+request.screeningId;
        this.jdbcTemplate.execute(updateMatchedEntitySql);
        final String updateResultSql = "UPDATE m_client_screening_matched_entity SET is_verified_match=true where id="+request.matchId;
        this.jdbcTemplate.execute(updateResultSql);
        return getScreeningHistory(request.clientId, 1);
    }
}
