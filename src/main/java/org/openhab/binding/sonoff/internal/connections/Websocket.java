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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.io.net.http.WebSocketFactory;
import org.openhab.binding.sonoff.internal.listeners.ConnectionListener;
import org.openhab.binding.sonoff.internal.listeners.RawMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Websocket} class is the websocket Connection to the Ewelink API to
 * enable streaming data and uses the shared websocketClient
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
@WebSocket
public class Websocket {

    private final Logger logger = LoggerFactory.getLogger(Websocket.class);
    private final WebSocketClient webSocketClient;
    private final Api api;
    private final ConnectionListener connectionListener;
    private final RawMessageListener listener;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private @Nullable Session session;
    private @Nullable ScheduledFuture<?> pingTask;

    public Websocket(ConnectionListener connectionListener, RawMessageListener listener,
            WebSocketFactory webSocketFactory, Api api) {
        this.webSocketClient = webSocketFactory.getCommonWebSocketClient();
        this.listener = listener;
        this.api = api;
        this.connectionListener = connectionListener;
    }

    public void start()
            throws InterruptedException, TimeoutException, ExecutionException, IOException, URISyntaxException {
        String url = api.getWebsocketServer();
        if (url != "") {
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            URI uri = new URI(url);
            webSocketClient.connect(this, uri, request);
            Runnable ping = () -> {
                sendPing();
            };
            pingTask = executor.scheduleWithFixedDelay(ping, 60, 60, TimeUnit.SECONDS);
        } else {
            logger.error("Unable to start websocket as the server address is not set");
        }
    }

    private void sendPing() {
        sendMessage("ping");
    }

    public void stop() {
        logger.debug("Stopping websocket client");
        connectionListener.websocketConnected(false);
        final ScheduledFuture<?> pingTask = this.pingTask;
        if (pingTask != null) {
            pingTask.cancel(true);
            this.pingTask = null;
        }
        final Session session = this.session;
        if (session != null) {
            session.close();
            this.session = null;
        }
    }

    public void sendMessage(String message) {
        logger.debug("Websocket Sending Message:{}", message);
        Session session = this.session;
        if (session != null) {
            session.getRemote().sendStringByFuture(message);
        } else {
            logger.error("WebSocket couldn't send the message {} as the session was null", message);
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        logger.debug("WebSocket Socket successfully connected to {}", session.getRemoteAddress().getAddress());
        connectionListener.websocketConnected(true);
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        if (message.contains("pong")) {
            logger.debug("Pong Response received");
        } else {
            listener.websocketMessage(message);
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        stop();
        logger.error("Websocket Closed, Status Code: {}, Reason:{}", statusCode, reason);
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        String reason = cause.getMessage();
        if (reason != null) {
            onClose(0, reason);
        } else {
            onClose(0, "");
        }
    }
}
