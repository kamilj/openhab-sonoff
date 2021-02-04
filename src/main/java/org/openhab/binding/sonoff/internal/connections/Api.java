package org.openhab.binding.sonoff.internal.connections;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.sonoff.internal.Utils;
import org.openhab.binding.sonoff.internal.config.AccountConfig;
import org.openhab.binding.sonoff.internal.dto.api.ApiLoginResponse;
import org.openhab.binding.sonoff.internal.dto.api.ApiRegionResponse;
import org.openhab.binding.sonoff.internal.dto.api.Devices;
import org.openhab.binding.sonoff.internal.dto.api.WsServerResponse;
import org.openhab.binding.sonoff.internal.dto.payloads.ApiLoginRequest;
import org.openhab.binding.sonoff.internal.dto.payloads.ApiRegionCode;
import org.openhab.binding.sonoff.internal.dto.payloads.ApiStatusChange;
import org.openhab.binding.sonoff.internal.dto.payloads.GeneralRequest;
import org.openhab.binding.sonoff.internal.helpers.DtoHelper;
import org.openhab.binding.sonoff.internal.listeners.ConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Api {

    private final Logger logger = LoggerFactory.getLogger(Api.class);
    private final Gson gson;
    private final HttpClient httpClient;
    private final AccountConfig config;
    private final ConnectionListener listener;
    private String baseUrl = "";
    private String apiKey = "";
    private String at = "";

    public Api(AccountConfig config, ConnectionListener listener, HttpClientFactory httpClientFactory, Gson gson) {
        this.gson = gson;
        this.config = config;
        this.httpClient = httpClientFactory.createHttpClient("sonoffApi");
        this.listener = listener;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getAt() {
        return at;
    }

    public void start() {
        try {
            httpClient.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            httpClient.stop();
            // httpClient.destroy();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void login() {
        String url = baseUrl + "api/user/login";
        ApiLoginRequest request = new ApiLoginRequest();
        ApiLoginResponse response = new ApiLoginResponse();
        request.setEmail(config.email);
        request.setPassword(config.password);
        logger.debug("Api Login Request:{}", gson.toJson(request));
        try {
            ContentResponse contentResponse = httpClient.newRequest(url).header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Sign " + Utils.getAuthMac(gson.toJson(request))).method("POST")
                    .content(new StringContentProvider(gson.toJson(request)), "application/json").send();
            logger.debug("Api Login Response:{}", contentResponse.getContentAsString());
            response = gson.fromJson(contentResponse.getContentAsString(), ApiLoginResponse.class);
            if (response.getError() > 0) {
                listener.onError("api", response.getError() + "", response.getMsg());
            } else {
                at = response.getAt();
                apiKey = response.getUser().getApikey();
                listener.ApiconnectionOpen();
            }
        } catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException | InterruptedException
                | TimeoutException | ExecutionException e) {
            listener.onError("api", e.getCause().toString(), e.getMessage());
        }
    }

    public void getRegion() {
        ApiRegionCode request = new ApiRegionCode();
        ApiRegionResponse response = new ApiRegionResponse();
        request.setCountry_code(config.countryCode);
        logger.debug("Api Region Request:{}", gson.toJson(request));
        String url = "https://api.coolkit.cc:8080/api/user/region?lang=en&appid=" + request.getAppid() + "&ts="
                + request.getTs() + "&country_code=" + request.getCountry_code() + "&nonce=" + request.getNonce()
                + "&version=" + request.getVersion() + "&getTags=1";
        try {
            ContentResponse contentResponse = httpClient.newRequest(url).header("accept", "application/json")
                    .header("Content-Type", "application/json; utf-8")
                    .header("Authorization", "Sign " + Utils.getAuthMac(gson.toJson(request))).method("GET").send();
            logger.debug("Api Region Response:{}", contentResponse.getContentAsString());
            response = gson.fromJson(contentResponse.getContentAsString(), ApiRegionResponse.class);
            if (!response.getError().equals("0") || response.getError() == null) {
                listener.onError("api", response.getRtnCode(), response.getRtnMsg());
            } else {
                baseUrl = "https://" + response.getRegion() + "-api.coolkit.cc:8080/";
            }
        } catch (Exception e) {
            listener.onError("api", e.getCause().toString(), e.getMessage());
        }
    }

    public WsServerResponse getWsServer() {
        String url = baseUrl + "dispatch/app";
        GeneralRequest request = new GeneralRequest();
        request.setAccept("ws");
        WsServerResponse response = new WsServerResponse();
        logger.debug("Websocket URL Request:{}", gson.toJson(request));
        try {
            ContentResponse contentResponse = httpClient.newRequest(url).header("accept", "application/json")
                    .header("Content-Type", "application/json; utf-8").header("Authorization", "Bearer " + at)
                    .method("POST").content(new StringContentProvider(gson.toJson(request)), "application/json").send();
            logger.debug("Websocket URL Response:{}", contentResponse.getContentAsString());
            response = gson.fromJson(contentResponse.getContentAsString(), WsServerResponse.class);
            if (response.getError() > 0) {
                listener.onError("api", response.getError() + "", response.getReason());
                return null;
            } else {
                return response;
            }
        } catch (Exception e) {
            listener.onError("api", e.getCause().toString(), e.getMessage());
            return null;
        }
    }

    public void setStatusApi(String params, String deviceid, String deviceKey) {
        String url = baseUrl + "api/user/device/status";
        ApiStatusChange request = new ApiStatusChange();
        request.setDeviceid(deviceid);
        request.setParams(params);
        logger.debug("Api Set Status Request:{}", gson.toJson(request));
        try {
            ContentResponse response = httpClient.newRequest(url).header("accept", "application/json")
                    .header("Content-Type", "application/json; utf-8").header("Authorization", "Bearer " + at)
                    .method("POST").content(new StringContentProvider(gson.toJson(request)), "application/json").send();
            logger.debug("Api Set Status Response :{}", response.getContentAsString());
        } catch (Exception e) {
            listener.onError("api", e.getCause().toString(), e.getMessage());
        }
    }

    public Devices discover() {
        Devices response = new Devices();
        GeneralRequest request = new GeneralRequest();
        String url = baseUrl + "api/user/device?lang=en&appid=" + request.getAppid() + "&ts=" + request.getTs()
                + "&version=" + request.getVersion() + "&getTags=1";
        logger.debug("Api Discovery Request:{}", url);
        String url2 = baseUrl + "api/user/device?lang=en&appid=" + DtoHelper.appid + "&ts=" + new Date().getTime()
                + "&version=8&getTags=1";
        logger.debug("Api Discovery Request:{}", at);
        try {
            ContentResponse contentResponse = httpClient.newRequest(url2).header("accept", "text/html")
                    .header("connection", "Keep-Alive").header("Authorization", "Bearer " + at).method("GET").send();
            logger.debug("Api Discovery response:{}", contentResponse.getContentAsString());
            response = gson.fromJson(contentResponse.getContentAsString(), Devices.class);
            if (response.getError() > 0) {
                listener.onError("api", response.getError() + "", "Not yet set in the binding");
                return null;
            } else {
                return response;
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            listener.onError("api", e.getCause().toString(), e.getMessage());
            return null;
        }
    }

    public void setStatusLan(String data, String command, String deviceid, String ipaddress, String deviceKey,
            String seq) {
        JsonObject payload = Utils.encrypt(data, deviceKey, deviceid, seq);
        String url = "http://" + ipaddress + ":8081/zeroconf/" + command;
        logger.debug("Updating url: {}", url);
        logger.debug("with unencrypted payload:{}", data);
        logger.debug("with encrypted payload:{}", payload);
        try {
            ContentResponse response = httpClient.newRequest(url).method("POST").header("accept", "application/json")
                    .header("Content-Type", "application/json; utf-8")
                    .content(new StringContentProvider(gson.toJson(payload)), "application/json")
                    .timeout(5, TimeUnit.SECONDS).send();
            logger.debug("Lan Response:{}", response.getContentAsString());
        } catch (Exception e) {
            logger.warn("Sonoff - Failed to send update:{}", e);
        }
    }
}
