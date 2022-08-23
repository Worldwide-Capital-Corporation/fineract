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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Date;


@Service
public class JwtUtil {

    private static final int expireInMs = 3 * 60 * 1000; //three minutes
    private static final int refreshTokenExpireInMs = 5 * 60 * 1000;
    private static final int bCryptEncoderStrength = 10;
    private static final String tokenTypeKey = "token_type";
    private static final String securityCheckKey = "guid";
    private enum TokenType { ACCESS_TOKEN, REFRESH_TOKEN }

    SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode("aGdkNzYzeWhka3NpOHVkNzM2eXNobmVrd25zeXQzNGQ="));

    public String generate(AppUser user) {
        BCryptPasswordEncoder bCryptPasswordEncoder =
                new BCryptPasswordEncoder(bCryptEncoderStrength, new SecureRandom());
        String tokenId = bCryptPasswordEncoder.encode(user.getPassword());
        String securityCheck = bCryptPasswordEncoder.encode(String.valueOf(user.getId()));
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuer("fb.cbs")
                .setId(tokenId)
                .claim(tokenTypeKey, TokenType.ACCESS_TOKEN)
                .claim(securityCheckKey, securityCheck)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireInMs))
                .signWith(key)
                .compact();
    }

    public String refreshToken(AppUser user) {
        BCryptPasswordEncoder bCryptPasswordEncoder =
                new BCryptPasswordEncoder(bCryptEncoderStrength, new SecureRandom());
        String tokenId = bCryptPasswordEncoder.encode(user.getPassword());
        String securityCheck = bCryptPasswordEncoder.encode(String.valueOf(user.getId()));
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuer("fb.cbs")
                .setId(tokenId)
                .claim(tokenTypeKey, TokenType.REFRESH_TOKEN)
                .claim(securityCheckKey, securityCheck)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpireInMs))
                .signWith(key)
                .compact();
    }

    public boolean validate(String token) {
        if (getUsername(token) != null && isExpired(token)) {
            return true;
        }
        return false;
    }

    public String getUsername(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    public boolean isExpired(String token) {
        Claims claims = getClaims(token);
        return claims.getExpiration().after(new Date(System.currentTimeMillis()));
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
