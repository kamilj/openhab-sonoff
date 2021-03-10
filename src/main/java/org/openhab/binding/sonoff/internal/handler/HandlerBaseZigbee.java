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
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.*;
import org.openhab.binding.sonoff.internal.config.DeviceConfig;
import org.openhab.binding.sonoff.internal.helpers.CommandMessage;
import org.openhab.binding.sonoff.internal.helpers.States;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;
import org.openhab.binding.sonoff.internal.states.StateBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link HandlerBaseZigbee} allows the handling of commands and updates to Devices
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public abstract class HandlerBaseZigbee<T> extends BaseThingHandler implements DeviceListener<T> {

    private final Logger logger = LoggerFactory.getLogger(HandlerBaseZigbee.class);
    private DeviceConfig config = this.getConfigAs(DeviceConfig.class);
    private Boolean cloud = false;

    public HandlerBaseZigbee(Thing thing, Gson gson) {
        super(thing);
    }

    @Override
    public void initialize() {
        setCloud(false);
        logger.debug("Initialising device: {}", this.thing.getUID());
        config = this.getConfigAs(DeviceConfig.class);
        Handler66 bridge = getZigbeeHandler();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge Not set");
            return;
        }
        HandlerAccount account = bridge.getAccountHandler();
        if (account == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Account not set");
            return;
        }
        if (account.getMode().equals("local")) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Local Mode Not supported by device");
            return;
        }

        // Get the current state, if not there attempt to create it
        StateBase device;
        device = States.getState(config.deviceid);
        if (device == null) {
            Boolean added = account.createState(config.deviceid);
            if (!added) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "This device has not been initilized and a further attempt to add it failed");
                return;
            } else {
                device = States.getState(config.deviceid);
                logger.debug("New device {} sucessfully added to states ", config.deviceid);
            }
        }
        if (device != null) {
            States.setDeviceListener(config.deviceid, this);
            setProperties(device.getProperties());
            device.sendUpdate();
        }
    }

    protected @Nullable Handler66 getZigbeeHandler() {
        Bridge bridge = getBridge();
        return bridge != null ? (Handler66) bridge.getHandler() : null;
    }

    public Boolean getCloud() {
        return this.cloud;
    }

    public void setCloud(Boolean cloud) {
        this.cloud = cloud;
    }

    public String getDeviceid() {
        return config.deviceid;
    }

    public DeviceConfig getDeviceConfig() {
        return this.config;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());
        Handler66 bridge = getZigbeeHandler();
        if (bridge != null) {
            HandlerAccount account = bridge.getAccountHandler();
            if (account != null) {
                if (account.getMode().equals("local")) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Local Mode Not supported by device");
                    return;
                }
                if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
                    isConnected();
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        States.removeDeviceListener(config.deviceid);
    }

    private void setProperties(Map<String, String> properties) {
        updateProperties(properties);
    }

    public void sendUpdate(String endpoint, String params) {
        logger.debug("Sonoff - Command Payload:{}", params);
        CommandMessage message = new CommandMessage(endpoint, params, config.deviceid);
        Handler66 bridge = getZigbeeHandler();
        if (bridge != null) {
            HandlerAccount account = bridge.getAccountHandler();
            if (account != null) {
                account.sendUpdate(message);
            }
        } else {
            logger.debug("Couldn't send Command {} with parameters {} as the bridge returned null", endpoint, params);
        }
    }

    public void isConnected() {
        // process the states
        if (cloud) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    public abstract void handleCommand(ChannelUID channelUID, Command command);

    public abstract void updateDevice(T newDevice);
}
