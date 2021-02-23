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
 * The {@link lightwaverfBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author David Murton - Initial contribution
 */

public class DeviceConfig {

    public String deviceid;
    public Integer consumptionPoll;
    public Integer localPoll;
    public Boolean consumption;
    public Boolean local;

    @Override
    public String toString() {
        return "[deviceid=" + deviceid + ", localPoll=" + localPoll + ", consumptionPoll=" + consumptionPoll
                + ", local=" + local + ", consumption=" + consumption + "]";
    }
}
