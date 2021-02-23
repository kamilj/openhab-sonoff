package org.openhab.binding.sonoff.internal.connections;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.io.net.http.WebSocketFactory;
import org.openhab.binding.sonoff.internal.dto.api.WsServerResponse;
import org.openhab.binding.sonoff.internal.helpers.MessageConverter;
import org.openhab.binding.sonoff.internal.listeners.ConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebSocket
// (maxIdleTime = Integer.MAX_VALUE)
public class Websocket {

    private final Logger logger = LoggerFactory.getLogger(Websocket.class);
    private final WebSocketClient webSocketClient;
    private final Api api;
    private final ConnectionListener connectionListener;
    private final MessageConverter converter;
    private Session session;

    // private WebsocketQueue queue;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    // private ScheduledFuture<?> queueTask;
    private ScheduledFuture<?> pingTask;
    // private final ConcurrentMap<Long, String> messages = new ConcurrentHashMap<Long, String>();

    public Websocket(ConnectionListener connectionListener, MessageConverter converter,
            WebSocketFactory webSocketFactory, Api api) {
        this.webSocketClient = webSocketFactory.getCommonWebSocketClient();
        this.converter = converter;
        this.api = api;
        this.connectionListener = connectionListener;
    }

    public void start() {
        try {
            WsServerResponse response = api.getWsServer();
            if (response.getError() > 0) {
                connectionListener.websocketConnected(false);
            } else {
                String url = "wss://" + response.getDomain() + ":" + response.getPort() + "/api/ws";
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                URI uri = new URI(url);
                webSocketClient.connect(this, uri, request);
                // queue.startRunning();
                // queueTask = executor.scheduleWithFixedDelay(queue, 0, 1000, TimeUnit.MILLISECONDS);

                Runnable ping = () -> {
                    sendPing();
                };
                pingTask = executor.scheduleWithFixedDelay(ping, 60, 60, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            connectionListener.websocketConnected(false);
            logger.debug("Error while starting websocket connection", e);
        }
    }

    public void sendPing() {
        sendMessage("ping");
    }

    public void stop() {
        logger.debug("Stopping websocket client");
        connectionListener.websocketConnected(false);
        if (pingTask != null) {
            pingTask.cancel(true);
            pingTask = null;
        }
        if (session != null) {
            session.close();
            session = null;
        }
    }

    public void sendMessage(String message) {
        logger.debug("Websocket Sending Message:{}", message);
        this.session.getRemote().sendStringByFuture(message);
    }

    @OnWebSocketConnect
    public void onConnect(Session wssession) {
        session = wssession;
        logger.debug("WebSocket Socket successfully connected to {}", session.getRemoteAddress().getAddress());
        connectionListener.websocketConnected(true);
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        if (message.contains("pong")) {
            logger.debug("Pong Response received");
        } else {
            converter.convertWebsocketMessage(message);
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        logger.warn("Websocket Closed, Reason:{}", reason);
        stop();
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        logger.error("Websocket Closed, Error:{}", cause.getMessage());
        onClose(0, cause.getMessage());
    }
}
