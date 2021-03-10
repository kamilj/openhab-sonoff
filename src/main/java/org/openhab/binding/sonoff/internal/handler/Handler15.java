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
import org.openhab.binding.sonoff.internal.dto.payloads.SLed;
import org.openhab.binding.sonoff.internal.dto.payloads.SingleSwitch;
import org.openhab.binding.sonoff.internal.states.State15;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.*;

import com.google.gson.Gson;

/**
 * The {@link Handler15} allows the handling of commands and updates to Devices with uuid's:
 * 1
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class Handler15 extends HandlerBaseCloud<State15> {

    private final Gson gson;

    public Handler15(Thing thing, Gson gson) {
        super(thing, gson);
        this.gson = gson;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String params = "";
        String endpoint = "";
        if (command instanceof RefreshType) {
            return;
        } else {
            switch (channelUID.getId()) {
                case "switch":
                    SingleSwitch singleSwitch = new SingleSwitch();
                    singleSwitch.setSwitch(command.toString().toLowerCase());
                    params = gson.toJson(singleSwitch);
                    endpoint = "switch";
                    break;
                case "sled":
                    SLed sled = new SLed();
                    sled.setSledOnline(command.toString().toLowerCase());
                    params = gson.toJson(sled);
                    endpoint = "sledOnline";
                    break;
            }
            sendUpdate(endpoint, params);
        }
    }

    @Override
    public void updateDevice(State15 newDevice) {
        updateState("switch", newDevice.getSwitch0());
        updateState("rssi", newDevice.getRssi());
        updateState("sled", newDevice.getNetworkLED());
        updateState("temperature", newDevice.getTemperature());
        updateState("humidity", newDevice.getHumidity());
        updateState("mainSwitch", newDevice.getMainSwitch());
        updateState("sensorType", newDevice.getSensorType());
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
