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

import org.apache.fineract.farmersbank.kyc.data.response.MonitoringListResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

import java.util.List;

public interface MonitoringListClient {

    @GET("/api/v2/monitoring-lists/member")
    Call<List<MonitoringListResponse>> monitoringList(@Header("api-key") String apiKey,
                                                      @Query("firstName") String firstName,
                                                      @Query("middleName") String middleName,
                                                      @Query("lastName") String lastName,
                                                      @Query("scriptNameFullName") String scriptNameFullName,
                                                      @Query("memberNumber") String memberNumber,
                                                      @Query("clientId") String clientId,
                                                      @Query("status") String status,
                                                      @Query("pageIndex") String pageIndex,
                                                      @Query("pageSize") String pageSize);
    @GET("/api/v2/monitoring-lists/corp")
    Call<List<MonitoringListResponse>> corpMonitoringList(@Header("api-key") String apiKey,
                                                          @Query("companyName") String companyName,
                                                          @Query("entityNumber") String entityNumber,
                                                          @Query("clientId") String clientId,
                                                          @Query("status") String status,
                                                          @Query("pageIndex") String pageIndex,
                                                          @Query("pageSize") String pageSize);
}
