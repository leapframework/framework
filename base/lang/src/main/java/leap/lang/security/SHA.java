package leap.lang.security;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author kael.
 */
public class SHA {
    private static final String SHA256 = "SHA-256";
    
    public static String sha256(String source) {
        MessageDigest messageDigest;
        String encoded;
        try {
            messageDigest = MessageDigest.getInstance(SHA256);
            messageDigest.update(source.getBytes(Charset.forName("UTF-8")));
            encoded = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return encoded;
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String temp;
        for(int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                sb.append("0");
            }
            sb.append(temp);
        }
        return sb.toString();
    }
}
