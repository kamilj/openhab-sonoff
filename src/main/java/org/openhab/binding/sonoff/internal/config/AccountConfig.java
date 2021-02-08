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
package org.openhab.binding.sonoff.internal.config;

/**
 * The {@link lightwaverfBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Murton - Initial contribution
 */

public class AccountConfig {

    public String email;
    public String password;
    public String countryCode;
    public String ipaddress;
    public String accessmode;
    public Integer pollingInterval;

    @Override
    public String toString() {
        return "[email=" + email + ", password=" + getPasswordForPrinting() + ", countryCode=" + countryCode
                + ", accessmode=" + accessmode + ", ipaddress=" + ipaddress + ", pollingInterval=" + pollingInterval
                + "]";
    }

    private String getPasswordForPrinting() {
        if (password != null) {
            return password.isEmpty() ? "<empty>" : "*********";
        }
        return "<null>";
    }
}
