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
package org.openhab.binding.sonoff.internal.helpers;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;
import org.openhab.binding.sonoff.internal.states.State1;
import org.openhab.binding.sonoff.internal.states.State102;
import org.openhab.binding.sonoff.internal.states.State15;
import org.openhab.binding.sonoff.internal.states.State1770;
import org.openhab.binding.sonoff.internal.states.State2;
import org.openhab.binding.sonoff.internal.states.State2026;
import org.openhab.binding.sonoff.internal.states.State28;
import org.openhab.binding.sonoff.internal.states.State3;
import org.openhab.binding.sonoff.internal.states.State32;
import org.openhab.binding.sonoff.internal.states.State4;
import org.openhab.binding.sonoff.internal.states.State5;
import org.openhab.binding.sonoff.internal.states.State59;
import org.openhab.binding.sonoff.internal.states.State66;
import org.openhab.binding.sonoff.internal.states.State77;
import org.openhab.binding.sonoff.internal.states.StateBase;

/**
 * The {@link States} provides the current states to the message system and devices
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class States {

    private static ConcurrentMap<String, StateBase> states = new ConcurrentHashMap<String, StateBase>();

    public static @Nullable ConcurrentMap<String, StateBase> getStates() {
        return states;
    }

    public static @Nullable StateBase getState(String deviceid) {
        return states.get(deviceid);
    }

    private static void setState(String deviceid, StateBase state) {
        states.putIfAbsent(deviceid, state);
    }

    public static @Nullable List<?> getSubDevices(String deviceid) {
        StateBase state = getState(deviceid);
        if (state != null) {
            return state.getSubDevices();
        } else {
            return null;
        }
    }

    // Add child device listener
    @SuppressWarnings("rawtypes")
    public static void setDeviceListener(String deviceid, @Nullable DeviceListener listener) {
        StateBase state = getState(deviceid);
        if (state != null) {
            state.setDeviceListener(listener);
        } else {
        }
    }

    // remove child device listener
    public static void removeDeviceListener(String deviceid) {
        StateBase state = getState(deviceid);
        if (state != null) {
            state.setDeviceListener(null);
        } else {
        }
    }

    public static void addState(Device device) {
        Integer uiid = device.getExtra().getUiid();
        String deviceid = device.getDeviceid();
        switch (uiid) {
            case 1:
            case 6:
            case 24:
            case 27:
            case 81:
            case 107:
                setState(deviceid, new State1(device));
                break;
            case 2:
            case 7:
            case 29:
            case 82:
                setState(deviceid, new State2(device));
                break;
            case 3:
            case 8:
            case 30:
            case 83:
                setState(deviceid, new State3(device));
                break;
            case 4:
            case 9:
            case 31:
            case 84:
                setState(deviceid, new State4(device));
                break;
            case 5:
                setState(deviceid, new State5(device));
                break;
            case 15:
                setState(deviceid, new State15(device));
                break;
            case 28:
                setState(deviceid, new State28(device));
                break;
            case 32:
                setState(deviceid, new State32(device));
                break;
            case 59:
                setState(deviceid, new State59(device));
                break;
            case 66:
                setState(deviceid, new State66(device));
                break;
            case 77:
            case 78:
                setState(deviceid, new State77(device));
                break;
            case 102:
                setState(deviceid, new State102(device));
                break;

            case 1770:
                setState(deviceid, new State1770(device));
                break;
            case 2026:
                setState(deviceid, new State2026(device));
                break;
        }
    }
}
