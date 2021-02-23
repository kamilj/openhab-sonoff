package org.openhab.binding.sonoff.internal.helpers;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jmdns.ServiceInfo;

import org.openhab.binding.sonoff.internal.Utils;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.dto.api.LanResponse;
import org.openhab.binding.sonoff.internal.dto.api.Params;
import org.openhab.binding.sonoff.internal.dto.api.ThingResponse;
import org.openhab.binding.sonoff.internal.dto.api.WsMessage;
import org.openhab.binding.sonoff.internal.listeners.ConnectionListener;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;
import org.openhab.binding.sonoff.internal.listeners.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MessageConverter {

    private final MessageListener okListener;
    private final ConnectionListener connectionListener;
    private final ConcurrentMap<Long, String> messageTypes = new ConcurrentHashMap<Long, String>();
    private final ConcurrentMap<String, DeviceListener> deviceListeners = new ConcurrentHashMap<String, DeviceListener>();
    private final ConcurrentMap<String, String> deviceKeys = new ConcurrentHashMap<String, String>();

    public MessageConverter(ConnectionListener connectionListener, MessageListener okListener) {
        this.connectionListener = connectionListener;
        this.okListener = okListener;
    }

    private final Logger logger = LoggerFactory.getLogger(MessageConverter.class);
    private Gson gson = new Gson();

    public synchronized void addMessage(CommandMessage message) {
        messageTypes.put(message.getSequence(), message.getCommand());
        logger.debug("Message Type added: {} - {}", message.getSequence(), message.getCommand());
    }

    public void addDeviceListener(String deviceid, String deviceKey, DeviceListener listener) {
        deviceKeys.putIfAbsent(deviceid, deviceKey);
        deviceListeners.putIfAbsent(deviceid, listener);
        logger.debug("Device Listener Added for deviceId: {}", deviceid);
    }

    public void removeDeviceListener(String deviceid) {
        deviceListeners.remove(deviceid);
        deviceKeys.remove(deviceid);
    }

    public void clearQueue() {
        messageTypes.clear();
    }

    public void convertLanEvent(ServiceInfo eventInfo) {
        JsonObject jsonObject = new JsonObject();
        Enumeration<String> info = eventInfo.getPropertyNames();
        while (info.hasMoreElements()) {
            String name = info.nextElement().toString();
            String value = eventInfo.getPropertyString(name);
            jsonObject.addProperty(name, value);
        }
        String deviceid = jsonObject.get("id").getAsString();
        String deviceKey = deviceKeys.get(deviceid);
        if (deviceKey != null) {
            Device device = new Device();
            String decryptedMessage = Utils.decrypt(jsonObject, deviceKey);
            device.setParams(gson.fromJson(decryptedMessage, Params.class));
            device.setDeviceid(deviceid);
            device.setLocalAddress(eventInfo.getInet4Addresses()[0].getHostAddress());
            sendDeviceMessage(device);
        }
    }

    public void convertLanResponse(String message) {
        LanResponse response = gson.fromJson(message, LanResponse.class);
        okListener.okMessage(Long.parseLong(response.getSequence()));
        messageTypes.remove(Long.parseLong(response.getSequence()));
    }

    public void sendDeviceMessage(Device device) {
        String deviceid = device.getDeviceid();
        // Send the parameter update to our device
        if (deviceListeners.get(deviceid) != null) {
            DeviceListener listener = deviceListeners.get(deviceid);
            listener.updateDevice(device);
        } else {
            // let the user know the device hasnt initialized correctly
            logger.debug("No device listener present for {}", deviceid);
        }
    }

    public void convertApiMessage(String message) {
        sendDeviceMessage(gson.fromJson(message, ThingResponse.class).getData().getThingList().get(0).getItemData());
    }

    public void convertWebsocketMessage(String message) {
        logger.debug("Websocket received message: {}", message);
        WsMessage response = gson.fromJson(message, WsMessage.class);
        if (response.getSequence() != null) {
            String type = messageTypes.get(Long.parseLong(response.getSequence()));
            messageTypes.remove(Long.parseLong(response.getSequence()));
            okListener.okMessage(Long.parseLong(response.getSequence()));
            switch (type) {
                case "uiActive":
                    logger.trace("UiActive Response received: {}", message);

                    if (!response.getError().equals(0)) {
                        String msg = response.getReason() != null ? response.getReason() : "No Reason";
                        String devid = response.getDeviceid() != null ? response.getDeviceid() : "Unknown Device";
                        logger.trace("Streaming Data Activation Error {} - {} ,For Device:{}", response.getError(), msg,
                                devid);
                    }
                    break;
                case "login":
                    logger.trace("Login Response received: {}", message);
                    okListener.okMessage(Long.parseLong(response.getSequence()));
                    if (response.getError() == 0) {
                        connectionListener.websocketLoggedIn(true);
                    } else {
                        connectionListener.websocketLoggedIn(false);
                    }
                    break;
                case "consumption":
                    logger.trace("Consumption Response received: {}", message);
                    if (response.getConfig() != null) {
                        okListener.okMessage(Long.parseLong(response.getSequence()));
                        sendDeviceMessage(gson.fromJson(message, Device.class));
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
        } else if (response.getAction() != null) {
            logger.trace("Device Response received: {}", message);
            switch (response.getAction()) {
                case "update":
                case "sysmsg":
                    sendDeviceMessage(gson.fromJson(message, Device.class));
                    break;
                default:
                    logger.error("Device message not handled, please report:{}");
            }
        } else {
            logger.error("Websocket message not handled, please report:{}");
        }
    }
}
