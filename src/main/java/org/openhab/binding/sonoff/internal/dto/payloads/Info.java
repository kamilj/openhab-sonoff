
package org.openhab.binding.sonoff.internal.dto.payloads;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Info implements Serializable {

    @SerializedName("cmd")
    @Expose
    private String cmd;

    public String getCmd() {
        return this.cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    private final static long serialVersionUID = 1205249120703729170L;
}
