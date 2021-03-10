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
public class ButtonName implements Serializable {

    @SerializedName("0")
    @Expose
    private String b0;
    @SerializedName("1")
    @Expose
    private String b1;
    @SerializedName("2")
    @Expose
    private String b2;
    @SerializedName("3")
    @Expose
    private String b3;
    private static final long serialVersionUID = -5427089966108900064L;

    public String get0() {
        return b0;
    }

    public void set0(String b0) {
        this.b0 = b0;
    }

    public String get1() {
        return b1;
    }

    public void set1(String b1) {
        this.b1 = b1;
    }

    public String get2() {
        return b2;
    }

    public void set2(String b2) {
        this.b2 = b2;
    }

    public String get3() {
        return b3;
    }

    public void set3(String b3) {
        this.b3 = b3;
    }
}
