/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.sonoff.internal.helpers;

import java.util.Enumeration;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.dto.api.LanResponse;
import org.openhab.binding.sonoff.internal.dto.api.Params;
import org.openhab.binding.sonoff.internal.dto.api.ThingResponse;
import org.openhab.binding.sonoff.internal.dto.api.WsMessage;
import org.openhab.binding.sonoff.internal.listeners.ConnectionListener;
import org.openhab.binding.sonoff.internal.listeners.MessageListener;
import org.openhab.binding.sonoff.internal.listeners.RawMessageListener;
import org.openhab.binding.sonoff.internal.states.StateBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link MessageQueue} provides a sequential queue for outgoing messages
 * accross connections and allows for retrying when messages are not delivered
 * correctly
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class MessageSystem implements Runnable, RawMessageListener {

    private final Logger logger = LoggerFactory.getLogger(MessageSystem.class);

    // Map of message retry attempts
    private final ConcurrentMap<Long, CountDownLatch> latchMap = new ConcurrentHashMap<Long, CountDownLatch>();
    // Queue of messages to send
    private final BlockingDeque<CommandMessage> queue = new LinkedBlockingDeque<CommandMessage>();
    // Map of Integers so we can count retry attempts.
    private final ConcurrentMap<Long, Integer> retryCountMap = new ConcurrentHashMap<Long, Integer>();
    // Map of our message types so we can process them correctly
    private final ConcurrentMap<Long, String> messageTypes = new ConcurrentHashMap<Long, String>();
    // Timeout
    private final int timeoutForOkMessagesMs = 1000;
    // Listeners
    private final MessageListener listener;
    private final ConnectionListener connectionListener;
    // Boolean to indicate if we are running
    private boolean running;

    private final Gson gson;

    public MessageSystem(ConnectionListener connectionListener, MessageListener listener, Gson gson) {
        this.listener = listener;
        this.gson = gson;
        this.connectionListener = connectionListener;
    }

    @Override
    public void run() {
        try {
            // Get the first message in the queue
            CommandMessage message = queue.take();
            if (message.getSequence().equals(0L)) {
                message.setSequence();
            }
            // Add our message type so we can identify it correctly when we get the response
            messageTypes.put(message.getSequence(), message.getCommand());
            logger.debug("Message Type added: {} - {}", message.getSequence(), message.getCommand());

            CountDownLatch latch = new CountDownLatch(1);
            latchMap.putIfAbsent(message.getSequence(), latch);
            retryCountMap.putIfAbsent(message.getSequence(), Integer.valueOf(1));
            listener.sendMessage(message);
            boolean unlatched = latch.await(timeoutForOkMessagesMs, TimeUnit.MILLISECONDS);
            latchMap.remove(message.getSequence());
            Integer newRetryCount = 0;
            if (!unlatched) {
                Integer sendCount = retryCountMap.get(message.getSequence());
                if (sendCount != null) {
                    if (sendCount.intValue() >= 3) {
                        logger.warn("Unable to send transaction {}, command was {}, after {} retry attempts",
                                message.getSequence(), message.getCommand(), 3);
                        return;
                    }
                    newRetryCount = Integer.valueOf(sendCount.intValue() + 1);
                }
                if (!running) {
                    logger.error("Not retrying transactionId {} as we are stopping", message.getSequence());
                    return;

                }
                logger.warn(
                        "Ok message not received for transaction: {}, command was {}, retrying again. Retry count {}",
                        message.getSequence(), message.getCommand(), newRetryCount);
                retryCountMap.put(message.getSequence(), newRetryCount);
                queue.addFirst(message);
            }
        } catch (InterruptedException e) {
            logger.error("Error Running queue:{}", e.getMessage());
        }
    }

    public synchronized void stopRunning() {
        running = false;
    }

    public synchronized void startRunning() {
        running = true;
    }

    // Add the messsage to the queue
    public void queueMessage(CommandMessage message) {
        try {
            if (running) {
                queue.put(message);
            } else {
                logger.info("Message not added to queue as we are shutting down");
            }
        } catch (InterruptedException e) {
            logger.error("Error adding command to queue:{}", e.getMessage());
        }
    }

    // clear the queue if we are re-initialising / disposing etc
    public void queueClear() {
        queue.clear();
        retryCountMap.clear();
        latchMap.clear();
        messageTypes.clear();
    }

    // send message (bypass the queue for important things such as logging in)
    public void sendMessage(CommandMessage message) {
        if (message.getSequence().equals(0L)) {
            message.setSequence();
        }
        messageTypes.put(message.getSequence(), message.getCommand());
        logger.debug("Message Type added: {} - {}", message.getSequence(), message.getCommand());
        listener.sendMessage(message);
    }

    private void okMessage(Long sequence) {
        CountDownLatch latch = latchMap.get(sequence);
        if (latch != null) {
            latch.countDown();
        }
    }

    @Override
    public void lanEvent(ServiceInfo eventInfo) {
        JsonObject jsonObject = new JsonObject();
        Enumeration<String> info = eventInfo.getPropertyNames();
        while (info.hasMoreElements()) {
            String name = info.nextElement().toString();
            String value = eventInfo.getPropertyString(name);
            jsonObject.addProperty(name, value);
        }
        String deviceid = jsonObject.get("id").getAsString();
        StateBase device = States.getState(deviceid);
        if (device == null) {
            logger.error("The device {} doesnt exist, unable to decrypt incoming lan message", deviceid);
            return;
        } else {
            String deviceKey = device.getDeviceKey();
            if (deviceKey.equals("")) {
                logger.error("The device {} doesnt have a device key present, unable to decrypt incoming lan message",
                        deviceid);
                return;
            }
            Device newDevice = new Device();
            String decryptedMessage = Utils.decrypt(jsonObject, deviceKey);
            newDevice.setParams(gson.fromJson(decryptedMessage, Params.class));
            newDevice.setDeviceid(deviceid);
            String localAddress = eventInfo.getInet4Addresses()[0].getHostAddress();
            newDevice.setLocalAddress(localAddress.equals("null") ? "" : localAddress);
            device.updateState(newDevice);
        }
    }

    @Override
    public void lanResponse(String message) {
        LanResponse response = gson.fromJson(message, LanResponse.class);
        if (response != null) {
            okMessage(Long.parseLong(response.getSequence()));
            messageTypes.remove(Long.parseLong(response.getSequence()));
        } else {
            logger.error("LAN response returned null for message: {}", message);
        }
    }

    @Override
    public void websocketMessage(String message) {
        logger.debug("Websocket received message: {}", message);
        WsMessage response = gson.fromJson(message, WsMessage.class);
        if (response != null) {
            Integer error = response.getError();
            if (response.getSequence() != null) {
                Long sequence = Long.parseLong(response.getSequence());
                latchMap.get(sequence);
                String type = messageTypes.get(sequence);
                if (type != null) {
                    messageTypes.remove(sequence);
                    okMessage(sequence);
                    switch (type) {
                        case "uiActive":
                            logger.trace("UiActive Response received: {}", message);
                            if (!error.equals(0)) {
                                String msg = response.getReason() != null ? response.getReason() : "No Reason";
                                String devid = response.getDeviceid() != null ? response.getDeviceid()
                                        : "Unknown Device";
                                logger.trace("Streaming Data Activation Error {} - {} ,For Device:{}",
                                        response.getError(), msg, devid);
                            }
                            break;
                        case "login":
                            logger.trace("Login Response received: {}", message);
                            if (error.equals(0)) {
                                connectionListener.websocketLoggedIn(true);
                            } else {
                                connectionListener.websocketLoggedIn(false);
                            }
                            break;
                        case "consumption":
                            logger.trace("Consumption Response received: {}", message);
                            if (response.getConfig() != null) {
                                Device newDevice = gson.fromJson(message, Device.class);
                                if (newDevice != null) {
                                    StateBase device = States.getState(newDevice.getDeviceid());
                                    if (device == null) {
                                        logger.error(
                                                "The device {} doesnt exist, unable to forward incoming websocket message",
                                                newDevice.getDeviceid());
                                        return;
                                    } else {
                                        logger.debug("Forwarding consumption message for {}", newDevice.getDeviceid());
                                        device.updateState(newDevice);
                                    }
                                } else {
                                    logger.debug("Couldn't create device for consumption message");
                                }
                            } else {
                                logger.debug("This is a bug in the ewelink api where consumption returns empty");
                            }
                            break;
                        case "switch":
                            logger.trace("Switch Response received: {}", message);
                            break;
                        default:
                            logger.error("Response message not handled for type {}, please report:{}", type, message);
                    }
                } else {
                    logger.debug(
                            "Message type didnt exist for sequence: {}, if we were sent via lan a websocket response will also come in for the same sequence so this is normal",
                            sequence);
                }
            } else if (response.getSequence() == null) {
                if (response.getAction() != null) {
                    logger.trace("Device Response received: {}", message);
                    switch (response.getAction()) {
                        case "update":
                        case "sysmsg":
                            Device newDevice = gson.fromJson(message, Device.class);
                            if (newDevice != null) {
                                StateBase device = States.getState(newDevice.getDeviceid());
                                if (device == null) {
                                    logger.error(
                                            "The device {} doesnt exist, unable to forward incoming websocket message",
                                            newDevice.getDeviceid());
                                    return;
                                } else {
                                    device.updateState(newDevice);
                                }
                            } else {
                                logger.debug("Couldn't create device for update message");
                            }
                            break;
                        default:
                            logger.error("Device message not handled, please report:{}", message);
                    }
                } else {
                    logger.error("Websocket message not handled, please report:{}", message);
                }
            }
        }
    }

    @Override
    public void apiMessage(ThingResponse thingResponse) {
        for (int i = 0; i < thingResponse.getData().getThingList().size(); i++) {
            Device newDevice = thingResponse.getData().getThingList().get(i).getItemData();
            StateBase device = States.getState(newDevice.getDeviceid());
            if (device == null) {
                logger.error("The device {} doesnt exist, unable to forward incoming api message",
                        newDevice.getDeviceid());
            } else {
                logger.trace("processing api message for deviceid {}", newDevice.getDeviceid());
                if (newDevice.getOnline()) {
                    device.updateState(newDevice);
                } else {
                    logger.trace("Didnt update device state for deviceid {} as its not connected to the internet",
                            newDevice.getDeviceid());
                }
            }
        }
    }
}
