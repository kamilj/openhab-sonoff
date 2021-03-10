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
public class RemoteInfo implements Serializable {

    @SerializedName("numberOfRemote")
    @Expose
    private Integer numberOfRemote;
    @SerializedName("fwVersion")
    @Expose
    private String fwVersion;
    private static final long serialVersionUID = -2235920435696114329L;

    public Integer getNumberOfRemote() {
        return numberOfRemote;
    }

    public void setNumberOfRemote(Integer numberOfRemote) {
        this.numberOfRemote = numberOfRemote;
    }

    public String getFwVersion() {
        return fwVersion;
    }

    public void setFwVersion(String fwVersion) {
        this.fwVersion = fwVersion;
    }
}
