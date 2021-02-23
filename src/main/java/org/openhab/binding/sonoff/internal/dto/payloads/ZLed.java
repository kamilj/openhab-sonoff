
package org.openhab.binding.sonoff.internal.dto.payloads;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ZLed implements Serializable {

    @SerializedName("zled")
    @Expose
    private String zled;
    private final static long serialVersionUID = 1205249120703729170L;

    public String getZled() {
        return zled;
    }

    public void setZled(String zled) {
        this.zled = zled;
    }
}
