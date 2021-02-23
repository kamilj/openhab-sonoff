
package org.openhab.binding.sonoff.internal.dto.payloads;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ThingList implements Serializable {

    @SerializedName("thingList")
    @Expose
    private List<Things> things = new ArrayList<Things>();
    private final static long serialVersionUID = 1205249120703729170L;

    public List<Things> getThings() {
        return things;
    }

    public void setThings(List<Things> things) {
        this.things = things;
    }
}
