package org.openhab.binding.sonoff.internal.dto.api;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RfList implements Serializable {

    @SerializedName("rfChl")
    @Expose
    private Integer rfChl;
    @SerializedName("rfVal")
    @Expose
    private String rfVal;
    private final static long serialVersionUID = 8895724428882072316L;

    public Integer getRfChl() {
        return rfChl;
    }

    public void setRfChl(Integer rfChl) {
        this.rfChl = rfChl;
    }

    public String getRfVal() {
        return rfVal;
    }

    public void setRfVal(String rfVal) {
        this.rfVal = rfVal;
    }
}
