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

package org.openhab.binding.sonoff.internal.dto.payloads;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author David Murton - Initial contribution
 */
public class RGBLight implements Serializable {

    @SerializedName("mode")
    @Expose
    private Integer mode;
    @SerializedName("colorR")
    @Expose
    private Integer colorR;
    @SerializedName("colorG")
    @Expose
    private Integer colorG;
    @SerializedName("colorB")
    @Expose
    private Integer colorB;
    @SerializedName("bright")
    @Expose
    private Integer bright;

    private static final long serialVersionUID = 5376512912986765998L;

    public Integer getMode() {
        return this.mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public Integer getColorR() {
        return this.colorR;
    }

    public void setColorR(Integer colorR) {
        this.colorR = colorR;
    }

    public Integer getColorG() {
        return this.colorG;
    }

    public void setColorG(Integer colorG) {
        this.colorG = colorG;
    }

    public Integer getColorB() {
        return this.colorB;
    }

    public void setColorB(Integer colorB) {
        this.colorB = colorB;
    }

    public Integer getBright() {
        return this.bright;
    }

    public void setBright(Integer bright) {
        this.bright = bright;
    }
}
