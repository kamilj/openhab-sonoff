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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sonoff.internal.Constants;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.dto.api.ThingList;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The {@link Utils} contains uitilities that are used accross the binding
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class Utils {

    private static final String HMAC = "HmacSHA256";
    private static final String ENCRYPTION = "AES/CBC/PKCS5Padding";
    private static final String KEYALG = "AES";
    private static final String DIGESTALG = "MD5";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public static String getAuthMac(String data)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = null;
        byte[] byteKey = DtoHelper.APPSECRET.getBytes(CHARSET);
        mac = Mac.getInstance(HMAC);
        SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC);
        mac.init(keySpec);
        byte[] macData = mac.doFinal(data.getBytes(CHARSET));
        return Base64.getEncoder().encodeToString(macData);
    }

    public static String encrypt(String params, String deviceKey, String deviceId, Long sequence) {
        try {
            byte[] keyBytes = deviceKey.getBytes(CHARSET);
            byte[] byteToEncrypt = params.getBytes(CHARSET);
            MessageDigest digest = MessageDigest.getInstance(DIGESTALG);
            digest.update(keyBytes);
            byte[] key = digest.digest();
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, KEYALG);
            Cipher cipher = Cipher.getInstance(ENCRYPTION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            AlgorithmParameters p = cipher.getParameters();
            byte[] iv = p.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] ciphertext = cipher.doFinal(byteToEncrypt);
            String ivEncoded = new String(Base64.getEncoder().encode(iv), CHARSET);
            String payloadEncoded = new String(Base64.getEncoder().encode(ciphertext), CHARSET);
            JsonObject newPayload = new JsonObject();
            newPayload.addProperty("sequence", sequence + "");
            newPayload.addProperty("deviceid", deviceId);
            newPayload.addProperty("selfApikey", "123");
            newPayload.addProperty("iv", ivEncoded);
            newPayload.addProperty("encrypt", true);
            newPayload.addProperty("data", payloadEncoded);

            return newPayload.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String decrypt(JsonObject payload, String deviceKey) {
        try {
            MessageDigest md = MessageDigest.getInstance(DIGESTALG);
            byte[] bytesOfMessage = deviceKey.getBytes(CHARSET);
            byte[] keyBytes = md.digest(bytesOfMessage);
            SecretKeySpec key = new SecretKeySpec(keyBytes, KEYALG);
            String data1 = payload.get("data1") != null ? payload.get("data1").getAsString() : "";
            String data2 = payload.get("data2") != null ? payload.get("data2").getAsString() : "";
            String data3 = payload.get("data3") != null ? payload.get("data3").getAsString() : "";
            String data4 = payload.get("data4") != null ? payload.get("data4").getAsString() : "";
            String encoded = data1 + data2 + data3 + data4;
            Cipher cipher = Cipher.getInstance(ENCRYPTION);
            byte[] ciphertext = Base64.getDecoder().decode(encoded);
            String ivString = payload.get("iv").getAsString();
            byte[] ivBytes = Base64.getDecoder().decode(ivString);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decodedBytes = cipher.doFinal(ciphertext);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
            return decoded;
        } catch (Exception e) {
            return "";
        }
    }

    public static Integer createFiles(List<ThingList> things, Gson gson) throws IOException {
        File dir = new File(Constants.SAVEFOLDER);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Integer filesCreated = 0;
        for (int i = 0; i < things.size(); i++) {
            Device device = things.get(i).getItemData();
            File file = new File(Constants.SAVEFOLDER + device.getDeviceid() + ".txt");
            if (!file.exists()) {
                file.createNewFile();
                filesCreated++;
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(gson.toJson(device));
            fileWriter.close();
        }
        return filesCreated;
    }

    public static void createCacheFile(String deviceid, String thing) throws IOException {
        File dir = new File(Constants.SAVEFOLDER);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(Constants.SAVEFOLDER + deviceid + ".txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(thing);
        fileWriter.close();
    }

    public static String getFileContent(File file) throws FileNotFoundException {
        String deviceJson = "";
        Scanner reader = new Scanner(file);
        while (reader.hasNextLine()) {
            deviceJson = reader.nextLine();
        }
        reader.close();
        return deviceJson;
    }

    public static String getDeviceFile(String deviceid) throws FileNotFoundException {
        File file = new File(Constants.SAVEFOLDER + deviceid + ".txt");
        if (file.exists() && !file.isDirectory()) {
            return getFileContent(file);
        } else {
            return "";
        }
    }

    public static List<String> getDeviceFiles() throws IOException {
        List<File> files = Files.walk(Paths.get(Constants.SAVEFOLDER)).filter(Files::isRegularFile).map(Path::toFile)
                .collect(Collectors.toList());
        if (files.isEmpty() || files.size() == 0) {
            return new ArrayList<String>();
        } else {
            List<String> devices = new ArrayList<String>();
            for (int l = 0; l < files.size(); l++) {
                String deviceJson = Utils.getFileContent(files.get(l));
                devices.add(deviceJson);
            }
            return devices;
        }
    }
}
