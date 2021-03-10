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
public class OnlyDevice implements Serializable {

    @SerializedName("ota")
    @Expose
    private String ota;
    private static final long serialVersionUID = 3674980726452527678L;

    public String getOta() {
        return ota;
    }

    public void setOta(String ota) {
        this.ota = ota;
    }
}
