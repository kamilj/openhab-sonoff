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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.*;
import org.openhab.binding.sonoff.internal.config.DeviceConfig;
import org.openhab.binding.sonoff.internal.dto.payloads.RFChannel;
import org.openhab.binding.sonoff.internal.listeners.RFListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link RFDeviceHandler} allows the handling of commands and updates to RF Remotes
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class Handler28Children extends BaseThingHandler implements RFListener {

    private final Logger logger = LoggerFactory.getLogger(Handler28Children.class);
    private @Nullable Handler28 rfbridge;
    private final Gson gson;
    private String deviceid = "";

    public Handler28Children(Thing thing, Gson gson) {
        super(thing);
        this.gson = gson;
    }

    @Override
    public void initialize() {
        logger.debug("Initialising device: {}", this.thing.getUID());
        deviceid = this.getConfigAs(DeviceConfig.class).deviceid;
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge Not set");
            return;
        }
        initializeBridge(bridge.getHandler(), bridge.getStatus());
    }

    private void addListener() {
        final Handler28 rfBridge = this.rfbridge;
        if (rfBridge != null) {
            if (this.getThing().getThingTypeUID().getId().equals("rfsensor")) {
                rfBridge.addListener(deviceid, this);
            } else {
                Integer buttons = Integer.parseInt(this.getThing().getThingTypeUID().getId().substring(8));
                for (int i = 0; i < buttons; i++) {
                    String number = (Integer.parseInt(deviceid) + i) + "";
                    rfBridge.addListener(number, this);
                }
            }
        }
    }

    private void removeListener() {
        final Handler28 rfBridge = this.rfbridge;
        if (rfBridge != null) {
            if (this.getThing().getThingTypeUID().getId().equals("rfsensor")) {
                rfBridge.removeListener(deviceid);
            } else {
                Integer buttons = Integer.parseInt(this.getThing().getThingTypeUID().getId().substring(8));
                for (int i = 0; i < buttons; i++) {
                    String number = (Integer.parseInt(deviceid) + i) + "";
                    rfBridge.removeListener(number);
                }
            }
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());
        removeListener();
        Bridge bridge = getBridge();
        if (bridge != null) {
            initializeBridge(bridge.getHandler(), bridgeStatusInfo.getStatus());
        }
    }

    private void initializeBridge(@Nullable ThingHandler thingHandler, ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());
        if (thingHandler != null) {
            rfbridge = (Handler28) thingHandler;

            if (bridgeStatus == ThingStatus.ONLINE) {
                addListener();
                updateStatus(ThingStatus.ONLINE);
            } else {
                removeListener();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            removeListener();
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        dispose();
        this.thing = thing;
        initialize();
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        final Handler28 rfbridge = this.rfbridge;
        if (rfbridge != null) {
            if (this.getThing().getThingTypeUID().getId().equals("rfsensor")) {
                rfbridge.removeListener(deviceid);
            } else {
                Integer buttons = Integer.parseInt(this.getThing().getThingTypeUID().getId().substring(8));
                for (int i = 0; i < buttons; i++) {
                    String number = (Integer.parseInt(deviceid) + i) + "";
                    rfbridge.removeListener(number);
                }
            }
            this.rfbridge = null;
        }
    }

    private void sendUpdate(String endpoint, String data) {
        final Handler28 rfBridge = this.rfbridge;
        if (rfBridge != null) {
            rfBridge.sendUpdate(endpoint, data);
        } else {
            logger.error("Couldnt send RF Data {} for device {} as the bridge returned null", data, deviceid);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        } else {
            if (command.toString().toLowerCase().equals("on") && channelUID.getId().contains("button")) {
                RFChannel rfChannel = new RFChannel();
                Integer button = Integer.parseInt(channelUID.getId().substring(6));
                Integer no = Integer.parseInt(deviceid) + button;
                rfChannel.setCmd("transmit");
                rfChannel.setRfChannel(no);
                String data = gson.toJson(rfChannel);
                String endpoint = "transmit";
                // if (data != null) {
                sendUpdate(endpoint, data);
                Channel channel = this.thing.getChannel("rf" + button + "Internal");
                if (channel != null) {
                    updateState(channel.getUID(), new DateTimeType(System.currentTimeMillis() + ""));
                }
                // } else {
                // logger.error("Couldnt send RF Command {} for device {} as the parameters were empty",
                // command.toString(), deviceid);
                // }

                updateState(channelUID, OnOffType.OFF);
            }
        }
    }

    @Override
    public void rfTriggered(Integer chl, DateTimeType date) {
        String channelName = "rf" + (chl - Integer.parseInt(deviceid)) + "External";
        updateState(channelName, date);
    }

    @Override
    public void rfCode(Integer chl, String rfVal) {
        Map<String, String> properties = editProperties();
        properties.put("RF Code For Channel " + (chl - Integer.parseInt(deviceid)), rfVal);
        updateProperties(properties);
    }
}
