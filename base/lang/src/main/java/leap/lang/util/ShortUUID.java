package leap.lang.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;

/**
 * A short, unambiguous and URL-safe UUID
 *
 * <p/>
 * Inspired by <a href="https://github.com/hsingh/java-shortuuid">shortuuid</a> under MIT license.
 */
public class ShortUUID {
    private static final char[] ALPHABET     = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final int    ALPHABET_LEN = ALPHABET.length;

    /**
     * Generates a shorter uuid for {@link UUID#randomUUID()}.
     */
    public static String randomUUID() {
        return encode(UUID.randomUUID().toString());
    }

    /**
     * Encodes the long uuid from {@link java.util.UUID} to shorter value.
     */
    public static String encode(String longUUID) {
        String uuidStr = longUUID.toString().replaceAll("-", "");

        Double factor = Math.log(25d) / Math.log(ALPHABET_LEN);
        Double length = Math.ceil(factor * 16);

        BigInteger number = new BigInteger(uuidStr, 16);

        return encode(number, ALPHABET, length.intValue());
    }

    /**
     * Encodes the shorter uuid to long uuid
     */
    public static String decode(String shortUUID) {
        return decode(shortUUID.toCharArray(), ALPHABET);
    }

    private static String encode(final BigInteger bigInt, final char[] alphabet, final int padToLen) {
        BigInteger value = new BigInteger(bigInt.toString());
        BigInteger alphaSize = BigInteger.valueOf(ALPHABET_LEN);
        StringBuilder shortUuid = new StringBuilder();

        while (value.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] fracAndRemainder = value.divideAndRemainder(alphaSize);
            shortUuid.append(alphabet[fracAndRemainder[1].intValue()]);
            value = fracAndRemainder[0];
        }

        if (padToLen > 0) {
            int padding = Math.max(padToLen - shortUuid.length(), 0);
            for (int i = 0; i < padding; i++)
                shortUuid.append(alphabet[0]);
        }

        return shortUuid.toString();
    }

    private static String decode(final char[] encoded, final char[] alphabet) {
        BigInteger sum = BigInteger.ZERO;
        BigInteger alphaSize = BigInteger.valueOf(ALPHABET_LEN);
        int charLen = encoded.length;

        for (int i = 0; i < charLen; i++) {
            sum = sum.add(alphaSize.pow(i).multiply(BigInteger.valueOf(
                    Arrays.binarySearch(alphabet, encoded[i]))));
        }

        String str = sum.toString(16);

        // Pad the most significant bit (MSG) with 0 (zero) if the string is too short.
        if (str.length() < 32) {
            str = String.format("%32s", str).replace(' ', '0');
        }

        StringBuilder sb = new StringBuilder()
                .append(str.substring(0, 8))
                .append("-")
                .append(str.substring(8, 12))
                .append("-")
                .append(str.substring(12, 16))
                .append("-")
                .append(str.substring(16, 20))
                .append("-")
                .append(str.substring(20, 32));

        return sb.toString();
    }
}
