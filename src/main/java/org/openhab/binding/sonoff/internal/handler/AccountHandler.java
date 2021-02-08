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

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.eclipse.smarthome.io.net.http.WebSocketFactory;
import org.openhab.binding.sonoff.internal.MainDiscovery;
import org.openhab.binding.sonoff.internal.config.AccountConfig;
import org.openhab.binding.sonoff.internal.connections.Api;
import org.openhab.binding.sonoff.internal.connections.Lan;
import org.openhab.binding.sonoff.internal.connections.Websocket;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.dto.api.Devices;
import org.openhab.binding.sonoff.internal.dto.payloads.Consumption;
import org.openhab.binding.sonoff.internal.listeners.ConnectionListener;
import org.openhab.binding.sonoff.internal.listeners.DeviceStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link sonoffHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler implements ConnectionListener {

    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);

    private final WebSocketFactory webSocketFactory;
    private final HttpClientFactory httpClientFactory;
    private @Nullable Websocket ws;
    private @Nullable Lan lan;
    private @Nullable Api api;
    private @Nullable AccountConfig config;
    private @Nullable ScheduledFuture<?> tokenTask;
    private @Nullable ScheduledFuture<?> pingTask;
    private @Nullable ScheduledFuture<?> connectionTask;
    private @Nullable ScheduledFuture<?> deviceTask;
    private final Gson gson;
    private Boolean lanOnline = false;
    private Boolean wsOnline = false;
    private Boolean apiOnline = false;
    private String mode = "";
    private Integer pollingInterval = -1;
    private final Map<String, DeviceStateListener> deviceStateListener = new HashMap<>();
    final Map<String, Device> deviceState = new HashMap<>();

    public AccountHandler(Bridge thing, WebSocketFactory webSocketFactory, HttpClientFactory httpClientFactory,
            Gson gson) {
        super(thing);
        this.webSocketFactory = webSocketFactory;
        this.httpClientFactory = httpClientFactory;
        this.gson = gson;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(MainDiscovery.class);
    }

    @Override
    public void thingUpdated(Thing thing) {
        dispose();
        this.thing = thing;
        initialize();
    }

    public void addDevice(Device device) {
        deviceState.put(device.getDeviceid(), device);
    }

    @Override
    public void initialize() {
        config = this.getConfigAs(AccountConfig.class);
        mode = config.accessmode.toString();
        if (config.pollingInterval < 30 && config.pollingInterval >= 0) {
            pollingInterval = 30;
        } else {
            pollingInterval = config.pollingInterval;
        }
        logger.debug("Starting Api");
        api = new Api(config, this, httpClientFactory, gson);
        api.start();

        if ((mode.equals("mixed") || mode.equals("local"))) {
            logger.debug("Starting mDNS Client");
            lan = new Lan(config.ipaddress, this);
        }

        if ((mode.equals("mixed") || mode.equals("cloud"))) {
            logger.debug("Starting Websocket Client");
            ws = new Websocket(webSocketFactory, gson, api, this);
        }

        Runnable getToken = () -> {
            if ((mode.equals("mixed") || mode.equals("cloud"))) {
                api.login();
            }
        };
        tokenTask = scheduler.scheduleWithFixedDelay(getToken, 6, 6, TimeUnit.HOURS);

        if (pollingInterval != -1) {
            Runnable refreshDevices = () -> {
                if ((mode.equals("mixed") || mode.equals("cloud"))) {
                    Devices devices = new Devices();
                    devices = api.discover();
                    for (int i = 0; i < devices.getDevicelist().size(); i++) {
                        Device device = devices.getDevicelist().get(i);
                        String deviceid = device.getDeviceid();
                        Integer deviceUUID = device.getUiid();
                        deviceState.put(deviceid, device);
                        DeviceStateListener dl = deviceStateListener.get(deviceid);
                        if (deviceUUID == 32 || deviceUUID == 5) {
                            logger.debug("Device UUID:{}", deviceUUID);
                            Consumption params = new Consumption();
                            ws.getConsumption(gson.toJson(params), deviceid);
                        }
                        if (dl != null) {
                            dl.cloudUpdate(device);
                        }
                    }
                }
            };
            deviceTask = scheduler.scheduleWithFixedDelay(refreshDevices, pollingInterval, pollingInterval,
                    TimeUnit.SECONDS);
        }

        Runnable connect = () -> {
            logger.debug("Sonoff Connection Check Running");
            if (!apiOnline) {
                api.getRegion();
                api.login();
                logger.debug("Performing Initial Discovery");
                Devices newdevices = new Devices();
                newdevices = api.discover();
                for (int i = 0; i < newdevices.getDevicelist().size(); i++) {
                    deviceState.put(newdevices.getDevicelist().get(i).getDeviceid(), newdevices.getDevicelist().get(i));
                }
                if ((mode.equals("mixed") || mode.equals("cloud"))) {
                    logger.debug("Initialising Cloud Connection");
                    ws.start();
                }
                if ((mode.equals("mixed") || mode.equals("local"))) {
                    logger.debug("Initialising Local Connection");
                    lan.start();
                }
            } else {
                if ((mode.equals("mixed") || mode.equals("cloud")) && !wsOnline) {
                    logger.debug("Reconnecting Cloud Connection as was disconnected");
                    ws.start();
                }
                if ((mode.equals("mixed") || mode.equals("local")) && !lanOnline) {
                    logger.debug("Reconnecting Local Connection as was disconnected");
                    lan.start();
                }
            }
        };
        connectionTask = scheduler.scheduleWithFixedDelay(connect, 0, 60, TimeUnit.SECONDS);
        Runnable wsKeepAlive = () -> {
            if (wsOnline) {
                ws.sendPing();
            }
        };
        pingTask = scheduler.scheduleWithFixedDelay(wsKeepAlive, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Sonoff - Running dispose()");
        if (pingTask != null) {
            pingTask.cancel(true);
            pingTask = null;
        }
        if (tokenTask != null) {
            tokenTask.cancel(true);
            tokenTask = null;
        }
        if (connectionTask != null) {
            connectionTask.cancel(true);
            connectionTask = null;
        }
        if (deviceTask != null) {
            deviceTask.cancel(true);
            deviceTask = null;
        }
        if (ws != null) {
            ws.stop();
            ws = null;
        }
        wsOnline = false;
        if (lan != null) {
            lan.stop();
            lan = null;
            lanOnline = false;
        }
        if (api != null) {
            api.stop();
            api = null;
            apiOnline = false;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public @Nullable Websocket getWebsocket() {
        return ws;
    }

    public @Nullable Api getApi() {
        return api;
    }

    public @Nullable Lan getLan() {
        return lan;
    }

    public @Nullable Device getDevice(String deviceid) {
        return deviceState.get(deviceid);
    }

    public void registerStateListener(String deviceId, DeviceStateListener listener) {
        deviceStateListener.put(deviceId, listener);
        logger.debug("Device Listener Added for deviceId: {}", deviceId);
    }

    public void unregisterStateListener(String deviceId) {
        deviceStateListener.remove(deviceId);
    }

    public @Nullable AccountConfig getAccountConfig() {
        return config;
    }

    public Boolean lanOnline() {
        return lanOnline;
    }

    public Boolean wsOnline() {
        return wsOnline;
    }

    @Override
    public void webSocketConnectionOpen() {
        this.wsOnline = true;
        logger.debug("Cloud Mode Connected");
        if (mode.equals("mixed") && lanOnline) {
            updateStatus(ThingStatus.ONLINE);
        } else if (mode.equals("cloud")) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    public void lanConnectionOpen() {
        this.lanOnline = true;
        logger.debug("Local Mode Connected");
        if (mode.equals("mixed") && wsOnline) {
            updateStatus(ThingStatus.ONLINE);
        } else if (mode.equals("local")) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void webSocketMessage(Device device) {
        if (deviceStateListener.get(device.getDeviceid()) != null) {
            DeviceStateListener listener = deviceStateListener.get(device.getDeviceid());
            listener.cloudUpdate(device);
        }
    }

    public void onLanMessage(ServiceInfo serviceInfo) {
        JsonObject jsonObject = new JsonObject();
        Enumeration<String> info = serviceInfo.getPropertyNames();
        while (info.hasMoreElements()) {
            String name = info.nextElement().toString();
            String value = serviceInfo.getPropertyString(name);
            jsonObject.addProperty(name, value);
        }
        String seq = jsonObject.get("seq").getAsString();
        String deviceid = jsonObject.get("id").getAsString();
        String ipaddress = serviceInfo.getInet4Addresses()[0].getHostAddress();
        DeviceStateListener listener = deviceStateListener.get(deviceid);
        deviceState.get(deviceid).setLocalAddress(ipaddress);
        deviceState.get(deviceid).setSequence(seq);
        listener.lanUpdate(jsonObject, ipaddress, seq);
    }

    @Override
    public void onError(String module, @Nullable String code, @Nullable String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                module + " has thrown an error and is offline, binding will atempt to restart");
        logger.debug("Error code:{}, message:{}", code, message);
        if (module.equals("api")) {
            ws.stop();
            wsOnline = false;
            try {
                lan.stop();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            lanOnline = false;
            apiOnline = false;
        } else if (module == "websocket") {
            wsOnline = false;
        } else if (module == "lan") {
            lanOnline = false;
        }
    }

    @Override
    public void ApiconnectionOpen() {
        apiOnline = true;
    }

    @Override
    public void webSocketConsumptionMessage(String deviceid, String data) {
        if (deviceStateListener.get(deviceid) != null) {
            DeviceStateListener listener = deviceStateListener.get(deviceid);
            listener.consumption(data);
        }
    }
}
