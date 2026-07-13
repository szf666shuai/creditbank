package com.creditbank.platform.integration.trtc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.Deflater;

/**
 * Tencent TRTC UserSig generator (TLS Sig API v2).
 */
public class TlsSigApiV2 {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final long sdkAppId;
    private final String key;

    public TlsSigApiV2(long sdkAppId, String key) {
        this.sdkAppId = sdkAppId;
        this.key = key;
    }

    public String genUserSig(String userId, long expire) {
        return genUserSig(userId, expire, null);
    }

    private String genUserSig(String userId, long expire, byte[] userBuf) {
        long currTime = System.currentTimeMillis() / 1000;

        ObjectNode sigDoc = OBJECT_MAPPER.createObjectNode();
        sigDoc.put("TLS.ver", "2.0");
        sigDoc.put("TLS.identifier", userId);
        sigDoc.put("TLS.sdkappid", sdkAppId);
        sigDoc.put("TLS.expire", expire);
        sigDoc.put("TLS.time", currTime);

        String base64UserBuf = null;
        if (userBuf != null) {
            base64UserBuf = Base64.getEncoder().encodeToString(userBuf).replaceAll("\\s*", "");
            sigDoc.put("TLS.userbuf", base64UserBuf);
        }

        String sig = hmacSha256(userId, currTime, expire, base64UserBuf);
        if (sig.isEmpty()) {
            return "";
        }
        sigDoc.put("TLS.sig", sig);

        Deflater compressor = new Deflater();
        compressor.setInput(sigDoc.toString().getBytes(StandardCharsets.UTF_8));
        compressor.finish();
        byte[] compressedBytes = new byte[2048];
        int compressedBytesLength = compressor.deflate(compressedBytes);
        compressor.end();
        return new String(Base64Url.base64EncodeUrl(Arrays.copyOfRange(compressedBytes, 0, compressedBytesLength)))
                .replaceAll("\\s*", "");
    }

    private String hmacSha256(String identifier, long currTime, long expire, String base64UserBuf) {
        String contentToBeSigned = "TLS.identifier:" + identifier + "\n"
                + "TLS.sdkappid:" + sdkAppId + "\n"
                + "TLS.time:" + currTime + "\n"
                + "TLS.expire:" + expire + "\n";
        if (base64UserBuf != null) {
            contentToBeSigned += "TLS.userbuf:" + base64UserBuf + "\n";
        }
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(keySpec);
            byte[] byteSig = hmac.doFinal(contentToBeSigned.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(byteSig).replaceAll("\\s*", "");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return "";
        }
    }

    private static final class Base64Url {
        private Base64Url() {
        }

        static byte[] base64EncodeUrl(byte[] input) {
            byte[] base64 = Base64.getEncoder().encode(input);
            for (int i = 0; i < base64.length; ++i) {
                switch (base64[i]) {
                    case '+' -> base64[i] = '*';
                    case '/' -> base64[i] = '-';
                    case '=' -> base64[i] = '_';
                    default -> {
                    }
                }
            }
            return base64;
        }
    }
}
