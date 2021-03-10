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
package org.openhab.binding.sonoff.internal;

import static org.openhab.binding.sonoff.internal.Constants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.dto.api.SubDevices;
import org.openhab.binding.sonoff.internal.dto.api.ThingList;
import org.openhab.binding.sonoff.internal.dto.api.ZyxInfo;
import org.openhab.binding.sonoff.internal.handler.*;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MainDiscovery} Allows Discovery of Ewelink devices
 *
 * @author David Murton - Initial contribution
 */

@Component(service = MainDiscovery.class, immediate = true, configurationPid = "discovery.sonoff")

@NonNullByDefault
public class MainDiscovery extends AbstractDiscoveryService implements ThingHandlerService, DiscoveryService {
    // , DiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(MainDiscovery.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 10;
    private @Nullable HandlerAccount account;
    private @Nullable ScheduledFuture<?> scanTask;
    // private ThingTypeUID thingTypeUid;

    public MainDiscovery() {
        super(Constants.DISCOVERABLE_THING_TYPE_UIDS, DISCOVER_TIMEOUT_SECONDS, false);
    }

    @Override
    protected void activate(@Nullable Map<String, Object> configProperties) {
    }

    @Override
    public void deactivate() {
    }

    @Override
    protected void startScan() {
        logger.debug("Start Scan");
        final ScheduledFuture<?> scanTask = this.scanTask;
        if (scanTask != null) {
            scanTask.cancel(true);
        }
        this.scanTask = scheduler.schedule(() -> {
            try {
                discover();
            } catch (Exception e) {
            }
        }, 0, TimeUnit.SECONDS);
    }

    @Override
    protected void stopScan() {
        logger.debug("Stop Scan");
        super.stopScan();
        final ScheduledFuture<?> scanTask = this.scanTask;
        if (scanTask != null) {
            scanTask.cancel(true);
            this.scanTask = null;
        }
    }

    @SuppressWarnings({ "unchecked" })
    private void discover() {
        logger.debug("Sonoff - Start Discovery");
        // Get the master Bridge
        final HandlerAccount account = this.account;
        if (account != null) {
            // Create the variables we need
            List<ThingList> devices = new ArrayList<ThingList>();
            ThingUID bridgeUID = account.getThing().getUID();
            int i = 0;
            // Integer filesCreated = 0;
            try {
                devices = account.createCache();
            } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
                logger.trace("Unable to create device cache: {}", e.getMessage());
            }

            // Create Top Level Devices
            for (i = 0; i < devices.size(); i++) {
                Device device = devices.get(i).getItemData();
                String deviceid = device.getDeviceid();
                ThingTypeUID thingTypeUid = Constants.createMap().get(device.getExtra().getUiid());
                if (thingTypeUid != null) {
                    ThingUID deviceThing = new ThingUID(thingTypeUid, account.getThing().getUID(), deviceid);
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("Name", device.getName());
                    properties.put("Brand", device.getBrandName());
                    properties.put("Model", device.getProductModel());
                    properties.put("FW Version", device.getParams().getFwVersion());
                    properties.put("Device ID", deviceid);
                    properties.put("Device Key", device.getDevicekey());
                    properties.put("UIID", device.getExtra().getUiid());
                    properties.put("API Key", device.getApikey());
                    properties.put("Connected To SSID", device.getParams().getSsid());
                    properties.put("deviceid", deviceid);
                    String label = createLabelDevice(device);
                    thingDiscovered(
                            DiscoveryResultBuilder.create(deviceThing).withLabel(label).withProperties(properties)
                                    .withRepresentationProperty(deviceid).withBridge(bridgeUID).build());
                }
            }

            // Create Child Devices
            int j = 0;
            List<Thing> things = account.getThing().getThings();
            for (i = 0; i < things.size(); i++) {
                String uiid = things.get(i).getThingTypeUID().getId();
                switch (uiid) {
                    // RF Devices
                    case "28":
                        Handler28 rfBridge = (Handler28) account.getThing().getThings().get(i).getHandler();
                        if (rfBridge != null) {
                            List<ZyxInfo> subDevices = (List<ZyxInfo>) rfBridge.getSubDevices();
                            if (subDevices != null) {
                                for (j = 0; j < subDevices.size(); j++) {
                                    Integer type = Integer.parseInt(subDevices.get(j).getRemoteType());
                                    ThingTypeUID thingTypeUid = ((Constants.createSensorMap().get(type)) != null)
                                            ? Constants.createSensorMap().get(type)
                                            : THING_TYPE_UNKNOWNDEVICE;
                                    if (thingTypeUid != null) {
                                        ThingUID rfThing = new ThingUID(thingTypeUid, rfBridge.getThing().getUID(),
                                                j + "");
                                        Map<String, Object> properties = new HashMap<>();
                                        properties.clear();
                                        properties.put("deviceid", j + "");
                                        properties.put("Name", subDevices.get(j).getName());
                                        String rfLabel = subDevices.get(j).getName();
                                        thingDiscovered(DiscoveryResultBuilder.create(rfThing).withLabel(rfLabel)
                                                .withProperties(properties).withRepresentationProperty(j + "")
                                                .withBridge(rfBridge.getThing().getUID()).build());
                                    }
                                }
                            }
                        }
                        break;
                    // Zigbee Devices
                    case "66":
                        Handler66 zigbeeBridge = (Handler66) account.getThing().getThings().get(i).getHandler();
                        if (zigbeeBridge != null) {
                            List<SubDevices> subDevices = (List<SubDevices>) zigbeeBridge.getSubDevices();
                            if (subDevices != null) {
                                for (j = 0; j < subDevices.size(); j++) {
                                    String subDeviceid = subDevices.get(j).getDeviceid();
                                    Integer subDeviceuiid = Integer.parseInt(subDevices.get(j).getUiid());
                                    // Lookup our device in the main list
                                    for (int k = 0; k < devices.size(); k++) {
                                        if (devices.get(k).getItemData().getDeviceid().equals(subDeviceid)) {
                                            Device subDevice = devices.get(k).getItemData();
                                            ThingTypeUID thingTypeUid = ((Constants.createZigbeeMap()
                                                    .get(subDeviceuiid)) != null)
                                                            ? Constants.createZigbeeMap().get(subDeviceuiid)
                                                            : THING_TYPE_UNKNOWNDEVICE;
                                            if (thingTypeUid != null) {
                                                ThingUID zigbeeThing = new ThingUID(thingTypeUid,
                                                        zigbeeBridge.getThing().getUID(), subDeviceid);
                                                Map<String, Object> properties = new HashMap<>();
                                                properties.clear();
                                                properties.put("deviceid", subDeviceid);
                                                properties.put("Name", subDevice.getName());
                                                properties.put("UUID", subDeviceuiid);
                                                properties.put("Type", subDevice.getProductModel());
                                                properties.put("API Key", subDevice.getApikey());
                                                properties.put("deviceKey", subDevice.getDevicekey());
                                                properties.put("IP Address", subDevice.getIp());
                                                properties.put("Brand", subDevice.getBrandName());
                                                properties.put("Firmware Version",
                                                        subDevice.getParams().getFwVersion());
                                                String label = subDevice.getName();
                                                thingDiscovered(DiscoveryResultBuilder.create(zigbeeThing)
                                                        .withLabel(label).withProperties(properties)
                                                        .withRepresentationProperty(subDeviceid)
                                                        .withBridge(zigbeeBridge.getThing().getUID()).build());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                }
            }
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof HandlerAccount) {
            account = (HandlerAccount) handler;
        }
    }

    public String createLabelDevice(Device device) {
        StringBuilder sb = new StringBuilder();
        sb.append(device.getName());
        return sb.toString();
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return account;
    }
}
