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

import org.openhab.binding.sonoff.internal.helpers.DtoHelper;

/**
 * @author David Murton - Initial contribution
 */
public class ApiRegionCode {

    private String appid = DtoHelper.APPID;
    private String nonce = DtoHelper.getNonce();
    private Long ts = DtoHelper.getTs();
    private Integer version = DtoHelper.VERSION;
    private String countryCode;

    @Override
    public String toString() {
        return "ApiRegionCode{" + "appid='" + appid + '\'' + ", country_code='" + countryCode + '\'' + ", nonce='"
                + nonce + '\'' + ", ts='" + ts + '\'' + ", version='" + version + '\'' + '}';
    }

    public Long getTs() {
        return this.ts;
    }

    public Integer getVersion() {
        return this.version;
    }

    public String getAppid() {
        return this.appid;
    }

    public String getNonce() {
        return this.nonce;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
