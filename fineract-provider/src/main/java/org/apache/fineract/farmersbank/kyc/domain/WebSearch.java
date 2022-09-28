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

package org.apache.fineract.farmersbank.kyc.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.farmersbank.kyc.data.response.WebSearchResultResponse;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Entity
@Getter
@Setter
@Table(name = "m_client_screening_web_search")
public class WebSearch extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "title")
    private String title;

    @Column(name = "snippet")
    private String snippet;

    @Column(name = "mime")
    private String mime;

    @Column(name = "link")
    private String link;

    @Column(name = "kind")
    private String kind;

    @Column(name = "html_title")
    private String htmlTitle;

    @Column(name = "html_snippet")
    private String htmlSnippet;

    @Column(name = "html_formatted_url")
    private String htmlFormattedUrl;

    @Column(name = "formatted_url")
    private String formattedUrl;

    @Column(name = "file_format")
    private String fileFormat;

    @Column(name = "display_link")
    private String displayLink;

    @ManyToOne(optional = false)
    @JoinColumn(name = "screening_id", nullable = false)
    private ClientScreening clientScreening;

    public static Set<WebSearch> createNew(
            final ArrayList<WebSearchResultResponse> responses,
            final ClientScreening clientScreening
    ) {
        Set<WebSearch> webSearchSet = new HashSet<>();
        for(WebSearchResultResponse webSearch: responses) {
            WebSearch search = new WebSearch(
                    webSearch.title,
                    webSearch.snippet,
                    webSearch.mime,
                    webSearch.link,
                    webSearch.kind,
                    webSearch.htmlTitle,
                    webSearch.htmlSnippet,
                    webSearch.htmlFormattedUrl,
                    webSearch.formattedUrl,
                    webSearch.fileFormat,
                    webSearch.displayLink
            );
            search.setClientScreening(clientScreening);
            webSearchSet.add(search);
        }
        return webSearchSet;
    }

    public WebSearch(
            String title,
            String snippet,
            String mime,
            String link,
            String kind,
            String htmlTitle,
            String htmlSnippet,
            String htmlFormattedUrl,
            String formattedUrl,
            String fileFormat,
            String displayLink) {
        this.title = title;
        this.snippet = snippet;
        this.mime = mime;
        this.link = link;
        this.kind = kind;
        this.htmlTitle = htmlTitle;
        this.htmlSnippet = htmlSnippet;
        this.htmlFormattedUrl = htmlFormattedUrl;
        this.formattedUrl = formattedUrl;
        this.fileFormat = fileFormat;
        this.displayLink = displayLink;
    }

    protected WebSearch() {}
}
