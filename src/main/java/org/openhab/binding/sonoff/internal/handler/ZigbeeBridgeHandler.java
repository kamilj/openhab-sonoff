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

import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.*;

import java.io.FileNotFoundException;
import java.util.Map;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.*;
import org.openhab.binding.sonoff.internal.Utils;
import org.openhab.binding.sonoff.internal.config.DeviceConfig;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.dto.payloads.SLed;
import org.openhab.binding.sonoff.internal.dto.payloads.ZLed;
import org.openhab.binding.sonoff.internal.helpers.CommandMessage;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link lightwaverfBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author David Murton - Initial contribution
 */
// @NonNullByDefault
public class ZigbeeBridgeHandler extends BaseBridgeHandler implements DeviceListener {

    private final Logger logger = LoggerFactory.getLogger(ZigbeeBridgeHandler.class);
    private @Nullable AccountHandler account;
    private @Nullable DeviceConfig config;
    private final Gson gson;
    private @Nullable Device device = new Device();
    private Boolean cloud = false;

    public ZigbeeBridgeHandler(Bridge thing, Gson gson) {
        super(thing);
        this.gson = gson;
    }

    @Override
    public void initialize() {
        logger.debug("Initialising device: {}", this.thing.getUID());
        config = this.getConfigAs(DeviceConfig.class);
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge Not set");
            return;
        }
        try {
            device = gson.fromJson(Utils.getDevice(config.deviceid), Device.class);
        } catch (FileNotFoundException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Unable to read device cache");
            logger.debug("Could not read file: {}", e);
            return;
        }
        if (device == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "This device has not been initilized");
            return;
        }
        setProperties();
        initializeBridge(bridge.getHandler(), bridge.getStatus());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());
        Bridge bridge = getBridge();
        if (bridge != null) {
            initializeBridge(bridge.getHandler(), bridgeStatusInfo.getStatus());
        }
    }

    private void initializeBridge(ThingHandler thingHandler, ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());
        if (thingHandler != null && bridgeStatus != null) {
            account = (AccountHandler) thingHandler;

            if (bridgeStatus == ThingStatus.ONLINE) {
                if (account.getMode().equals("local")) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "LOCAL MODE NOT SUPPORTED");
                } else {
                    account.addDeviceListener(config.deviceid, device.getDevicekey(), this);
                    isConnected();
                }
            } else {
                account.removeDeviceListener(config.deviceid);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            account.removeDeviceListener(config.deviceid);
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
        if (account != null) {
            account.removeDeviceListener(config.deviceid);
            account = null;
        }
    }

    private void setProperties() {
        Map<String, String> properties = editProperties();
        properties.put("Name", device.getName());
        properties.put("Brand", device.getBrandName());
        properties.put("Model", device.getProductModel());
        properties.put("FW Version", device.getParams().getFwVersion());
        properties.put("Device ID", device.getDeviceid());
        properties.put("Device Key", device.getDevicekey());
        properties.put("Connected To SSID", device.getParams().getSsid());
        properties.put("UUID", device.getExtra().getUiid().toString());
        updateProperties(properties);
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
            CommandMessage message = new CommandMessage(endpoint, params, config.deviceid);
            account.sendUpdate(message);
        }
    }

    private synchronized void updateState(Device newDevice) {
        if (newDevice.getParams().getRssi() != null && this.thing.getChannel("rssi") != null) {
            updateState(this.thing.getChannel("rssi").getUID(),
                    new QuantityType<Power>(newDevice.getParams().getRssi(), (DECIBEL_MILLIWATTS)));
        }
        if (newDevice.getParams().getSledOnline() != null && this.thing.getChannel("sled") != null) {
            updateState(this.thing.getChannel("sled").getUID(),
                    newDevice.getParams().getSledOnline().equals("on") ? OnOffType.ON : OnOffType.OFF);
        }

        if (newDevice.getParams().getZled() != null && this.thing.getChannel("zled") != null) {
            updateState(this.thing.getChannel("zled").getUID(),
                    newDevice.getParams().getZled().equals("on") ? OnOffType.ON : OnOffType.OFF);
        }
    }

    @Override
    public void updateDevice(Device newDevice) {
        updateState(newDevice);

        // Get the current states
        Boolean cloudConnected = cloud;

        // Set the new state
        if (newDevice.getError() != null) {
            if (newDevice.getError() == 0) {
                cloudConnected = true;
            } else {
                cloudConnected = false;
            }
        }
        if (newDevice.getOnline() != null) {
            cloudConnected = newDevice.getOnline();
        }
        cloud = cloudConnected;
        if (this.thing.getChannel("cloudOnline") != null) {
            updateState(this.thing.getChannel("cloudOnline").getUID(),
                    cloud ? new StringType("Connected") : new StringType("Disconnected"));
        }

        isConnected();
    }

    private void isConnected() {
        // process the states
        if (cloud == true) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    // Required for discovery
    public @Nullable Device getDevice() {
        return device;
    }

    // Required for child devices
    // public @Nullable AccountHandler getAccount() {
    // return this.account;
    // }

    public void addChildListener(String deviceid, String deviceKey, DeviceListener deviceListener) {
        account.addDeviceListener(deviceid, deviceKey, deviceListener);
    }

    public void removeChildListener(String deviceid) {
        account.removeDeviceListener(deviceid);
    }
}
