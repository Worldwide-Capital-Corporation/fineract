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
package org.apache.fineract.portfolio.client.domain;

/**
 * Enum representation of client identifier status states.
 */
public enum ClientIdentifierStatus {

    INACTIVE(100, "clientIdentifierStatusType.inactive"), //
    PENDING(101, "clientIdentifierStatusType.pending"),
    ACTIVE(200, "clientIdentifierStatusType.active"), //
    INVALID(0, "clientIdentifierStatusType.invalid"),
    EXPIRED(201, "clientIdentifierStatusType.expired");

    private final Integer value;
    private final String code;

    public static ClientIdentifierStatus fromInt(final Integer statusValue) {

        ClientIdentifierStatus enumeration = ClientIdentifierStatus.INVALID;
        switch (statusValue) {
            case 100:
                enumeration = ClientIdentifierStatus.INACTIVE;
            break;
            case 101:
                enumeration = ClientIdentifierStatus.PENDING;
                break;
            case 200:
                enumeration = ClientIdentifierStatus.ACTIVE;
                break;
            case 201:
                enumeration = ClientIdentifierStatus.EXPIRED;
            break;
        }
        return enumeration;
    }

    ClientIdentifierStatus(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    // public boolean hasStateOf(final ClientIdentifierStatus state) {
    // return this.value.equals(state.getValue());
    // }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isInactive() {
        return this.value.equals(ClientIdentifierStatus.INACTIVE.getValue());
    }

    public boolean isPending() {
        return this.value.equals(ClientIdentifierStatus.PENDING.getValue());
    }

    public boolean isExpired() {
        return this.value.equals(ClientIdentifierStatus.EXPIRED.getValue());
    }

    public boolean isActive() {
        return this.value.equals(ClientIdentifierStatus.ACTIVE.getValue());
    }
}
