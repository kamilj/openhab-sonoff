package org.openhab.binding.sonoff.internal.helpers;

import org.openhab.binding.sonoff.internal.Utils;

public class CommandMessage {

    private final String command;
    private final String deviceid;
    private final String ipaddress;
    private final String deviceKey;
    private String params;
    private Long sequence;

    // LAN Device message type
    public CommandMessage(String command, String params, String deviceid, String ipaddress, String deviceKey) {
        this.command = command;
        this.params = params;
        this.deviceid = deviceid;
        this.ipaddress = ipaddress;
        this.deviceKey = deviceKey;
    }

    // Websocket Device Message Type
    public CommandMessage(String command, String params, String deviceid) {
        this.command = command;
        this.params = params;
        this.deviceid = deviceid;
        this.ipaddress = null;
        this.deviceKey = null;
    }

    // WebSocket Login Message Type
    public CommandMessage(String command) {
        this.command = command;
        this.deviceid = null;
        this.ipaddress = null;
        this.deviceKey = null;
    }

    public String getUrl() {
        return "http://" + ipaddress + ":8081/zeroconf/" + command;
    }

    public String getPayload() {
        return Utils.encrypt(params, deviceKey, deviceid, sequence);
    }

    public String getCommand() {
        return this.command;
    }

    public String getDeviceid() {
        return this.deviceid;
    }

    public String getIpaddress() {
        return this.ipaddress;
    }

    public String getDeviceKey() {
        return this.deviceKey;
    }

    public String getParams() {
        return this.params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public Long getSequence() {
        return this.sequence;
    }

    public void setSequence() {
        this.sequence = DtoHelper.getSequence();
    }
}
