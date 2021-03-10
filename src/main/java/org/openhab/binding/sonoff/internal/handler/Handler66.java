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

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.*;
import org.openhab.binding.sonoff.internal.config.DeviceConfig;
import org.openhab.binding.sonoff.internal.dto.payloads.SLed;
import org.openhab.binding.sonoff.internal.dto.payloads.ZLed;
import org.openhab.binding.sonoff.internal.helpers.CommandMessage;
import org.openhab.binding.sonoff.internal.helpers.States;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;
import org.openhab.binding.sonoff.internal.states.State66;
import org.openhab.binding.sonoff.internal.states.StateBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link Handler66} is responsible for Zigbee Bridge Devices and manages the connections to child devices
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class Handler66 extends BaseBridgeHandler implements DeviceListener<State66> {

    private final Logger logger = LoggerFactory.getLogger(Handler66.class);
    private final Gson gson;
    private DeviceConfig config = this.getConfigAs(DeviceConfig.class);
    private Boolean cloud = false;

    public Handler66(Bridge thing, Gson gson) {
        super(thing);
        this.gson = gson;
    }

    @Override
    public void initialize() {
        setCloud(false);
        logger.debug("Initialising device: {}", this.thing.getUID());
        config = this.getConfigAs(DeviceConfig.class);
        HandlerAccount account = getAccountHandler();
        if (account == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge Not set");
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

    protected @Nullable HandlerAccount getAccountHandler() {
        Bridge bridge = getBridge();
        return bridge != null ? (HandlerAccount) bridge.getHandler() : null;
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
        HandlerAccount account = getAccountHandler();
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
        HandlerAccount account = getAccountHandler();
        if (account != null) {
            account.sendUpdate(message);
        } else {
            logger.debug("Couldn't send Command {} with parameters {} as the bridge returned null", endpoint, params);
        }
    }

    public void isConnected() {
        // process the states
        if (getCloud()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String params = "";
        String endpoint = "";
        if (command instanceof RefreshType) {
            return;
        } else {
            switch (channelUID.getId()) {
                case "sled":
                    SLed sled = new SLed();
                    sled.setSledOnline(command.toString().toLowerCase());
                    params = gson.toJson(sled);
                    endpoint = "sledOnline";
                    break;
                case "zled":
                    ZLed zled = new ZLed();
                    zled.setZled(command.toString().toLowerCase());
                    params = gson.toJson(zled);
                    endpoint = "sledOnline";
                    break;
            }
            logger.debug("Sonoff - Command Payload:{}", params);
            sendUpdate(endpoint, params);
        }
    }

    @Override
    public void updateDevice(State66 newDevice) {
        // Other
        updateState("rssi", newDevice.getRssi());
        updateState("sled", newDevice.getNetworkLED());
        updateState("zled", newDevice.getZigbeeLED());
        // Connections
        HandlerAccount account = getAccountHandler();
        if (account != null) {
            String mode = account.getMode();
            setCloud(mode.equals("local") ? false : newDevice.getCloud());
        }
        updateState("cloudOnline", getCloud() ? new StringType("Connected") : new StringType("Disconnected"));
        isConnected();
    }

    // required for discovery
    public @Nullable List<?> getSubDevices() {
        return States.getSubDevices(getDeviceConfig().deviceid);
    }
}
