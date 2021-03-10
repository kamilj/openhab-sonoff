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

import static org.openhab.core.library.unit.Units.*;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.dto.api.SubDevices;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;

/**
 * The {@link State66} contains the state of a device with uiid 66
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class State66 extends StateBase {

    private @Nullable DeviceListener<State66> listener;
    // States (only keep what we need)
    private OnOffType networkLED = OnOffType.OFF;
    private OnOffType zigbeeLED = OnOffType.OFF;
    private QuantityType<Power> rssi = new QuantityType<Power>(0.0, DECIBEL_MILLIWATTS);
    private List<SubDevices> subDevices = new ArrayList<SubDevices>();

    public State66(Device device) {
        super(device);
        updateState(device);
    }

    @Override
    public void updateState(Device newDevice) {
        Boolean cloud = newDevice.getOnline();
        Integer rssi = newDevice.getParams().getRssi();
        String networkLED = newDevice.getParams().getSledOnline();
        String zigbeeLED = newDevice.getParams().getZled();
        // Offline states com in on the websocket in a different place so we need 2 cloud online parameters
        Boolean cloud2 = newDevice.getParams().getOnline();

        setCloud(cloud2 != null ? cloud2 : getCloud());
        setCloud(cloud != null ? cloud : getCloud());
        setRssi(rssi != null ? new QuantityType<Power>(newDevice.getParams().getRssi(), (DECIBEL_MILLIWATTS))
                : getRssi());
        setNetworkLED(networkLED != null ? networkLED.equals("on") ? OnOffType.ON : OnOffType.OFF : getNetworkLED());
        setZigbeeLED(zigbeeLED != null ? zigbeeLED.equals("on") ? OnOffType.ON : OnOffType.OFF : getZigbeeLED());

        if (newDevice.getParams().getSubDevices() != null) {
            subDevices = newDevice.getParams().getSubDevices();
        }

        sendUpdate();
    }

    public void sendUpdate() {
        final DeviceListener<State66> listener = this.listener;
        if (listener != null) {
            listener.updateDevice(this);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setDeviceListener(@Nullable DeviceListener listener) {
        this.listener = (@Nullable DeviceListener<State66>) listener;
    }

    // Getters & Setters for states

    public OnOffType getNetworkLED() {
        return this.networkLED;
    }

    public void setNetworkLED(OnOffType networkLED) {
        this.networkLED = networkLED;
    }

    public OnOffType getZigbeeLED() {
        return this.zigbeeLED;
    }

    public void setZigbeeLED(OnOffType zigbeeLED) {
        this.zigbeeLED = zigbeeLED;
    }

    public QuantityType<Power> getRssi() {
        return this.rssi;
    }

    public void setRssi(QuantityType<Power> rssi) {
        this.rssi = rssi;
    }

    public @Nullable List<?> getSubDevices() {
        return subDevices;
    }
}
