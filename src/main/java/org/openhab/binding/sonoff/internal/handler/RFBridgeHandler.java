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
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import org.openhab.binding.sonoff.internal.listeners.RFListener;
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

    private final Logger logger = LoggerFactory.getLogger(RFBridgeHandler.class);
    private @Nullable AccountHandler account;
    private @Nullable DeviceConfig config;
    private @Nullable ScheduledFuture<?> wsTask;
    private final Gson gson;
    private String deviceKey = "";
    private String ipaddress = "";
    final Map<String, RFListener> rfListeners = new HashMap<>();

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
                    params.setUiActive(60);
                    sendUpdate(gson.toJson(params), "uiActive", "");
                };
                wsTask = scheduler.scheduleWithFixedDelay(activateWs, 10, 60, TimeUnit.SECONDS);
                if (account.getThing().getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
            }
        }
    }

    public void addListener(String sensorid, RFListener listener) {
        rfListeners.put(sensorid, listener);
    }

    public void removeListener(String sensorid) {
        rfListeners.remove(sensorid);
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

    public void sendUpdate(String params, String command, String seq) {
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
        if (device.getParams().getCmd().equals("trigger")) {

            String ch0 = device.getParams().getRfTrig0();
            if (ch0 != null && rfListeners.get("0") != null) {
                rfListeners.get("0").sensorTriggered(ch0);
            }

            String ch1 = device.getParams().getRfTrig1();
            if (ch1 != null && rfListeners.get("1") != null) {
                rfListeners.get("1").sensorTriggered(ch1);
            }

            String ch2 = device.getParams().getRfTrig2();
            if (ch2 != null && rfListeners.get("2") != null) {
                rfListeners.get("2").sensorTriggered(ch2);
            }

            String ch3 = device.getParams().getRfTrig3();
            if (ch3 != null && rfListeners.get("3") != null) {
                rfListeners.get("3").sensorTriggered(ch3);
            }

            String ch4 = device.getParams().getRfTrig4();
            if (ch4 != null && rfListeners.get("4") != null) {
                rfListeners.get("4").sensorTriggered(ch4);
            }

            String ch5 = device.getParams().getRfTrig5();
            if (ch5 != null && rfListeners.get("5") != null) {
                rfListeners.get("5").sensorTriggered(ch5);
            }

            String ch6 = device.getParams().getRfTrig6();
            if (ch6 != null && rfListeners.get("6") != null) {
                rfListeners.get("6").sensorTriggered(ch6);
            }

            String ch7 = device.getParams().getRfTrig7();
            if (ch7 != null && rfListeners.get("7") != null) {
                rfListeners.get("7").sensorTriggered(ch7);
            }

            String ch8 = device.getParams().getRfTrig8();
            if (ch8 != null && rfListeners.get("8") != null) {
                rfListeners.get("8").sensorTriggered(ch8);
            }
            String ch9 = device.getParams().getRfTrig9();
            if (ch9 != null && rfListeners.get("9") != null) {
                rfListeners.get("9").sensorTriggered(ch9);
            }

            String ch10 = device.getParams().getRfTrig10();
            if (ch10 != null && rfListeners.get("10") != null) {
                rfListeners.get("10").sensorTriggered(ch10);
            }

            String ch11 = device.getParams().getRfTrig11();
            if (ch11 != null && rfListeners.get("11") != null) {
                rfListeners.get("11").sensorTriggered(ch11);
            }

            String ch12 = device.getParams().getRfTrig12();
            if (ch12 != null && rfListeners.get("12") != null) {
                rfListeners.get("12").sensorTriggered(ch12);
            }
            String ch13 = device.getParams().getRfTrig13();
            if (ch13 != null && rfListeners.get("13") != null) {
                rfListeners.get("13").sensorTriggered(ch13);
            }

            String ch14 = device.getParams().getRfTrig14();
            if (ch14 != null && rfListeners.get("14") != null) {
                rfListeners.get("14").sensorTriggered(ch14);
            }

            String ch15 = device.getParams().getRfTrig15();
            if (ch15 != null && rfListeners.get("15") != null) {
                rfListeners.get("15").sensorTriggered(ch15);
            }
        }
    }

    @Override
    public void cloudUpdate(Device device) {
        updateState(device);
    }

    public @Nullable Device getDevice() {
        return account.getDevice(config.deviceId);
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

    @Override
    public void consumption(String data) {
        // TODO Auto-generated method stub
    }
}
