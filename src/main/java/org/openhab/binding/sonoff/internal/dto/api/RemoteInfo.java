package org.openhab.binding.sonoff.internal.dto.api;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RemoteInfo implements Serializable {

    @SerializedName("numberOfRemote")
    @Expose
    private Integer numberOfRemote;
    @SerializedName("fwVersion")
    @Expose
    private String fwVersion;
    private final static long serialVersionUID = -2235920435696114329L;

    public Integer getNumberOfRemote() {
        return numberOfRemote;
    }

    public void setNumberOfRemote(Integer numberOfRemote) {
        this.numberOfRemote = numberOfRemote;
    }

    public String getFwVersion() {
        return fwVersion;
    }

    public void setFwVersion(String fwVersion) {
        this.fwVersion = fwVersion;
    }
}
