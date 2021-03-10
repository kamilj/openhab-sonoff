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
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author David Murton - Initial contribution
 */
public class Device implements Serializable {

    @SerializedName("settings")
    @Expose
    private Settings settings;
    @SerializedName("family")
    @Expose
    private Family family;
    @SerializedName("group")
    @Expose
    private String group;
    @SerializedName("online")
    @Expose
    private Boolean online;
    @SerializedName("local")
    @Expose
    private Boolean local;
    @SerializedName("shareUsersInfo")
    @Expose
    private List<Object> shareUsersInfo = new ArrayList<Object>();
    @SerializedName("groups")
    @Expose
    private List<Object> groups = new ArrayList<Object>();
    @SerializedName("devGroups")
    @Expose
    private List<Object> devGroups = new ArrayList<Object>();
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("deviceid")
    @Expose
    private String deviceid;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("apikey")
    @Expose
    private String apikey;
    @SerializedName("extra")
    @Expose
    private ExtraExtra extra;
    @SerializedName("createdAt")
    @Expose
    private String createdAt;
    @SerializedName("__v")
    @Expose
    private Integer v;
    @SerializedName("onlineTime")
    @Expose
    private String onlineTime;
    @SerializedName("ip")
    @Expose
    private String ip;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("params")
    @Expose
    private Params params;
    @SerializedName("offlineTime")
    @Expose
    private String offlineTime;
    @SerializedName("tags")
    @Expose
    private Tags tags;
    @SerializedName("sharedTo")
    @Expose
    private List<Object> sharedTo = new ArrayList<Object>();
    @SerializedName("devicekey")
    @Expose
    private String devicekey;
    @SerializedName("deviceUrl")
    @Expose
    private String deviceUrl;
    @SerializedName("brandName")
    @Expose
    private String brandName;
    @SerializedName("showBrand")
    @Expose
    private Boolean showBrand;
    @SerializedName("brandLogoUrl")
    @Expose
    private String brandLogoUrl;
    @SerializedName("productModel")
    @Expose
    private String productModel;
    @SerializedName("devConfig")
    @Expose
    private DevConfig devConfig;
    @SerializedName("uiid")
    @Expose
    private Integer uiid;
    @SerializedName("localAddress")
    @Expose
    private String localAddress;
    @SerializedName("sequence")
    @Expose
    private String sequence;
    @SerializedName("config")
    @Expose
    private Config config;
    @SerializedName("error")
    @Expose
    private Integer error;
    @SerializedName("errmsg")
    @Expose
    private String errmsg;
    @SerializedName("action")
    @Expose
    private String action;
    @SerializedName("denyFeatures")
    @Expose
    private List<String> denyFeatures = new ArrayList<String>();

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getErrmsg() {
        return this.errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public Integer getError() {
        return this.error;
    }

    public void setError(Integer error) {
        this.error = error;
    }

    public Config getConfig() {
        return this.config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getSequence() {
        return this.sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    private static final long serialVersionUID = 2958983062170485126L;

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public Family getFamily() {
        return family;
    }

    public void setFamily(Family family) {
        this.family = family;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public Boolean getLocal() {
        return local;
    }

    public void setLocal(Boolean local) {
        this.local = local;
    }

    public List<Object> getShareUsersInfo() {
        return shareUsersInfo;
    }

    public void setShareUsersInfo(List<Object> shareUsersInfo) {
        this.shareUsersInfo = shareUsersInfo;
    }

    public List<Object> getGroups() {
        return groups;
    }

    public void setGroups(List<Object> groups) {
        this.groups = groups;
    }

    public List<Object> getDevGroups() {
        return devGroups;
    }

    public void setDevGroups(List<Object> devGroups) {
        this.devGroups = devGroups;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public ExtraExtra getExtra() {
        return extra;
    }

    public void setExtra(ExtraExtra extra) {
        this.extra = extra;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getV() {
        return v;
    }

    public void setV(Integer v) {
        this.v = v;
    }

    public String getOnlineTime() {
        return onlineTime;
    }

    public void setOnlineTime(String onlineTime) {
        this.onlineTime = onlineTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public String getOfflineTime() {
        return offlineTime;
    }

    public void setOfflineTime(String offlineTime) {
        this.offlineTime = offlineTime;
    }

    public Tags getTags() {
        return tags;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }

    public List<Object> getSharedTo() {
        return sharedTo;
    }

    public void setSharedTo(List<Object> sharedTo) {
        this.sharedTo = sharedTo;
    }

    public String getDevicekey() {
        return devicekey;
    }

    public void setDevicekey(String devicekey) {
        this.devicekey = devicekey;
    }

    public String getDeviceUrl() {
        return deviceUrl;
    }

    public void setDeviceUrl(String deviceUrl) {
        this.deviceUrl = deviceUrl;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Boolean getShowBrand() {
        return showBrand;
    }

    public void setShowBrand(Boolean showBrand) {
        this.showBrand = showBrand;
    }

    public String getBrandLogoUrl() {
        return brandLogoUrl;
    }

    public void setBrandLogoUrl(String brandLogoUrl) {
        this.brandLogoUrl = brandLogoUrl;
    }

    public String getProductModel() {
        return productModel;
    }

    public void setProductModel(String productModel) {
        this.productModel = productModel;
    }

    public DevConfig getDevConfig() {
        return devConfig;
    }

    public void setDevConfig(DevConfig devConfig) {
        this.devConfig = devConfig;
    }

    public Integer getUiid() {
        return uiid;
    }

    public void setUiid(Integer uiid) {
        this.uiid = uiid;
    }

    public String getLocalAddress() {
        return this.localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public List<String> getDenyFeatures() {
        return denyFeatures;
    }

    public void setDenyFeatures(List<String> denyFeatures) {
        this.denyFeatures = denyFeatures;
    }
}
