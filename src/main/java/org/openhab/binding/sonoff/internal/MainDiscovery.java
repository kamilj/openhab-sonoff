package org.openhab.binding.sonoff.internal;

import static org.openhab.binding.sonoff.internal.Constants.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.dto.api.SubDevices;
import org.openhab.binding.sonoff.internal.handler.*;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link lightwaverfBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author David Murton - Initial contribution
 */

@Component(service = MainDiscovery.class, immediate = true, configurationPid = "discovery.sonoff")

public class MainDiscovery extends AbstractDiscoveryService implements ThingHandlerService, DiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(MainDiscovery.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 10;
    private AccountHandler account;
    private ScheduledFuture<?> scanTask;
    private ThingTypeUID thingTypeUid;
    private final Gson gson = new Gson();

    public MainDiscovery() {
        super(Constants.DISCOVERABLE_THING_TYPE_UIDS, DISCOVER_TIMEOUT_SECONDS, true);
    }

    @Override
    protected void activate(Map<String, Object> configProperties) {
        logger.debug("Activate Background Discovery");
        super.activate(configProperties);
    }

    @Override
    public void deactivate() {
        logger.debug("Deactivate Background discovery");
        super.deactivate();
    }

    @Override
    @Modified
    protected void modified(Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    public void startBackgroundDiscovery() {
        logger.debug("Start Background Discovery");
        try {
            discover();
        } catch (Exception e) {
        }
    }

    @Override
    protected void startScan() {
        // logger.debug("Start Scan");
        if (this.scanTask != null) {
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
        // logger.debug("Stop Scan");
        super.stopScan();

        if (this.scanTask != null) {
            this.scanTask.cancel(true);
            this.scanTask = null;
        }
    }

    private void discover() {
        logger.debug("Sonoff - Start Discovery");
        try {
            List<File> files = Utils.getFiles();
            logger.debug("Sonoff - Discovery files size:{}", files.size());
            if (files.isEmpty() || files.size() == 0 || files == null) {
                logger.debug(
                        "You Havnt initialized the file cache yet, please set Initialize to true in the account configuration");
            } else {
                ThingUID bridgeUID = account.getThing().getUID();
                Device device = new Device();
                List<SubDevices> subdevices = new ArrayList<SubDevices>();
                String deviceid = "";
                String label = "";
                Integer uuid = 0;
                int i = 0;
                int j = 0;
                int k = 0;
                Map<String, Object> properties = new HashMap<>();
                // Create Device List
                List<Device> devices = new ArrayList<Device>();
                for (int l = 0; l < files.size(); l++) {
                    String deviceJson = Utils.getFileContent(files.get(l));
                    device = gson.fromJson(deviceJson, Device.class);
                    devices.add(device);
                }
                // Get Main Devices and Bridges
                for (int o = 0; o < devices.size(); o++) {
                    device = devices.get(o);
                    deviceid = device.getDeviceid();
                    uuid = device.getExtra().getUiid();
                    thingTypeUid = Constants.createMap().get(uuid);
                    if (thingTypeUid != null) {
                        ThingUID deviceThing = new ThingUID(thingTypeUid, account.getThing().getUID(), deviceid);
                        properties.put("deviceid", deviceid);
                        properties.put("Name", device.getName());
                        properties.put("Type", device.getProductModel());
                        properties.put("API Key", device.getApikey());
                        properties.put("deviceKey", device.getDevicekey());
                        properties.put("IP Address", device.getIp());
                        properties.put("Brand", device.getBrandName());
                        properties.put("Firmware Version", device.getParams().getFwVersion());
                        label = createLabelDevice(device);
                        thingDiscovered(
                                DiscoveryResultBuilder.create(deviceThing).withLabel(label).withProperties(properties)
                                        .withRepresentationProperty(deviceid.toString()).withBridge(bridgeUID).build());
                    }
                }
                // Get RF & Zigbee Child Devices
                for (i = 0; i < account.getThing().getThings().size(); i++) {
                    String bUuid = account.getThing().getThings().get(i).getThingTypeUID().getId();
                    if (bUuid.equals("28")) {
                        RFBridgeHandler rfbridge = (RFBridgeHandler) account.getThing().getThings().get(i).getHandler();
                        device = rfbridge.getDevice();
                        for (j = 0; j < device.getTags().getZyxInfo().size(); j++) {
                            Integer type = Integer.parseInt(device.getTags().getZyxInfo().get(j).getRemoteType());
                            thingTypeUid = ((Constants.createSensorMap().get(type)) != null)
                                    ? Constants.createSensorMap().get(type)
                                    : THING_TYPE_UNKNOWNDEVICE;
                            ThingUID rfThing = new ThingUID(thingTypeUid, rfbridge.getThing().getUID(), j + "");
                            properties.clear();
                            properties.put("deviceid", j + "");
                            properties.put("Name", device.getTags().getZyxInfo().get(j).getName());
                            label = device.getTags().getZyxInfo().get(j).getName();
                            thingDiscovered(DiscoveryResultBuilder.create(rfThing).withLabel(label)
                                    .withProperties(properties).withRepresentationProperty(j + "")
                                    .withBridge(rfbridge.getThing().getUID()).build());
                        }
                    } else if (bUuid.equals("66")) {
                        ZigbeeBridgeHandler zBridge = (ZigbeeBridgeHandler) account.getThing().getThings().get(i)
                                .getHandler();
                        subdevices = zBridge.getDevice().getParams().getSubDevices();
                        for (j = 0; j < subdevices.size(); j++) {
                            deviceid = subdevices.get(j).getDeviceid();
                            for (k = 0; k < files.size(); k++) {
                                if (devices.get(k).getDeviceid().equals(deviceid)) {
                                    device = devices.get(k);
                                    String zdeviceid = device.getDeviceid();
                                    uuid = device.getExtra().getUiid();
                                    thingTypeUid = ((Constants.createZigbeeMap().get(uuid)) != null)
                                            ? Constants.createZigbeeMap().get(uuid)
                                            : THING_TYPE_UNKNOWNDEVICE;
                                    ThingUID zigbeeThing = new ThingUID(thingTypeUid, zBridge.getThing().getUID(),
                                            zdeviceid);
                                    properties.clear();
                                    properties.put("deviceid", deviceid);
                                    properties.put("Name", device.getName());
                                    properties.put("UUID", uuid);
                                    properties.put("Type", device.getProductModel());
                                    properties.put("API Key", device.getApikey());
                                    properties.put("deviceKey", device.getDevicekey());
                                    properties.put("IP Address", device.getIp());
                                    properties.put("Brand", device.getBrandName());
                                    properties.put("Firmware Version", device.getParams().getFwVersion());
                                    label = device.getName();
                                    thingDiscovered(DiscoveryResultBuilder.create(zigbeeThing).withLabel(label)
                                            .withProperties(properties).withRepresentationProperty(zdeviceid)
                                            .withBridge(zBridge.getThing().getUID()).build());
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.debug("Discovery threw an error:{}", e);
        }
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof AccountHandler) {
            account = (AccountHandler) handler;
        }
    }

    public String createLabelDevice(Device device) {
        StringBuilder sb = new StringBuilder();
        sb.append(device.getName());
        return sb.toString();
    }

    @Override
    public ThingHandler getThingHandler() {
        return account;
    }
}
