package org.openhab.binding.sonoff.internal.connections;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.openhab.binding.sonoff.internal.helpers.CommandMessage;
import org.openhab.binding.sonoff.internal.helpers.MessageConverter;
import org.openhab.binding.sonoff.internal.listeners.ConnectionListener;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lan {
    private final Logger logger = LoggerFactory.getLogger(Lan.class);
    private final HttpClient httpClient;
    private final String ipaddress;
    private final ConnectionListener connectionListener;
    private final MessageConverter converter;

    private JmDNS mdns;
    private ServiceListener serviceListener;

    public Lan(ConnectionListener connectionListener, MessageConverter converter, HttpClientFactory httpClientFactory,
            String ipaddress) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.ipaddress = ipaddress;
        this.connectionListener = connectionListener;
        this.converter = converter;
    }

    public void start() {
        try {
            mdns = JmDNS.create(InetAddress.getByName(ipaddress));
            serviceListener = new MDnsListener();
            mdns.addServiceListener("_ewelink._tcp.local.", serviceListener);
            connectionListener.lanConnected(true);
        } catch (Exception e) {
            connectionListener.lanConnected(false);
            logger.debug("Lan Exception:{}", e);
        }
    }

    public void stop() {
        logger.debug("Sonoff - Stopping LAN connection");
        connectionListener.lanConnected(false);
        try {
            if (mdns != null) {
                mdns.removeServiceListener("_ewelink._tcp.local.", serviceListener);
                mdns.close();
            }
        } catch (Exception e) {
            connectionListener.lanConnected(false);
            logger.debug("Lan Exception:{}", e.getMessage());
        }
    }

    public void setStatusLan(CommandMessage message) {
        logger.debug("Send LAN Update to {} with unencrypted payload:{}", message.getUrl(), message.getParams());
        try {
            // ContentResponse response =
            httpClient.newRequest(message.getUrl()).method("POST").header("accept", "application/json")
                    .header("Content-Type", "application/json; utf-8")
                    .content(new StringContentProvider(message.getPayload()), "application/json")
                    .timeout(10, TimeUnit.SECONDS).send(new Response.Listener.Adapter() {
                        @Override
                        public void onContent(Response response, ByteBuffer buffer) {
                            logger.debug("Lan RAW response received: {}", response);
                            String content = StandardCharsets.UTF_8.decode(buffer).toString();
                            logger.debug("Lan response received: {}", content);
                            converter.convertLanResponse(content);
                        }
                    });
        } catch (Exception e) {
            connectionListener.lanConnected(false);
            logger.warn("Sonoff - Failed to send update:{}", e);
        }
    }

    public class MDnsListener implements ServiceListener {

        @Override
        public void serviceAdded(ServiceEvent event) {
            logger.trace("Sonoff - LAN Service added:{}", event.getInfo());
            converter.convertLanEvent(event.getInfo());
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            logger.debug("Sonoff - LAN Service removed:{}", event.getInfo());
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            logger.trace("Sonoff - LAN Service resolved:{}", event.getInfo());
            converter.convertLanEvent(event.getInfo());
        }
    }
}
