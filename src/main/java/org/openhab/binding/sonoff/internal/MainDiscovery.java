package org.openhab.binding.sonoff.internal;

import static org.openhab.binding.sonoff.internal.Constants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.dto.api.Devices;
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
        ThingUID bridgeUID = account.getThing().getUID();
        try {
            Devices devices = new Devices();
            devices = account.getApi().discover();
            for (int i = 0; i < devices.getDevicelist().size(); i++) {
                Device device = devices.getDevicelist().get(i);
                account.addDevice(device);
                String deviceid = device.getDeviceid();
                Integer uuid = device.getUiid();
                thingTypeUid = (Constants.createMap().get(uuid) != null) ? Constants.createMap().get(uuid)
                        : THING_TYPE_UNKNOWNDEVICE;
                ThingUID deviceThing = new ThingUID(thingTypeUid, account.getThing().getUID(), deviceid);
                Map<String, Object> dProperties = new HashMap<>();
                dProperties.put("deviceId", deviceid);
                dProperties.put("Name", device.getName());
                dProperties.put("Type", device.getProductModel());
                dProperties.put("API Key", device.getApikey());
                dProperties.put("deviceKey", device.getDevicekey());
                dProperties.put("IP Address", device.getIp());
                dProperties.put("Brand", device.getBrandName());
                String label = createLabelDevice(device);
                thingDiscovered(DiscoveryResultBuilder.create(deviceThing).withLabel(label).withProperties(dProperties)
                        .withRepresentationProperty(deviceid.toString()).withBridge(bridgeUID).build());
                for (int t = 0; t < account.getThing().getThings().size(); t++) {
                    String deviceUUID = account.getThing().getThings().get(t).getProperties().get("UUID");
                    logger.debug("Discovery UUID = {}", deviceUUID);
                    if (deviceUUID.equals("28")) {
                        RFBridgeHandler rfBridge = (RFBridgeHandler) account.getThing().getThings().get(t).getHandler();
                        Device rfdevice = rfBridge.getDevice();
                        for (int j = 0; j < rfdevice.getTags().getZyxInfo().size(); j++) {
                            Integer type = Integer.parseInt(rfdevice.getTags().getZyxInfo().get(j).getRemoteType());
                            thingTypeUid = ((Constants.createSensorMap().get(type)) != null)
                                    ? Constants.createSensorMap().get(type)
                                    : THING_TYPE_UNKNOWNDEVICE;
                            ThingUID sensorThing = new ThingUID(thingTypeUid, rfBridge.getThing().getUID(), j + "");
                            Map<String, Object> properties = new HashMap<>();
                            properties.put("id", j + "");
                            properties.put("Name", rfdevice.getTags().getZyxInfo().get(j).getName());
                            String slabel = rfdevice.getTags().getZyxInfo().get(j).getName();
                            thingDiscovered(DiscoveryResultBuilder.create(sensorThing).withLabel(slabel)
                                    .withProperties(properties).withRepresentationProperty(j + "")
                                    .withBridge(rfBridge.getThing().getUID()).build());
                        }
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
