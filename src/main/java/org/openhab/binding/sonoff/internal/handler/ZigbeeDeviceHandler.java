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

import java.io.FileNotFoundException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonoff.internal.Utils;
import org.openhab.binding.sonoff.internal.config.DeviceConfig;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.*;
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
public class ZigbeeDeviceHandler extends BaseThingHandler implements DeviceListener {

    private final Logger logger = LoggerFactory.getLogger(ZigbeeDeviceHandler.class);
    private @Nullable ZigbeeBridgeHandler zbridge;
    private @Nullable Device device = new Device();
    private final Gson gson;
    private @Nullable DeviceConfig config;
    private Boolean cloud = false;

    public ZigbeeDeviceHandler(Thing thing, Gson gson) {
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

    private void initializeBridge(@Nullable ThingHandler thingHandler, ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());
        if (thingHandler != null && bridgeStatus != null) {
            zbridge = (ZigbeeBridgeHandler) thingHandler;
            if (bridgeStatus == ThingStatus.ONLINE) {
                zbridge.addChildListener(config.deviceid, device.getDevicekey(), this);
                isConnected();
            } else {
                zbridge.removeChildListener(config.deviceid);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            zbridge.removeChildListener(config.deviceid);
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
        if (zbridge != null) {
            zbridge.removeChildListener(config.deviceid);
            zbridge = null;
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
    }

    private synchronized void updateState(Device newDevice) {
        if (newDevice.getParams().getMotion() != null && this.thing.getChannel("motion") != null) {
            updateState(this.thing.getChannel("motion").getUID(),
                    newDevice.getParams().getMotion() == 1 ? OnOffType.ON : OnOffType.OFF);
        }
        if (newDevice.getParams().getBattery() != null && (this.thing.getChannel("battery") != null)) {
            updateState(this.thing.getChannel("battery").getUID(), new DecimalType(newDevice.getParams().getBattery()));
        }
        if (newDevice.getParams().getTrigTime() != null && this.thing.getChannel("trigTime") != null) {
            updateState(this.thing.getChannel("trigTime").getUID(),
                    new DateTimeType(newDevice.getParams().getTrigTime()));
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
}
