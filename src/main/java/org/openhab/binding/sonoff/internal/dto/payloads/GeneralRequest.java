package org.openhab.binding.sonoff.internal.dto.payloads;

import org.openhab.binding.sonoff.internal.helpers.DtoHelper;

public class GeneralRequest {

    private String appid = DtoHelper.appid;
    private String nonce = DtoHelper.getNonce();
    private Long ts = DtoHelper.getTs();
    private int version = DtoHelper.version;
    private String accept;

    public String getAppid() {
        return this.appid;
    }

    public String getNonce() {
        return this.nonce;
    }

    public Long getTs() {
        return this.ts;
    }

    public int getVersion() {
        return this.version;
    }

    public String getAccept() {
        return this.accept;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }
}
