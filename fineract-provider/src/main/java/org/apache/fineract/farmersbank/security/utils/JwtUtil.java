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

package org.apache.fineract.farmersbank.security.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.fineract.farmersbank.security.data.JwtTokenData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Date;

@Service
public class JwtUtil {

    private static final String TOKEN_TYPE = "token_type";
    private static final String GUID = "guid";
    private static final String UUID = "uuid";
    private static final String ACCESS_TOKEN_UUID = "uuid";

    private final int accessTokenExpiresIn;
    private final int refreshTokenExpiresIn;
    private final int refreshTokenBeforeTimeout;
    private final int bCryptEncoderStrength;
    private SecretKey key;
    private BCryptPasswordEncoder encoder;

    private enum TokenType {

        ACCESS_TOKEN("access_token"),
        REFRESH_TOKEN("refresh_token");

        private final String value;

        private TokenType(String name) {
            value = name;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    @Autowired
    public JwtUtil(
            @Value("${fineract.security.oauth.jwt.key}") String jwtKey,
            @Value("${fineract.security.oauth.jwt.access-token-expires-in}") int accessTokenExpiresIn,
            @Value("${fineract.security.oauth.jwt.refresh-token-expires-in}") int refreshTokenExpiresIn,
            @Value("${fineract.security.oauth.jwt.refresh-token-before-expires}") int refreshTokenBeforeTimeout,
            @Value("${fineract.security.oauth.jwt.crypt-encoder-strength}") int bCryptEncoderStrength) {
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
        this.refreshTokenBeforeTimeout = refreshTokenBeforeTimeout;
        this.bCryptEncoderStrength = bCryptEncoderStrength;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtKey));
        this.encoder = new BCryptPasswordEncoder(bCryptEncoderStrength, new SecureRandom());
    }

    public JwtTokenData generate(AppUser user) {
        String tokenId = encoder.encode(String.valueOf(user.getId()));
        String securityCheck = encoder.encode(tokenId.concat(user.getPassword()));
        Date issuedAt = new Date(System.currentTimeMillis());
        Date expiresIn = new Date(System.currentTimeMillis() + (accessTokenExpiresIn * 1000));
        String uuid = java.util.UUID.randomUUID().toString();
        String token =
                Jwts.builder()
                        .setSubject(user.getUsername())
                        .setIssuer("fb.cbs")
                        .setId(tokenId)
                        .claim(TOKEN_TYPE, TokenType.ACCESS_TOKEN.toString())
                        .claim(GUID, securityCheck)
                        .claim(UUID, uuid)
                        .setIssuedAt(issuedAt)
                        .setExpiration(expiresIn)
                        .signWith(key)
                        .compact();
        return new JwtTokenData(
                token, uuid, ((expiresIn.getTime() - issuedAt.getTime()) / 1000) - refreshTokenBeforeTimeout);
    }

    public JwtTokenData refreshToken(AppUser user, JwtTokenData accessTokenData) {
        String tokenId = encoder.encode(String.valueOf(user.getId()));
        String securityCheck = encoder.encode(tokenId.concat(user.getPassword()));
        Date issuedAt = new Date(System.currentTimeMillis());
        Date expiresIn = new Date(System.currentTimeMillis() + (refreshTokenExpiresIn * 1000));
        String token =
                Jwts.builder()
                        .setSubject(user.getUsername())
                        .setIssuer("fb.cbs")
                        .setId(tokenId)
                        .claim(TOKEN_TYPE, TokenType.REFRESH_TOKEN.toString())
                        .claim(GUID, securityCheck)
                        .claim(ACCESS_TOKEN_UUID, accessTokenData.getUuid())
                        .setIssuedAt(issuedAt)
                        .setExpiration(expiresIn)
                        .signWith(key)
                        .compact();
        return new JwtTokenData(
                token,
                accessTokenData.getUuid(),
                ((expiresIn.getTime() - issuedAt.getTime()) / 1000) - refreshTokenBeforeTimeout);
    }

    public boolean validate(String token) throws JwtException {
        if (getUsername(token) != null && isExpired(token)) {
            return true;
        }
        return false;
    }

    public boolean isRefreshToken(String token) throws JwtException {
        Claims claims = getClaims(token);
        return claims.get(TOKEN_TYPE, String.class).equals(TokenType.REFRESH_TOKEN.toString());
    }

    public boolean validateId(String token, AppUser user) throws JwtException {
        Claims claims = getClaims(token);
        return claims.getId().matches(encoder.encode(String.valueOf(user.getId())));
    }

    public boolean validateTokenUuid(String token, AppUser user) throws JwtException {
        Claims claims = getClaims(token);
        return claims.get(ACCESS_TOKEN_UUID, String.class).equals(user.getAccessTokenUuid());
    }

    public String getUsername(String token) throws JwtException {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    public boolean validateGuid(String token, AppUser user) throws JwtException {
        Claims claims = getClaims(token);
        String tokenId = claims.getId();
        return claims
                .get(GUID, String.class)
                .matches(encoder.encode(tokenId.concat(user.getPassword())));
    }

    public boolean isExpired(String token) throws JwtException {
        Claims claims = getClaims(token);
        return claims.getExpiration().after(new Date(System.currentTimeMillis()));
    }

    private Claims getClaims(String token) throws JwtException {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
