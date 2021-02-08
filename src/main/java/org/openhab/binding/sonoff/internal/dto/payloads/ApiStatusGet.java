
package org.openhab.binding.sonoff.internal.dto.payloads;

import java.io.Serializable;

import org.openhab.binding.sonoff.internal.helpers.DtoHelper;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ApiStatusGet implements Serializable {

    @SerializedName("action")
    @Expose
    private String action = "query";
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
    private String params;
    @SerializedName("selfApikey")
    @Expose
    private String selfApikey;
    @SerializedName("ts")
    @Expose
    private Long ts = DtoHelper.getTs() / 1000;
    @SerializedName("sequence")
    @Expose
    private Long sequence = DtoHelper.getSequence();
    @SerializedName("version")
    @Expose
    private Integer version = DtoHelper.version;
    @SerializedName("nonce")
    @Expose
    private String nonce = DtoHelper.getNonce();
    private final static long serialVersionUID = 2958983062170484126L;

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getTs() {
        return this.ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getApikey() {
        return this.apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }
}
