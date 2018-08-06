package com.btctaxi.gate.vendor;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * google authenticator生成器
 */
public class Totp {
    private static final int KEY_N = 16;

    /**
     * 生成随机密钥
     *
     * @return 密钥
     */
    public static String generateKey() {
        SecureRandom random = new SecureRandom();
        byte[] bs = new byte[KEY_N];
        for (int i = 0; i < KEY_N; i++) {
            bs[i] = (byte) (random.nextInt(26) + 'A');
        }
        return new String(bs);
    }

    /**
     * 根据密钥和当前时间（秒）生成验证码
     *
     * @param key
     * @param now
     * @return
     */
    public static String generateCode(String key, long now) {
        Base32 base = new Base32();
        byte[] bs = base.decode(key);
        String hexKey = Hex.encodeHexString(bs);
        String hexTime = Long.toHexString(now / 30);
        return generateTotp(hexKey, hexTime);
    }

    private static byte[] hmacSha1(String crypto, byte[] key, byte[] text) {
        try {
            Mac hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey = new SecretKeySpec(key, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }

    private static byte[] bytes(String hex) {
        byte[] bs = new BigInteger("10" + hex, 16).toByteArray();
        byte[] ret = new byte[bs.length - 1];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = bs[i + 1];
        }
        return ret;
    }

    private static final int[] DIGITS_POWER = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};

    private static String generateTotp(String key, String time) {
        StringBuilder sb = new StringBuilder();
        int len = 16 - time.length();
        for (int i = 0; i < len; i++) {
            sb.append("0");
        }
        sb.append(time);
        time = sb.toString();
        byte[] msg = bytes(time);
        byte[] k = bytes(key);
        byte[] hash = hmacSha1("HmacSHA1", k, msg);
        int offset = hash[hash.length - 1] & 0xf;
        int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16) | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
        int otp = binary % DIGITS_POWER[6];
        String result = Integer.toString(otp);
        sb = new StringBuilder();
        len = 6 - result.length();
        for (int i = 0; i < len; i++) {
            sb.append("0");
        }
        sb.append(result);
        return sb.toString();
    }
}
