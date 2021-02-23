package org.openhab.binding.sonoff.internal.helpers;

import java.security.SecureRandom;
import java.util.Date;

public class DtoHelper {

    public static final String appid = "oeVkj2lYFGnJu5XUtWisfW4utiN4u9Mq";
    public static final String appSecret = "6Nz4n0xA8s8qdxQf2GqurZj2Fs55FUvM";
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static SecureRandom rnd = new SecureRandom();
    public static final Integer version = 8;

    static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public static String getNonce() {
        return randomString(8);
    }

    public static synchronized Long getSequence() {
        return new Date().getTime();
    }

    public static synchronized Long getTs() {
        return new Date().getTime();
    }
}
