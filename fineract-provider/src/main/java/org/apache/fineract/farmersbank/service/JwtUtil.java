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
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.fineract.farmersbank.security.data.FBJwtTokenData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Date;

@Service
public class JwtUtil {

    private static final int expireInMs = 2 * 60 * 1000; //two minutes
    private static final int expireInMsDev = 20 * 60 * 1000; //two minutes
    private static final int refreshTokenExpireInMs = 5 * 60 * 1000;
    private static final int bCryptEncoderStrength = 10;
    private static final int refreshTokenIn = 5; //five seconds before token expires
    private static final String TOKEN_TYPE = "token_type";
    private static final String GUID = "guid";
    private static final String UUID = "uuid";
    private static final String ACCESS_TOKEN_UUID = "uuid";

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
    //TODO:- Move key to application properties
    SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode("aGdkNzYzeWhka3NpOHVkNzM2eXNobmVrd25zeXQzNGQ="));
    BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder(bCryptEncoderStrength, new SecureRandom());

  public FBJwtTokenData generate(AppUser user) {
    String tokenId = encoder.encode(String.valueOf(user.getId()));
    String securityCheck = encoder.encode(tokenId.concat(user.getPassword()));
    Date issuedAt = new Date(System.currentTimeMillis());
    Date expiresIn = new Date(System.currentTimeMillis() + expireInMsDev);
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
    return new FBJwtTokenData(
        token, uuid, ((expiresIn.getTime() - issuedAt.getTime()) / 1000) - refreshTokenIn);
  }

  public FBJwtTokenData refreshToken(AppUser user, FBJwtTokenData accessTokenData) {
    String tokenId = encoder.encode(String.valueOf(user.getId()));
    String securityCheck = encoder.encode(tokenId.concat(user.getPassword()));
    Date issuedAt = new Date(System.currentTimeMillis());
    Date expiresIn = new Date(System.currentTimeMillis() + expireInMsDev);
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
    return new FBJwtTokenData(
        token,
        accessTokenData.getUuid(),
        ((expiresIn.getTime() - issuedAt.getTime()) / 1000) - refreshTokenIn);
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
