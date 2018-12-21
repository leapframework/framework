package leap.lang.security;

import leap.lang.Strings;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * @author kael.
 */
public class DES {
    private static final String DES = "DES";
    public static String encryptToBase64String(String src, String key) throws Throwable {
        return Base64.getEncoder().encodeToString(encrypt(Strings.getBytesUtf8(src),key));
    }
    /**
     * 加密
     */
    public static byte[] encrypt(byte[] data, String sKey) throws Throwable {
        SecretKey secretKey = decodeToSecretKey(sKey);
        Cipher cipher = Cipher.getInstance(DES);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }
    public static String decryptFromBase64(String src, String key) throws Throwable {
        return Strings.newStringUtf8(decrypt(Base64.getDecoder().decode(src),key));
    }
    /**
     * 解密
     */
    public static byte[] decrypt(byte[] src, String sKey) throws Throwable {
        SecretKey secretKey = decodeToSecretKey(sKey);
        Cipher cipher = Cipher.getInstance(DES);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(src);
    }
    
    private static SecretKey decodeToSecretKey(String sKey) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] key = Base64.getEncoder().encode(Strings.getBytesUtf8(sKey));
        DESKeySpec desKey = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        return keyFactory.generateSecret(desKey);
    }
}
