package org.openhab.binding.sonoff.internal.dto.api;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class V2Data implements Serializable {

    @SerializedName("user")
    @Expose
    private V2User user;
    @SerializedName("at")
    @Expose
    private String at;
    @SerializedName("rt")
    @Expose
    private String rt;
    @SerializedName("region")
    @Expose
    private String region;
    private final static long serialVersionUID = -2287519807558496790L;

    public V2User getUser() {
        return user;
    }

    public void setUser(V2User user) {
        this.user = user;
    }

    public String getAt() {
        return at;
    }

    public void setAt(String at) {
        this.at = at;
    }

    public String getRt() {
        return rt;
    }

    public void setRt(String rt) {
        this.rt = rt;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
