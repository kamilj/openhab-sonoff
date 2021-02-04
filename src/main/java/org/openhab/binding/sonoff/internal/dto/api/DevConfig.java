package org.openhab.binding.sonoff.internal.dto.api;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DevConfig implements Serializable {

    @SerializedName("remoteInfos")
    @Expose
    private List<RemoteInfo> remoteInfos = null;
    private final static long serialVersionUID = 2598343375148836997L;

    public List<RemoteInfo> getRemoteInfos() {
        return remoteInfos;
    }

    public void setRemoteInfos(List<RemoteInfo> remoteInfos) {
        this.remoteInfos = remoteInfos;
    }
}
