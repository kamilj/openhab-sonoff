/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.io.Serializable;

import org.openhab.binding.sonoff.internal.helpers.DtoHelper;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author David Murton - Initial contribution
 */
public class ApiStatusGet implements Serializable {

    @SerializedName("action")
    @Expose
    private String action = "query";
    @SerializedName("deviceid")
    @Expose
    private String deviceid;
    @SerializedName("apikey")
    @Expose
    private String apikey;
    @SerializedName("userAgent")
    @Expose
    private String userAgent = "app";
    @SerializedName("params")
    @Expose
    private String params;
    @SerializedName("selfApikey")
    @Expose
    private String selfApikey;
    @SerializedName("ts")
    @Expose
    private Long ts = DtoHelper.getTs() / 1000;
    @SerializedName("sequence")
    @Expose
    private Long sequence = DtoHelper.getSequence();
    @SerializedName("version")
    @Expose
    private Integer version = DtoHelper.VERSION;
    @SerializedName("nonce")
    @Expose
    private String nonce = DtoHelper.getNonce();
    private static final long serialVersionUID = 2958983062170484126L;

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getTs() {
        return this.ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getApikey() {
        return this.apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }
}
