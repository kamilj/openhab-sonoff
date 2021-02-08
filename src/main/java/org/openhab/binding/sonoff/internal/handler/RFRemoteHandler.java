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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.*;
import org.openhab.binding.sonoff.internal.config.RemoteConfig;
import org.openhab.binding.sonoff.internal.dto.payloads.RFChannel;
import org.openhab.binding.sonoff.internal.listeners.RFListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link lightwaverfBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class RFRemoteHandler extends BaseThingHandler implements RFListener {

    private final Logger logger = LoggerFactory.getLogger(RFRemoteHandler.class);
    private @Nullable RFBridgeHandler rfbridge;
    private @Nullable RemoteConfig config;
    private final Gson gson;

    public RFRemoteHandler(Thing thing, Gson gson) {
        super(thing);
        this.gson = gson;
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge Not set");
        } else {
            initializeBridge(bridge.getHandler(), bridge.getStatus());
            config = this.getConfigAs(RemoteConfig.class);
            rfbridge.addListener(config.id, this);
            if (rfbridge.getThing().getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        dispose();
        this.thing = thing;
        initialize();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (config.id.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "ID not set");
            return;
        }
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
            return;
        }
    }

    private void initializeBridge(@Nullable ThingHandler thingHandler, ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());
        if (thingHandler != null && bridgeStatus != null) {
            rfbridge = (RFBridgeHandler) thingHandler;
            if (bridgeStatus != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        rfbridge.removeListener(config.id);
        if (rfbridge != null) {
            rfbridge = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        } else {
            String data = "";
            String endpoint = "";
            RFChannel rfChannel = new RFChannel();
            if (command.toString().toLowerCase().equals("on") && channelUID.getId().contains("button")) {
                rfChannel.setCmd("transmit");
                switch (channelUID.getId()) {
                    case "button0":
                        rfChannel.setRfChannel(0);
                        break;
                    case "button1":
                        rfChannel.setRfChannel(1);
                        break;
                    case "button2":
                        rfChannel.setRfChannel(2);
                        break;
                    case "button3":
                        rfChannel.setRfChannel(3);
                        break;
                }
                data = gson.toJson(rfChannel);
                endpoint = "button";
                logger.debug("Sonoff - Command Payload:{}", data);
                rfbridge.sendUpdate(data, endpoint, "");
                updateState(channelUID, OnOffType.OFF);
            }
        }
    }

    @Override
    public void sensorTriggered(String date) {
        if (date != null && this.thing.getChannel("sensorTriggered") != null) {
            updateState(this.thing.getChannel("sensorTriggered").getUID(), new DateTimeType(date));
        }
    }

    @Override
    public void buttonPressed(Integer button, String date) {
        logger.debug("Sonoff - Button Pressed:{}, Date:{}", button, date);
    }
}
