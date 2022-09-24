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

package org.apache.fineract.farmersbank.kyc.client;

import okhttp3.ResponseBody;
import org.apache.fineract.farmersbank.kyc.data.request.IdVerificationRequest;
import org.apache.fineract.farmersbank.kyc.data.response.IdVerificationResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface IdVerificationClient {

    @POST("/api/v2/id-verification/single")
    Call<Long> singleVerification(@Header("api-key") String apiKey,
                                          @Body IdVerificationRequest request);

    @GET("/api/v2/id-verification/single/{scanId}")
    Call<IdVerificationResponse> idVerificationResult(@Header("api-key") String apiKey,
                                                      @Path("scanId") Long scanId);

    @POST("/api/v2/id-verification/single/{scanId}/resend")
    Call<ResponseBody> resendVerificationLink(@Header("api-key") String apiKey,
                                              @Path("scanId") Long scanId);

    @POST("/api/v2/id-verification/single/{scanId}/report")
    Call<ResponseBody> idVerificationReport(@Header("api-key") String apiKey,
                                            @Path("scanId") Long scanId,
                                            @Path("format") String format);
}
