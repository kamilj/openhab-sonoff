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
public class RfList implements Serializable {

    @SerializedName("rfChl")
    @Expose
    private Integer rfChl;
    @SerializedName("rfVal")
    @Expose
    private String rfVal;
    private static final long serialVersionUID = 8895724428882072316L;

    public Integer getRfChl() {
        return rfChl;
    }

    public void setRfChl(Integer rfChl) {
        this.rfChl = rfChl;
    }

    public String getRfVal() {
        return rfVal;
    }

    public void setRfVal(String rfVal) {
        this.rfVal = rfVal;
    }
}
