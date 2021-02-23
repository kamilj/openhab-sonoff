package org.openhab.binding.sonoff.internal.dto.api;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class V2ApiLoginResponse implements Serializable {

    @SerializedName("error")
    @Expose
    private Long error;
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("data")
    @Expose
    private V2Data data;
    private final static long serialVersionUID = -8030884031406247073L;

    public Long getError() {
        return error;
    }

    public void setError(Long error) {
        this.error = error;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public V2Data getData() {
        return data;
    }

    public void setData(V2Data data) {
        this.data = data;
    }
}
