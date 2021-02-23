package org.openhab.binding.sonoff.internal.dto.payloads;

public class ApiLoginRequest {

    // private String appid = DtoHelper.appid;
    // private Long ts = DtoHelper.getTs();
    // private Integer version = DtoHelper.version;
    // private String nonce = DtoHelper.getNonce();
    private String email;
    // private String phoneNumber;
    private String password;
    private String countryCode;

    public String getCountryCode() {
        return this.countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" + "email='" + email + '\'' + ", password='" + password + '\'' + ", countryCode='"
                + countryCode + '\'' + '}';
    }
}
