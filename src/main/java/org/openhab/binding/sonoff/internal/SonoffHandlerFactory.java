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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.eclipse.smarthome.io.net.http.WebSocketFactory;
import org.openhab.binding.sonoff.internal.handler.Handler1;
import org.openhab.binding.sonoff.internal.handler.Handler102;
import org.openhab.binding.sonoff.internal.handler.Handler15;
import org.openhab.binding.sonoff.internal.handler.Handler1770;
import org.openhab.binding.sonoff.internal.handler.Handler2;
import org.openhab.binding.sonoff.internal.handler.Handler2026;
import org.openhab.binding.sonoff.internal.handler.Handler24;
import org.openhab.binding.sonoff.internal.handler.Handler28;
import org.openhab.binding.sonoff.internal.handler.Handler28Children;
import org.openhab.binding.sonoff.internal.handler.Handler3;
import org.openhab.binding.sonoff.internal.handler.Handler32;
import org.openhab.binding.sonoff.internal.handler.Handler4;
import org.openhab.binding.sonoff.internal.handler.Handler5;
import org.openhab.binding.sonoff.internal.handler.Handler59;
import org.openhab.binding.sonoff.internal.handler.Handler66;
import org.openhab.binding.sonoff.internal.handler.Handler77;
import org.openhab.binding.sonoff.internal.handler.HandlerAccount;
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
@NonNullByDefault
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
        String id = thing.getThingTypeUID().getId();
        switch (id) {
            case "account":
                return new HandlerAccount((Bridge) thing, webSocketFactory, httpClientFactory, gson);
            case "1":
            case "6":
            case "27":
            case "81":
            case "107":
                return new Handler1(thing, gson);
            case "2":
            case "7":
            case "29":
            case "82":
                return new Handler2(thing, gson);
            case "3":
            case "8":
            case "30":
            case "83":
                return new Handler3(thing, gson);
            case "4":
            case "9":
            case "31":
            case "84":
                return new Handler4(thing, gson);
            case "5":
                return new Handler5(thing, gson);
            case "15":
                return new Handler15(thing, gson);
            case "24":
                return new Handler24(thing, gson);
            case "28":
                return new Handler28((Bridge) thing, gson);

            case "32":
                return new Handler32(thing, gson);

            case "59":
                return new Handler59(thing, gson);

            case "66":
                return new Handler66((Bridge) thing, gson);

            case "77":
            case "78":
                return new Handler77(thing, gson);

            case "102":
                return new Handler102(thing, gson);

            case "1770":
                return new Handler1770(thing, gson);
            case "2026":
                return new Handler2026(thing, gson);

            case "rfremote1":
            case "rfremote2":
            case "rfremote3":
            case "rfremote4":
            case "rfsensor":
                return new Handler28Children(thing, gson);

            default:
                return null;
        }
    }
}

// thingTypeUID.equals(THING_TYPE_6)
// || thingTypeUID.equals(THING_TYPE_7) || thingTypeUID.equals(THING_TYPE_8)
// || thingTypeUID.equals(THING_TYPE_9) ||

// thingTypeUID.equals(THING_TYPE_15) ||

// thingTypeUID.equals(THING_TYPE_24) || thingTypeUID.equals(THING_TYPE_27)
// || thingTypeUID.equals(THING_TYPE_29) || thingTypeUID.equals(THING_TYPE_30)
// || thingTypeUID.equals(THING_TYPE_31) ||

// thingTypeUID.equals(THING_TYPE_77) || thingTypeUID.equals(THING_TYPE_78) ||

// thingTypeUID.equals(THING_TYPE_81) || thingTypeUID.equals(THING_TYPE_82)
// || thingTypeUID.equals(THING_TYPE_83) || thingTypeUID.equals(THING_TYPE_84) ||

// thingTypeUID.equals(THING_TYPE_107)) {
// return new Handler1(thing, gson);
