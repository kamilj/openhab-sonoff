package org.openhab.binding.sonoff.internal.dto.api;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ZyxInfo implements Serializable {

    @SerializedName("remote_type")
    @Expose
    private String remoteType;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("buttonName")
    @Expose
    private List<ButtonName> buttonName = null;
    private final static long serialVersionUID = 6008423529967350538L;

    public String getRemoteType() {
        return remoteType;
    }

    public void setRemoteType(String remoteType) {
        this.remoteType = remoteType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ButtonName> getButtonName() {
        return buttonName;
    }

    public void setButtonName(List<ButtonName> buttonName) {
        this.buttonName = buttonName;
    }
}
