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
import org.openhab.binding.sonoff.internal.dto.payloads.MultiSwitch;
import org.openhab.binding.sonoff.internal.dto.payloads.RGBLight;
import org.openhab.binding.sonoff.internal.dto.payloads.SLed;
import org.openhab.binding.sonoff.internal.dto.payloads.SingleSwitch;
import org.openhab.binding.sonoff.internal.states.State59;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link Handler4} allows the handling of commands and updates to Devices with uuid's:
 * 1
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class Handler59 extends HandlerBaseCloud<State59> {

    private final Logger logger = LoggerFactory.getLogger(Handler59.class);
    private final Gson gson;
    private @Nullable ScheduledFuture<?> localTask;

    public Handler59(Thing thing, Gson gson) {
        super(thing, gson);
        this.gson = gson;
    }

    public void startTasks() {
        HandlerAccount account = getAccountHandler();
        if (account != null) {
            DeviceConfig config = getDeviceConfig();
            String mode = account.getMode();
            Integer localPoll = config.localPoll;
            Boolean local = config.local;

            // Task to poll the lan if we are in local only mode or internet access is blocked (POW / POWR2)
            Runnable localPollData = () -> {
                MultiSwitch multiSwitch = new MultiSwitch();
                String params = gson.toJson(multiSwitch);
                String endpoint = "switches";
                sendUpdate(endpoint, params);
            };
            if ((mode.equals("local") || (getCloud().equals(false) && mode.equals("mixed")))) {
                // if (this.thing.getConfiguration().containsKey("local")) {
                if (local.equals(true)) {
                    logger.debug("Starting local task for {}", config.deviceid);
                    localTask = scheduler.scheduleWithFixedDelay(localPollData, 10, localPoll, TimeUnit.SECONDS);
                }
            }
        }
    }

    public void cancelTasks() {
        final ScheduledFuture<?> localTask = this.localTask;
        if (localTask != null) {
            localTask.cancel(true);
            this.localTask = null;
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
                case "color":
                    HSBType hsb = new HSBType(command.toString());
                    PercentType red = hsb.getRed();
                    PercentType green = hsb.getGreen();
                    PercentType blue = hsb.getBlue();
                    int redr = (int) (red.doubleValue() * 255 / 100);
                    int greenr = (int) (green.doubleValue() * 255 / 100);
                    int bluer = (int) (blue.doubleValue() * 255 / 100);
                    RGBLight rgb = new RGBLight();
                    rgb.setColorB(bluer);
                    rgb.setColorG(greenr);
                    rgb.setColorR(redr);
                    rgb.setMode(1);
                    rgb.setBright(hsb.getBrightness().intValue());
                    params = gson.toJson(rgb);
                    endpoint = "color";
                    break;
            }
            sendUpdate(endpoint, params);
        }
    }

    @Override
    public void updateDevice(State59 newDevice) {
        updateState("switch", newDevice.getSwitch0());
        updateState("color", newDevice.getColor());
        updateState("brightness", newDevice.getBrightness());
        updateState("rssi", newDevice.getRssi());
        updateState("sled", newDevice.getNetworkLED());
        // Connections
        HandlerAccount account = getAccountHandler();
        if (account != null) {
            String mode = account.getMode();
            setCloud(mode.equals("local") ? false : newDevice.getCloud());
        }
        updateState("cloudOnline", getCloud() ? new StringType("Connected") : new StringType("Disconnected"));
        isConnected();
    }
}
