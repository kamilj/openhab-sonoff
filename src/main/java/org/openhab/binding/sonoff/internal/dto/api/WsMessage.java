/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.sonoff.internal.dto.api;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author David Murton - Initial contribution
 */
public class WsMessage implements Serializable {

    @SerializedName("apikey")
    @Expose
    private String apikey;
    @SerializedName("error")
    @Expose
    private Integer error;
    @SerializedName("sequence")
    @Expose
    private String sequence;
    @SerializedName("config")
    @Expose
    private Config config;
    @SerializedName("switch")
    @Expose
    private String switch0;
    @SerializedName("power")
    @Expose
    private String power;
    @SerializedName("voltage")
    @Expose
    private String voltage;
    @SerializedName("current")
    @Expose
    private String current;
    @SerializedName("rssi")
    @Expose
    private Integer rssi;
    @SerializedName("offlineTime")
    @Expose
    private String offlineTime;
    @SerializedName("uiActive")
    @Expose
    private String uiActive;
    @SerializedName("action")
    @Expose
    private String action;
    @SerializedName("reason")
    @Expose
    private String reason;
    @SerializedName("port")
    @Expose
    private Integer port;
    @SerializedName("from")
    @Expose
    private String from;
    @SerializedName("seq")
    @Expose
    private String seq;
    @SerializedName("IP")
    @Expose
    private String iP;
    @SerializedName("domain")
    @Expose
    private String domain;
    @SerializedName("deviceid")
    @Expose
    private String deviceid;
    @SerializedName("userAgent")
    @Expose
    private String userAgent;
    @SerializedName("d_seq")
    @Expose
    private Long dseq;
    @SerializedName("params")
    @Expose
    private Params params;
    private static final long serialVersionUID = 9111270141241205415L;

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getIP() {
        return this.iP;
    }

    public void setIP(String iP) {
        this.iP = iP;
    }

    public String getDomain() {
        return this.domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDeviceid() {
        return this.deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Long getDseq() {
        return this.dseq;
    }

    public void setDseq(Long dseq) {
        this.dseq = dseq;
    }

    public Params getParams() {
        return this.params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSeq() {
        return this.seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public Integer getError() {
        return this.error;
    }

    public void setError(Integer error) {
        this.error = error;
    }

    public String getSequence() {
        return this.sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getSwitch() {
        return this.switch0;
    }

    public void setSwitch(String switch0) {
        this.switch0 = switch0;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getVoltage() {
        return voltage;
    }

    public void setVoltage(String voltage) {
        this.voltage = voltage;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public Integer getRssi() {
        return this.rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public String getOfflineTime() {
        return this.offlineTime;
    }

    public void setOfflineTime(String offlineTime) {
        this.offlineTime = offlineTime;
    }

    public String getUiActive() {
        return this.uiActive;
    }

    public void setUiActive(String uiActive) {
        this.uiActive = uiActive;
    }
}
