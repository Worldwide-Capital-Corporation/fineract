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

import org.apache.fineract.farmersbank.kyc.data.request.IndividualScanRequest;
import org.apache.fineract.farmersbank.kyc.data.request.OrganisationScanRequest;
import org.apache.fineract.farmersbank.kyc.data.response.ScanResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ScanClient {

    @POST("/api/v2/member-scans/single")
    Call<ScanResponse> individualScan(@Header("api-key") String apiKey,
                                      @Body IndividualScanRequest request);

    @POST("/api/v2/corp-scans/single")
    Call<ScanResponse> organisationScan(@Header("api-key") String apiKey,
                                        @Body OrganisationScanRequest request);
}
