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
package org.openhab.binding.sonoff.internal.connections;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceListener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.sonoff.internal.helpers.CommandMessage;
import org.openhab.binding.sonoff.internal.listeners.ConnectionListener;
import org.openhab.binding.sonoff.internal.listeners.MDNSListener;
import org.openhab.binding.sonoff.internal.listeners.RawMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Lan} class is the Http/mDNS Connection to local ewelink enabled
 * devices and uses the shared httpClient
 *
 * @author David Murton - Initial contribution
 */

@NonNullByDefault
public class Lan {
    private final Logger logger = LoggerFactory.getLogger(Lan.class);
    private final HttpClient httpClient;
    private final ConnectionListener connectionListener;
    private final RawMessageListener listener;

    private @Nullable ServiceListener serviceListener;

    private @Nullable JmDNS mdns;

    public Lan(ConnectionListener connectionListener, RawMessageListener listener,
            HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.connectionListener = connectionListener;
        this.listener = listener;
    }

    public void start(String ipaddress) throws UnknownHostException, IOException {
        mdns = JmDNS.create(InetAddress.getByName(ipaddress), "sonoff");
        final JmDNS mdns = this.mdns;
        if (mdns != null) {
            this.serviceListener = new MDNSListener(listener);
            mdns.addServiceListener("_ewelink._tcp.local.", serviceListener);
            connectionListener.lanConnected(true);
        } else {
            logger.error("Unable to add service listener to LAN connection");
            connectionListener.lanConnected(false);
        }
    }

    public void stop() throws IOException {
        logger.debug("Sonoff - Stopping LAN connection");
        final JmDNS mdns = this.mdns;
        if (mdns != null) {
            mdns.unregisterAllServices();

            // mdns.removeServiceListener("_ewelink._tcp.local.", serviceListener);
            logger.debug("Unregistered all LAN services");
            mdns.close();
            this.mdns = null;
            this.serviceListener = null;
        }
    }

    public void setStatusLan(CommandMessage message) {
        logger.debug("Send LAN Update to {} with unencrypted payload:{}", message.getUrl(), message.getParams());
        try {
            httpClient.newRequest(message.getUrl()).method("POST").header("accept", "application/json")
                    .header("Content-Type", "application/json; utf-8")
                    .content(new StringContentProvider(message.getPayload()), "application/json")
                    .timeout(10, TimeUnit.SECONDS).send(new Response.Listener.Adapter() {
                        @Override
                        public void onContent(@Nullable Response response, @Nullable ByteBuffer buffer) {
                            if (buffer != null) {
                                String content = StandardCharsets.UTF_8.decode(buffer).toString();
                                logger.debug("Lan response received: {}", content);
                                listener.lanResponse(content);
                            }
                        }
                    });
        } catch (Exception e) {
            connectionListener.lanConnected(false);
            logger.warn("Sonoff - Failed to send update:{}", e.getMessage());
        }
    }
}
