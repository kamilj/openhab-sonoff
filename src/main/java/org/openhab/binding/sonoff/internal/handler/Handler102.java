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
package org.openhab.binding.sonoff.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sonoff.internal.states.State102;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

import com.google.gson.Gson;

/**
 * The {@link Handler102} allows the handling of commands and updates to Magnetic Switch Type Devices
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class Handler102 extends HandlerBaseCloud<State102> {

    public Handler102(Thing thing, Gson gson) {
        super(thing, gson);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void updateDevice(State102 newDevice) {
        // Switches
        updateState("switch", newDevice.getSwitch0());
        // Other
        updateState("rssi", newDevice.getRssi());
        // Action Times
        updateState("lastUpdate", newDevice.getLastUpdate());
        updateState("actionTime", newDevice.getActionTime());
        // Connections
        HandlerAccount account = getAccountHandler();
        if (account != null) {
            String mode = account.getMode();
            setCloud(mode.equals("local") ? false : newDevice.getCloud());
        }
        updateState("cloudOnline", getCloud() ? new StringType("Connected") : new StringType("Disconnected"));
        isConnected();
    }
}
