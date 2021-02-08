
package org.openhab.binding.sonoff.internal.dto.payloads;

import java.io.Serializable;

import org.openhab.binding.sonoff.internal.helpers.DtoHelper;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WsUpdate implements Serializable {

    @SerializedName("action")
    @Expose
    private String action = "update";
    @SerializedName("deviceid")
    @Expose
    private String deviceid;
    @SerializedName("apikey")
    @Expose
    private String apikey;
    @SerializedName("userAgent")
    @Expose
    private String userAgent = "app";
    @SerializedName("params")
    @Expose
    private JsonObject params;
    @SerializedName("tempRec")
    @Expose
    private String tempRec;
    @SerializedName("selfApikey")
    @Expose
    private String selfApikey;
    private String at;
    private Long ts = DtoHelper.getTs() / 1000;
    private Long sequence = DtoHelper.getSequence();
    private Integer version = DtoHelper.version;
    private String nonce = DtoHelper.getNonce();

    private final static long serialVersionUID = -1947653206187395468L;

    public String getAt() {
        return this.at;
    }

    public void setAt(String at) {
        this.at = at;
    }

    public Long getTs() {
        return this.ts;
    }

    public Long getSequence() {
        return sequence;
    }

    public int getVersion() {
        return this.version;
    }

    public String getNonce() {
        return this.nonce;
    }

    public String getAction() {
        return action;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public String getApikey() {
        return apikey;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public JsonObject getParams() {
        return params;
    }

    public void setParams(JsonObject params) {
        this.params = params;
    }

    public String getTempRec() {
        return this.tempRec;
    }

    public void setTempRec(String tempRec) {
        this.tempRec = tempRec;
    }

    public String getSelfApikey() {
        return this.selfApikey;
    }

    public void setSelfApikey(String selfApikey) {
        this.selfApikey = selfApikey;
    }
}
