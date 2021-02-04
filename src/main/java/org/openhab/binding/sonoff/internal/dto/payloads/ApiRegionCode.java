package org.openhab.binding.sonoff.internal.dto.payloads;

import org.openhab.binding.sonoff.internal.helpers.DtoHelper;

public class ApiRegionCode {

    private String appid = DtoHelper.appid;
    private String nonce = DtoHelper.getNonce();
    private Long ts = DtoHelper.getTs();
    private Integer version = DtoHelper.version;
    private String country_code;

    @Override
    public String toString() {
        return "ApiRegionCode{" + "appid='" + appid + '\'' + ", country_code='" + country_code + '\'' + ", nonce='"
                + nonce + '\'' + ", ts='" + ts + '\'' + ", version='" + version + '\'' + '}';
    }

    public Long getTs() {
        return this.ts;
    }

    public Integer getVersion() {
        return this.version;
    }

    public String getAppid() {
        return this.appid;
    }

    public String getNonce() {
        return this.nonce;
    }

    public String getCountry_code() {
        return this.country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }
}
