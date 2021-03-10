/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
public class Params implements Serializable {

    @SerializedName("init")
    @Expose
    private Integer init;
    @SerializedName("online")
    @Expose
    private Boolean online;
    @SerializedName("staMac")
    @Expose
    private String staMac;
    @SerializedName("rssi")
    @Expose
    private Integer rssi;
    @SerializedName("alarmCValue")
    @Expose
    private List<Integer> alarmCValue = new ArrayList<Integer>();
    @SerializedName("alarmVValue")
    @Expose
    private List<Integer> alarmVValue = new ArrayList<Integer>();
    @SerializedName("switch")
    @Expose
    private String switch0;
    @SerializedName("switches")
    @Expose
    private List<Switch> switches = new ArrayList<Switch>();
    @SerializedName("voltage")
    @Expose
    private String voltage;
    @SerializedName("oneKwh")
    @Expose
    private String oneKwh;
    @SerializedName("current")
    @Expose
    private String current;
    @SerializedName("alarmType")
    @Expose
    private String alarmType;
    @SerializedName("fwVersion")
    @Expose
    private String fwVersion;
    @SerializedName("power")
    @Expose
    private String power;
    @SerializedName("alarmPValue")
    @Expose
    private List<Integer> alarmPValue = new ArrayList<Integer>();
    @SerializedName("uiActive")
    @Expose
    private Integer uiActive;
    @SerializedName("timeZone")
    @Expose
    private Double timeZone;
    @SerializedName("hundredDaysKwh")
    @Expose
    private String hundredDaysKwh;
    @SerializedName("sledOnline")
    @Expose
    private String sledOnline;
    @SerializedName("startup")
    @Expose
    private String startup;
    @SerializedName("endTime")
    @Expose
    private String endTime;
    @SerializedName("startTime")
    @Expose
    private String startTime;
    @SerializedName("version")
    @Expose
    private Integer version;
    @SerializedName("pulse")
    @Expose
    private String pulse;
    @SerializedName("pulseWidth")
    @Expose
    private Integer pulseWidth;
    @SerializedName("timers")
    @Expose
    private List<Timer> timers = new ArrayList<Timer>();
    @SerializedName("only_device")
    @Expose
    private OnlyDevice onlyDevice;
    @SerializedName("ssid")
    @Expose
    private String ssid;
    @SerializedName("bssid")
    @Expose
    private String bssid;
    @SerializedName("configure")
    @Expose
    private List<Configure> configure = new ArrayList<Configure>();
    @SerializedName("sharedTo")
    @Expose
    private List<SharedTo> sharedTo = new ArrayList<SharedTo>();
    @SerializedName("pulses")
    @Expose
    private List<Pulse> pulses = new ArrayList<Pulse>();
    @SerializedName("senMode")
    @Expose
    private SenMode senMode;
    @SerializedName("savMode")
    @Expose
    private SavMode savMode;
    @SerializedName("alertMode")
    @Expose
    private AlertMode alertMode;

    @SerializedName("currentHumidity")
    @Expose
    private String currentHumidity;
    @SerializedName("currentTemperature")
    @Expose
    private String currentTemperature;
    @SerializedName("sensorType")
    @Expose
    private String sensorType;
    @SerializedName("deviceType")
    @Expose
    private String deviceType;
    @SerializedName("mainSwitch")
    @Expose
    private String mainSwitch;
    @SerializedName("bindInfos")
    @Expose
    private BindInfos bindInfos;
    @SerializedName("partnerApikey")
    @Expose
    private String partnerApikey;
    @SerializedName("cmd")
    @Expose
    private String cmd;
    @SerializedName("rfChl")
    @Expose
    private Integer rfChl;
    @SerializedName("rfCh2")
    @Expose
    private Integer rfCh2;
    @SerializedName("rfCh3")
    @Expose
    private Integer rfCh3;
    @SerializedName("rfCh4")
    @Expose
    private Integer rfCh4;
    @SerializedName("type")
    @Expose
    private Long type;
    @SerializedName("chipID")
    @Expose
    private String chipID;
    @SerializedName("mac")
    @Expose
    private String mac;
    @SerializedName("lastUpdateTime")
    @Expose
    private String lastUpdateTime;
    @SerializedName("actionTime")
    @Expose
    private String actionTime;
    @SerializedName("light_type")
    @Expose
    private Integer light_type;
    @SerializedName("colorR")
    @Expose
    private Integer colorR;
    @SerializedName("colorG")
    @Expose
    private Integer colorG;
    @SerializedName("colorB")
    @Expose
    private Integer colorB;
    @SerializedName("mode")
    @Expose
    private Integer mode;
    @SerializedName("speed")
    @Expose
    private Integer speed;
    @SerializedName("sensitive")
    @Expose
    private Integer sensitive;
    @SerializedName("bright")
    @Expose
    private Integer bright;

    @SerializedName("humidity")
    @Expose
    private Integer humidity;

    public Integer getHumidity() {
        return this.humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    @SerializedName("temperature")
    @Expose
    private Integer temperature;

    public Integer getTemperature() {
        return this.temperature;
    }

    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }

    private static final long serialVersionUID = 1205249120703729170L;

    public Integer getInit() {
        return init;
    }

    public void setInit(Integer init) {
        this.init = init;
    }

    public Boolean getOnline() {
        return this.online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public String getStaMac() {
        return staMac;
    }

    public void setStaMac(String staMac) {
        this.staMac = staMac;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public List<Integer> getAlarmCValue() {
        return alarmCValue;
    }

    public void setAlarmCValue(List<Integer> alarmCValue) {
        this.alarmCValue = alarmCValue;
    }

    public List<Integer> getAlarmVValue() {
        return alarmVValue;
    }

    public void setAlarmVValue(List<Integer> alarmVValue) {
        this.alarmVValue = alarmVValue;
    }

    public String getSwitch() {
        return switch0;
    }

    public void setSwitch(String switch0) {
        this.switch0 = switch0;
    }

    public List<Switch> getSwitches() {
        return switches;
    }

    public void setSwitches(List<Switch> switches) {
        this.switches = switches;
    }

    public String getVoltage() {
        return voltage;
    }

    public void setVoltage(String voltage) {
        this.voltage = voltage;
    }

    public String getOneKwh() {
        return oneKwh;
    }

    public void setOneKwh(String oneKwh) {
        this.oneKwh = oneKwh;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public String getFwVersion() {
        return fwVersion;
    }

    public void setFwVersion(String fwVersion) {
        this.fwVersion = fwVersion;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public List<Integer> getAlarmPValue() {
        return alarmPValue;
    }

    public void setAlarmPValue(List<Integer> alarmPValue) {
        this.alarmPValue = alarmPValue;
    }

    public Integer getUiActive() {
        return uiActive;
    }

    public void setUiActive(Integer uiActive) {
        this.uiActive = uiActive;
    }

    public Double getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(Double timeZone) {
        this.timeZone = timeZone;
    }

    public String getHundredDaysKwh() {
        return hundredDaysKwh;
    }

    public void setHundredDaysKwh(String hundredDaysKwh) {
        this.hundredDaysKwh = hundredDaysKwh;
    }

    public String getSledOnline() {
        return sledOnline;
    }

    public void setSledOnline(String sledOnline) {
        this.sledOnline = sledOnline;
    }

    public String getStartup() {
        return startup;
    }

    public void setStartup(String startup) {
        this.startup = startup;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getPulse() {
        return pulse;
    }

    public void setPulse(String pulse) {
        this.pulse = pulse;
    }

    public Integer getPulseWidth() {
        return pulseWidth;
    }

    public void setPulseWidth(Integer pulseWidth) {
        this.pulseWidth = pulseWidth;
    }

    public List<Timer> getTimers() {
        return timers;
    }

    public void setTimers(List<Timer> timers) {
        this.timers = timers;
    }

    public OnlyDevice getOnlyDevice() {
        return onlyDevice;
    }

    public void setOnlyDevice(OnlyDevice onlyDevice) {
        this.onlyDevice = onlyDevice;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public List<Configure> getConfigure() {
        return configure;
    }

    public void setConfigure(List<Configure> configure) {
        this.configure = configure;
    }

    public List<Pulse> getPulses() {
        return pulses;
    }

    public void setPulses(List<Pulse> pulses) {
        this.pulses = pulses;
    }

    public SenMode getSenMode() {
        return senMode;
    }

    public void setSenMode(SenMode senMode) {
        this.senMode = senMode;
    }

    public SavMode getSavMode() {
        return savMode;
    }

    public void setSavMode(SavMode savMode) {
        this.savMode = savMode;
    }

    public AlertMode getAlertMode() {
        return alertMode;
    }

    public void setAlertMode(AlertMode alertMode) {
        this.alertMode = alertMode;
    }

    public String getCurrentHumidity() {
        return currentHumidity;
    }

    public void setCurrentHumidity(String currentHumidity) {
        this.currentHumidity = currentHumidity;
    }

    public String getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(String currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getMainSwitch() {
        return mainSwitch;
    }

    public void setMainSwitch(String mainSwitch) {
        this.mainSwitch = mainSwitch;
    }

    public BindInfos getBindInfos() {
        return bindInfos;
    }

    public void setBindInfos(BindInfos bindInfos) {
        this.bindInfos = bindInfos;
    }

    public String getPartnerApikey() {
        return this.partnerApikey;
    }

    public void setPartnerApikey(String partnerApikey) {
        this.partnerApikey = partnerApikey;
    }

    public String getCmd() {
        return this.cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public Integer getRfChl() {
        return this.rfChl;
    }

    public void setRfChl(Integer rfChl) {
        this.rfChl = rfChl;
    }

    @SerializedName("rfTrig0")
    @Expose
    private String rfTrig0;
    @SerializedName("rfTrig1")
    @Expose
    private String rfTrig1;
    @SerializedName("rfTrig2")
    @Expose
    private String rfTrig2;
    @SerializedName("rfTrig3")
    @Expose
    private String rfTrig3;
    @SerializedName("rfTrig4")
    @Expose
    private String rfTrig4;
    @SerializedName("rfTrig5")
    @Expose
    private String rfTrig5;
    @SerializedName("rfTrig6")
    @Expose
    private String rfTrig6;
    @SerializedName("rfTrig7")
    @Expose
    private String rfTrig7;
    @SerializedName("rfTrig8")
    @Expose
    private String rfTrig8;
    @SerializedName("rfTrig9")
    @Expose
    private String rfTrig9;
    @SerializedName("rfTrig10")
    @Expose
    private String rfTrig10;
    @SerializedName("rfTrig11")
    @Expose
    private String rfTrig11;
    @SerializedName("rfTrig12")
    @Expose
    private String rfTrig12;
    @SerializedName("rfTrig13")
    @Expose
    private String rfTrig13;
    @SerializedName("rfTrig14")
    @Expose
    private String rfTrig14;
    @SerializedName("rfTrig15")
    @Expose
    private String rfTrig15;

    public String getRfTrig0() {
        return this.rfTrig0;
    }

    public void setRfTrig0(String rfTrig0) {
        this.rfTrig0 = rfTrig0;
    }

    public String getRfTrig1() {
        return this.rfTrig1;
    }

    public void setRfTrig1(String rfTrig1) {
        this.rfTrig1 = rfTrig1;
    }

    public String getRfTrig2() {
        return this.rfTrig2;
    }

    public void setRfTrig2(String rfTrig2) {
        this.rfTrig2 = rfTrig2;
    }

    public String getRfTrig3() {
        return this.rfTrig3;
    }

    public void setRfTrig3(String rfTrig3) {
        this.rfTrig3 = rfTrig3;
    }

    public String getRfTrig4() {
        return this.rfTrig4;
    }

    public void setRfTrig4(String rfTrig4) {
        this.rfTrig4 = rfTrig4;
    }

    public String getRfTrig5() {
        return this.rfTrig5;
    }

    public void setRfTrig5(String rfTrig5) {
        this.rfTrig5 = rfTrig5;
    }

    public String getRfTrig6() {
        return this.rfTrig6;
    }

    public void setRfTrig6(String rfTrig6) {
        this.rfTrig6 = rfTrig6;
    }

    public String getRfTrig7() {
        return this.rfTrig7;
    }

    public void setRfTrig7(String rfTrig7) {
        this.rfTrig7 = rfTrig7;
    }

    public String getRfTrig8() {
        return this.rfTrig8;
    }

    public void setRfTrig8(String rfTrig8) {
        this.rfTrig8 = rfTrig8;
    }

    public String getRfTrig9() {
        return this.rfTrig9;
    }

    public void setRfTrig9(String rfTrig9) {
        this.rfTrig9 = rfTrig9;
    }

    public String getRfTrig10() {
        return this.rfTrig10;
    }

    public void setRfTrig10(String rfTrig10) {
        this.rfTrig10 = rfTrig10;
    }

    public String getRfTrig11() {
        return this.rfTrig11;
    }

    public void setRfTrig11(String rfTrig11) {
        this.rfTrig11 = rfTrig11;
    }

    public String getRfTrig12() {
        return this.rfTrig12;
    }

    public void setRfTrig12(String rfTrig12) {
        this.rfTrig12 = rfTrig12;
    }

    public String getRfTrig13() {
        return this.rfTrig13;
    }

    public void setRfTrig13(String rfTrig13) {
        this.rfTrig13 = rfTrig13;
    }

    public String getRfTrig14() {
        return this.rfTrig14;
    }

    public void setRfTrig14(String rfTrig14) {
        this.rfTrig14 = rfTrig14;
    }

    public String getRfTrig15() {
        return this.rfTrig15;
    }

    public void setRfTrig15(String rfTrig15) {
        this.rfTrig15 = rfTrig15;
    }

    @SerializedName("rfList")
    @Expose
    private List<RfList> rfList = new ArrayList<RfList>();

    public List<RfList> getRfList() {
        return this.rfList;
    }

    public void setRfList(List<RfList> rfList) {
        this.rfList = rfList;
    }

    @SerializedName("zled")
    @Expose
    private String zled;
    @SerializedName("subDevices")
    @Expose
    private List<SubDevices> subDevices = new ArrayList<SubDevices>();

    public String getZled() {
        return this.zled;
    }

    public void setZled(String zled) {
        this.zled = zled;
    }

    public List<SubDevices> getSubDevices() {
        return this.subDevices;
    }

    public void setSubDevices(List<SubDevices> subDevices) {
        this.subDevices = subDevices;
    }

    @SerializedName("subDevId")
    @Expose
    private String subDevId;
    @SerializedName("parentid")
    @Expose
    private String parentid;
    @SerializedName("motion")
    @Expose
    private Integer motion;
    @SerializedName("trigTime")
    @Expose
    private String trigTime;
    @SerializedName("battery")
    @Expose
    private Integer battery;

    public String getSubDevId() {
        return this.subDevId;
    }

    public void setSubDevId(String subDevId) {
        this.subDevId = subDevId;
    }

    public String getParentid() {
        return this.parentid;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public Integer getMotion() {
        return this.motion;
    }

    public void setMotion(Integer motion) {
        this.motion = motion;
    }

    public String getTrigTime() {
        return this.trigTime;
    }

    public void setTrigTime(String trigTime) {
        this.trigTime = trigTime;
    }

    public Integer getBattery() {
        return this.battery;
    }

    public void setBattery(Integer battery) {
        this.battery = battery;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public String getChipID() {
        return chipID;
    }

    public void setChipID(String chipID) {
        this.chipID = chipID;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getActionTime() {
        return actionTime;
    }

    public void setActionTime(String actionTime) {
        this.actionTime = actionTime;
    }

    public Integer getLight_type() {
        return this.light_type;
    }

    public void setLight_type(Integer light_type) {
        this.light_type = light_type;
    }

    public Integer getColorR() {
        return this.colorR;
    }

    public void setColorR(Integer colorR) {
        this.colorR = colorR;
    }

    public Integer getColorG() {
        return this.colorG;
    }

    public void setColorG(Integer colorG) {
        this.colorG = colorG;
    }

    public Integer getColorB() {
        return this.colorB;
    }

    public void setColorB(Integer colorB) {
        this.colorB = colorB;
    }

    public Integer getMode() {
        return this.mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public Integer getSpeed() {
        return this.speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public Integer getSensitive() {
        return this.sensitive;
    }

    public void setSensitive(Integer sensitive) {
        this.sensitive = sensitive;
    }

    public Integer getBright() {
        return this.bright;
    }

    public void setBright(Integer bright) {
        this.bright = bright;
    }
}
