package org.openhab.binding.sonoff.internal.dto.payloads;

import org.openhab.binding.sonoff.internal.helpers.DtoHelper;

public class ApiLoginRequest {

    private String appid = DtoHelper.appid;
    private Long ts = DtoHelper.getTs();
    private Integer version = DtoHelper.version;
    private String nonce = DtoHelper.getNonce();
    private String email;
    private String phoneNumber;
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" + "appid='" + appid + '\'' + ", email='" + email + '\'' + ", phoneNumber='" + phoneNumber
                + '\'' + ", password='" + password + '\'' + ", ts='" + ts + '\'' + ", version='" + version + '\''
                + ", nonce='" + nonce + '\'' + '}';
    }
}
