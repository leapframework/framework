package leap.lang.security;

import leap.lang.Charsets;
import leap.lang.Strings;
import leap.lang.codec.Base64;

import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

/**
 * @author kael.
 */
public class DESede {
    
    private final String cipherType;

    private DESede(String cipherType) {
        this.cipherType = cipherType;
    }

    public static DESede create(String cipherType, Provider ... providers){
        if(null != providers && providers.length > 0){
            for(Provider provider : providers) {
                Security.addProvider(provider);
            }
        }
        return new DESede(cipherType);
    }
    
    public static DESede create(){
        return new DESede("DESede/ECB/PKCS5Padding");
    }
    
    public String encryptToBase64(String key, String iv, String plaintext) throws IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        return Base64.encode(encrypt(key, iv, plaintext));
    }

    public String decryptToBase64(String key, String iv, byte[] encrypted) throws InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        return Base64.encode(decrypt(key,iv, encrypted));
    }
    
    public byte[] encrypt(String key, String iv, String plaintext) throws InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = initCipher(key, iv, Cipher.ENCRYPT_MODE);
        return cipher.doFinal(plaintext.getBytes(Charsets.UTF_8));
    }

    public byte[] decrypt(String key, String iv, byte[] encrypted) throws InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = initCipher(key, iv, Cipher.DECRYPT_MODE);
        return cipher.doFinal(encrypted);
    }
    
    protected Cipher initCipher(String key, String iv, int mode) throws InvalidKeySpecException, InvalidKeyException {
        try {
            DESedeKeySpec dks = new DESedeKeySpec(key.getBytes(Charsets.UTF_8));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
            SecretKey securekey = keyFactory.generateSecret(dks);
            Cipher cipher = Cipher.getInstance(cipherType);
            if(Strings.isNotEmpty(iv)){
                SecureRandom sr = new SecureRandom();
                IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(Charsets.UTF_8));
                cipher.init(mode, securekey, ivSpec, sr);
            }else {
                cipher.init(mode, securekey);
            }
            return cipher;
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
