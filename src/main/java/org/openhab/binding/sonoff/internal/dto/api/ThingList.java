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
public class ThingList implements Serializable {

    @SerializedName("itemType")
    @Expose
    private Long itemType;
    @SerializedName("itemData")
    @Expose
    private Device itemData;
    @SerializedName("index")
    @Expose
    private Long index;
    private static final long serialVersionUID = -3702952942291135491L;

    public Long getItemType() {
        return itemType;
    }

    public void setItemType(Long itemType) {
        this.itemType = itemType;
    }

    public Device getItemData() {
        return itemData;
    }

    public void setItemData(Device itemData) {
        this.itemData = itemData;
    }

    public Long getIndex() {
        return index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }
}
