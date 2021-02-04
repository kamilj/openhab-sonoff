package org.openhab.binding.sonoff.internal.dto.api;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Tags implements Serializable {

    @SerializedName("zyx_info")
    @Expose
    private List<ZyxInfo> zyxInfo = null;
    private final static long serialVersionUID = 7529147661865551616L;

    public List<ZyxInfo> getZyxInfo() {
        return zyxInfo;
    }

    public void setZyxInfo(List<ZyxInfo> zyxInfo) {
        this.zyxInfo = zyxInfo;
    }
}
