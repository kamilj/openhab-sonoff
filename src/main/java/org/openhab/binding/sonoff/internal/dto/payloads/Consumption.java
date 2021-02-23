
package org.openhab.binding.sonoff.internal.dto.payloads;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Consumption implements Serializable {

    @SerializedName("hundredDaysKwh")
    @Expose
    private String hundredDaysKwh = "get";
    private final static long serialVersionUID = 1205249120703729170L;

    public String getHundredDaysKwh() {
        return hundredDaysKwh;
    }

    public void setHundredDaysKwh(String hundredDaysKwh) {
        this.hundredDaysKwh = hundredDaysKwh;
    }
}
