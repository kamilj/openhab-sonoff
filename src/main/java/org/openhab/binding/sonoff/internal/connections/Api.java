/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.openhab.binding.sonoff.internal.dto.api.ApiRegionResponse;
import org.openhab.binding.sonoff.internal.dto.api.ThingResponse;
import org.openhab.binding.sonoff.internal.dto.api.V2ApiLoginResponse;
import org.openhab.binding.sonoff.internal.dto.api.WsServerResponse;
import org.openhab.binding.sonoff.internal.dto.payloads.ApiLoginRequest;
import org.openhab.binding.sonoff.internal.dto.payloads.ApiRegionCode;
import org.openhab.binding.sonoff.internal.dto.payloads.GeneralRequest;
import org.openhab.binding.sonoff.internal.dto.payloads.ThingList;
import org.openhab.binding.sonoff.internal.dto.payloads.Things;
import org.openhab.binding.sonoff.internal.helpers.DtoHelper;
import org.openhab.binding.sonoff.internal.helpers.Utils;
import org.openhab.binding.sonoff.internal.listeners.ConnectionListener;
import org.openhab.binding.sonoff.internal.listeners.RawMessageListener;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link Api} class is the Http Api Connection to the Ewelink Servers and uses the shared httpClient
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class Api {

    private final Logger logger = LoggerFactory.getLogger(Api.class);
    private final Gson gson;
    private final HttpClient httpClient;
    private final ConnectionListener connectionListener;
    private final RawMessageListener listener;

    private String apiKey = "";
    private String at = "";
    private String countryCode = "";
    private String region = "";
    private String baseUrl = "";
    private String dispUrl = "";
    private String email = "";
    private String password = "";
    private String websocketServer = "";
    private Boolean connected = false;

    public Api(Gson gson, ConnectionListener connectionListener, RawMessageListener listener,
            HttpClientFactory httpClientFactory) {
        this.gson = gson;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.connectionListener = connectionListener;
        this.listener = listener;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    private void setApiKey(String apiKey) {
        logger.debug("Api ApiKey changed to:{}", apiKey);
        this.apiKey = apiKey;
    }

    public String getAt() {
        return this.at;
    }

    private void setAt(String at) {
        logger.debug("Api Access Token changed to:{}", at);
        this.at = at;
    }

    private String getRegion() {
        return this.region;
    }

    private void setRegion(String region) {
        logger.debug("Api Region changed to:{}", region);
        this.region = region;
    }

    private void updateRegion() {
        ApiRegionCode request = new ApiRegionCode();
        ApiRegionResponse response = new ApiRegionResponse();
        request.setCountryCode(getCountryCode());
        logger.debug("Api Region Request:{}", gson.toJson(request));
        String url = "https://api.coolkit.cc:8080/api/user/region?lang=en&appid=" + request.getAppid() + "&ts="
                + request.getTs() + "&country_code=" + getCountryCode() + "&nonce=" + request.getNonce() + "&version="
                + request.getVersion() + "&getTags=1";
        try {
            ContentResponse contentResponse = httpClient.newRequest(url).header("accept", "application/json")
                    .header("Content-Type", "application/json; utf-8")
                    .header("Authorization", "Sign " + Utils.getAuthMac(gson.toJson(request))).method("GET").send();
            logger.debug("Api Region Response:{}", contentResponse.getContentAsString());
            response = gson.fromJson(contentResponse.getContentAsString(), ApiRegionResponse.class);
            if (response != null) {
                if (!response.getError().equals("0") || response.getError() == null) {
                    setConnected(false);
                } else {
                    setRegion(response.getRegion());
                    setDispUrl("https://" + getRegion() + "-dispa.coolkit.cc/dispatch/app");
                    setBaseUrl("https://" + getRegion() + "-apia.coolkit.cc");
                }
            } else {
                logger.error("Api Region Response returned empty");
            }
        } catch (Exception e) {
            setConnected(false);
            logger.trace("Api Exception:{}", e.getMessage());
        }
    }

    private String getCountryCode() {
        return this.countryCode;
    }

    public void setCountryCode(String countryCode) {
        logger.debug("Api CountryCode changed to:{}", countryCode);
        this.countryCode = countryCode;
    }

    private String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String getBaseUrl() {
        return this.baseUrl;
    }

    private void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private String getDispUrl() {
        return this.dispUrl;
    }

    private void setDispUrl(String dispUrl) {
        this.dispUrl = dispUrl;
    }

    public String getWebsocketServer() {
        return this.websocketServer;
    }

    private void setWebsocketServer(String websocketServer) {
        this.websocketServer = websocketServer;
    }

    private void updateWebsocketServer() {
        logger.debug("Attempt to get websocket server");
        GeneralRequest request = new GeneralRequest();
        request.setAccept("ws");
        // WsServerResponse response = new WsServerResponse();
        logger.debug("Websocket URL Request:{}", gson.toJson(request));
        try {
            ContentResponse contentResponse = httpClient.newRequest(getDispUrl()).header("accept", "application/json")
                    .header("Content-Type", "application/json; utf-8").header("Authorization", "Bearer " + getAt())
                    .method("POST").content(new StringContentProvider(gson.toJson(request)), "application/json").send();
            logger.debug("Websocket URL Response:{}", contentResponse.getContentAsString());
            WsServerResponse response = gson.fromJson(contentResponse.getContentAsString(), WsServerResponse.class);
            if (response != null) {
                if (!response.getError().equals(0) || response.getError() == null) {
                    setConnected(false);
                } else {
                    setWebsocketServer("wss://" + response.getDomain() + ":" + response.getPort() + "/api/ws");
                    setConnected(true);
                }
            } else {
                logger.error("Api Websocket Server Response returned empty");
            }
        } catch (Exception e) {
            setConnected(false);
            logger.trace("Api Exception:{}", e.getMessage());
        }
    }

    private void setConnected(Boolean connected) {
        this.connected = connected;
        if (!connected) {
            connectionListener.apiConnected(false);
        }
    }

    private Boolean getConnected() {
        return this.connected;
    }

    private void login() {
        String url = getBaseUrl() + "/v2/user/login";
        ApiLoginRequest request = new ApiLoginRequest();
        request.setEmail(getEmail());
        request.setPassword(getPassword());
        request.setCountryCode(getCountryCode());
        logger.debug("Api Login Request:{}", gson.toJson(request));
        try {
            ContentResponse contentResponse = httpClient.newRequest(url).header("accept", "application/json")
                    .header("Content-Type", "application/json").header("X-CK-Appid", DtoHelper.APPID)
                    .header("X-CK-Nonce", DtoHelper.getNonce())
                    .header("Authorization", "Sign " + Utils.getAuthMac(gson.toJson(request))).method("POST")
                    .content(new StringContentProvider(gson.toJson(request)), "application/json").send();
            V2ApiLoginResponse response = gson.fromJson(contentResponse.getContentAsString(), V2ApiLoginResponse.class);
            if (response != null) {
                logger.debug("Api Login Respone:{}", contentResponse.getContentAsString());
                if (response.getError() > 0) {
                    connectionListener.apiConnected(false);
                } else {
                    setAt(response.getData().getAt());
                    setApiKey(response.getData().getUser().getApikey());
                    setConnected(true);
                }
            } else {
                logger.error("Api Login Response returned empty");
            }
        } catch (Exception e) {
            logger.trace("Api Exception:{}", e.getMessage());
        }
    }

    public void startApi() {
        updateRegion();
        login();
        updateWebsocketServer();
        connectionListener.apiConnected(getConnected());
    }

    public void updateCredentials() {
        updateRegion();
        login();
        connectionListener.apiConnected(getConnected());
    }

    private void processError(Integer code, String message) {
        String logError = "";
        switch (code) {
            case 401:
                logError = "You cannot use more than 1 instance of Ewelink at one time, please check the app or another binding is not logged into your account";
                break;
            case 0:
                logError = "This was a serious exception and should be reported to the binding developer";
                break;
            default:
                logError = "";
        }
        logger.error("Api threw an error code {} with message {}. Additional User information: {}", code, message,
                logError);
        setConnected(false);
    }

    public ThingResponse createCache() throws IOException, InterruptedException, TimeoutException, ExecutionException {
        GeneralRequest request = new GeneralRequest();
        logger.debug("Api Cache Request:{}", gson.toJson(request));
        String url = getBaseUrl() + "/v2/device/thing";
        ContentResponse contentResponse = httpClient.newRequest(url).header("Authorization", "Bearer " + getAt())
                .header("Content-Type", "application/json").header("X-CK-Appid", DtoHelper.APPID)
                .header("X-CK-Nonce", DtoHelper.getNonce()).method("GET").send();
        logger.debug("Api Cache response:{}", contentResponse.getContentAsString());
        ThingResponse response = gson.fromJson(contentResponse.getContentAsString(), ThingResponse.class);
        if (response != null) {
            if (!response.getError().equals(0)) {
                processError(response.getError(), response.getMsg());
                return new ThingResponse();
            } else {
                for (int i = 0; i < response.getData().getThingList().size(); i++) {
                    String deviceid = response.getData().getThingList().get(i).getItemData().getDeviceid();
                    String device = getDeviceCache(deviceid);
                    Utils.createCacheFile(deviceid, device);
                }
                return response;
            }
        } else {
            return new ThingResponse();
        }
    }

    public String getDeviceCache(String deviceid) throws InterruptedException, TimeoutException, ExecutionException {
        ThingList request = new ThingList();
        Things thing = new Things();
        thing.setItemType(1);
        thing.setId(deviceid);
        request.getThings().add(thing);
        String url = getBaseUrl() + "/v2/device/thing";
        logger.debug("Api Get Device Request for id:{}", deviceid);
        ContentResponse response = httpClient.newRequest(url).header("Content-Type", "application/json")
                .header("X-CK-Appid", DtoHelper.APPID).header("X-CK-Nonce", DtoHelper.getNonce())
                .header("Authorization", "Bearer " + getAt())
                .content(new StringContentProvider(gson.toJson(request)), "application/json").method("POST").send();
        return response.getContentAsString();
    }

    public void getDevices() {
        GeneralRequest request = new GeneralRequest();
        logger.debug("Api Discovery Request:{}", gson.toJson(request));
        String url = getBaseUrl() + "/v2/device/thing";
        httpClient.newRequest(url).header("Authorization", "Bearer " + getAt())
                .header("Content-Type", "application/json").header("X-CK-Appid", DtoHelper.APPID)
                .header("X-CK-Nonce", DtoHelper.getNonce()).method("GET").send(new Response.Listener.Adapter() {
                    @Override
                    public void onContent(@Nullable Response contentResponse, @Nullable ByteBuffer buffer) {
                        String string = StandardCharsets.UTF_8.decode(buffer).toString();
                        logger.debug("Api Devices response:{}", string);
                        ThingResponse response = gson.fromJson(string, ThingResponse.class);
                        if (response != null) {
                            if (!response.getError().equals(0)) {
                                processError(response.getError(), response.getMsg());
                            } else {
                                listener.apiMessage(response);
                            }
                        }
                    }
                });
    }

    public void getDevice(String deviceid) {
        ThingList request = new ThingList();
        Things thing = new Things();
        thing.setItemType(1);
        thing.setId(deviceid);
        request.getThings().add(thing);
        String url = getBaseUrl() + "/v2/device/thing";
        logger.debug("Api Get Device Request for id:{}", deviceid);
        httpClient.newRequest(url).header("Content-Type", "application/json").header("X-CK-Appid", DtoHelper.APPID)
                .header("X-CK-Nonce", DtoHelper.getNonce()).header("Authorization", "Bearer " + getAt())
                .content(new StringContentProvider(gson.toJson(request)), "application/json").method("POST")
                .send(new Response.Listener.Adapter() {
                    @Override
                    public void onContent(@Nullable Response contentResponse, @Nullable ByteBuffer buffer) {
                        String string = StandardCharsets.UTF_8.decode(buffer).toString();
                        logger.debug("Api Device response:{} for id {}", string, deviceid);
                        ThingResponse response = gson.fromJson(string, ThingResponse.class);
                        if (response != null) {
                            if (!response.getError().equals(0)) {
                                processError(response.getError(), response.getMsg());
                            } else {
                                listener.apiMessage(response);
                            }
                        }
                    }
                });
    }
}
