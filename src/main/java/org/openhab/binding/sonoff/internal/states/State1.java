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
package org.openhab.binding.sonoff.internal.states;

import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.*;

import java.util.List;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;

/**
 * The {@link State1} contains the state of a device with uiid 1
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class State1 extends StateBase {

    private @Nullable DeviceListener<State1> listener;
    // States (only keep what we need)
    private OnOffType switch0 = OnOffType.OFF;
    private OnOffType networkLED = OnOffType.OFF;
    private QuantityType<Power> rssi = new QuantityType<Power>(0.0, DECIBEL_MILLIWATTS);

    public State1(Device device) {
        super(device);
        updateState(device);
    }

    public void updateState(Device newDevice) {
        Boolean cloud = newDevice.getOnline();
        String switch0 = newDevice.getParams().getSwitch();
        Integer rssi = newDevice.getParams().getRssi();
        String networkLED = newDevice.getParams().getSledOnline();
        String ipAddress = newDevice.getLocalAddress();
        // Offline states com in on the websocket in a different place so we need 2 cloud online parameters
        Boolean cloud2 = newDevice.getParams().getOnline();

        setCloud(cloud2 != null ? cloud2 : getCloud());
        setCloud(cloud != null ? cloud : getCloud());
        setSwitch0(switch0 != null ? switch0.equals("on") ? OnOffType.ON : OnOffType.OFF : getSwitch0());
        setRssi(rssi != null ? new QuantityType<Power>(newDevice.getParams().getRssi(), (DECIBEL_MILLIWATTS))
                : getRssi());
        setNetworkLED(networkLED != null ? networkLED.equals("on") ? OnOffType.ON : OnOffType.OFF : getNetworkLED());
        setIpAddress(ipAddress != null ? new StringType(ipAddress) : getIpAddress());
        setLocal(getIpAddress().toString().equals("") ? false : true);

        sendUpdate();
    }

    public void sendUpdate() {
        if (listener != null) {
            listener.updateDevice(this);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setDeviceListener(@Nullable DeviceListener listener) {
        this.listener = (@Nullable DeviceListener<State1>) listener;
    }

    public OnOffType getSwitch0() {
        return this.switch0;
    }

    public void setSwitch0(OnOffType switch0) {
        this.switch0 = switch0;
    }

    public OnOffType getNetworkLED() {
        return this.networkLED;
    }

    public void setNetworkLED(OnOffType networkLED) {
        this.networkLED = networkLED;
    }

    public QuantityType<Power> getRssi() {
        return this.rssi;
    }

    public void setRssi(QuantityType<Power> rssi) {
        this.rssi = rssi;
    }

    public @Nullable List<?> getSubDevices() {
        return null;
    }
}
