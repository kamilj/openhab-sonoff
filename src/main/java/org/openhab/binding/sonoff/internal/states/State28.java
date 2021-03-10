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
import org.openhab.binding.sonoff.internal.dto.api.RfList;
import org.openhab.binding.sonoff.internal.dto.api.ZyxInfo;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;

/**
 * The {@link State28} contains the state of a device with uiid 28
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class State28 extends StateBase {

    private @Nullable DeviceListener<State28> listener;
    // States (only keep what we need)
    private OnOffType networkLED = OnOffType.OFF;
    private QuantityType<Power> rssi = new QuantityType<Power>(0.0, DECIBEL_MILLIWATTS);
    private DateTimeType rf0 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf1 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf2 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf3 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf4 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf5 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf6 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf7 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf8 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf9 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf10 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf11 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf12 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf13 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf14 = new DateTimeType(System.currentTimeMillis() + "");
    private DateTimeType rf15 = new DateTimeType(System.currentTimeMillis() + "");
    private List<RfList> rfCodeList = new ArrayList<RfList>();
    private List<ZyxInfo> subDevices = new ArrayList<ZyxInfo>();

    public State28(Device device) {
        super(device);
        updateState(device);
    }

    @Override
    public void updateState(Device newDevice) {
        Boolean cloud = newDevice.getOnline();
        Integer rssi = newDevice.getParams().getRssi();
        String networkLED = newDevice.getParams().getSledOnline();
        String ipAddress = newDevice.getLocalAddress();
        // Offline states com in on the websocket in a different place so we need 2 cloud online parameters
        Boolean cloud2 = newDevice.getParams().getOnline();

        setCloud(cloud2 != null ? cloud2 : getCloud());
        setCloud(cloud != null ? cloud : getCloud());
        setRssi(rssi != null ? new QuantityType<Power>(newDevice.getParams().getRssi(), (DECIBEL_MILLIWATTS))
                : getRssi());
        setNetworkLED(networkLED != null ? networkLED.equals("on") ? OnOffType.ON : OnOffType.OFF : getNetworkLED());
        setIpAddress(ipAddress != null ? new StringType(ipAddress) : getIpAddress());
        setLocal(getIpAddress().toString().equals("") ? false : true);

        if (newDevice.getParams().getRfTrig0() != null) {
            rf0 = new DateTimeType(newDevice.getParams().getRfTrig0());
        }
        if (newDevice.getParams().getRfTrig1() != null) {
            rf1 = new DateTimeType(newDevice.getParams().getRfTrig1());
        }
        if (newDevice.getParams().getRfTrig2() != null) {
            rf2 = new DateTimeType(newDevice.getParams().getRfTrig2());
        }
        if (newDevice.getParams().getRfTrig3() != null) {
            rf3 = new DateTimeType(newDevice.getParams().getRfTrig3());
        }
        if (newDevice.getParams().getRfTrig4() != null) {
            rf4 = new DateTimeType(newDevice.getParams().getRfTrig4());
        }
        if (newDevice.getParams().getRfTrig5() != null) {
            rf5 = new DateTimeType(newDevice.getParams().getRfTrig5());
        }
        if (newDevice.getParams().getRfTrig6() != null) {
            rf6 = new DateTimeType(newDevice.getParams().getRfTrig6());
        }
        if (newDevice.getParams().getRfTrig7() != null) {
            rf7 = new DateTimeType(newDevice.getParams().getRfTrig7());
        }
        if (newDevice.getParams().getRfTrig8() != null) {
            rf8 = new DateTimeType(newDevice.getParams().getRfTrig8());
        }
        if (newDevice.getParams().getRfTrig9() != null) {
            rf9 = new DateTimeType(newDevice.getParams().getRfTrig9());
        }
        if (newDevice.getParams().getRfTrig10() != null) {
            rf10 = new DateTimeType(newDevice.getParams().getRfTrig10());
        }
        if (newDevice.getParams().getRfTrig11() != null) {
            rf11 = new DateTimeType(newDevice.getParams().getRfTrig11());
        }
        if (newDevice.getParams().getRfTrig12() != null) {
            rf12 = new DateTimeType(newDevice.getParams().getRfTrig12());
        }
        if (newDevice.getParams().getRfTrig13() != null) {
            rf13 = new DateTimeType(newDevice.getParams().getRfTrig13());
        }
        if (newDevice.getParams().getRfTrig14() != null) {
            rf14 = new DateTimeType(newDevice.getParams().getRfTrig14());
        }
        if (newDevice.getParams().getRfTrig15() != null) {
            rf15 = new DateTimeType(newDevice.getParams().getRfTrig15());
        }

        if (newDevice.getParams().getRfList() != null) {
            rfCodeList = newDevice.getParams().getRfList();
        }

        if (newDevice.getTags() != null) {
            if (newDevice.getTags().getZyxInfo() != null) {
                subDevices = newDevice.getTags().getZyxInfo();
            }
        }

        sendUpdate();
    }

    public void sendUpdate() {
        final DeviceListener<State28> listener = this.listener;
        if (listener != null) {
            listener.updateDevice(this);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setDeviceListener(@Nullable DeviceListener listener) {
        this.listener = (@Nullable DeviceListener<State28>) listener;
    }

    // Getters & Setters for states
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

    public DateTimeType getRf0() {
        return this.rf0;
    }

    public void setRf0(DateTimeType rf0) {
        this.rf0 = rf0;
    }

    public DateTimeType getRf1() {
        return this.rf1;
    }

    public void setRf1(DateTimeType rf1) {
        this.rf1 = rf1;
    }

    public DateTimeType getRf2() {
        return this.rf2;
    }

    public void setRf2(DateTimeType rf2) {
        this.rf2 = rf2;
    }

    public DateTimeType getRf3() {
        return this.rf3;
    }

    public void setRf3(DateTimeType rf3) {
        this.rf3 = rf3;
    }

    public DateTimeType getRf4() {
        return this.rf4;
    }

    public void setRf4(DateTimeType rf4) {
        this.rf4 = rf4;
    }

    public DateTimeType getRf5() {
        return this.rf5;
    }

    public void setRf5(DateTimeType rf5) {
        this.rf5 = rf5;
    }

    public DateTimeType getRf6() {
        return this.rf6;
    }

    public void setRf6(DateTimeType rf6) {
        this.rf6 = rf6;
    }

    public DateTimeType getRf7() {
        return this.rf7;
    }

    public void setRf7(DateTimeType rf7) {
        this.rf7 = rf7;
    }

    public DateTimeType getRf8() {
        return this.rf8;
    }

    public void setRf8(DateTimeType rf8) {
        this.rf8 = rf8;
    }

    public DateTimeType getRf9() {
        return this.rf9;
    }

    public void setRf9(DateTimeType rf9) {
        this.rf9 = rf9;
    }

    public DateTimeType getRf10() {
        return this.rf10;
    }

    public void setRf10(DateTimeType rf10) {
        this.rf10 = rf10;
    }

    public DateTimeType getRf11() {
        return this.rf11;
    }

    public void setRf11(DateTimeType rf11) {
        this.rf11 = rf11;
    }

    public DateTimeType getRf12() {
        return this.rf12;
    }

    public void setRf12(DateTimeType rf12) {
        this.rf12 = rf12;
    }

    public DateTimeType getRf13() {
        return this.rf13;
    }

    public void setRf13(DateTimeType rf13) {
        this.rf13 = rf13;
    }

    public DateTimeType getRf14() {
        return this.rf14;
    }

    public void setRf14(DateTimeType rf14) {
        this.rf14 = rf14;
    }

    public DateTimeType getRf15() {
        return this.rf15;
    }

    public void setRf15(DateTimeType rf15) {
        this.rf15 = rf15;
    }

    public List<RfList> getRfList() {
        return this.rfCodeList;
    }

    public void setRfList(List<RfList> rfCodeList) {
        this.rfCodeList = rfCodeList;
    }

    public void setSubDevices(List<ZyxInfo> subDevices) {
        this.subDevices = subDevices;
    }

    public @Nullable List<?> getSubDevices() {
        return subDevices;
    }
}
