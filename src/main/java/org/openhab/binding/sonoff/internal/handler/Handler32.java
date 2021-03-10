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
package org.openhab.binding.sonoff.internal.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonoff.internal.config.DeviceConfig;
import org.openhab.binding.sonoff.internal.dto.payloads.Consumption;
import org.openhab.binding.sonoff.internal.dto.payloads.SLed;
import org.openhab.binding.sonoff.internal.dto.payloads.SingleSwitch;
import org.openhab.binding.sonoff.internal.states.State32;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link Handler32} allows the handling of commands and updates to Devices with uuid 32
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class Handler32 extends HandlerBaseDual<State32> {

    private final Logger logger = LoggerFactory.getLogger(Handler32.class);
    private final Gson gson;
    private @Nullable ScheduledFuture<?> localTask;
    private @Nullable ScheduledFuture<?> consumptionTask;

    public Handler32(Thing thing, Gson gson) {
        super(thing, gson);
        this.gson = gson;
    }

    @Override
    public void startTasks() {
        HandlerAccount account = getAccountHandler();
        if (account != null) {
            DeviceConfig config = getDeviceConfig();
            String mode = account.getMode();
            Integer consumptionPoll = config.consumptionPoll;
            Integer localPoll = config.localPoll;
            Boolean consumption = config.consumption;
            Boolean local = config.local;

            // Task to poll for Consumption Data if we are using the cloud (POW / POWR2)
            Runnable consumptionData = () -> {
                if (getCloud()) {
                    Consumption params = new Consumption();
                    sendUpdate("consumption", gson.toJson(params));
                }
            };
            if (!mode.equals("local") && consumption) {
                logger.debug("Starting consumption task for {}", config.deviceid);
                consumptionTask = scheduler.scheduleWithFixedDelay(consumptionData, 10, consumptionPoll,
                        TimeUnit.SECONDS);
            }

            // Task to poll the lan if we are in local only mode or internet access is blocked (POW / POWR2)
            Runnable localPollData = () -> {
                SingleSwitch singleSwitch = new SingleSwitch();
                String params = gson.toJson(singleSwitch);
                String endpoint = "switch";
                sendUpdate(endpoint, params);
            };
            if ((mode.equals("local") || (getCloud().equals(false) && mode.equals("mixed")))) {
                if (local) {
                    logger.debug("Starting local task for {}", config.deviceid);
                    localTask = scheduler.scheduleWithFixedDelay(localPollData, 10, localPoll, TimeUnit.SECONDS);
                }
            }
        }
    }

    @Override
    public void cancelTasks() {
        final ScheduledFuture<?> localTask = this.localTask;
        if (localTask != null) {
            localTask.cancel(true);
            this.localTask = null;
        }
        final ScheduledFuture<?> consumptionTask = this.consumptionTask;
        if (consumptionTask != null) {
            consumptionTask.cancel(true);
            this.consumptionTask = null;
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
                case "sled":
                    SLed sled = new SLed();
                    sled.setSledOnline(command.toString().toLowerCase());
                    params = gson.toJson(sled);
                    endpoint = "sledOnline";
                    break;
            }
            sendUpdate(endpoint, params);
        }
    }

    @Override
    public void updateDevice(State32 newDevice) {
        // Switches
        updateState("switch", newDevice.getSwitch0());
        updateState("power", newDevice.getPower());
        updateState("voltage", newDevice.getVoltage());
        updateState("current", newDevice.getCurrent());
        updateState("rssi", newDevice.getRssi());
        updateState("sled", newDevice.getNetworkLED());
        updateState("todayKwh", newDevice.getTodayKwh());
        updateState("yesterdayKwh", newDevice.getYesterdayKwh());
        updateState("sevenKwh", newDevice.getSevenKwh());
        updateState("thirtyKwh", newDevice.getThirtyKwh());
        updateState("hundredKwh", newDevice.getHundredKwh());
        updateState("ipaddress", newDevice.getIpAddress());

        // Connections
        HandlerAccount account = getAccountHandler();
        if (account != null) {
            String mode = account.getMode();
            setCloud(mode.equals("local") ? false : newDevice.getCloud());
            setLocal(mode.equals("cloud") ? false : newDevice.getLocal());
        }
        updateState("cloudOnline", getCloud() ? new StringType("Connected") : new StringType("Disconnected"));
        updateState("localOnline", getLocal() ? new StringType("Connected") : new StringType("Disconnected"));
        isConnected();
    }
}
