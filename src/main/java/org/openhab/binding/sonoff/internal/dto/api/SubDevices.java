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

package org.openhab.binding.sonoff.internal.dto.api;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author David Murton - Initial contribution
 */
public class SubDevices implements Serializable {

    @SerializedName("subDevId")
    @Expose
    private String subDevId;
    @SerializedName("deviceid")
    @Expose
    private String deviceid;
    @SerializedName("uiid")
    @Expose
    private String uiid;
    private static final long serialVersionUID = 1205249124971729170L;

    public String getSubDevId() {
        return this.subDevId;
    }

    public void setSubDevId(String subDevId) {
        this.subDevId = subDevId;
    }

    public String getDeviceid() {
        return this.deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getUiid() {
        return this.uiid;
    }

    public void setUiid(String uiid) {
        this.uiid = uiid;
    }
}
