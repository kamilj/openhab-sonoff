/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.sonoff.internal.dto.payloads;

/**
 * @author David Murton - Initial contribution
 */
public class ApiLoginRequest {

    // private String appid = DtoHelper.appid;
    // private Long ts = DtoHelper.getTs();
    // private Integer version = DtoHelper.version;
    // private String nonce = DtoHelper.getNonce();
    private String email;
    // private String phoneNumber;
    private String password;
    private String countryCode;

    public String getCountryCode() {
        return this.countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" + "email='" + email + '\'' + ", password='" + password + '\'' + ", countryCode='"
                + countryCode + '\'' + '}';
    }
}
