
package org.openhab.binding.sonoff.internal.dto.payloads;

import java.io.Serializable;

import org.openhab.binding.sonoff.internal.helpers.DtoHelper;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WsQuery implements Serializable {

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
    private JsonObject params;

    public JsonObject getParams() {
        return this.params;
    }

    public void setParams(JsonObject params) {
        this.params = params;
    }

    @SerializedName("tempRec")
    @Expose
    private String tempRec;
    @SerializedName("sequence")
    @Expose
    private Long sequence = DtoHelper.getSequence();

    private final static long serialVersionUID = -1947653206187395468L;

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public Long getSequence() {
        return this.sequence;
    }
}
