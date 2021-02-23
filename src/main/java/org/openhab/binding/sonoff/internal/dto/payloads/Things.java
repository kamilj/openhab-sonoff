package org.openhab.binding.sonoff.internal.dto.payloads;

public class Things {

    Integer itemType;
    String id;

    public Integer getItemType() {
        return this.itemType;
    }

    public void setItemType(Integer itemType) {
        this.itemType = itemType;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
