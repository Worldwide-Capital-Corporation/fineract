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
import org.apache.fineract.farmersbank.kyc.data.response.ResultEntityResponse;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Entity
@Getter
@Setter
@Table(name = "m_client_screening_result_entity")
public class ScreeningResultEntity extends AbstractAuditableWithUTCDateTimeCustom {

  @Column(name = "unique_id")
  private Long uniqueId;

  @Column(name = "category")
  private String category;

  @Column(name = "categories")
  private String categories;

  @Column(name = "subcategory")
  private String subcategory;

  @Column(name = "gender")
  private String gender;

  @Column(name = "deceased")
  private String deceased;

  @Column(name = "primary_first_name")
  private String primaryFirstName;

  @Column(name = "primary_middle_name")
  private String primaryMiddleName;

  @Column(name = "primary_last_name")
  private String primaryLastName;

  @Column(name = "title")
  private String title;

  @Column(name = "position")
  private String position;

  @Column(name = "date_of_birth")
  private String dateOfBirth;

  @Column(name = "deceased_date")
  private String deceasedDate;

  @Column(name = "place_of_birth")
  private String placeOfBirth;

  @Column(name = "primary_location")
  private String primaryLocation;

  @Column(name = "image")
  private String image;

  @Column(name = "further_information")
  private String furtherInformation;

  @Column(name = "enter_date")
  private String enterDate;

  @Column(name = "last_reviewed")
  private String lastReviewed;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "general_info_id")
  private ScreeningGeneralInfo generalInfo;

  @OneToMany(mappedBy = "resultEntity", cascade = CascadeType.ALL)
  private Set<Description> descriptions = new HashSet<>();

  @OneToMany(mappedBy = "resultEntity", cascade = CascadeType.ALL)
  private Set<ScreeningJobHistory> rolesHistory = new HashSet<>();

  @OneToMany(mappedBy = "resultEntity", cascade = CascadeType.ALL)
  private Set<ScreeningIdNumbers> idNumbers = new HashSet<>();

  @OneToMany(mappedBy = "resultEntity", cascade = CascadeType.ALL)
  private Set<LinkedIndividual> linkedIndividuals = new HashSet<>();

  @OneToMany(mappedBy = "resultEntity", cascade = CascadeType.ALL)
  private Set<LinkedCompany> linkedCompanies = new HashSet<>();

  protected ScreeningResultEntity() {}

  public ScreeningResultEntity(
        Long uniqueId,
        String category,
        String categories,
        String subcategory,
        String gender,
        String deceased,
        String primaryFirstName,
        String primaryMiddleName,
        String primaryLastName,
        String title,
        String position,
        String dateOfBirth,
        String deceasedDate,
        String placeOfBirth,
        String primaryLocation,
        String image,
        String furtherInformation,
        String enterDate,
        String lastReviewed) {
    this.uniqueId = uniqueId;
    this.category = category;
    this.categories = categories;
    this.subcategory = subcategory;
    this.gender = gender;
    this.deceased = deceased;
    this.primaryFirstName = primaryFirstName;
    this.primaryMiddleName = primaryMiddleName;
    this.primaryLastName = primaryLastName;
    this.title = title;
    this.position = position;
    this.dateOfBirth = dateOfBirth;
    this.deceasedDate = deceasedDate;
    this.placeOfBirth = placeOfBirth;
    this.primaryLocation = primaryLocation;
    this.image = image;
    this.furtherInformation = furtherInformation;
    this.enterDate = enterDate;
    this.lastReviewed = lastReviewed;
  }

  public static ScreeningResultEntity createNewFrom(ResultEntityResponse response) {
    ScreeningResultEntity resultEntity = new ScreeningResultEntity(
              response.uniqueId,
              response.category,
              response.categories,
              response.subcategory,
              response.gender,
              response.deceased,
              response.primaryFirstName,
              response.primaryMiddleName,
              response.primaryLastName,
              response.title,
              response.position,
              response.dateOfBirth,
              response.deceasedDate,
              response.placeOfBirth,
              response.primaryLocation,
              response.image,
              response.furtherInformation,
              response.enterDate,
              response.lastReviewed
      );
      if (response.generalInfo != null)
        resultEntity.setGeneralInfo(ScreeningGeneralInfo.createNew(response.generalInfo));
      return resultEntity;
  }
}
