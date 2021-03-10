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

import java.util.List;

import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;

/**
 * The {@link State1770} contains the state of a device with uiid 1770
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class State1770 extends StateBase {

    private @Nullable DeviceListener<State1770> listener;
    // States (only keep what we need)
    private QuantityType<ElectricPotential> battery = new QuantityType<ElectricPotential>(0.0, (VOLT));
    private DateTimeType trigTime = new DateTimeType(System.currentTimeMillis() + "");
    private QuantityType<Temperature> temperature = new QuantityType<Temperature>(0.0, KELVIN);
    private PercentType humidity = new PercentType(0);

    public State1770(Device device) {
        super(device);
        updateState(device);
    }

    @Override
    public void updateState(Device newDevice) {
        Boolean cloud = newDevice.getOnline();
        Integer battery = newDevice.getParams().getBattery();
        String trigTime = newDevice.getParams().getTrigTime();
        Integer temperature = newDevice.getParams().getTemperature();
        Integer humidity = newDevice.getParams().getHumidity();
        // Offline states com in on the websocket in a different place so we need 2 cloud online parameters
        Boolean cloud2 = newDevice.getParams().getOnline();

        setCloud(cloud2 != null ? cloud2 : getCloud());
        setCloud(cloud != null ? cloud : getCloud());
        setBattery(battery != null ? new QuantityType<ElectricPotential>(battery, VOLT) : getBattery());
        setTrigTime(trigTime != null ? new DateTimeType(trigTime) : getTrigTime());
        setTemperature(
                temperature != null ? new QuantityType<Temperature>(temperature / 100, KELVIN) : getTemperature());
        setHumidity(humidity != null ? new PercentType((int) humidity / 100) : getHumidity());
        sendUpdate();
    }

    public void sendUpdate() {
        final DeviceListener<State1770> listener = this.listener;
        if (listener != null) {
            listener.updateDevice(this);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setDeviceListener(@Nullable DeviceListener listener) {
        this.listener = (@Nullable DeviceListener<State1770>) listener;
    }

    // Getters & Setters for states
    public QuantityType<ElectricPotential> getBattery() {
        return this.battery;
    }

    public void setBattery(QuantityType<ElectricPotential> battery) {
        this.battery = battery;
    }

    public DateTimeType getTrigTime() {
        return this.trigTime;
    }

    public void setTrigTime(DateTimeType trigTime) {
        this.trigTime = trigTime;
    }

    public QuantityType<Temperature> getTemperature() {
        return this.temperature;
    }

    public void setTemperature(QuantityType<Temperature> temperature) {
        this.temperature = temperature;
    }

    public PercentType getHumidity() {
        return this.humidity;
    }

    public void setHumidity(PercentType humidity) {
        this.humidity = humidity;
    }

    public @Nullable List<?> getSubDevices() {
        return null;
    }
}
