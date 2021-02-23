package org.openhab.binding.sonoff.internal.dto.api;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubDevices implements Serializable {

    @SerializedName("subDevId")
    @Expose
    private String subDevId;
    @SerializedName("deviceid")
    @Expose
    private String deviceid;
    @SerializedName("uiid")
    @Expose
    private String uiid;
    private final static long serialVersionUID = 1205249124971729170L;

    public String getSubDevId() {
        return this.subDevId;
    }

    public void setSubDevId(String subDevId) {
        this.subDevId = subDevId;
    }

    public String getDeviceid() {
        return this.deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getUiid() {
        return this.uiid;
    }

    public void setUiid(String uiid) {
        this.uiid = uiid;
    }
}
