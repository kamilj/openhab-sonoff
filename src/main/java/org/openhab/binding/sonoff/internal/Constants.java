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
package org.openhab.binding.sonoff.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link Constants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class Constants {

    private static final String BINDING_ID = "sonoff";

    // Data Directory
    public static final @Nullable String configFolder = System.getProperty("smarthome.userdata");
    public static final String saveFolder = configFolder + "/sonoff/";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_1 = new ThingTypeUID(BINDING_ID, "1"); // S20 , S26 , BASIC , MINI, Mini
                                                                                       // PCiE Card
    public static final ThingTypeUID THING_TYPE_2 = new ThingTypeUID(BINDING_ID, "2"); // SOCKET_2 Unknown Model
    public static final ThingTypeUID THING_TYPE_3 = new ThingTypeUID(BINDING_ID, "3"); // SOCKET_3 Unknown Model
    public static final ThingTypeUID THING_TYPE_4 = new ThingTypeUID(BINDING_ID, "4"); // SOCKET_4 Unknown Model
    public static final ThingTypeUID THING_TYPE_5 = new ThingTypeUID(BINDING_ID, "5"); // POW
    public static final ThingTypeUID THING_TYPE_6 = new ThingTypeUID(BINDING_ID, "6"); // T11C , TX1C , G1
    public static final ThingTypeUID THING_TYPE_7 = new ThingTypeUID(BINDING_ID, "7"); // T12C , TX2C
    public static final ThingTypeUID THING_TYPE_8 = new ThingTypeUID(BINDING_ID, "8"); // T13C , TX3C
    public static final ThingTypeUID THING_TYPE_9 = new ThingTypeUID(BINDING_ID, "9"); // SWITCH_4 Unknown Model
    public static final ThingTypeUID THING_TYPE_15 = new ThingTypeUID(BINDING_ID, "15"); // TH10 , TH16
    public static final ThingTypeUID THING_TYPE_24 = new ThingTypeUID(BINDING_ID, "24"); // GSM Socket
    public static final ThingTypeUID THING_TYPE_27 = new ThingTypeUID(BINDING_ID, "27"); // GSM Socket
    public static final ThingTypeUID THING_TYPE_28 = new ThingTypeUID(BINDING_ID, "28"); // RF-BRIDGE (RF3)
    public static final ThingTypeUID THING_TYPE_29 = new ThingTypeUID(BINDING_ID, "29"); // GSM Socket
    public static final ThingTypeUID THING_TYPE_30 = new ThingTypeUID(BINDING_ID, "30"); // GSM Socket
    public static final ThingTypeUID THING_TYPE_31 = new ThingTypeUID(BINDING_ID, "31"); // GSM Socket
    public static final ThingTypeUID THING_TYPE_32 = new ThingTypeUID(BINDING_ID, "32"); // POWR2
    public static final ThingTypeUID THING_TYPE_66 = new ThingTypeUID(BINDING_ID, "66"); // ZIGBEE Bridge
    public static final ThingTypeUID THING_TYPE_77 = new ThingTypeUID(BINDING_ID, "77"); // MICRO (USB)
    public static final ThingTypeUID THING_TYPE_78 = new ThingTypeUID(BINDING_ID, "78"); // unknown
    public static final ThingTypeUID THING_TYPE_81 = new ThingTypeUID(BINDING_ID, "81"); // GSM Socket
    public static final ThingTypeUID THING_TYPE_82 = new ThingTypeUID(BINDING_ID, "82"); // GSM Socket
    public static final ThingTypeUID THING_TYPE_83 = new ThingTypeUID(BINDING_ID, "83"); // GSM Socket
    public static final ThingTypeUID THING_TYPE_84 = new ThingTypeUID(BINDING_ID, "84"); // GSM Socket
    public static final ThingTypeUID THING_TYPE_107 = new ThingTypeUID(BINDING_ID, "107"); // GSM Socket

    // Zigbee Child Devices
    public static final ThingTypeUID THING_TYPE_ZMOTION = new ThingTypeUID(BINDING_ID, "zmotion"); // Motion Sensor
    public static final ThingTypeUID THING_TYPE_ZCONTACT = new ThingTypeUID(BINDING_ID, "zcontact"); // Contact Sensor
    public static final ThingTypeUID THING_TYPE_ZWATER = new ThingTypeUID(BINDING_ID, "zwater"); // Water Sensor
    public static final ThingTypeUID THING_TYPE_ZTEMP = new ThingTypeUID(BINDING_ID, "ztemp"); // Temp Sensor
    public static final ThingTypeUID THING_TYPE_ZSWITCH1 = new ThingTypeUID(BINDING_ID, "zswitch1"); // 1 way Switch
    public static final ThingTypeUID THING_TYPE_ZSWITCH2 = new ThingTypeUID(BINDING_ID, "zswitch2"); // 2 way Switch
    public static final ThingTypeUID THING_TYPE_ZSWITCH3 = new ThingTypeUID(BINDING_ID, "zswitch3"); // 3 way Switch
    public static final ThingTypeUID THING_TYPE_ZSWITCH4 = new ThingTypeUID(BINDING_ID, "zswitch4"); // 4 way Switch
    public static final ThingTypeUID THING_TYPE_ZLIGHT = new ThingTypeUID(BINDING_ID, "zlight"); // White Light

    // RF Child Devices
    public static final ThingTypeUID THING_TYPE_RF1 = new ThingTypeUID(BINDING_ID, "rfremote1"); // 1 Button RF Remote
    public static final ThingTypeUID THING_TYPE_RF2 = new ThingTypeUID(BINDING_ID, "rfremote2"); // 2 Button RF Remote
    public static final ThingTypeUID THING_TYPE_RF3 = new ThingTypeUID(BINDING_ID, "rfremote3"); // 3 Button RF Remote
    public static final ThingTypeUID THING_TYPE_RF4 = new ThingTypeUID(BINDING_ID, "rfremote4"); // 4 Button RF Remote
    public static final ThingTypeUID THING_TYPE_RF6 = new ThingTypeUID(BINDING_ID, "rfsensor"); // RF Sensor

    // For unknowns
    public static final ThingTypeUID THING_TYPE_UNKNOWNDEVICE = new ThingTypeUID(BINDING_ID, "device");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Collections.unmodifiableSet(Stream.of(
            THING_TYPE_ACCOUNT, THING_TYPE_1, THING_TYPE_2, THING_TYPE_3, THING_TYPE_4, THING_TYPE_5, THING_TYPE_6,
            THING_TYPE_7, THING_TYPE_8, THING_TYPE_9,

            THING_TYPE_15, THING_TYPE_24, THING_TYPE_27, THING_TYPE_29, THING_TYPE_30, THING_TYPE_31,

            THING_TYPE_28, THING_TYPE_RF1, THING_TYPE_RF2, THING_TYPE_RF3, THING_TYPE_RF4, THING_TYPE_RF6,

            THING_TYPE_32,

            THING_TYPE_66, THING_TYPE_ZMOTION, THING_TYPE_ZCONTACT, THING_TYPE_ZWATER, THING_TYPE_ZTEMP,
            THING_TYPE_ZLIGHT, THING_TYPE_ZSWITCH1, THING_TYPE_ZSWITCH2, THING_TYPE_ZSWITCH3, THING_TYPE_ZSWITCH4,

            THING_TYPE_77, THING_TYPE_78, THING_TYPE_81, THING_TYPE_82, THING_TYPE_83, THING_TYPE_84, THING_TYPE_107

    ).collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPE_UIDS = Collections.unmodifiableSet(Stream.of(
            THING_TYPE_1, THING_TYPE_2, THING_TYPE_3, THING_TYPE_4, THING_TYPE_5, THING_TYPE_6, THING_TYPE_7,
            THING_TYPE_8, THING_TYPE_9,

            THING_TYPE_15, THING_TYPE_24, THING_TYPE_27, THING_TYPE_29, THING_TYPE_30, THING_TYPE_31,

            THING_TYPE_28, THING_TYPE_RF1, THING_TYPE_RF2, THING_TYPE_RF3, THING_TYPE_RF4, THING_TYPE_RF6,

            THING_TYPE_32,

            THING_TYPE_66, THING_TYPE_ZMOTION, THING_TYPE_ZCONTACT, THING_TYPE_ZWATER, THING_TYPE_ZTEMP,
            THING_TYPE_ZLIGHT, THING_TYPE_ZSWITCH1, THING_TYPE_ZSWITCH2, THING_TYPE_ZSWITCH3, THING_TYPE_ZSWITCH4,

            THING_TYPE_77, THING_TYPE_78, THING_TYPE_81, THING_TYPE_82, THING_TYPE_83, THING_TYPE_84, THING_TYPE_107)
            .collect(Collectors.toSet()));

    public static final Map<Integer, ThingTypeUID> createMap() { // thing type denotes number of channels
        Map<Integer, ThingTypeUID> DEVICE_TYPES = new HashMap<>();
        DEVICE_TYPES.put(1, THING_TYPE_1);
        DEVICE_TYPES.put(2, THING_TYPE_2);
        DEVICE_TYPES.put(3, THING_TYPE_3);
        DEVICE_TYPES.put(4, THING_TYPE_4);
        DEVICE_TYPES.put(5, THING_TYPE_5);
        DEVICE_TYPES.put(6, THING_TYPE_6);
        DEVICE_TYPES.put(7, THING_TYPE_7);
        DEVICE_TYPES.put(8, THING_TYPE_8);
        DEVICE_TYPES.put(9, THING_TYPE_9);

        DEVICE_TYPES.put(15, THING_TYPE_15);

        DEVICE_TYPES.put(24, THING_TYPE_24);
        DEVICE_TYPES.put(27, THING_TYPE_27);
        DEVICE_TYPES.put(28, THING_TYPE_28);
        DEVICE_TYPES.put(29, THING_TYPE_29);
        DEVICE_TYPES.put(30, THING_TYPE_30);
        DEVICE_TYPES.put(31, THING_TYPE_31);

        DEVICE_TYPES.put(32, THING_TYPE_32);
        DEVICE_TYPES.put(66, THING_TYPE_66);
        DEVICE_TYPES.put(77, THING_TYPE_77);
        DEVICE_TYPES.put(77, THING_TYPE_77);
        DEVICE_TYPES.put(78, THING_TYPE_78);
        DEVICE_TYPES.put(81, THING_TYPE_81);
        DEVICE_TYPES.put(82, THING_TYPE_82);
        DEVICE_TYPES.put(83, THING_TYPE_83);
        DEVICE_TYPES.put(84, THING_TYPE_84);

        DEVICE_TYPES.put(107, THING_TYPE_107);

        return Collections.unmodifiableMap(DEVICE_TYPES);
    }

    public static final Map<Integer, ThingTypeUID> createSensorMap() { // thing type denotes number of channels
        Map<Integer, ThingTypeUID> SENSOR_TYPES = new HashMap<>();
        SENSOR_TYPES.put(4, THING_TYPE_RF1);
        SENSOR_TYPES.put(4, THING_TYPE_RF2);
        SENSOR_TYPES.put(4, THING_TYPE_RF3);
        SENSOR_TYPES.put(4, THING_TYPE_RF4);
        SENSOR_TYPES.put(6, THING_TYPE_RF6);

        return Collections.unmodifiableMap(SENSOR_TYPES);
    }

    public static final Map<Integer, ThingTypeUID> createZigbeeMap() { // thing type denotes number of channels
        Map<Integer, ThingTypeUID> ZIGBEE_TYPES = new HashMap<>();
        ZIGBEE_TYPES.put(1000, THING_TYPE_ZSWITCH1);
        ZIGBEE_TYPES.put(1009, THING_TYPE_ZSWITCH1);
        ZIGBEE_TYPES.put(1256, THING_TYPE_ZSWITCH1);
        ZIGBEE_TYPES.put(1257, THING_TYPE_ZLIGHT);
        ZIGBEE_TYPES.put(1770, THING_TYPE_ZTEMP);
        ZIGBEE_TYPES.put(2026, THING_TYPE_ZMOTION);
        ZIGBEE_TYPES.put(3026, THING_TYPE_ZCONTACT);
        ZIGBEE_TYPES.put(4026, THING_TYPE_ZWATER);
        ZIGBEE_TYPES.put(2256, THING_TYPE_ZSWITCH2);
        ZIGBEE_TYPES.put(3256, THING_TYPE_ZSWITCH3);
        ZIGBEE_TYPES.put(4256, THING_TYPE_ZSWITCH4);
        return Collections.unmodifiableMap(ZIGBEE_TYPES);
    }

    // To Do
    // public static final ThingTypeUID THING_TYPE_10 = new ThingTypeUID(BINDING_ID, "10"); // OSPF Please let me know
    // // model
    // public static final ThingTypeUID THING_TYPE_11 = new ThingTypeUID(BINDING_ID, "11"); // King Q4 Cover
    // public static final ThingTypeUID THING_TYPE_OSPF = new ThingTypeUID(BINDING_ID, "ospf");
    // public static final ThingTypeUID THING_TYPE_CURTAIN = new ThingTypeUID(BINDING_ID, "curtain");
    // public static final ThingTypeUID THING_TYPE_EWRE = new ThingTypeUID(BINDING_ID, "ewre");
    // public static final ThingTypeUID THING_TYPE_FIREPLACE = new ThingTypeUID(BINDING_ID, "fireplace");
    // public static final ThingTypeUID THING_TYPE_SWITCHCHANGE = new ThingTypeUID(BINDING_ID, "switchchange");
    // public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");
    // public static final ThingTypeUID THING_TYPE_COLDWARMLED = new ThingTypeUID(BINDING_ID, "coldwarmled");
    // public static final ThingTypeUID THING_TYPE_THREEGEARFAN = new ThingTypeUID(BINDING_ID, "threegearfan");
    // public static final ThingTypeUID THING_TYPE_SENSORSCENTRE = new ThingTypeUID(BINDING_ID, "sensorscentre");
    // public static final ThingTypeUID THING_TYPE_HUMIDIFIER = new ThingTypeUID(BINDING_ID, "humidifier");
    // public static final ThingTypeUID THING_TYPE_RGBBALLLIGHT = new ThingTypeUID(BINDING_ID, "rgbballlight");
    // public static final ThingTypeUID THING_TYPE_NESTTHERMOSTAT = new ThingTypeUID(BINDING_ID, "nestthermostat");
    // public static final ThingTypeUID THING_TYPE_GSMSOCKET = new ThingTypeUID(BINDING_ID, "gsmsocket");
    // public static final ThingTypeUID THING_TYPE_GSMSOCKET2 = new ThingTypeUID(BINDING_ID, "gsmsocket2");
    // public static final ThingTypeUID THING_TYPE_GSMSOCKET3 = new ThingTypeUID(BINDING_ID, "gsmsocket3");
    // public static final ThingTypeUID THING_TYPE_GSMSOCKET4 = new ThingTypeUID(BINDING_ID, "gsmsocket4");
    // public static final ThingTypeUID THING_TYPE_AROMATHERAPY = new ThingTypeUID(BINDING_ID, "aromatherapy");
    // public static final ThingTypeUID THING_TYPE_GSMUNLIMITSOCKET = new ThingTypeUID(BINDING_ID, "gsmunlimitsocket");
    // public static final ThingTypeUID THING_TYPE_LIGHTBELT = new ThingTypeUID(BINDING_ID, "lightbelt");
    // public static final ThingTypeUID THING_TYPE_FANLIGHT = new ThingTypeUID(BINDING_ID, "fanlight");
    // public static final ThingTypeUID THING_TYPE_EZVIZCAMERA = new ThingTypeUID(BINDING_ID, "ezvizcamera");
    // public static final ThingTypeUID THING_TYPE_SINGLECHANNELDIMMERSWITCH = new ThingTypeUID(BINDING_ID,
    // "singlechanneldimmerswitch");
    // public static final ThingTypeUID THING_TYPE_HOMEKITBRIDGE = new ThingTypeUID(BINDING_ID, "homekitbridge");
    // public static final ThingTypeUID THING_TYPE_FUJINOPS = new ThingTypeUID(BINDING_ID, "fujinops");
}
/*
 * deviceTypes.put(10, 0); // "OSPF" \\
 * deviceTypes.put(11, 1); // "CURTAIN" \\ King Q4 Cover
 * deviceTypes.put(12, 0); // "EW-RE" \\
 * deviceTypes.put(13, 0); // "FIREPLACE" \\
 * deviceTypes.put(14, 1); // "SWITCH_CHANGE" \\
 * deviceTypes.put(16, 0); // "COLD_WARM_LED" \\
 * deviceTypes.put(17, 0); // "THREE_GEAR_FAN" \\
 * deviceTypes.put(18, 0); // "SENSORS_CENTER" \\
 * deviceTypes.put(19, 0); // "HUMIDIFIER" \\
 * deviceTypes.put(22, 1); // "RGB_BALL_LIGHT" \\ B1, B1_R2
 * deviceTypes.put(23, 0); // "NEST_THERMOSTAT" \\
 * deviceTypes.put(24, 1); // "GSM_SOCKET" \\
 * deviceTypes.put(25, 0); // "AROMATHERAPY"); \\ Diffuser); Komeito 1515-X
 * deviceTypes.put(26, 0); // "RuiMiTeWenKongQi" \\
 * deviceTypes.put(27, 1); // "GSM_UNLIMIT_SOCKET" \\
 * deviceTypes.put(29, 2); // "GSM_SOCKET_2" \\
 * deviceTypes.put(30, 3); // "GSM_SOCKET_3" \\
 * deviceTypes.put(31, 4); // "GSM_SOCKET_4" \\
 * deviceTypes.put(33, 0); // "LIGHT_BELT"); \\
 * deviceTypes.put(34, 4); // "FAN_LIGHT" \\ iFan02); iFan
 * deviceTypes.put(35, 0); // "EZVIZ_CAMERA"); \\
 * deviceTypes.put(36, 1); // "SINGLE_CHANNEL_DIMMER_SWITCH" \\ KING-M4
 * deviceTypes.put(38, 0); // "HOME_KIT_BRIDGE"); \\
 * deviceTypes.put(40, 0); // "FUJIN_OPS" \\
 * deviceTypes.put(41, 4); // "CUN_YOU_DOOR" \\
 * deviceTypes.put(42, 0); // "SMART_BEDSIDE_AND_NEW_RGB_BALL_LIGHT" \\
 * deviceTypes.put(43, 0); // "?" \\
 * deviceTypes.put(44, 1); // "SNOFF_LIGHT" \\ D1
 * deviceTypes.put(45, 0); // "DOWN_CEILING_LIGHT" \\
 * deviceTypes.put(46, 0); // "AIR_CLEANER" \\
 * deviceTypes.put(49, 0); // "MACHINE_BED" \\
 * deviceTypes.put(51, 0); // "COLD_WARM_DESK_LIGHT" \\
 * deviceTypes.put(52, 0); // "DOUBLE_COLOR_DEMO_LIGHT" \\
 * deviceTypes.put(53, 0); // "ELECTRIC_FAN_WITH_LAMP" \\
 * deviceTypes.put(55, 0); // "SWEEPING_ROBOT" \\
 * deviceTypes.put(56, 0); // "RGB_BALL_LIGHT_4" \\
 * deviceTypes.put(57, 0); // "MONOCHROMATIC_BALL_LIGHT" \\
 * deviceTypes.put(59, 1); // "MUSIC_LIGHT_BELT" \\ L1
 * deviceTypes.put(60, 0); // "NEW_HUMIDIFIER" \\
 * deviceTypes.put(61, 0); // "KAI_WEI_ROUTER" \\
 * deviceTypes.put(62, 0); // "MEARICAMERA" \\
 * deviceTypes.put(64, 0); // "HeatingTable" \\
 * deviceTypes.put(65, 0); // "CustomCamera" \\ eWeLink camera app
 * deviceTypes.put(67, 0); // "RollingDoor" \\
 * deviceTypes.put(68, 0); // "KOOCHUWAH" \\ a whhaaaaat?
 * deviceTypes.put(69, 0); // "ATMOSPHERE_LAMP" \\
 * deviceTypes.put(76, 0); // "YI_GE_ER_LAMP" \\
 * deviceTypes.put(78, 4); // "SINGLE_SWITCH_MULTIPLE" \\ (1 switch device using data structure of four ,()
 * deviceTypes.put(79, 0); // "CHRISTMAS_LIGHT" \\
 * deviceTypes.put(80, 0); // "HANYUAN_AIR_CONDITION" \\
 * deviceTypes.put(81, 1); // "GSM_SOCKET_NO_FLOW" \\
 * deviceTypes.put(82, 2); // "GSM_SOCKET_2_NO_FLOW" \\
 * deviceTypes.put(83, 3); // "GSM_SOCKET_3_NO_FLOW" \\
 * deviceTypes.put(84, 4); // "GSM_SOCKET_4_NO_FLOW" \\
 * deviceTypes.put(86, 0); // "CLEAR_BOOT" \\
 * deviceTypes.put(87, 0); // "EWELINK_IOT_CAMERA" \\ GK-200MP2B
 * deviceTypes.put(88, 0); // "YK_INFRARED" \\
 * deviceTypes.put(89, 0); // "SMART_OPEN_MACHINE" \\
 * deviceTypes.put(90, 0); // "GSM_RFBridge" \\
 * deviceTypes.put(91, 0); // "ROLLING_DOOR_91" \\
 * deviceTypes.put(93, 0); // "HTHD_AIR_CLEANER" \\
 * deviceTypes.put(94, 0); // "YIAN_ELECTRIC_PROTECT" \\
 * deviceTypes.put(98, 0); // "DOORBELL_RFBRIDGE" \\
 * deviceTypes.put(102, 1); // "DOOR_MAGNETIC" \\ OPL-DMA); DW2
 * deviceTypes.put(103, 1); // "WOTEWODE_TEM_LIGHT" \\ B02-F
 * deviceTypes.put(104, 1); // "WOTEWODE_RGB_TEM_LIGHT" \\
 * deviceTypes.put(107, 0); // "GSM_SOCKET_NO_FLOW" \\
 * deviceTypes.put(109, 0); // "YK_INFRARED_2" \\
 * deviceTypes.put(1000, 1); // "ZIGBEE_WIRELESS_SWITCH" \\
 * deviceTypes.put(1001, 0); // "BLADELESS_FAN" \\
 * deviceTypes.put(1002, 0); // "NEW_HUMIDIFIER" \\
 * deviceTypes.put(1003, 0); // "WARM_AIR_BLOWER" \\
 * deviceTypes.put(1009, 1); // "" \\ Some sort of single switch device
 * deviceTypes.put(1256, 1); // "ZIGBEE_SINGLE_SWITCH" \\
 * deviceTypes.put(1770, 1); // "ZIGBEE_TEMPERATURE_SENSOR" \\
 * deviceTypes.put(2256, 2); // "ZIGBEE_SWITCH_2" \\
 * deviceTypes.put(3026, 1); // "ZIGBEE_DOOR_AND_WINDOW_SENSOR" \\
 * deviceTypes.put(3256, 3); // "ZIGBEE_SWITCH_3" \\
 * deviceTypes.put(4026, 1); // "ZIGBEE_WATER_SENSOR" \\
 * deviceTypes.put(4256, 4); // "ZIGBEE_SWITCH_4" \\
 */
