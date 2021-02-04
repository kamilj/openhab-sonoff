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

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import org.openhab.binding.sonoff.internal.dto.api.Params;
import org.openhab.binding.sonoff.internal.dto.payloads.UiActive;
import org.openhab.binding.sonoff.internal.listeners.DeviceStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link lightwaverfBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class RFBridgeHandler extends BaseBridgeHandler implements DeviceStateListener {

    private final Logger logger = LoggerFactory.getLogger(SwitchHandler.class);
    private @Nullable AccountHandler account;
    private @Nullable DeviceConfig config;
    private @Nullable ScheduledFuture<?> wsTask;
    private final Gson gson;
    private String deviceKey = "";
    private String ipaddress = "";

    public RFBridgeHandler(Bridge thing, Gson gson) {
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
            config = this.getConfigAs(DeviceConfig.class);
            Device device = new Device();
            device = account.getDevice(config.deviceId);
            if (device == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Device ID Doesnt Exist, please check your configuration");
            } else {
                setProperties(device);
                account.registerStateListener(config.deviceId, this);
                Runnable activateWs = () -> {
                    UiActive params = new UiActive();
                    params.setUiActive(21600);
                    sendUpdate(gson.toJson(params), "uiActive", "");
                };
                wsTask = scheduler.scheduleWithFixedDelay(activateWs, 10, 21600, TimeUnit.SECONDS);
                if (account.getThing().getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
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
        Device device = new Device();
        device = account.getDevice(config.deviceId);
        if (device == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Device ID Doesnt Exist, please check your configuration");
        } else if (config.deviceId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "ID not set");
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            account.unregisterStateListener(config.deviceId);
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            account.registerStateListener(config.deviceId, this);
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void initializeBridge(@Nullable ThingHandler thingHandler, ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());
        if (thingHandler != null && bridgeStatus != null) {
            account = (AccountHandler) thingHandler;
        }
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        if (wsTask != null) {
            wsTask.cancel(true);
            wsTask = null;
        }
        if (account != null) {
            account.unregisterStateListener(config.deviceId);
            account = null;
        }
    }

    private void setProperties(Device device) {
        ipaddress = device.getLocalAddress();
        deviceKey = device.getDevicekey();
        Map<String, String> properties = editProperties();
        properties.put("Name", device.getName());
        properties.put("Brand", device.getBrandName());
        properties.put("Model", device.getProductModel());
        properties.put("FW Version", device.getParams().getFwVersion());
        properties.put("Device ID", device.getDeviceid());
        properties.put("Device Key", device.getDevicekey());
        properties.put("Connected To SSID", device.getParams().getSsid());
        properties.put("UUID", device.getUiid().toString());
        updateProperties(properties);
    }

    private void sendUpdate(String params, String command, String seq) {
        if (command.equals("uiActive")) {
            account.getApi().setStatusApi(params, config.deviceId, deviceKey);
        } else if (!command.contains("switch") && account.getAccountConfig().accessmode.equals("local")) {
            logger.warn("Sonoff - Cannot send command {}, Not supported by LAN", command.toString());
        } else if (account.lanOnline() && !ipaddress.equals("") && command.contains("switch")) {
            account.getApi().setStatusLan(params, command, config.deviceId, ipaddress, deviceKey, seq);
        } else if (account.wsOnline()) {
            account.getWebsocket().sendChange(params, config.deviceId, deviceKey);
        } else {
            logger.info("Cannot send command, all connections are offline");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    private synchronized void updateState(Device device) {
        if (device.getParams().getRssi() != null && this.thing.getChannel("rssi") != null) {
            updateState(this.thing.getChannel("rssi").getUID(),
                    new QuantityType<Power>(device.getParams().getRssi(), (DECIBEL_MILLIWATTS)));
        }
        if (device.getOfflineTime() != null && this.thing.getChannel("offlineTime") != null) {
            updateState(this.thing.getChannel("offlineTime").getUID(), new StringType(device.getOfflineTime()));
        }
        if (device.getOnline() != null && this.thing.getChannel("online") != null) {
            updateState(this.thing.getChannel("online").getUID(),
                    new StringType(device.getOnline() ? "connected" : "disconnected"));
        }
    }

    @Override
    public void cloudUpdate(Device device) {
        updateState(device);
    }

    @Override
    public void lanUpdate(JsonObject jsonObject, String ipaddress, String sequence) {
        logger.debug("Sonoff - Lan Encrypted Message:{}", gson.toJson(jsonObject));
        String message = Utils.decrypt(jsonObject, deviceKey);
        logger.debug("Sonoff - Lan Decrypted Message:{}", message);
        this.ipaddress = ipaddress;
        Device device = new Device();
        device.setParams(gson.fromJson(message, Params.class));
        updateState(device);
    }
}
