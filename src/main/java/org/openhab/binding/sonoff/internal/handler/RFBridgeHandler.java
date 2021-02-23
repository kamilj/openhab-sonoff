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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
import org.openhab.binding.sonoff.internal.helpers.CommandMessage;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;
import org.openhab.binding.sonoff.internal.listeners.RFListener;
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
public class RFBridgeHandler extends BaseBridgeHandler implements DeviceListener {

    private final Logger logger = LoggerFactory.getLogger(RFBridgeHandler.class);
    private @Nullable AccountHandler account;
    private @Nullable DeviceConfig config;
    private @Nullable ScheduledFuture<?> localTask;
    private final Gson gson;
    private String ipaddress = "";
    private @Nullable Device device = new Device();
    private Boolean cloud = false;
    private Boolean local = false;
    private final Map<String, RFListener> rfListeners = new HashMap<>();

    public RFBridgeHandler(Bridge thing, Gson gson) {
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
        account.removeDeviceListener(config.deviceid);
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());
        Bridge bridge = getBridge();
        if (bridge != null) {
            initializeBridge(bridge.getHandler(), bridgeStatusInfo.getStatus());
        }
    }

    private void initializeBridge(@Nullable ThingHandler thingHandler, ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());

        if (thingHandler != null && bridgeStatus != null) {
            account = (AccountHandler) thingHandler;

            if (bridgeStatus == ThingStatus.ONLINE) {
                account.addDeviceListener(config.deviceid, device.getDevicekey(), this);
                isConnected();
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
        if (localTask != null) {
            localTask.cancel(true);
            localTask = null;
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

    public void sendUpdate(String endpoint, String params) {
        logger.debug("Sonoff - Command Payload:{}", params);
        CommandMessage message = new CommandMessage(endpoint, params, config.deviceid, ipaddress,
                device.getDevicekey());
        account.sendUpdate(message);
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
            CommandMessage message = new CommandMessage(params, endpoint, config.deviceid, ipaddress,
                    device.getDevicekey());
            account.sendUpdate(message);
        }
    }

    private synchronized void updateState(Device newDevice) {
        if (newDevice.getParams().getRssi() != null && this.thing.getChannel("rssi") != null) {
            updateState(this.thing.getChannel("rssi").getUID(),
                    new QuantityType<Power>(newDevice.getParams().getRssi(), (DECIBEL_MILLIWATTS)));
        }
        if (newDevice.getOnline() != null && this.thing.getChannel("cloudOnline") != null) {
            updateState(this.thing.getChannel("cloudOnline").getUID(),
                    new StringType(newDevice.getOnline() ? "connected" : "disconnected"));
        }
        if (newDevice.getParams().getSledOnline() != null && this.thing.getChannel("sled") != null) {
            updateState(this.thing.getChannel("sled").getUID(),
                    newDevice.getParams().getSledOnline().equals("on") ? OnOffType.ON : OnOffType.OFF);
        }

        // handle new updates to rf External triggers
        String ch0 = newDevice.getParams().getRfTrig0();
        if (ch0 != null && rfListeners.get("0") != null) {
            rfListeners.get("0").rfTriggered(0, ch0);
        }

        String ch1 = newDevice.getParams().getRfTrig1();
        if (ch1 != null && rfListeners.get("1") != null) {
            rfListeners.get("1").rfTriggered(1, ch1);
        }

        String ch2 = newDevice.getParams().getRfTrig2();
        if (ch2 != null && rfListeners.get("2") != null) {
            rfListeners.get("2").rfTriggered(2, ch2);
        }

        String ch3 = newDevice.getParams().getRfTrig3();
        if (ch3 != null && rfListeners.get("3") != null) {
            rfListeners.get("3").rfTriggered(3, ch3);
        }

        String ch4 = newDevice.getParams().getRfTrig4();
        if (ch4 != null && rfListeners.get("4") != null) {
            rfListeners.get("4").rfTriggered(4, ch4);
        }

        String ch5 = newDevice.getParams().getRfTrig5();
        if (ch5 != null && rfListeners.get("5") != null) {
            rfListeners.get("5").rfTriggered(5, ch5);
        }

        String ch6 = newDevice.getParams().getRfTrig6();
        if (ch6 != null && rfListeners.get("6") != null) {
            rfListeners.get("6").rfTriggered(6, ch6);
        }

        String ch7 = newDevice.getParams().getRfTrig7();
        if (ch7 != null && rfListeners.get("7") != null) {
            rfListeners.get("7").rfTriggered(7, ch7);
        }

        String ch8 = newDevice.getParams().getRfTrig8();
        if (ch8 != null && rfListeners.get("8") != null) {
            rfListeners.get("8").rfTriggered(8, ch8);
        }
        String ch9 = newDevice.getParams().getRfTrig9();
        if (ch9 != null && rfListeners.get("9") != null) {
            rfListeners.get("9").rfTriggered(9, ch9);
        }

        String ch10 = newDevice.getParams().getRfTrig10();
        if (ch10 != null && rfListeners.get("10") != null) {
            rfListeners.get("10").rfTriggered(10, ch10);
        }

        String ch11 = newDevice.getParams().getRfTrig11();
        if (ch11 != null && rfListeners.get("11") != null) {
            rfListeners.get("11").rfTriggered(11, ch11);
        }

        String ch12 = newDevice.getParams().getRfTrig12();
        if (ch12 != null && rfListeners.get("12") != null) {
            rfListeners.get("12").rfTriggered(12, ch12);
        }
        String ch13 = newDevice.getParams().getRfTrig13();
        if (ch13 != null && rfListeners.get("13") != null) {
            rfListeners.get("13").rfTriggered(13, ch13);
        }

        String ch14 = newDevice.getParams().getRfTrig14();
        if (ch14 != null && rfListeners.get("14") != null) {
            rfListeners.get("14").rfTriggered(14, ch14);
        }

        String ch15 = newDevice.getParams().getRfTrig15();
        if (ch15 != null && rfListeners.get("15") != null) {
            rfListeners.get("15").rfTriggered(15, ch15);
        }
        for (int i = 0; i < newDevice.getParams().getRfList().size(); i++) {
            Integer rfChl = newDevice.getParams().getRfList().get(i).getRfChl();
            String rfVal = newDevice.getParams().getRfList().get(i).getRfVal();
            RFListener rflistener = rfListeners.get(rfChl.toString());
            if (rflistener != null) {
                rflistener.rfCode(rfChl, rfVal);
            }
        }
    }

    @Override
    public void updateDevice(Device newDevice) {
        updateState(newDevice);

        // Get the current states
        Boolean cloudConnected = cloud;

        // Set the new state
        if (newDevice.getLocalAddress() == null) {
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
            if (cloud == true) {
                updateState(newDevice);
            }
            if (this.thing.getChannel("cloudOnline") != null) {
                updateState(this.thing.getChannel("cloudOnline").getUID(),
                        cloud ? new StringType("Connected") : new StringType("Disconnected"));
            }
        } else {
            ipaddress = newDevice.getLocalAddress();
            if (!ipaddress.equals("")) {
                local = true;
            } else {
                local = false;
            }
            if (this.thing.getChannel("localOnline") != null) {
                updateState(this.thing.getChannel("localOnline").getUID(),
                        local ? new StringType("Connected") : new StringType("Disconnected"));
            }
            updateState(newDevice);
        }

        isConnected();
    }

    private void isConnected() {
        // process the states
        if (account.getMode().equals("local")) {
            if (local == true) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else if (account.getMode().equals("cloud")) {
            if (cloud == true) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Device is not connected to the internet");
            }
        } else {
            if (cloud == true && local == true) {
                updateStatus(ThingStatus.ONLINE);
            } else if (cloud == true) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Cloud Only");
            } else if (local == true) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Local Only");
            } else {
                updateStatus(ThingStatus.OFFLINE);
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
    public @Nullable Device getDevice() {
        return this.device;
    }
}
