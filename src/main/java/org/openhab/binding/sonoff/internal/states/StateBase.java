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
package org.openhab.binding.sonoff.internal.states;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;
import org.openhab.core.library.types.StringType;

/**
 * The {@link StateBase} contains the base state of a device
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public abstract class StateBase {

    // Main Parameters
    private final String deviceKey;
    private final Integer uiid;
    private final String deviceid;

    private String name = "";
    private String brand = "";
    private String model = "";
    private String fw = "";
    private String ssid = "";

    private StringType ipAddress = new StringType("");
    private Boolean local = false;
    private Boolean cloud = false;

    public StateBase(Device device) {
        this.deviceid = device.getDeviceid();
        this.uiid = device.getExtra().getUiid();
        this.deviceKey = device.getDevicekey();
        this.name = device.getName();
        this.brand = device.getBrandName();
        this.model = device.getProductModel();
        this.fw = device.getParams().getFwVersion();
        this.ssid = device.getParams().getSsid();
    }

    public Integer getUiid() {
        return this.uiid;
    }

    public String getDeviceid() {
        return this.deviceid;
    }

    public Boolean getLocal() {
        return this.local;
    }

    public void setLocal(Boolean local) {
        this.local = local;
    }

    public Boolean getCloud() {
        return this.cloud;
    }

    public void setCloud(Boolean cloud) {
        this.cloud = cloud;
    }

    public String getDeviceKey() {
        return this.deviceKey;
    }

    public StringType getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(StringType ipAddress) {
        this.ipAddress = ipAddress;
    }

    public abstract void updateState(Device newDevice);

    public abstract void sendUpdate();

    public abstract @Nullable List<?> getSubDevices();

    @SuppressWarnings("rawtypes")
    public abstract void setDeviceListener(@Nullable DeviceListener listener);

    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("Name", name);
        properties.put("Brand", brand);
        properties.put("Model", model);
        properties.put("FW Version", fw);
        properties.put("Device ID", deviceid);
        properties.put("Device Key", deviceKey);
        properties.put("UIID", uiid.toString());
        properties.put("deviceid", deviceid);
        properties.put("Connected To SSID", ssid);
        return properties;
    }
}
