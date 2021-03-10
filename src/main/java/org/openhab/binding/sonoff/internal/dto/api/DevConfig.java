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
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author David Murton - Initial contribution
 */
public class DevConfig implements Serializable {

    @SerializedName("remoteInfos")
    @Expose
    private List<RemoteInfo> remoteInfos = null;
    @SerializedName("lowVolAlarm")
    @Expose
    private Double lowVolAlarm;
    private static final long serialVersionUID = 2598343375148836997L;

    public List<RemoteInfo> getRemoteInfos() {
        return remoteInfos;
    }

    public void setRemoteInfos(List<RemoteInfo> remoteInfos) {
        this.remoteInfos = remoteInfos;
    }

    public Double getLowVolAlarm() {
        return lowVolAlarm;
    }

    public void setLowVolAlarm(Double lowVolAlarm) {
        this.lowVolAlarm = lowVolAlarm;
    }
}
