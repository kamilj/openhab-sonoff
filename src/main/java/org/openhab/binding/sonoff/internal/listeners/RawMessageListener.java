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
package org.openhab.binding.sonoff.internal.listeners;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sonoff.internal.dto.api.ThingResponse;

/**
 * The {@link RawMessageListener} passes all received messages from connections to be converted
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public interface RawMessageListener {

    void lanEvent(ServiceInfo eventInfo);

    void lanResponse(String message);

    void websocketMessage(String message);

    void apiMessage(ThingResponse thingResponse);
}
