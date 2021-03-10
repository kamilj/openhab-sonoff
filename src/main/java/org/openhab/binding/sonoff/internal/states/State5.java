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

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;

/**
 * The {@link State5} contains the state of a device with uiid 5
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class State5 extends StateBase {

    private @Nullable DeviceListener<State5> listener;
    // States (only keep what we need)
    private OnOffType switch0 = OnOffType.OFF;
    private OnOffType networkLED = OnOffType.OFF;
    private QuantityType<Power> power = new QuantityType<Power>(0.0, WATT);
    private QuantityType<Power> rssi = new QuantityType<Power>(0.0, DECIBEL_MILLIWATTS);
    private QuantityType<Energy> todayKwh = new QuantityType<Energy>(0.0, KILOWATT_HOUR);
    private QuantityType<Energy> yesterdayKwh = new QuantityType<Energy>(0.0, KILOWATT_HOUR);
    private QuantityType<Energy> sevenKwh = new QuantityType<Energy>(0.0, KILOWATT_HOUR);
    private QuantityType<Energy> thirtyKwh = new QuantityType<Energy>(0.0, KILOWATT_HOUR);
    private QuantityType<Energy> hundredKwh = new QuantityType<Energy>(0.0, KILOWATT_HOUR);

    public State5(Device device) {
        super(device);
        updateState(device);
    }

    public void updateState(Device newDevice) {
        if (newDevice.getConfig() != null) {
            if (newDevice.getConfig().getHundredDaysKwhData() != null) {
                updateConsumption(newDevice);
            }
        } else {
            updateMain(newDevice);
        }
    }

    private void updateMain(Device newDevice) {
        Boolean cloud = newDevice.getOnline();
        String switch0 = newDevice.getParams().getSwitch();
        String power = newDevice.getParams().getPower();
        Integer rssi = newDevice.getParams().getRssi();
        String networkLED = newDevice.getParams().getSledOnline();
        String ipAddress = newDevice.getLocalAddress();
        // Offline states com in on the websocket in a different place so we need 2 cloud online parameters
        Boolean cloud2 = newDevice.getParams().getOnline();

        setCloud(cloud2 != null ? cloud2 : getCloud());
        setCloud(cloud != null ? cloud : getCloud());
        setSwitch0(switch0 != null ? switch0.equals("on") ? OnOffType.ON : OnOffType.OFF : getSwitch0());
        setPower(power != null ? new QuantityType<Power>(Float.parseFloat(newDevice.getParams().getPower()), WATT)
                : getPower());
        setRssi(rssi != null ? new QuantityType<Power>(newDevice.getParams().getRssi(), (DECIBEL_MILLIWATTS))
                : getRssi());
        setNetworkLED(networkLED != null ? networkLED.equals("on") ? OnOffType.ON : OnOffType.OFF : getNetworkLED());
        setIpAddress(ipAddress != null ? new StringType(ipAddress) : getIpAddress());
        setLocal(getIpAddress().toString().equals("") ? false : true);

        sendUpdate();
    }

    private void updateConsumption(Device newDevice) {
        String[] hexValues;
        String[] splitData = newDevice.getConfig().getHundredDaysKwhData().split("(?<=\\G.{6})");
        double total = 0.00;
        for (int i = 0; i < 100; i++) {
            hexValues = splitData[i].split("(?<=\\G.{2})");
            total = total + Double.parseDouble(Integer.parseInt(hexValues[0], 16) + "."
                    + Integer.parseInt(hexValues[1], 16) + Integer.parseInt(hexValues[2], 16));
            if (i == 0) {
                setTodayKwh(new QuantityType<Energy>(total, (KILOWATT_HOUR)));
            }
            if (i == 1) {
                double newtotal = Double.parseDouble(Integer.parseInt(hexValues[0], 16) + "."
                        + Integer.parseInt(hexValues[1], 16) + Integer.parseInt(hexValues[2], 16));
                setYesterdayKwh(new QuantityType<Energy>(newtotal, (KILOWATT_HOUR)));
            }
            if (i == 6) {
                setSevenKwh(new QuantityType<Energy>(total, (KILOWATT_HOUR)));
            }
            if (i == 29) {
                setThirtyKwh(new QuantityType<Energy>(total, (KILOWATT_HOUR)));
            }
            if (i == 99) {
                setHundredKwh(new QuantityType<Energy>(total, (KILOWATT_HOUR)));
            }
        }
        sendUpdate();
    }

    public void sendUpdate() {
        final DeviceListener<State5> listener = this.listener;
        if (listener != null) {
            listener.updateDevice(this);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setDeviceListener(@Nullable DeviceListener listener) {
        this.listener = (@Nullable DeviceListener<State5>) listener;
    }

    // Getters & Setters for states

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

    public QuantityType<Power> getPower() {
        return this.power;
    }

    public void setPower(QuantityType<Power> power) {
        this.power = power;
    }

    public QuantityType<Power> getRssi() {
        return this.rssi;
    }

    public void setRssi(QuantityType<Power> rssi) {
        this.rssi = rssi;
    }

    public QuantityType<Energy> getTodayKwh() {
        return this.todayKwh;
    }

    public void setTodayKwh(QuantityType<Energy> todayKwh) {
        this.todayKwh = todayKwh;
    }

    public QuantityType<Energy> getYesterdayKwh() {
        return this.yesterdayKwh;
    }

    public void setYesterdayKwh(QuantityType<Energy> yesterdayKwh) {
        this.yesterdayKwh = yesterdayKwh;
    }

    public QuantityType<Energy> getSevenKwh() {
        return this.sevenKwh;
    }

    public void setSevenKwh(QuantityType<Energy> sevenKwh) {
        this.sevenKwh = sevenKwh;
    }

    public QuantityType<Energy> getThirtyKwh() {
        return this.thirtyKwh;
    }

    public void setThirtyKwh(QuantityType<Energy> thirtyKwh) {
        this.thirtyKwh = thirtyKwh;
    }

    public QuantityType<Energy> getHundredKwh() {
        return this.hundredKwh;
    }

    public void setHundredKwh(QuantityType<Energy> hundredKwh) {
        this.hundredKwh = hundredKwh;
    }

    public @Nullable List<?> getSubDevices() {
        return null;
    }
}
