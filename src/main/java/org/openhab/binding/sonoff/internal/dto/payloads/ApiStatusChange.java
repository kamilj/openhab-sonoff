
package org.openhab.binding.sonoff.internal.dto.payloads;

import java.io.Serializable;

import org.openhab.binding.sonoff.internal.helpers.DtoHelper;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ApiStatusChange implements Serializable {

    @SerializedName("deviceid")
    @Expose
    private String deviceid;
    @SerializedName("version")
    @Expose
    private Integer version = DtoHelper.version;

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @SerializedName("appid")
    @Expose
    private String appid = DtoHelper.appid;

    public String getAppid() {
        return this.appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    @SerializedName("ts")
    @Expose
    private Long ts = DtoHelper.getTs();

    public Long getTs() {
        return this.ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    @SerializedName("params")
    @Expose
    private String params;
    private final static long serialVersionUID = 2958983062170484126L;

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public void setParams(String params) {
        this.params = params;
    }
}
