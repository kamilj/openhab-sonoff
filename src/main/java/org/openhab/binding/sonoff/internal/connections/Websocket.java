package org.openhab.binding.sonoff.internal.connections;

import java.net.URI;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.dto.api.WsMessage;
import org.openhab.binding.sonoff.internal.dto.api.WsServerResponse;
import org.openhab.binding.sonoff.internal.dto.payloads.WsLoginRequest;
import org.openhab.binding.sonoff.internal.dto.payloads.WsUpdate;
import org.openhab.binding.sonoff.internal.listeners.ConnectionListener;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebSocket
public class Websocket {

    private final Logger logger = LoggerFactory.getLogger(Websocket.class);
    private final WebSocketClient webSocketClient;
    private final Api api;
    private final Gson gson;
    private Session session;
    private final ConnectionListener listener;
    private long lastSequence;
    private ScheduledFuture<?> pingTask;

    public Websocket(WebSocketFactory webSocketFactory, Gson gson, Api api, ConnectionListener listener) {
        this.webSocketClient = webSocketFactory.createWebSocketClient("SonoffWebSocket");
        this.webSocketClient.setMaxIdleTimeout(86400);
        this.gson = gson;
        this.api = api;
        this.listener = listener;
    }

    public synchronized void start() {
        try {
            webSocketClient.start();
            WsServerResponse response = api.getWsServer();
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            URI uri = new URI("wss://" + response.getDomain() + ":" + response.getPort() + "/api/ws");
            // webSocketClient.setAsyncWriteTimeout(10000);
            // webSocketClient.setMaxTextMessageBufferSize(10000);
            webSocketClient.connect(this, uri, request);
        } catch (Exception e) {
        }
    }

    public void sendPing() {
        sendMessage("ping");
        logger.debug("Ping Sent");
    }

    public void stop() {
        try {
            if (pingTask != null) {
                pingTask.cancel(true);
                pingTask = null;
            }
            webSocketClient.stop();
        } catch (Exception e) {
            logger.debug("Error while closing connection", e);
        }
    }

    public void login() {
        WsLoginRequest request = new WsLoginRequest();
        request.setAt(api.getAt());
        request.setApikey(api.getApiKey());
        logger.debug("Websocket Login Request:{}", gson.toJson(request));
        sendMessage(gson.toJson(request));
    }

    private void sendMessage(String message) {
        logger.debug("Message sent: {}", message);
        this.session.getRemote().sendStringByFuture(message);
    }

    public void sendChange(String data, String deviceid, String deviceKey) {
        JsonObject payload = new JsonParser().parse(data).getAsJsonObject();
        WsUpdate request = new WsUpdate();
        request.setAt(api.getAt());
        request.setApikey(api.getApiKey());
        request.setDeviceid(deviceid);
        request.setSelfApikey(deviceKey);
        request.setParams(payload);
        logger.debug("Websocket Set Status Request:{}", gson.toJson(request));
        queueMessage(request.getSequence(), gson.toJson(request));
    }

    private synchronized void queueMessage(Long sequence, String message) {
        sendMessage(message);
        if (lastSequence - sequence < 100) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session wssession) {
        session = wssession;
        logger.debug("WebSocket Socket successfully connected to {}", session.getRemoteAddress().getAddress());
        login();
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        logger.debug("Websocket Response: {}", message);
        if (!message.contains("pong")) {
            WsMessage response = gson.fromJson(message, WsMessage.class);
            if (response.getError() != null) {
                if (response.getError() > 0) {
                    listener.onError("websocket", response.getError().toString(), response.getReason());
                } else {
                    listener.webSocketConnectionOpen();
                }
            } else {
                Device device = gson.fromJson(message, Device.class);
                if (device != null) {
                    listener.webSocketMessage(device);
                }
            }
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        listener.onError("websocket", statusCode + "", reason);
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        onClose(0, cause.getMessage());
    }
}
