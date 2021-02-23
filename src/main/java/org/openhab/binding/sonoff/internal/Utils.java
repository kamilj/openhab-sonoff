package org.openhab.binding.sonoff.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonoff.internal.dto.api.Device;
import org.openhab.binding.sonoff.internal.dto.api.ThingList;
import org.openhab.binding.sonoff.internal.helpers.DtoHelper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Utils {

    private static Gson gson = new Gson();

    public static String getAuthMac(String data)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256_HMAC = null;
        byte[] byteKey = DtoHelper.appSecret.getBytes("UTF-8");
        final String HMAC_SHA256 = "HmacSHA256";
        sha256_HMAC = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA256);
        sha256_HMAC.init(keySpec);
        byte[] mac_data = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(mac_data);
    }

    public static String encrypt(String params, String deviceKey, String deviceId, Long sequence) {
        try {
            byte[] keyBytes = deviceKey.getBytes(StandardCharsets.UTF_8);
            byte[] byteToEncrypt = params.getBytes(StandardCharsets.UTF_8);
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(keyBytes);
            byte[] key = digest.digest();
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            AlgorithmParameters p = cipher.getParameters();
            byte[] iv = p.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] ciphertext = cipher.doFinal(byteToEncrypt);
            String ivEncoded = new String(Base64.getEncoder().encode(iv), StandardCharsets.UTF_8);
            String payloadEncoded = new String(Base64.getEncoder().encode(ciphertext), StandardCharsets.UTF_8);
            JsonObject newPayload = new JsonObject();
            newPayload.addProperty("sequence", sequence + "");
            newPayload.addProperty("deviceid", deviceId);
            newPayload.addProperty("selfApikey", "123");
            newPayload.addProperty("iv", ivEncoded);
            newPayload.addProperty("encrypt", true);
            newPayload.addProperty("data", payloadEncoded);

            return gson.toJson(newPayload);
        } catch (Exception e) {
            return null;
        }
    }

    public static @Nullable String decrypt(JsonObject payload, String deviceKey) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytesOfMessage = deviceKey.getBytes("UTF-8");
            byte[] keyBytes = md.digest(bytesOfMessage);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            String data1 = payload.get("data1") != null ? payload.get("data1").getAsString() : "";
            String data2 = payload.get("data2") != null ? payload.get("data2").getAsString() : "";
            String data3 = payload.get("data3") != null ? payload.get("data3").getAsString() : "";
            String data4 = payload.get("data4") != null ? payload.get("data4").getAsString() : "";
            String encoded = data1 + data2 + data3 + data4;
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] ciphertext = Base64.getDecoder().decode(encoded);
            String ivString = payload.get("iv").getAsString();
            byte[] ivBytes = Base64.getDecoder().decode(ivString);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decodedBytes = cipher.doFinal(ciphertext);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
            return decoded;
        } catch (Exception e) {
            return null;
        }
    }

    public static Boolean createFiles(List<ThingList> things, Gson gson) throws IOException {
        File dir = new File(Constants.saveFolder);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        for (int i = 0; i < things.size(); i++) {
            Device device = things.get(i).getItemData();
            File file = new File(Constants.saveFolder + device.getDeviceid() + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(gson.toJson(device));
            fileWriter.close();
        }
        return true;
    }

    public static @Nullable List<File> getFiles() throws IOException {
        List<File> files = Files.list(Paths.get(Constants.saveFolder)).filter(Files::isRegularFile).map(Path::toFile)
                .collect(Collectors.toList());
        return files;
    }

    public static @Nullable String getFileContent(File file) throws FileNotFoundException {
        String deviceJson = "";
        Scanner reader = new Scanner(file);
        while (reader.hasNextLine()) {
            deviceJson = reader.nextLine();
        }
        reader.close();
        return deviceJson;
    }

    public static String getDevice(String deviceid) throws FileNotFoundException {
        File file = new File(Constants.saveFolder + deviceid + ".txt");
        if (file.exists() && !file.isDirectory()) {
            return getFileContent(file);
        } else {
            return null;
        }
    }

    public static List<String> getDevices() throws IOException {
        List<File> files = Utils.getFiles();
        if (files.isEmpty() || files.size() == 0 || files == null) {
            return null;
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
