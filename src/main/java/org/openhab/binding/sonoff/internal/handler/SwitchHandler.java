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

import static org.openhab.core.library.unit.Units.*;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.ElectricCurrent;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonoff.internal.Utils;
import org.openhab.binding.sonoff.internal.config.DeviceConfig;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.dto.payloads.Consumption;
import org.openhab.binding.sonoff.internal.dto.payloads.MultiSwitch;
import org.openhab.binding.sonoff.internal.dto.payloads.SLed;
import org.openhab.binding.sonoff.internal.dto.payloads.SingleSwitch;
import org.openhab.binding.sonoff.internal.helpers.CommandMessage;
import org.openhab.binding.sonoff.internal.listeners.DeviceListener;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
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
public class SwitchHandler extends BaseThingHandler implements DeviceListener {

    private final Logger logger = LoggerFactory.getLogger(SwitchHandler.class);
    private @Nullable AccountHandler account;
    private @Nullable DeviceConfig config;
    private @Nullable ScheduledFuture<?> localTask;
    private @Nullable ScheduledFuture<?> consumptionTask;
    private final Gson gson;
    private String ipaddress = "";
    private Boolean cloud = false;
    private Boolean local = false;
    private @Nullable Device device = new Device();

    public SwitchHandler(Thing thing, Gson gson) {
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
            account = (AccountHandler) thingHandler;

            if (bridgeStatus == ThingStatus.ONLINE) {
                account.addDeviceListener(config.deviceid, device.getDevicekey(), this);
                startTasks();
                isConnected();
            } else {
                account.removeDeviceListener(config.deviceid);
                cancelTasks();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            account.removeDeviceListener(config.deviceid);
            cancelTasks();
            updateStatus(ThingStatus.OFFLINE);
        }
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

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        cancelTasks();
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

    private void startTasks() {
        // Task to poll for Consumption Data if we are using the cloud (POW / POWR2)
        Runnable consumptionData = () -> {
            if (cloud) {
                Consumption params = new Consumption();
                CommandMessage message = new CommandMessage("consumption", gson.toJson(params), config.deviceid,
                        ipaddress, device.getDevicekey());
                account.sendUpdate(message);
            }
        };
        if (this.thing.getConfiguration().containsKey("consumption")) {
            if ((!account.getMode().equals("local")) && config.consumption.equals(true)) {
                logger.debug("Starting consumption task for {}", config.deviceid);
                consumptionTask = scheduler.scheduleWithFixedDelay(consumptionData, 10, config.consumptionPoll,
                        TimeUnit.SECONDS);
            }
        }

        // Task to poll the lan if we are in local only mode or internet access is blocked (POW / POWR2)
        Runnable localPollData = () -> {
            String id = this.thing.getThingTypeUID().getId();
            String params = "";
            String endpoint = "";
            switch (id) {
                case "1":
                case "5":
                case "6":
                case "14":
                case "15":
                case "32":
                    SingleSwitch singleSwitch = new SingleSwitch();
                    params = gson.toJson(singleSwitch);
                    endpoint = "switch";
                    break;
                default:
                    MultiSwitch multiSwitch = new MultiSwitch();
                    params = gson.toJson(multiSwitch);
                    endpoint = "switches";
                    break;
            }
            CommandMessage message = new CommandMessage(endpoint, params, config.deviceid, ipaddress,
                    device.getDevicekey());
            account.sendUpdate(message);
        };
        if (account.getMode() != null
                && (account.getMode().equals("local") || (cloud.equals(false) && account.getMode().equals("mixed")))) {
            if (this.thing.getConfiguration().containsKey("local")) {
                if (config.local.equals(true)) {
                    logger.debug("Starting local task for {}", config.deviceid);
                    localTask = scheduler.scheduleWithFixedDelay(localPollData, 10, config.localPoll, TimeUnit.SECONDS);
                }
            }
        }
    }

    private void cancelTasks() {
        if (localTask != null) {
            localTask.cancel(true);
            localTask = null;
        }
        if (consumptionTask != null) {
            consumptionTask.cancel(true);
            consumptionTask = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String params = "";
        String endpoint = "";
        if (command instanceof RefreshType) {
            return;
        } else {
            switch (channelUID.getId()) {
                case "switch":
                    SingleSwitch singleSwitch = new SingleSwitch();
                    singleSwitch.setSwitch(command.toString().toLowerCase());
                    params = gson.toJson(singleSwitch);
                    endpoint = "switch";
                    break;
                case "switch0":
                case "switch1":
                case "switch2":
                case "switch3":
                    MultiSwitch multiSwitch = new MultiSwitch();
                    MultiSwitch.Switch newSwitch = multiSwitch.new Switch();
                    Integer outlet = Integer.parseInt(channelUID.getId().substring(channelUID.getId().length() - 1));
                    newSwitch.setOutlet(outlet);
                    newSwitch.setSwitch(command.toString().toLowerCase());
                    multiSwitch.getSwitches().add(newSwitch);
                    params = gson.toJson(multiSwitch);
                    endpoint = "switches";
                    break;
                case "sled":
                    SLed sled = new SLed();
                    sled.setSledOnline(command.toString().toLowerCase());
                    params = gson.toJson(sled);
                    endpoint = "sledOnline";
                    break;
            }
            logger.debug("Sonoff - Command Payload:{}", params);
            CommandMessage message = new CommandMessage(endpoint, params, config.deviceid, ipaddress,
                    device.getDevicekey());
            account.sendUpdate(message);
        }
    }

    private synchronized void updateState(Device newDevice) {
        for (int i = 0; i < newDevice.getParams().getSwitches().size(); i++) {
            if (newDevice.getParams().getSwitches().get(i).getSwitch() != null && this.thing
                    .getChannel("switch" + newDevice.getParams().getSwitches().get(i).getOutlet()) != null) {
                updateState(
                        this.thing.getChannel("switch" + newDevice.getParams().getSwitches().get(i).getOutlet())
                                .getUID(),
                        newDevice.getParams().getSwitches().get(i).getSwitch().equals("on") ? OnOffType.ON
                                : OnOffType.OFF);
            }
        }
        if (newDevice.getParams().getSwitch() != null && this.thing.getChannel("switch") != null) {
            updateState(this.thing.getChannel("switch").getUID(),
                    newDevice.getParams().getSwitch().equals("on") ? OnOffType.ON : OnOffType.OFF);
        }
        if (newDevice.getParams().getPower() != null && (this.thing.getChannel("power") != null)) {
            updateState(this.thing.getChannel("power").getUID(),
                    new QuantityType<Power>(Float.parseFloat(newDevice.getParams().getPower()), WATT));
        }
        if (newDevice.getParams().getVoltage() != null && this.thing.getChannel("voltage") != null) {
            updateState(this.thing.getChannel("voltage").getUID(),
                    new QuantityType<ElectricPotential>(Float.parseFloat(newDevice.getParams().getVoltage()), VOLT));
        }
        if (newDevice.getParams().getCurrent() != null && this.thing.getChannel("current") != null) {
            updateState(this.thing.getChannel("current").getUID(),
                    new QuantityType<ElectricCurrent>(Float.parseFloat(newDevice.getParams().getCurrent()), (AMPERE)));
        }

        if (newDevice.getParams().getRssi() != null && this.thing.getChannel("rssi") != null) {
            updateState(this.thing.getChannel("rssi").getUID(),
                    new QuantityType<Power>(newDevice.getParams().getRssi(), (DECIBEL_MILLIWATTS)));
        }
        if (newDevice.getParams().getSledOnline() != null && this.thing.getChannel("sled") != null) {
            updateState(this.thing.getChannel("sled").getUID(),
                    newDevice.getParams().getSledOnline().equals("on") ? OnOffType.ON : OnOffType.OFF);
        }

        if (newDevice.getParams().getDeviceType() != null && this.thing.getChannel("deviceType") != null) {
            updateState(this.thing.getChannel("deviceType").getUID(),
                    new StringType(newDevice.getParams().getDeviceType()));
        }
        if (newDevice.getParams().getMainSwitch() != null && this.thing.getChannel("mainSwitch") != null) {
            updateState(this.thing.getChannel("mainSwitch").getUID(),
                    new StringType(newDevice.getParams().getMainSwitch()));
        }
        if (newDevice.getParams().getCurrentTemperature() != null && this.thing.getChannel("temperature") != null) {
            if (newDevice.getParams().getCurrentTemperature().equals("unavailable")) {
                updateState(this.thing.getChannel("temperature").getUID(), new QuantityType<>("0"));
            } else {
                updateState(this.thing.getChannel("temperature").getUID(),
                        new QuantityType<>(newDevice.getParams().getCurrentTemperature()));
            }
        }
        if (newDevice.getParams().getCurrentHumidity() != null && this.thing.getChannel("humidity") != null) {
            if (newDevice.getParams().getCurrentHumidity().equals("unavailable")) {
                updateState(this.thing.getChannel("humidity").getUID(), new PercentType("0"));
            } else {
                updateState(this.thing.getChannel("humidity").getUID(),
                        new PercentType(newDevice.getParams().getCurrentHumidity()));
            }
        }
    }

    @Override
    public void updateDevice(Device newDevice) {
        if (newDevice.getConfig() != null) {
            if (newDevice.getConfig().getHundredDaysKwhData() != null) {
                consumption(newDevice.getConfig().getHundredDaysKwhData());
            }
            return;
        }

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

    private void consumption(String data) {
        String[] hexValues;
        String[] splitData = data.split("(?<=\\G.{6})");
        double total = 0.00;
        for (int i = 0; i < 100; i++) {
            hexValues = splitData[i].split("(?<=\\G.{2})");
            total = total + Double.parseDouble(Integer.parseInt(hexValues[0], 16) + "."
                    + Integer.parseInt(hexValues[1], 16) + Integer.parseInt(hexValues[2], 16));
            if (i == 0) {
                if (this.thing.getChannel("todayKwh") != null) {
                    updateState(this.thing.getChannel("todayKwh").getUID(),
                            new QuantityType<Energy>(total, (KILOWATT_HOUR)));
                }
            }
            if (i == 1) {
                if (this.thing.getChannel("yesterdayKwh") != null) {
                    double newtotal = Double.parseDouble(Integer.parseInt(hexValues[0], 16) + "."
                            + Integer.parseInt(hexValues[1], 16) + Integer.parseInt(hexValues[2], 16));
                    updateState(this.thing.getChannel("yesterdayKwh").getUID(),
                            new QuantityType<Energy>(newtotal, (KILOWATT_HOUR)));
                }
            }
            if (i == 6) {
                if (this.thing.getChannel("sevenKwh") != null) {
                    updateState(this.thing.getChannel("sevenKwh").getUID(),
                            new QuantityType<Energy>(total, (KILOWATT_HOUR)));
                }
            }
            if (i == 29) {
                if (this.thing.getChannel("thirtyKwh") != null) {
                    updateState(this.thing.getChannel("thirtyKwh").getUID(),
                            new QuantityType<Energy>(total, (KILOWATT_HOUR)));
                }
            }
            if (i == 99) {
                if (this.thing.getChannel("hundredKwh") != null) {
                    updateState(this.thing.getChannel("hundredKwh").getUID(),
                            new QuantityType<Energy>(total, (KILOWATT_HOUR)));
                }
            }
        }
    }
}
