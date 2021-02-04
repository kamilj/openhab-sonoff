package org.openhab.binding.sonoff.internal.dto.api;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ApiRegionResponse implements Serializable {

    @SerializedName("requestid")
    @Expose
    private String requestid;
    @SerializedName("region")
    @Expose
    private String region;
    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("rtnCode")
    @Expose
    private String rtnCode;
    @SerializedName("rtnMsg")
    @Expose
    private String rtnMsg;

    private final static long serialVersionUID = 3129284069541224909L;

    public String getRequestid() {
        return this.requestid;
    }

    public void setRequestid(String requestid) {
        this.requestid = requestid;
    }

    public String getRegion() {
        return this.region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getRtnCode() {
        return this.rtnCode;
    }

    public void setRtnCode(String rtnCode) {
        this.rtnCode = rtnCode;
    }

    public String getRtnMsg() {
        return this.rtnMsg;
    }

    public void setRtnMsg(String rtnMsg) {
        this.rtnMsg = rtnMsg;
    }
}
