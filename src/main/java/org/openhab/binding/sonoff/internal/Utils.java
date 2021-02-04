package org.openhab.binding.sonoff.internal;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonoff.internal.helpers.DtoHelper;

import com.google.gson.JsonObject;

public class Utils {

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

    public static @Nullable JsonObject encrypt(String params, String deviceKey, String deviceId, String seq) {
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
            newPayload.addProperty("seq", seq);
            newPayload.addProperty("sequence", DtoHelper.getSequence() + "");
            newPayload.addProperty("deviceid", deviceId);
            newPayload.addProperty("selfApikey", "123");
            newPayload.addProperty("iv", ivEncoded);
            newPayload.addProperty("encrypt", true);
            newPayload.addProperty("data", payloadEncoded);
            return newPayload;
        } catch (Exception e) {
            // logger.warn("Encryption Exception: {}", e);
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
            // logger.warn("Decryption Exception: {}", e);
            return null;
        }
    }
}
