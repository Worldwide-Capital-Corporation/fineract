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

package org.apache.fineract.farmersbank.config;

import org.apache.fineract.farmersbank.filters.FarmersBankJWTAuthenticationFilter;
import org.apache.fineract.farmersbank.service.FarmersBankUserDetailsService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.instancemode.filter.FineractInstanceModeApiFilter;
import org.apache.fineract.infrastructure.security.filter.TwoFactorAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class FarmersBankSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private FarmersBankUserDetailsService userDetailsService;

    @Autowired
    private TwoFactorAuthenticationFilter twoFactorAuthenticationFilter;

    @Autowired
    private FineractInstanceModeApiFilter fineractInstanceModeApiFilter;

    @Autowired
    private FineractProperties fineractProperties;

    @Autowired
    private ServerProperties serverProperties;

    private static final String API_PATH = "/api/**";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http //
                .csrf().disable() // NOSONAR only creating a service that is used by non-browser clients
                .antMatcher(API_PATH).authorizeRequests() //
                .antMatchers(HttpMethod.OPTIONS, API_PATH).permitAll() //
                .antMatchers(HttpMethod.POST, "/api/*/echo").permitAll() //
                .antMatchers(HttpMethod.POST, "/api/*/authentication").permitAll() //
                .antMatchers(HttpMethod.POST, "/api/*/self/authentication").permitAll() //
                .antMatchers(HttpMethod.POST, "/api/*/self/registration").permitAll() //
                .antMatchers(HttpMethod.POST, "/api/*/self/registration/user").permitAll() //
                .antMatchers(HttpMethod.PUT, "/api/*/instance-mode").permitAll() //
                .antMatchers(HttpMethod.POST, "/api/*/twofactor/validate").fullyAuthenticated() //
                .antMatchers("/api/*/twofactor").fullyAuthenticated() //
                .antMatchers(API_PATH).access("isFullyAuthenticated() and hasAuthority('TWOFACTOR_AUTHENTICATED')").and() //
                .httpBasic() //
                .authenticationEntryPoint(basicAuthenticationEntryPoint()) //
                .and() //
                .sessionManagement() //
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //
                .and() //
                .addFilterAfter(fineractInstanceModeApiFilter, SecurityContextPersistenceFilter.class) //
                .addFilterAfter(authenticationFilter(), FineractInstanceModeApiFilter.class) //
                .addFilterAfter(twoFactorAuthenticationFilter, BasicAuthenticationFilter.class); //

        if (serverProperties.getSsl().isEnabled()) {
            http.requiresChannel(channel -> channel.antMatchers(API_PATH).requiresSecure());
        }
    }

    @Bean
    public FarmersBankJWTAuthenticationFilter authenticationFilter() throws Exception {
        return new FarmersBankJWTAuthenticationFilter(authenticationManagerBean(), basicAuthenticationEntryPoint());
    }

    @Bean
    public BasicAuthenticationEntryPoint basicAuthenticationEntryPoint() {
        BasicAuthenticationEntryPoint basicAuthenticationEntryPoint = new BasicAuthenticationEntryPoint();
        basicAuthenticationEntryPoint.setRealmName("Fineract Platform API");
        return basicAuthenticationEntryPoint;
    }

    @Bean(name = "customAuthenticationProvider")
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider());
        auth.eraseCredentials(false);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public FilterRegistrationBean<FarmersBankJWTAuthenticationFilter> authenticationFilterRegistration()
            throws Exception {
        FilterRegistrationBean<FarmersBankJWTAuthenticationFilter> registration = new FilterRegistrationBean<>(
                authenticationFilter());
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<TwoFactorAuthenticationFilter> twoFactorAuthenticationFilterRegistration() {
        FilterRegistrationBean<TwoFactorAuthenticationFilter> registration = new FilterRegistrationBean<>(
                twoFactorAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }
}
