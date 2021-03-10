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
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import org.openhab.binding.sonoff.internal.dto.api.ThingList;
import org.openhab.binding.sonoff.internal.dto.api.ThingResponse;
import org.openhab.binding.sonoff.internal.dto.payloads.UiActive;
import org.openhab.binding.sonoff.internal.dto.payloads.WebsocketRequest;
import org.openhab.binding.sonoff.internal.helpers.CommandMessage;
import org.openhab.binding.sonoff.internal.helpers.MessageSystem;
import org.openhab.binding.sonoff.internal.helpers.States;
import org.openhab.binding.sonoff.internal.helpers.Utils;
import org.openhab.binding.sonoff.internal.listeners.ConnectionListener;
import org.openhab.binding.sonoff.internal.listeners.MessageListener;
import org.openhab.binding.sonoff.internal.listeners.RawMessageListener;
import org.openhab.binding.sonoff.internal.states.StateBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link HandlerAccount} is responsible for the main Ewelink Account and
 * manages the connections to devices
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class HandlerAccount extends BaseBridgeHandler implements ConnectionListener, MessageListener {

    private final Logger logger = LoggerFactory.getLogger(HandlerAccount.class);
    private final Gson gson;
    private final MessageSystem messageSystem;
    private final Lan lan;
    private final Api api;
    private final Websocket ws;

    private @Nullable ScheduledFuture<?> tokenTask;
    private @Nullable ScheduledFuture<?> connectionTask;
    private @Nullable ScheduledFuture<?> activateTask;
    private @Nullable ScheduledFuture<?> queueTask;

    private AccountConfig config = this.getConfigAs(AccountConfig.class);
    private Boolean lanOnline = false;
    private Boolean cloudOnline = false;
    private String mode = "";

    public HandlerAccount(Bridge thing, WebSocketFactory webSocketFactory, HttpClientFactory httpClientFactory,
            Gson gson) {
        super(thing);
        this.gson = gson;
        this.messageSystem = new MessageSystem(this, this, gson);
        RawMessageListener listener = this.messageSystem;
        this.lan = new Lan(this, listener, httpClientFactory);
        this.api = new Api(gson, this, listener, httpClientFactory);
        this.ws = new Websocket(this, listener, webSocketFactory, api);
    }

    public String getMode() {
        return this.mode;
    }

    private void setMode(String mode) {
        this.mode = mode;
    }

    private Boolean getCloud() {
        return this.cloudOnline;
    }

    private void setCloud(Boolean cloudOnline) {
        this.cloudOnline = cloudOnline;
    }

    private Boolean getLan() {
        return this.lanOnline;
    }

    private void setLan(Boolean lanOnline) {
        this.lanOnline = lanOnline;
    }

    private void setAccountConfig(AccountConfig config) {
        this.config = config;
    }

    private AccountConfig getAccountConfig() {
        return this.config;
    }

    private Lan getLanConnection() {
        return this.lan;
    }

    private Websocket getWebsocketConnection() {
        return this.ws;
    }

    private Api getApiConnection() {
        return this.api;
    }

    private MessageSystem getMessageSystem() {
        return this.messageSystem;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(MainDiscovery.class);
    }

    @Override
    public void initialize() {

        // now we can start initialising
        logger.debug("Initialising Sonoff Account: {}", this.thing.getUID());
        // Clear the message queues
        getMessageSystem().queueClear();
        setAccountConfig(this.getConfigAs(AccountConfig.class));
        setMode(getAccountConfig().accessmode);
        logger.info("Account mode set to {}", getMode());
        // Set the device List
        createStates();

        // Start the Lan connection
        if (!getMode().equals("cloud")) {
            try {
                getLanConnection().start(getAccountConfig().ipaddress);
            } catch (IOException e) {
                logger.error("Unable to start LAN connection: {}", e.getMessage());
            }
        }

        // start the api connection
        if (!getMode().equals("local")) {
            getApiConnection().setCountryCode(getAccountConfig().countryCode);
            getApiConnection().setEmail(getAccountConfig().email);
            getApiConnection().setPassword(getAccountConfig().password);
            getApiConnection().startApi();
        }

        // Start our message queue for outgoing messages
        getMessageSystem().startRunning();
        queueTask = scheduler.scheduleWithFixedDelay(getMessageSystem(), 0, 100, TimeUnit.MILLISECONDS);

        // Start any polling tasks we need
        createTasks();

        // Set an initial status
        if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);
    }

    public Boolean createState(String deviceid) {
        try {
            String deviceJson = Utils.getDeviceFile(deviceid);
            if (!deviceJson.equals("")) {
                ThingResponse thing = gson.fromJson(deviceJson, ThingResponse.class);
                if (thing != null) {
                    Device device = thing.getData().getThingList().get(0).getItemData();
                    if (device != null) {
                        States.addState(device);
                        return true;
                    }
                }
                return false;
            } else {
                return false;
            }
        } catch (IOException e) {
            logger.error("Unable to create our device list: {}", e.getMessage());
            return false;
        }
    }

    private void createStates() {
        try {
            List<String> deviceList = Utils.getDeviceFiles();
            for (int i = 0; i < deviceList.size(); i++) {
                if (!deviceList.get(i).equals("")) {
                    ThingResponse thing = gson.fromJson(deviceList.get(i), ThingResponse.class);
                    if (thing != null) {
                        Device device = thing.getData().getThingList().get(0).getItemData();
                        if (device != null) {
                            States.addState(device);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Unable to create our device list: {}", e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Sonoff - Running dispose()");
        final ScheduledFuture<?> activateTask = this.activateTask;
        if (activateTask != null) {
            activateTask.cancel(true);
            this.activateTask = null;
        }
        final ScheduledFuture<?> tokenTask = this.tokenTask;
        if (tokenTask != null) {
            tokenTask.cancel(true);
            this.tokenTask = null;
        }
        final ScheduledFuture<?> connectionTask = this.connectionTask;
        if (connectionTask != null) {
            connectionTask.cancel(true);
            this.connectionTask = null;
        }
        getMessageSystem().stopRunning();
        final ScheduledFuture<?> queueTask = this.queueTask;
        if (queueTask != null) {
            queueTask.cancel(true);
            this.queueTask = null;
        }
        try {
            getLanConnection().stop();
        } catch (IOException e) {
            logger.debug("Unable to stop LAN connection : {}", e.getMessage());
        }
        getWebsocketConnection().stop();
        setLan(false);
        setCloud(false);
        super.dispose();
    }

    private void createTasks() {
        if (!getMode().equals("local")) {
            // Task to refresh our login credentials
            Runnable getToken = () -> {
                getApiConnection().updateCredentials();
            };
            tokenTask = scheduler.scheduleWithFixedDelay(getToken, 12, 12, TimeUnit.HOURS);

            // Task to check we are still connected
            Runnable connection = () -> {
                logger.debug("Sonoff Connection Check Running");
                if (!getCloud()) {
                    getApiConnection().startApi();
                }
            };
            connectionTask = scheduler.scheduleWithFixedDelay(connection, 60, 60, TimeUnit.SECONDS);

            Runnable activate = () -> {
                if (getCloud()) {
                    // Enquire the status of all our devices
                    getApiConnection().getDevices();
                    // For each device that supports streaming data send activation
                    ConcurrentMap<String, StateBase> map = States.getStates();
                    if (map != null) {
                        for (ConcurrentMap.Entry<String, StateBase> entry : map.entrySet()) {
                            Integer uiid = entry.getValue().getUiid();
                            if (uiid.equals(5) || uiid.equals(32)) {
                                if (entry.getValue().getCloud()) {
                                    UiActive params = new UiActive();
                                    params.setUiActive(60);
                                    CommandMessage message = new CommandMessage("uiActive", gson.toJson(params),
                                            entry.getValue().getDeviceid());
                                    sendUpdate(message);
                                }
                            }
                        }
                    }
                }
            };
            activateTask = scheduler.scheduleWithFixedDelay(activate, 5, 60, TimeUnit.SECONDS);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    // Used for discovery
    public List<ThingList> createCache()
            throws IOException, InterruptedException, TimeoutException, ExecutionException {
        return getApiConnection().createCache().getData().getThingList();
    }

    private void connected() {
        if (getMode().equals("local") && getLan()) {
            updateStatus(ThingStatus.ONLINE);
        } else if (getMode().equals("cloud") & getCloud()) {
            updateStatus(ThingStatus.ONLINE);
        } else if (getMode().equals("mixed")) {
            if (getLan() && getCloud()) {
                updateStatus(ThingStatus.ONLINE);
            } else if (getLan() && !getCloud()) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cloud Offline");
            } else if (!getLan() && getCloud()) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.COMMUNICATION_ERROR, "LAN Offline");
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    // login to websocket
    private void websocketLogin() {
        CommandMessage message = new CommandMessage("login");
        message.setSequence();
        WebsocketRequest login = new WebsocketRequest("userOnline", getApiConnection().getApiKey(),
                message.getSequence(), getApiConnection().getAt());
        message.setParams(gson.toJson(login));
        logger.debug("Logging in to websocket");
        getMessageSystem().sendMessage(message);
    }

    // Messages coming from devices - add them to the queue
    public void sendUpdate(CommandMessage message) {
        getMessageSystem().queueMessage(message);
    }

    // Message coming back from the queue to send to the appropriate connection
    @Override
    public void sendMessage(CommandMessage message) {
        // Send login requests straight away
        if (message.getCommand().equals("login")) {
            getWebsocketConnection().sendMessage(message.getParams());
            return;
        }

        String deviceid = message.getDeviceid();
        StateBase device = States.getState(deviceid);
        if (device != null) {
            message.setDeviceKey(device.getDeviceKey());
            message.setIpaddress(device.getIpAddress().toString());
        }

        // Mark the command as locally supported
        Boolean localSupported = false;
        String command = message.getCommand();
        if (command.contains("switch") || command.equals("transmit")) {
            localSupported = true;
        }

        // Dont send commands if not supported by local mode
        if (!localSupported && config.accessmode.equals("local")) {
            logger.warn("Sonoff - Cannot send command {} for device {}, Not supported by local mode", command,
                    deviceid);
        }
        // If local command supported and is online send local version
        else if (localSupported && getLan() && !message.getIpaddress().equals("")) {
            logger.debug("Sending message via LAN");
            getLanConnection().setStatusLan(message);
        }
        // If cloud connected send command
        else if (getCloud()) {
            logger.debug("Sending message via Cloud");
            JsonObject payload = new JsonParser().parse(message.getParams()).getAsJsonObject();
            WebsocketRequest update = new WebsocketRequest("update", getApiConnection().getApiKey(),
                    message.getSequence(), message.getDeviceid(), payload);
            message.setParams(gson.toJson(update));
            getWebsocketConnection().sendMessage(message.getParams());
        }
        // Oh no all our connections are offline but we shouldnt actually get here
        else {
            logger.error("Cannot send command, all connections are offline for deviceid {} and command {}", deviceid,
                    command);
        }
    }

    @Override
    public void websocketConnected(Boolean connected) {
        if (connected) {
            websocketLogin();
        } else {
            setCloud(false);
        }
    }

    @Override
    public void websocketLoggedIn(Boolean loggedIn) {
        setCloud(loggedIn);
        logger.debug("Remote Mode {}", loggedIn ? "connected" : "disconnected");
        connected();
    }

    @Override
    public void lanConnected(Boolean connected) {
        setLan(connected);
        logger.debug("Local Mode {}", connected ? "connected" : "disconnected");
        connected();
    }

    @Override
    public void apiConnected(Boolean connected) {
        if (connected) {
            if (!getMode().equals("local")) {
                // If we are already connected then just send new login credentials otherwise start the websocket
                if (getCloud()) {
                    websocketLogin();
                } else {
                    try {
                        getWebsocketConnection().start();
                    } catch (InterruptedException | TimeoutException | ExecutionException | IOException
                            | URISyntaxException e) {
                        logger.error("Unable to start websocket: {}", e.getMessage());
                    }
                }
            }
        }
    }
}
