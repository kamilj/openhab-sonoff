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
package org.openhab.binding.sonoff.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.*;
import org.openhab.binding.sonoff.internal.states.State1770;

import com.google.gson.Gson;

/**
 * The {@link Handler1770} is responsible for updates and handling commands to/from Zigbee Devices
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class Handler1770 extends HandlerBaseZigbee<State1770> {

    public Handler1770(Thing thing, Gson gson) {
        super(thing, gson);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void updateDevice(State1770 newDevice) {
        // Motion
        updateState("battery", newDevice.getBattery());
        updateState("trigTime", newDevice.getTrigTime());
        updateState("temperature", newDevice.getTemperature());
        updateState("humidity", newDevice.getHumidity());
        // Connections
        Handler66 bridge = getZigbeeHandler();
        if (bridge != null) {
            HandlerAccount account = bridge.getAccountHandler();
            if (account != null) {
                String mode = account.getMode();
                setCloud(mode.equals("local") ? false : newDevice.getCloud());
            }
        }
        updateState("cloudOnline", getCloud() ? new StringType("Connected") : new StringType("Disconnected"));
        isConnected();
    }
}
