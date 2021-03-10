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

package org.openhab.binding.sonoff.internal.dto.api;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author David Murton - Initial contribution
 */
public class Config implements Serializable {

    @SerializedName("hb")
    @Expose
    private Integer hb;
    @SerializedName("hbInterval")
    @Expose
    private Integer hbInterval;
    @SerializedName("hundredDaysKwhData")
    @Expose
    private String hundredDaysKwhData;
    private static final long serialVersionUID = -2605312819381631092L;

    public Integer getHb() {
        return hb;
    }

    public void setHb(Integer hb) {
        this.hb = hb;
    }

    public Integer getHbInterval() {
        return hbInterval;
    }

    public void setHbInterval(Integer hbInterval) {
        this.hbInterval = hbInterval;
    }

    public String getHundredDaysKwhData() {
        return hundredDaysKwhData;
    }

    public void setHundredDaysKwhData(String hundredDaysKwhData) {
        this.hundredDaysKwhData = hundredDaysKwhData;
    }
}
