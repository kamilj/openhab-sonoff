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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
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
import org.openhab.binding.sonoff.internal.helpers.CommandMessage;
import org.openhab.binding.sonoff.internal.helpers.States;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;
import org.openhab.binding.sonoff.internal.listeners.RFListener;
import org.openhab.binding.sonoff.internal.states.State28;
import org.openhab.binding.sonoff.internal.states.StateBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link Handler28} is responsible for RF Bridge Devices and manages the connections to child devices
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class Handler28 extends BaseBridgeHandler implements DeviceListener<State28> {

    private final Logger logger = LoggerFactory.getLogger(Handler28.class);
    private final Gson gson;
    // private @Nullable ScheduledFuture<?> localTask;
    private final Map<String, RFListener> rfListeners = new HashMap<>();
    private DeviceConfig config = this.getConfigAs(DeviceConfig.class);
    private Boolean cloud = false;
    private Boolean local = false;

    public Handler28(Bridge thing, Gson gson) {
        super(thing);
        this.gson = gson;
    }

    @Override
    public void initialize() {
        setCloud(false);
        setLocal(false);
        logger.debug("Initialising device: {}", this.thing.getUID());
        config = this.getConfigAs(DeviceConfig.class);
        HandlerAccount account = getAccountHandler();
        if (account == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge Not set");
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

    public Boolean getLocal() {
        return this.local;
    }

    public void setLocal(Boolean local) {
        this.local = local;
    }

    public String getDeviceid() {
        return config.deviceid;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());
        HandlerAccount account = getAccountHandler();
        if (account != null) {
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

    public DeviceConfig getDeviceConfig() {
        return this.config;
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
        HandlerAccount account = getAccountHandler();
        if (account != null) {
            // process the states
            if (account.getMode().equals("local")) {
                if (local) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            } else if (account.getMode().equals("cloud")) {
                if (cloud) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Device is not connected to the internet");
                }
            } else {
                if (cloud && local) {
                    updateStatus(ThingStatus.ONLINE);
                } else if (cloud) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Cloud Only");
                } else if (local) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Local Only");
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
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
            }
            logger.debug("Sonoff - Command Payload:{}", params);
            sendUpdate(endpoint, params);
        }
    }

    @Override
    public void updateDevice(State28 newDevice) {
        // Other
        updateState("rssi", newDevice.getRssi());
        updateState("sled", newDevice.getNetworkLED());
        updateState("ipaddress", newDevice.getIpAddress());
        // Connections
        HandlerAccount account = getAccountHandler();
        if (account != null) {
            String mode = account.getMode();
            setCloud(mode.equals("local") ? false : newDevice.getCloud());
            setLocal(mode.equals("cloud") ? false : newDevice.getLocal());
        }
        updateState("cloudOnline", getCloud() ? new StringType("Connected") : new StringType("Disconnected"));
        updateState("localOnline", getLocal() ? new StringType("Connected") : new StringType("Disconnected"));
        isConnected();

        // handle new updates to rf External triggers
        Map<Integer, DateTimeType> channels = new HashMap<Integer, DateTimeType>();
        channels.put(0, newDevice.getRf0());
        channels.put(1, newDevice.getRf1());
        channels.put(2, newDevice.getRf2());
        channels.put(3, newDevice.getRf3());
        channels.put(4, newDevice.getRf4());
        channels.put(5, newDevice.getRf5());
        channels.put(6, newDevice.getRf6());
        channels.put(7, newDevice.getRf7());
        channels.put(8, newDevice.getRf8());
        channels.put(9, newDevice.getRf9());
        channels.put(10, newDevice.getRf10());
        channels.put(11, newDevice.getRf11());
        channels.put(12, newDevice.getRf12());
        channels.put(13, newDevice.getRf13());
        channels.put(14, newDevice.getRf14());
        channels.put(15, newDevice.getRf15());

        for (Map.Entry<Integer, DateTimeType> entry : channels.entrySet()) {
            Integer key = entry.getKey();
            RFListener listener = rfListeners.get(key + "");
            if (listener != null) {
                listener.rfTriggered(key, entry.getValue());
            }
        }

        for (int i = 0; i < newDevice.getRfList().size(); i++) {
            Integer rfChl = newDevice.getRfList().get(i).getRfChl();
            String rfVal = newDevice.getRfList().get(i).getRfVal();
            RFListener rflistener = rfListeners.get(rfChl.toString());
            if (rflistener != null) {
                rflistener.rfCode(rfChl, rfVal);
            }
        }
    }

    public void addListener(String deviceid, RFListener listener) {
        rfListeners.putIfAbsent(deviceid, listener);
    }

    public void removeListener(String deviceid) {
        if (rfListeners.containsKey(deviceid)) {
            rfListeners.remove(deviceid);
        }
    }

    // required for discovery
    public @Nullable List<?> getSubDevices() {
        return States.getSubDevices(getDeviceConfig().deviceid);
    }
}
