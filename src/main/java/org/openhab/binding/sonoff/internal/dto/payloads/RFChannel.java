
package org.openhab.binding.sonoff.internal.dto.payloads;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RFChannel implements Serializable {

    @SerializedName("cmd")
    @Expose
    private String cmd;
    @SerializedName("rfChl")
    @Expose
    private Integer rfChl;

    private final static long serialVersionUID = 1205249120703729170L;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public Integer getRfChl() {
        return rfChl;
    }

    public void setRfChannel(Integer rfChl) {
        this.rfChl = rfChl;
    }
}
