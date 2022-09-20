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

package org.apache.fineract.farmersbank.service;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
public class GoogleAuthenticatorService implements AuthenticatorService {

    private final QrDataFactory qrDataFactory;
    private final QrGenerator qrGenerator;
    private final SecretGenerator secretGenerator;
    private final CodeVerifier verifier;

    @Autowired
    public GoogleAuthenticatorService(QrDataFactory qrDataFactory, QrGenerator qrGenerator, SecretGenerator secretGenerator, CodeVerifier verifier){
        this.qrDataFactory = qrDataFactory;
        this.qrGenerator = qrGenerator;
        this.secretGenerator = secretGenerator;
        this.verifier = verifier;
    }

    @Override
    public String generateQRCode(AppUser user, String secret) throws Exception {
        QrData data = qrDataFactory.newBuilder().label(user.getEmail()).secret(secret).issuer("Farmers Bank").build();
        return getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
    }

    @Override
    public String generateSecret() {
        return secretGenerator.generate();
    }

    @Override
    public boolean isAppCodeValid(String secret, String code) {
        return verifier.isValidCode(secret, code);
    }
}
