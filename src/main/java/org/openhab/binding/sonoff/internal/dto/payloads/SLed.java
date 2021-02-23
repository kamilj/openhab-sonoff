
package org.openhab.binding.sonoff.internal.dto.payloads;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SLed implements Serializable {

    @SerializedName("sledOnline")
    @Expose
    private String sledOnline;
    private final static long serialVersionUID = 1205249120703729170L;

    public String getSledOnline() {
        return sledOnline;
    }

    public void setSledOnline(String sledOnline) {
        this.sledOnline = sledOnline;
    }
}
