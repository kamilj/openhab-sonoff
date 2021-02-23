package org.openhab.binding.sonoff.internal.dto.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data implements Serializable {

    @SerializedName("thingList")
    @Expose
    private List<ThingList> thingList = new ArrayList<ThingList>();
    private final static long serialVersionUID = -5220986323592055740L;

    public List<ThingList> getThingList() {
        return thingList;
    }

    public void setThingList(List<ThingList> thingList) {
        this.thingList = thingList;
    }
}
