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
package org.openhab.binding.sonoff.internal;

import static org.openhab.binding.sonoff.internal.Constants.*;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.eclipse.smarthome.io.net.http.WebSocketFactory;
import org.openhab.binding.sonoff.internal.handler.AccountHandler;
import org.openhab.binding.sonoff.internal.handler.RFBridgeHandler;
import org.openhab.binding.sonoff.internal.handler.RFRemoteHandler;
import org.openhab.binding.sonoff.internal.handler.SwitchHandler;
import org.openhab.binding.sonoff.internal.handler.ZigbeeBridgeHandler;
import org.openhab.binding.sonoff.internal.handler.ZigbeeDeviceHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;

/**
 * The {@link sonoffHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Murton - Initial contribution
 */
// @NonNullByDefault
@Component(configurationPid = "binding.sonoff", service = ThingHandlerFactory.class)

public class SonoffHandlerFactory extends BaseThingHandlerFactory {
    private final WebSocketFactory webSocketFactory;
    private final HttpClientFactory httpClientFactory;
    private final Gson gson = new Gson();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID);
    }

    @Activate
    public SonoffHandlerFactory(final @Reference WebSocketFactory webSocketFactory,
            final @Reference HttpClientFactory httpClientFactory) {
        this.webSocketFactory = webSocketFactory;
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_ACCOUNT)) {
            // return new AccountHandler((Bridge) thing, gson);
            return new AccountHandler((Bridge) thing, webSocketFactory, httpClientFactory, gson);
        } else if (thingTypeUID.equals(THING_TYPE_1) || thingTypeUID.equals(THING_TYPE_2)
                || thingTypeUID.equals(THING_TYPE_3) || thingTypeUID.equals(THING_TYPE_4)
                || thingTypeUID.equals(THING_TYPE_5) || thingTypeUID.equals(THING_TYPE_6)
                || thingTypeUID.equals(THING_TYPE_7) || thingTypeUID.equals(THING_TYPE_8)
                || thingTypeUID.equals(THING_TYPE_9) ||

                thingTypeUID.equals(THING_TYPE_15) ||

                thingTypeUID.equals(THING_TYPE_24) || thingTypeUID.equals(THING_TYPE_27)
                || thingTypeUID.equals(THING_TYPE_29) || thingTypeUID.equals(THING_TYPE_30)
                || thingTypeUID.equals(THING_TYPE_31) ||

                thingTypeUID.equals(THING_TYPE_32) ||

                thingTypeUID.equals(THING_TYPE_77) || thingTypeUID.equals(THING_TYPE_78) ||

                thingTypeUID.equals(THING_TYPE_81) || thingTypeUID.equals(THING_TYPE_82)
                || thingTypeUID.equals(THING_TYPE_83) || thingTypeUID.equals(THING_TYPE_84) ||

                thingTypeUID.equals(THING_TYPE_107)) {
            return new SwitchHandler(thing, gson);
        } else if (thingTypeUID.equals(THING_TYPE_28)) {
            return new RFBridgeHandler((Bridge) thing, gson);
        } else if (thingTypeUID.equals(THING_TYPE_RF1) || thingTypeUID.equals(THING_TYPE_RF2)
                || thingTypeUID.equals(THING_TYPE_RF3) || thingTypeUID.equals(THING_TYPE_RF4)
                || thingTypeUID.equals(THING_TYPE_RF6)) {
            return new RFRemoteHandler(thing, gson);
        } else if (thingTypeUID.equals(THING_TYPE_66)) {
            return new ZigbeeBridgeHandler((Bridge) thing, gson);
        } else if (thingTypeUID.equals(THING_TYPE_ZMOTION) || thingTypeUID.equals(THING_TYPE_ZWATER)
                || thingTypeUID.equals(THING_TYPE_ZCONTACT) || thingTypeUID.equals(THING_TYPE_ZTEMP)
                || thingTypeUID.equals(THING_TYPE_ZLIGHT) || thingTypeUID.equals(THING_TYPE_ZSWITCH1)
                || thingTypeUID.equals(THING_TYPE_ZSWITCH2) || thingTypeUID.equals(THING_TYPE_ZSWITCH3)
                || thingTypeUID.equals(THING_TYPE_ZSWITCH4)

        ) {
            return new ZigbeeDeviceHandler(thing, gson);
        }

        else {
            return null;
        }
    }
}
