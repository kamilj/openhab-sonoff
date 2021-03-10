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

import java.util.HashMap;
import java.util.Map;

/**
 * @author David Murton - Initial contribution
 */
public class ClientInfo {

    private String model;
    private String os;
    private String imei;
    private String romVersion;
    private String appVersion;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getRomVersion() {
        return romVersion;
    }

    public void setRomVersion(String romVersion) {
        this.romVersion = romVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return "ClientInfo{" + "model='" + model + '\'' + ", os='" + os + '\'' + ", imei='" + imei + '\''
                + ", romVersion='" + romVersion + '\'' + ", appVersion='" + appVersion + '\''
                + ", additionalProperties=" + additionalProperties + '}';
    }
}
