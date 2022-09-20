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

package org.apache.fineract.farmersbank.security.data;

import org.apache.fineract.infrastructure.security.data.OTPDeliveryMethod;

import java.util.List;

public class TwoFactorData {
    @SuppressWarnings("unused")
    private final Boolean usingAuthenticator;

    @SuppressWarnings("unused")
    private List<OTPDeliveryMethod> otpDeliveryMethods;

    @SuppressWarnings("unused")
    private final Boolean authenticatorEnrolled;

    private final String qrCodeImage;

    public TwoFactorData(
            final Boolean usingAuthenticator,
            final List<OTPDeliveryMethod> otpDeliveryMethods,
            final Boolean authenticatorEnrolled,
            final String qrCodeImage) {
        this.usingAuthenticator = usingAuthenticator;
        this.otpDeliveryMethods = otpDeliveryMethods;
        this.authenticatorEnrolled = authenticatorEnrolled;
        this.qrCodeImage = qrCodeImage;
    }
}
