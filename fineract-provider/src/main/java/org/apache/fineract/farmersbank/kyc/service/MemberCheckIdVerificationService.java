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

import okhttp3.ResponseBody;
import org.apache.fineract.farmersbank.kyc.api.IdVerificationApiResource;
import org.apache.fineract.farmersbank.kyc.client.IdVerificationClient;
import org.apache.fineract.farmersbank.kyc.configs.KYCConfiguration;
import org.apache.fineract.farmersbank.kyc.data.request.IdVerificationRequest;
import org.apache.fineract.farmersbank.kyc.data.response.IdVerificationResponse;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

@Service
public class MemberCheckIdVerificationService implements KYCConfiguration {

    private IdVerificationClient client;

    public MemberCheckIdVerificationService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.client = retrofit.create(IdVerificationClient.class);
    }

    public IdVerificationApiResource.IdVerificationScanIdResponse singleVerification(IdVerificationRequest request) throws IOException {
        Call<Long> retrofitCall = client.singleVerification(API_KEY, request);

        Response<Long> response = retrofitCall.execute();

        if (!response.isSuccessful()) {
            throw new IOException(response.errorBody() != null
                    ? response.errorBody().string() : "Unknown error");
        }
        return new IdVerificationApiResource.IdVerificationScanIdResponse(response.body());
    }

    public IdVerificationResponse idVerificationResult(Long scanId) throws IOException {
        Call<IdVerificationResponse> retrofitCall = client.idVerificationResult(API_KEY, scanId);

        Response<IdVerificationResponse> response = retrofitCall.execute();

        if (!response.isSuccessful()) {
            throw new IOException(response.errorBody() != null
                    ? response.errorBody().string() : "Unknown error");
        }
        return response.body();
    }

    public ResponseBody resendVerificationLink(Long scanId) throws IOException {
        Call<ResponseBody> retrofitCall = client.resendVerificationLink(API_KEY, scanId);

        Response<ResponseBody> response = retrofitCall.execute();

        if (!response.isSuccessful()) {
            throw new IOException(response.errorBody() != null
                    ? response.errorBody().string() : "Unknown error");
        }
        return response.body();
    }

    public ResponseBody idVerificationReport(Long scanId, String format) throws IOException {
        Call<ResponseBody> retrofitCall = client.idVerificationReport(API_KEY, scanId, format);

        Response<ResponseBody> response = retrofitCall.execute();

        if (!response.isSuccessful()) {
            throw new IOException(response.errorBody() != null
                    ? response.errorBody().string() : "Unknown error");
        }
        return response.body();
    }
}
