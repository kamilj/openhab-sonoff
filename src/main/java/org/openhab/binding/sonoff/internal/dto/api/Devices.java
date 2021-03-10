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
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author David Murton - Initial contribution
 */
public class Devices implements Serializable {

    @SerializedName("error")
    @Expose
    private Integer error;
    @SerializedName("devicelist")
    @Expose
    private List<Device> devicelist = new ArrayList<Device>();
    private static final long serialVersionUID = 8145552471787779538L;

    public Integer getError() {
        return error;
    }

    public void setError(Integer error) {
        this.error = error;
    }

    public List<Device> getDevicelist() {
        return devicelist;
    }

    public void setDevicelist(List<Device> devicelist) {
        this.devicelist = devicelist;
    }
}
