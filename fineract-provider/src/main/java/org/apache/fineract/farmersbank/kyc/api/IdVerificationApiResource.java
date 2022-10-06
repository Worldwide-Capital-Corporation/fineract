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

package org.apache.fineract.farmersbank.kyc.api;

import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.fineract.farmersbank.kyc.data.request.IdVerificationRequest;
import org.apache.fineract.farmersbank.kyc.data.response.ScanResponse;
import org.apache.fineract.farmersbank.kyc.service.MemberCheckIdVerificationService;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Component
@Scope("singleton")
@Path("/id-verification")
@Tag(
        name = "Performs new ID Verification.",
        description =
                "ID Verification allows you to apply ID Verification for your members by entering member information into the fields provided.")
public class IdVerificationApiResource {

    public static class IdVerificationScanIdResponse {
        public Long scanId;
        public IdVerificationScanIdResponse(Long scanId) {
            this.scanId = scanId;
        }
    }

    private final ToApiJsonSerializer<ScanResponse> apiJsonSerializerService;
    private final MemberCheckIdVerificationService kycService;
    private final ToApiJsonSerializer<IdVerificationScanIdResponse> scanIdSerializer;

    @Autowired
    public IdVerificationApiResource(final ToApiJsonSerializer<ScanResponse> apiJsonSerializerService,
                                     final ToApiJsonSerializer<IdVerificationScanIdResponse> scanIdSerializer,
                                     final MemberCheckIdVerificationService kycService) {
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.kycService = kycService;
        this.scanIdSerializer = scanIdSerializer;
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Performs new ID Verification.",
            description =
                    " ID Verification allows you to apply ID Verification for your members by entering member information into the fields provided.")
    @RequestBody(
            required = true,
            content =
            @Content(
                    schema =
                    @Schema(
                            implementation =
                                    IdVerificationRequest.class)))
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content =
                    @Content(
                            schema =
                            @Schema(
                                    implementation =
                                            Long.class))),
            @ApiResponse(responseCode = "400", description = "Unauthenticated. Please login")
    })
    public String verifyId(@Parameter(hidden = true) final String apiRequestBodyAsJson) throws IOException {
        IdVerificationRequest request =
                new Gson().fromJson(apiRequestBodyAsJson, IdVerificationRequest.class);
        if (request == null) {
            throw new IllegalArgumentException(
                    "Invalid JSON in BODY  of POST to /kyc/id");
        }

        IdVerificationScanIdResponse response = kycService.singleVerification(request);

        return this.apiJsonSerializerService.serialize(response);
    }

}
