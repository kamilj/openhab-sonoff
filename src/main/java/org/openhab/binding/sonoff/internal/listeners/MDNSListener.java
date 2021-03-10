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

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MDNSListener} passes MDNS received messages to the account
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class MDNSListener implements ServiceListener {

    private final Logger logger = LoggerFactory.getLogger(MDNSListener.class);
    private final RawMessageListener listener;

    public MDNSListener(RawMessageListener listener) {
        this.listener = listener;
    }

    @Override
    public void serviceAdded(@Nullable ServiceEvent event) {
        if (event != null) {
            logger.trace("Sonoff - LAN Service added:{}", event.getInfo());
            listener.lanEvent(event.getInfo());
        }
    }

    @Override
    public void serviceRemoved(@Nullable ServiceEvent event) {
        if (event != null) {
            logger.debug("Sonoff - LAN Service removed:{}", event.getInfo());
        }
    }

    @Override
    public void serviceResolved(@Nullable ServiceEvent event) {
        if (event != null) {
            logger.trace("Sonoff - LAN Service resolved:{}", event.getInfo());
            listener.lanEvent(event.getInfo());
        }
    }
}
