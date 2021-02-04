package org.openhab.binding.sonoff.internal.dto.payloads;

import org.openhab.binding.sonoff.internal.helpers.DtoHelper;

public class WsLoginRequest {
    private String action = "userOnline";
    private String at;
    private String apikey;
    private String appid = DtoHelper.appid;
    private String nonce = DtoHelper.getNonce();
    private Long ts = DtoHelper.getTs() / 1000;
    private String userAgent = "app";
    private Long sequence = DtoHelper.getSequence();
    private Integer version = DtoHelper.version;

    public void setAt(String at) {
        this.at = at;
    }

    public String getAt() {
        return at;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public String getApikey() {
        return apikey;
    }

    public String getAction() {
        return action;
    }

    public String getAppid() {
        return appid;
    }

    public String getNonce() {
        return nonce;
    }

    public Long getTs() {
        return ts;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Long getSequence() {
        return sequence;
    }

    public int getVersion() {
        return version;
    }
}
