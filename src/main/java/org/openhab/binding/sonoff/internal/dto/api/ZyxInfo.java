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
public class ZyxInfo implements Serializable {

    @SerializedName("remote_type")
    @Expose
    private String remoteType;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("buttonName")
    @Expose
    private List<ButtonName> buttonName = null;
    private static final long serialVersionUID = 6008423529967350538L;

    public String getRemoteType() {
        return remoteType;
    }

    public void setRemoteType(String remoteType) {
        this.remoteType = remoteType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ButtonName> getButtonName() {
        return buttonName;
    }

    public void setButtonName(List<ButtonName> buttonName) {
        this.buttonName = buttonName;
    }
}
