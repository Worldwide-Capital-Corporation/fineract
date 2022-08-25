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

package org.apache.fineract.farmersbank.filters;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.fineract.farmersbank.service.FarmersBankUserDetailsService;
import org.apache.fineract.farmersbank.service.JwtUtil;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import org.apache.fineract.infrastructure.cache.domain.CacheType;
import org.apache.fineract.infrastructure.cache.service.CacheWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.data.PlatformRequestLog;
import org.apache.fineract.infrastructure.security.exception.InvalidTenantIdentifierException;
import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.apache.fineract.notification.service.NotificationReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class FarmersBankJWTAuthenticationFilter extends BasicAuthenticationFilter {

  private static AtomicBoolean firstRequestProcessed = new AtomicBoolean();
  private static final Logger LOG =
      LoggerFactory.getLogger(FarmersBankJWTAuthenticationFilter.class);

  @Autowired private ToApiJsonSerializer<PlatformRequestLog> toApiJsonSerializer;

  @Autowired private ConfigurationDomainService configurationDomainService;

  @Autowired private CacheWritePlatformService cacheWritePlatformService;

  @Autowired private NotificationReadPlatformService notificationReadPlatformService;

  @Autowired private BasicAuthTenantDetailsService basicAuthTenantDetailsService;

  @Autowired private BusinessDateReadPlatformService businessDateReadPlatformService;

  @Autowired private JwtUtil jwtUtil;

  @Autowired private FarmersBankUserDetailsService userDetailsService;

  private final String tenantRequestHeader = "Fineract-Platform-TenantId";
  private final boolean exceptionIfHeaderMissing = true;

  public FarmersBankJWTAuthenticationFilter(
      final AuthenticationManager authenticationManager,
      final AuthenticationEntryPoint authenticationEntryPoint) {
    super(authenticationManager, authenticationEntryPoint);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final StopWatch task = new StopWatch();
    task.start();

    try {

      // allows for Cross-Origin
      // Requests (CORs) to be performed against the platform API.
      response.setHeader("Access-Control-Allow-Origin", "*"); // NOSONAR
      response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
      final String reqHead = request.getHeader("Access-Control-Request-Headers");

      if (null != reqHead && !reqHead.isEmpty()) {
        response.setHeader("Access-Control-Allow-Headers", reqHead);
      }

      if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        // ignore to allow 'preflight' requests from AJAX applications
        // in different origin (domain name)
      } else {

        String tenantIdentifier = request.getHeader(this.tenantRequestHeader);

        if (org.apache.commons.lang3.StringUtils.isBlank(tenantIdentifier)) {
          tenantIdentifier = request.getParameter("tenantIdentifier");
        }

        if (tenantIdentifier == null && this.exceptionIfHeaderMissing) {
          throw new InvalidTenantIdentifierException(
              "No tenant identifier found: Add request header of '"
                  + this.tenantRequestHeader
                  + "' or add the parameter 'tenantIdentifier' to query string of request URL.");
        }

        String pathInfo = request.getRequestURI();
        boolean isReportRequest = false;
        if (pathInfo != null && pathInfo.contains("report")) {
          isReportRequest = true;
        }
        final FineractPlatformTenant tenant =
            this.basicAuthTenantDetailsService.loadTenantById(tenantIdentifier, isReportRequest);
        ThreadLocalContextUtil.setTenant(tenant);
        HashMap<BusinessDateType, LocalDate> businessDates =
            this.businessDateReadPlatformService.getBusinessDates();
        ThreadLocalContextUtil.setBusinessDates(businessDates);

        String authToken = parseJwt(request);
        if (authToken != null) {
          if (jwtUtil.validate(authToken)) {
            String username = jwtUtil.getUsername(authToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
            ThreadLocalContextUtil.setAuthToken(authToken);
          }
        }

        if (!firstRequestProcessed.get()) {
          final String baseUrl =
              request.getRequestURL().toString().replace(request.getPathInfo(), "/");
          System.setProperty("baseUrl", baseUrl);

          final boolean ehcacheEnabled = this.configurationDomainService.isEhcacheEnabled();
          if (ehcacheEnabled) {
            this.cacheWritePlatformService.switchToCache(CacheType.SINGLE_NODE);
          } else {
            this.cacheWritePlatformService.switchToCache(CacheType.NO_CACHE);
          }
          firstRequestProcessed.set(true);
        }
      }

      super.doFilterInternal(request, response, filterChain);
    } catch (final InvalidTenantIdentifierException e) {
      // deal with exception at low level
       handleFilterException(HttpServletResponse.SC_BAD_REQUEST, response, e);
    } catch (ExpiredJwtException e) {
        handleFilterException(HttpServletResponse.SC_UNAUTHORIZED, response, e);
    } catch (JwtException e) {
        handleFilterException(HttpServletResponse.SC_FORBIDDEN, response, e);
    } finally {
      task.stop();
      final PlatformRequestLog log = PlatformRequestLog.from(task, request);
      LOG.debug("{}", this.toApiJsonSerializer.serialize(log));
    }
  }

  private void handleFilterException(
      int httpStatusCode, HttpServletResponse response, Exception exception) throws IOException {
    // deal with exception at low level
    SecurityContextHolder.getContext().setAuthentication(null);
    response.addHeader("WWW-Authenticate",
            "Bearer realm=\"" + "Fineract Platform API" + "\", error_description=\"" + exception.getMessage() + "\"");
    response.sendError(httpStatusCode, exception.getMessage());
  }

  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");
    if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
      return headerAuth.replaceFirst("Bearer ", "");
    }
    return null;
  }

  // TODO: - Innocent add login history e.t.c
  @Override
  protected void onSuccessfulAuthentication(
      HttpServletRequest request, HttpServletResponse response, Authentication authResult)
      throws IOException {
    super.onSuccessfulAuthentication(request, response, authResult);
    AppUser user = (AppUser) authResult.getPrincipal();

    if (notificationReadPlatformService.hasUnreadNotifications(user.getId())) {
      response.addHeader("X-Notification-Refresh", "true");
    } else {
      response.addHeader("X-Notification-Refresh", "false");
    }

    String pathURL = request.getRequestURI();
    boolean isSelfServiceRequest = pathURL != null && pathURL.contains("/self/");

    boolean notAllowed =
        (isSelfServiceRequest && !user.isSelfServiceUser())
            || (!isSelfServiceRequest && user.isSelfServiceUser());

    if (notAllowed) {
      throw new BadCredentialsException("User not authorised to use the requested resource.");
    }
  }

  // TODO: - Innocent add login failure attempts e.t.c
  @Override
  protected void onUnsuccessfulAuthentication(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
      throws IOException {
    super.onUnsuccessfulAuthentication(request, response, failed);
  }
}
