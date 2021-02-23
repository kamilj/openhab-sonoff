package org.openhab.binding.sonoff.internal.connections;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.openhab.binding.sonoff.internal.Utils;
import org.openhab.binding.sonoff.internal.config.AccountConfig;
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
import org.openhab.binding.sonoff.internal.helpers.MessageConverter;
import org.openhab.binding.sonoff.internal.listeners.ConnectionListener;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class Api {

    private final Logger logger = LoggerFactory.getLogger(Api.class);
    private final Gson gson;
    private final HttpClient httpClient;
    private final AccountConfig config;
    private final ConnectionListener listener;
    private final MessageConverter converter;

    private String baseUrl = "";
    private String dispUrl = "";
    private String apiKey = "";
    private String at = "";

    public Api(Gson gson, ConnectionListener listener, MessageConverter converter, HttpClientFactory httpClientFactory,
            AccountConfig config) {
        this.gson = gson;
        this.config = config;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.listener = listener;
        this.converter = converter;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getAt() {
        return at;
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
                listener.apiConnected(false);
                // listener.onError("api", response.getRtnCode(), response.getRtnMsg());
            } else {
                baseUrl = "https://" + response.getRegion() + "-apia.coolkit.cc";
                dispUrl = "https://" + response.getRegion() + "-dispa.coolkit.cc/dispatch/app";
            }
        } catch (Exception e) {
            listener.apiConnected(false);
            logger.trace("Api Exception:{}", e.getMessage());
        }
    }

    public void login() {
        String url = baseUrl + "/v2/user/login";
        ApiLoginRequest request = new ApiLoginRequest();
        request.setEmail(config.email);
        request.setPassword(config.password);
        request.setCountryCode(config.countryCode);
        logger.debug("Api Login Request:{}", gson.toJson(request));
        try {
            ContentResponse response = httpClient.newRequest(url).header("accept", "application/json")
                    .header("Content-Type", "application/json").header("X-CK-Appid", DtoHelper.appid)
                    .header("X-CK-Nonce", DtoHelper.getNonce())
                    .header("Authorization", "Sign " + Utils.getAuthMac(gson.toJson(request))).method("POST")
                    .content(new StringContentProvider(gson.toJson(request)), "application/json").send();
            // new Response.Listener.Adapter() {
            // @Override
            // public void onContent(Response response, ByteBuffer buffer) {
            // String string = StandardCharsets.UTF_8.decode(buffer).toString();
            V2ApiLoginResponse loginResponse = gson.fromJson(response.getContentAsString(), V2ApiLoginResponse.class);
            logger.debug("Api Login Respone:{}", response);
            if (loginResponse.getError() > 0) {
                // listener.onError("api", loginResponse.getError() + "", loginResponse.getMsg());
                listener.apiConnected(false);
            } else {
                at = loginResponse.getData().getAt();
                apiKey = loginResponse.getData().getUser().getApikey();
                listener.apiConnected(true);
                logger.debug("Api Connected");
            }
        } catch (Exception e) {
            logger.trace("Api Exception:{}", e.getMessage());
            listener.apiConnected(false);
        }
    }

    public WsServerResponse getWsServer() {
        logger.debug("Attempt to get websocket server");
        GeneralRequest request = new GeneralRequest();
        request.setAccept("ws");
        WsServerResponse response = new WsServerResponse();
        logger.debug("Websocket URL Request:{}", gson.toJson(request));
        try {
            ContentResponse contentResponse = httpClient.newRequest(dispUrl).header("accept", "application/json")
                    .header("Content-Type", "application/json; utf-8").header("Authorization", "Bearer " + at)
                    .method("POST").content(new StringContentProvider(gson.toJson(request)), "application/json").send();
            logger.debug("Websocket URL Response:{}", contentResponse.getContentAsString());
            response = gson.fromJson(contentResponse.getContentAsString(), WsServerResponse.class);
            return response;
        } catch (Exception e) {
            listener.websocketConnected(false);
            logger.trace("Api Exception:{}", e.getMessage());
            return null;
        }
    }

    public ThingResponse getDevices() {
        ThingResponse response = new ThingResponse();
        GeneralRequest request = new GeneralRequest();
        logger.debug("Api Discovery Request:{}", gson.toJson(request));
        String url = baseUrl + "/v2/device/thing";
        try {
            ContentResponse contentResponse = httpClient.newRequest(url).header("Authorization", "Bearer " + at)
                    .header("Content-Type", "application/json").header("X-CK-Appid", DtoHelper.appid)
                    .header("X-CK-Nonce", DtoHelper.getNonce()).method("GET").send();
            logger.debug("Api Discovery response:{}", contentResponse.getContentAsString());
            response = gson.fromJson(contentResponse.getContentAsString(), ThingResponse.class);
            if (response.getError() > 0) {
                // listener.onError("api", response.getError() + "", "Not yet set in the binding");
                return null;
            } else {
                return response;
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.trace("Api Exception:{}", e.getMessage());
            return null;
        }
    }

    public void getDevice(String deviceid) {
        ThingList request = new ThingList();
        Things thing = new Things();
        thing.setItemType(1);
        thing.setId(deviceid);
        request.getThings().add(thing);
        String url = baseUrl + "/v2/device/thing";
        logger.debug("Api Get Device Request for id:{}", deviceid);
        httpClient.newRequest(url).header("Content-Type", "application/json").header("X-CK-Appid", DtoHelper.appid)
                .header("X-CK-Nonce", DtoHelper.getNonce()).header("Authorization", "Bearer " + at)
                .content(new StringContentProvider(gson.toJson(request)), "application/json").method("POST")
                .send(new Response.Listener.Adapter() {
                    @Override
                    public void onContent(Response response, ByteBuffer buffer) {
                        String string = StandardCharsets.UTF_8.decode(buffer).toString();
                        logger.debug("Api Device response:{} for id {}", string, deviceid);
                        converter.convertApiMessage(string);
                    }
                });
    }
}
