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

import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;

/**
 * The {@link State102} contains the state of a device with uiid 102
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class State102 extends StateBase {

    private @Nullable DeviceListener<State102> listener;

    public State102(Device device) {
        super(device);
        updateState(device);
    }

    // States (only keep what we need)
    private OnOffType switch0 = OnOffType.OFF;
    private QuantityType<Power> rssi = new QuantityType<Power>(0.0, DECIBEL_MILLIWATTS);
    private QuantityType<ElectricPotential> battery = new QuantityType<ElectricPotential>(0.0, (VOLT));
    private DateTimeType lastUpdate = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType actionTime = new DateTimeType(System.currentTimeMillis() + "");

    @Override
    public void updateState(Device newDevice) {
        Boolean cloud = newDevice.getOnline();
        String switch0 = newDevice.getParams().getSwitch();
        Integer rssi = newDevice.getParams().getRssi();
        Integer battery = newDevice.getParams().getBattery();
        String lastUpdate = newDevice.getParams().getLastUpdateTime();
        String actionTime = newDevice.getParams().getActionTime();
        // Offline states com in on the websocket in a different place so we need 2 cloud online parameters
        Boolean cloud2 = newDevice.getParams().getOnline();

        setCloud(cloud2 != null ? cloud2 : getCloud());
        setCloud(cloud != null ? cloud : getCloud());
        setSwitch0(switch0 != null ? switch0.equals("on") ? OnOffType.ON : OnOffType.OFF : getSwitch0());
        setRssi(rssi != null ? new QuantityType<Power>(newDevice.getParams().getRssi(), (DECIBEL_MILLIWATTS))
                : getRssi());
        setBattery(battery != null ? new QuantityType<ElectricPotential>(battery, VOLT) : getBattery());
        setLastUpdate(lastUpdate != null ? new DateTimeType(lastUpdate) : getLastUpdate());
        setActionTime(actionTime != null ? new DateTimeType(actionTime) : getActionTime());
        sendUpdate();
    }

    public void sendUpdate() {
        if (listener != null) {
            listener.updateDevice(this);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setDeviceListener(@Nullable DeviceListener listener) {
        this.listener = (@Nullable DeviceListener<State102>) listener;
    }

    // Getters & Setters for states

    public OnOffType getSwitch0() {
        return this.switch0;
    }

    public void setSwitch0(OnOffType switch0) {
        this.switch0 = switch0;
    }

    public QuantityType<Power> getRssi() {
        return this.rssi;
    }

    public void setRssi(QuantityType<Power> rssi) {
        this.rssi = rssi;
    }

    public QuantityType<ElectricPotential> getBattery() {
        return this.battery;
    }

    public void setBattery(QuantityType<ElectricPotential> battery) {
        this.battery = battery;
    }

    public DateTimeType getLastUpdate() {
        return this.lastUpdate;
    }

    public void setLastUpdate(DateTimeType lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public DateTimeType getActionTime() {
        return this.actionTime;
    }

    public void setActionTime(DateTimeType actionTime) {
        this.actionTime = actionTime;
    }

    public @Nullable List<?> getSubDevices() {
        return null;
    }
}
