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

import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;

/**
 * The {@link State15} contains the state of a device with uiid 15
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class State15 extends StateBase {

    private @Nullable DeviceListener<State15> listener;
    // States (only keep what we need)
    private OnOffType switch0 = OnOffType.OFF;
    private OnOffType networkLED = OnOffType.OFF;
    private QuantityType<Power> rssi = new QuantityType<Power>(0.0, DECIBEL_MILLIWATTS);
    private OnOffType mainSwitch = OnOffType.OFF;
    private StringType sensorType = new StringType();
    private QuantityType<Temperature> temperature = new QuantityType<Temperature>(0.0, KELVIN);
    private PercentType humidity = new PercentType(0);

    public State15(Device device) {
        super(device);
        updateState(device);
    }

    public void updateState(Device newDevice) {
        Boolean cloud = newDevice.getOnline();
        String switch0 = newDevice.getParams().getSwitch();
        Integer rssi = newDevice.getParams().getRssi();
        String networkLED = newDevice.getParams().getSledOnline();
        String temperature = newDevice.getParams().getCurrentTemperature();
        String humidity = newDevice.getParams().getCurrentHumidity();
        String mainSwitch = newDevice.getParams().getMainSwitch();
        String sensorType = newDevice.getParams().getSensorType();
        // Offline states com in on the websocket in a different place so we need 2 cloud online parameters
        Boolean cloud2 = newDevice.getParams().getOnline();

        setCloud(cloud2 != null ? cloud2 : getCloud());
        setCloud(cloud != null ? cloud : getCloud());
        setSwitch0(switch0 != null ? switch0.equals("on") ? OnOffType.ON : OnOffType.OFF : getSwitch0());
        setRssi(rssi != null ? new QuantityType<Power>(newDevice.getParams().getRssi(), (DECIBEL_MILLIWATTS))
                : getRssi());
        setNetworkLED(networkLED != null ? networkLED.equals("on") ? OnOffType.ON : OnOffType.OFF : getNetworkLED());
        setMainSwitch(mainSwitch != null ? mainSwitch.equals("on") ? OnOffType.ON : OnOffType.OFF : getMainSwitch());
        setTemperature(
                temperature != null
                        ? temperature.equals("unavailable") ? new QuantityType<Temperature>(0.0, KELVIN)
                                : new QuantityType<Temperature>(Float.parseFloat(temperature), KELVIN)
                        : getTemperature());
        setHumidity(humidity != null ? humidity.equals("unavailable") ? new PercentType(0) : new PercentType(humidity)
                : getHumidity());
        setSensorType(sensorType != null ? new StringType(sensorType) : getSensorType());

        sendUpdate();
    }

    public void sendUpdate() {
        final DeviceListener<State15> listener = this.listener;
        if (listener != null) {
            listener.updateDevice(this);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setDeviceListener(@Nullable DeviceListener listener) {
        this.listener = (@Nullable DeviceListener<State15>) listener;
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

    public OnOffType getMainSwitch() {
        return this.mainSwitch;
    }

    public void setMainSwitch(OnOffType mainSwitch) {
        this.mainSwitch = mainSwitch;
    }

    public StringType getSensorType() {
        return this.sensorType;
    }

    public void setSensorType(StringType sensorType) {
        this.sensorType = sensorType;
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
