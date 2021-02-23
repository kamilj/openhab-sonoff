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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.eclipse.smarthome.io.net.http.WebSocketFactory;
import org.openhab.binding.sonoff.internal.MainDiscovery;
import org.openhab.binding.sonoff.internal.Utils;
import org.openhab.binding.sonoff.internal.config.AccountConfig;
import org.openhab.binding.sonoff.internal.connections.Api;
import org.openhab.binding.sonoff.internal.connections.Lan;
import org.openhab.binding.sonoff.internal.connections.Websocket;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.dto.api.ThingList;
import org.openhab.binding.sonoff.internal.dto.payloads.UiActive;
import org.openhab.binding.sonoff.internal.dto.payloads.WebsocketRequest;
import org.openhab.binding.sonoff.internal.helpers.CommandMessage;
import org.openhab.binding.sonoff.internal.helpers.MessageConverter;
import org.openhab.binding.sonoff.internal.helpers.MessageQueue;
import org.openhab.binding.sonoff.internal.listeners.ConnectionListener;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;
import org.openhab.binding.sonoff.internal.listeners.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link sonoffHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler implements ConnectionListener, MessageListener {

    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
    private final HttpClientFactory httpClientFactory;
    private final WebSocketFactory webSocketFactory;
    private final Gson gson;
    private final MessageQueue queue = new MessageQueue(this);
    private final MessageConverter converter = new MessageConverter(this, queue);

    private @Nullable Lan lan;
    private @Nullable Api api;
    private @Nullable Websocket ws;
    private @Nullable ScheduledFuture<?> tokenTask;
    private @Nullable ScheduledFuture<?> connectionTask;
    private @Nullable ScheduledFuture<?> activateTask;
    // private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
    private @Nullable ScheduledFuture<?> queueTask;

    private Boolean lanOnline = false;
    private Boolean cloudOnline = false;
    private String mode = "";
    private Boolean init = false;

    public AccountHandler(Bridge thing, WebSocketFactory webSocketFactory, HttpClientFactory httpClientFactory,
            Gson gson) {
        super(thing);
        this.httpClientFactory = httpClientFactory;
        this.webSocketFactory = webSocketFactory;
        this.gson = gson;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(MainDiscovery.class);
    }

    @Override
    public void initialize() {
        queue.clearQueue();
        converter.clearQueue();
        logger.debug("Initialising Sonoff Account: {}", this.thing.getUID());
        // Set configuration variables
        AccountConfig config = this.getConfigAs(AccountConfig.class);
        logger.debug("Account mode set to {}", config.accessmode);
        mode = config.accessmode;
        init = config.initialize.equals("true") ? true : false;

        // Create the Lan connection
        if (!mode.equals("cloud")) {
            lan = new Lan(this, converter, httpClientFactory, config.ipaddress);
            lan.start();
        }

        // create the api connection
        if (!mode.equals("local") || init) {
            logger.debug("Starting Cloud Connection");
            api = new Api(gson, this, converter, httpClientFactory, config);
            // create the websocket connection
            if (!mode.equals("local")) {
                ws = new Websocket(this, converter, webSocketFactory, api);
            }
            api.getRegion();
            api.login();
        }

        // Start our message queue for outgoing messages
        queue.startRunning();
        queueTask = scheduler.scheduleWithFixedDelay(queue, 0, 100, TimeUnit.MILLISECONDS);

        // Start any polling tasks we need
        createTasks();
        if (!this.thing.getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void dispose() {
        updateStatus(ThingStatus.OFFLINE);
        logger.debug("Sonoff - Running dispose()");
        if (tokenTask != null) {
            tokenTask.cancel(true);
            tokenTask = null;
        }
        if (connectionTask != null) {
            connectionTask.cancel(true);
            connectionTask = null;
        }
        lanOnline = false;
        cloudOnline = false;
        queue.stopRunning();
        if (queueTask != null) {
            queueTask.cancel(true);
            queueTask = null;
        }
        if (lan != null) {
            lan.stop();
            lan = null;

        }
        if (ws != null) {
            ws.stop();
            ws = null;
        }
        if (api != null) {
            api = null;
        }
    }

    private void createTasks() {
        if ((mode.equals("mixed") || mode.equals("cloud"))) {
            // Task to refresh our login credentials
            Runnable getToken = () -> {
                api.login();
            };
            tokenTask = scheduler.scheduleWithFixedDelay(getToken, 12, 12, TimeUnit.HOURS);

            // Task to check we are still connected
            Runnable connection = () -> {
                logger.debug("Sonoff Connection Check Running");
                if (!cloudOnline) {
                    api.getRegion();
                    api.login();
                }
            };
            connectionTask = scheduler.scheduleWithFixedDelay(connection, 60, 60, TimeUnit.SECONDS);

            Runnable activate = () -> {
                if (cloudOnline) {
                    // Get All our devices in one api call to check the connection
                    List<ThingList> things = api.getDevices().getData().getThingList();
                    // For each device
                    for (int i = 0; i < things.size(); i++) {
                        Device device = things.get(i).getItemData();
                        // Activate streaming data for the device if its a POW type
                        if (device.getOnline().equals(true)
                                && (device.getExtra().getUiid().equals(5) || device.getExtra().getUiid().equals(32))) {
                            UiActive params = new UiActive();
                            params.setUiActive(60);
                            CommandMessage message = new CommandMessage("uiActive", gson.toJson(params),
                                    device.getDeviceid());
                            sendUpdate(message);
                        }
                        // Update our device with non essential data and connection status
                        converter.sendDeviceMessage(things.get(i).getItemData());
                    }
                }
            };
            activateTask = scheduler.scheduleWithFixedDelay(activate, 5, 60, TimeUnit.SECONDS);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public String getMode() {
        return mode;
    }

    public void addDeviceListener(String deviceid, String deviceKey, DeviceListener listener) {
        converter.addDeviceListener(deviceid, deviceKey, listener);
    }

    public void removeDeviceListener(String deviceid) {
        converter.removeDeviceListener(deviceid);
    }

    private void connected() {
        if (mode.equals("local") && lanOnline) {
            updateStatus(ThingStatus.ONLINE);
        } else if (mode.equals("cloud") & cloudOnline) {
            updateStatus(ThingStatus.ONLINE);
        } else if (mode.equals("mixed")) {
            if (lanOnline && cloudOnline) {
                updateStatus(ThingStatus.ONLINE);
            } else if (lanOnline && !cloudOnline) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cloud Offline");
            } else if (!lanOnline && cloudOnline) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.COMMUNICATION_ERROR, "LAN Offline");
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void apiConnected(Boolean connected) {
        if (connected) {
            // Initialize the device cache so we can work in local mode only if required
            if (init) {
                List<ThingList> things = api.getDevices().getData().getThingList();
                if (things != null) {
                    try {
                        Utils.createFiles(things, this.gson);
                    } catch (IOException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                                "Unable to create device cache");
                        logger.debug("Could not initialize files: {}", e);
                    }
                }
                init = false;
            }
            if (!mode.equals("local")) {
                // If we are already connected then just send new login credentials otherwise start the connection
                if (cloudOnline) {
                    logger.debug("Logging in to websocket");
                    // wslogin();
                    CommandMessage message = new CommandMessage("login");
                    message.setSequence();
                    converter.addMessage(message);
                    sendWebsocketMessage(message);
                } else {
                    ws.start();
                }
            }
        }
    }

    @Override
    public void websocketConnected(Boolean connected) {
        if (connected) {
            CommandMessage message = new CommandMessage("login");
            message.setSequence();
            converter.addMessage(message);
            sendWebsocketMessage(message);
        } else {
            cloudOnline = false;
        }
    }

    @Override
    public void lanConnected(Boolean connected) {
        this.lanOnline = connected;
        logger.debug("Local Mode {}", connected == true ? "connected" : "disconnected");
        connected();
    }

    private void sendWebsocketMessage(CommandMessage message) {
        logger.debug("Sending websocket Message: {} - {} - {}", message.getCommand(), message.getParams(),
                message.getDeviceid());
        // message.setSequence();
        switch (message.getCommand()) {
            case "login":
                WebsocketRequest login = new WebsocketRequest("userOnline", api.getApiKey(), message.getSequence(),
                        api.getAt());
                message.setParams(gson.toJson(login));
                break;
            default:
                JsonObject payload = new JsonParser().parse(message.getParams()).getAsJsonObject();
                WebsocketRequest update = new WebsocketRequest("update", api.getApiKey(), message.getSequence(),
                        message.getDeviceid(), payload);
                message.setParams(gson.toJson(update));
                break;
        }
        ws.sendMessage(message.getParams());
    }

    private void sendLANMessage(CommandMessage message) {
        lan.setStatusLan(message);
    }

    public void sendUpdate(CommandMessage message) {
        queue.sendCommand(message);
    }

    @Override
    public void sendMessage(CommandMessage message) {
        converter.addMessage(message);
        // Mark the command as locally supported
        Boolean localSupported = false;
        String deviceid = message.getDeviceid();
        String command = message.getCommand();
        if (command.contains("switch") || command.equals("transmit")) {
            localSupported = true;
        }

        // Dont send commands if not supported by local mode
        if (!localSupported && mode.equals("local")) {
            logger.warn("Sonoff - Cannot send command {} for device {}, Not supported by local mode", command,
                    deviceid);
        }
        // If local command supported and is online send local version
        else if (localSupported && lanOnline && message.getIpaddress() != null && !message.getIpaddress().equals("")) {
            logger.debug("Sending message via LAN");
            sendLANMessage(message);
        }
        // If cloud connected send command
        else if (cloudOnline) {
            logger.debug("Sending message via Cloud");
            sendWebsocketMessage(message);
        }
        // Oh no all our connections are offline but we shouldnt actually get here
        else {
            logger.error("Cannot send command, all connections are offline for deviceid {} and command {}", deviceid,
                    command);
        }
    }

    @Override
    public void okMessage(Long sequence) {
        // not applicable
    }

    @Override
    public void websocketLoggedIn(Boolean loggedIn) {
        this.cloudOnline = loggedIn;
        logger.debug("Remote Mode {}", loggedIn == true ? "connected" : "disconnected");
        connected();
    }
}
