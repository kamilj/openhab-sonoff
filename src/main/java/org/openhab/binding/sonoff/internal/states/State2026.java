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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;

/**
 * The {@link State2026} contains the state of a device with uiid 2026
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class State2026 extends StateBase {

    private @Nullable DeviceListener<State2026> listener;
    // States (only keep what we need)
    private OnOffType motion = OnOffType.OFF;
    private QuantityType<ElectricPotential> battery = new QuantityType<ElectricPotential>(0.0, (VOLT));
    private DateTimeType trigTime = new DateTimeType(System.currentTimeMillis() + "");

    public State2026(Device device) {
        super(device);
        updateState(device);
    }

    @Override
    public void updateState(Device newDevice) {
        Boolean cloud = newDevice.getOnline();
        Integer motion = newDevice.getParams().getMotion();
        Integer battery = newDevice.getParams().getBattery();
        String trigTime = newDevice.getParams().getTrigTime();
        // Offline states com in on the websocket in a different place so we need 2 cloud online parameters
        Boolean cloud2 = newDevice.getParams().getOnline();

        setCloud(cloud2 != null ? cloud2 : getCloud());
        setCloud(cloud != null ? cloud : getCloud());
        setMotion(motion != null ? motion.equals(1) ? OnOffType.ON : OnOffType.OFF : getMotion());
        setBattery(battery != null ? new QuantityType<ElectricPotential>(battery, VOLT) : getBattery());
        setTrigTime(trigTime != null ? new DateTimeType(trigTime) : getTrigTime());

        sendUpdate();
    }

    public void sendUpdate() {
        if (listener != null) {
            listener.updateDevice(this);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setDeviceListener(@Nullable DeviceListener listener) {
        this.listener = (@Nullable DeviceListener<State2026>) listener;
    }

    // Getters & Setters for states
    public QuantityType<ElectricPotential> getBattery() {
        return this.battery;
    }

    public void setBattery(QuantityType<ElectricPotential> battery) {
        this.battery = battery;
    }

    public OnOffType getMotion() {
        return this.motion;
    }

    public void setMotion(OnOffType motion) {
        this.motion = motion;
    }

    public DateTimeType getTrigTime() {
        return this.trigTime;
    }

    public void setTrigTime(DateTimeType trigTime) {
        this.trigTime = trigTime;
    }

    public @Nullable List<?> getSubDevices() {
        return null;
    }
}
