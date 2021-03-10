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
package org.openhab.binding.sonoff.internal.helpers;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CommandMessage} creates a new message to be sent to Ewelink devices
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class CommandMessage {

    private final String command;
    private String deviceid = "";
    private String ipaddress = "";
    private String deviceKey = "";
    private String params = "";
    private Long sequence = 0L;

    // Device Message Type
    public CommandMessage(String command, String params, String deviceid) {
        this.command = command;
        this.params = params;
        this.deviceid = deviceid;
    }

    // WebSocket Login Message Type
    public CommandMessage(String command) {
        this.command = command;
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

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public String getDeviceKey() {
        return this.deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
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
