package leap.lang.util;

import leap.lang.Buildable;

import java.security.SecureRandom;
import java.util.Random;

/**
 * /**
 * Short id generator. Url-friendly. Non-predictable. Cluster-compatible.
 *
 * <p/>
 * Inspired from <a href="https://github.com/dylang/shortid">shortid</a>
 */
public class ShortID {

    private static final ShortID INSTANCE = new ShortID.Builder().build();

    public static final String DEFAULT_ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-";

    public static final String DOLLARAT_ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$@";

    private static final long DEFAULT_REDUCE_TIME = 1403265799803L;

    private static final int DEFAULT_VERSION = 6;

    /**
     * Generate unique id and returns it.
     */
    public static String randomID() {
        return INSTANCE.generate();
    }

    private final Random random;

    // Ignore all milliseconds before a certain time to reduce the size of the date entropy without sacrificing uniqueness.
    // This number should be updated every year or so to keep the generated id short.
    // To regenerate `new Date() - 0` and bump the version. Always bump the version!
    private final long reduceTime;

    // don't change unless we change the algos or REDUCE_TIME
    // must be an integer and less than 16
    private final int version;

    // if you are using cluster or multiple servers use this to make each instance
    // has a unique value for worker
    // Note: I don't know if this is automatically set when using third
    // party cluster solutions such as pm2.
    private final int clusterWorkerId;

    // Counter is used when shortid is called multiple times in one second.
    private volatile int counter;

    // Remember the last time shortid was called in case counter is needed.
    private volatile long previousSeconds;

    private final char[] shuffled;

    private ShortID(Random random, String alphabet, long reduceTime, int version, int clusterWorkerId) {
        this.random = random;
        this.shuffled = shuffle(alphabet);
        this.reduceTime = reduceTime;
        this.version = version;
        this.clusterWorkerId = clusterWorkerId;
    }

    /**
     * Generate unique id and returns it.
     */
    public String generate() {
        String str = "";

        long seconds = (long)Math.floor((System.currentTimeMillis() - reduceTime) * 0.001);

        if (seconds == previousSeconds) {
            counter++;
        } else {
            counter = 0;
            previousSeconds = seconds;
        }

        str = str + encode(version);
        str = str + encode(clusterWorkerId);

        if (counter > 0) {
            str = str + encode(counter);
        }

        str = str + encode((int)seconds);

        return str;
    }

    private String encode(int number) {
        int loopCounter = 0;
        boolean done = false;

        String str = "";

        int index;
        while (!done) {
            index = ( (number >> (4 * loopCounter)) & 0x0f ) | randomByte();
            str = str + shuffled[index];
            done = number < (Math.pow(16, loopCounter + 1 ) );
            loopCounter++;
        }
        return str;
    }

    private int randomByte() {
        byte[] bytes = new byte[1];
        random.nextBytes(bytes);
        return bytes[0] & 0x30;
    }

    private char[] shuffle(String alphabet) {
        StringBuilder source = new StringBuilder(alphabet);
        StringBuilder target = new StringBuilder(source.length());

        double r;
        int charIndex;

        while (source.length() > 0) {
            r = random.nextDouble();
            charIndex = (int)Math.floor(r * source.length());

            target.append(source.charAt(charIndex));
            source.deleteCharAt(charIndex);
        }

        return target.toString().toCharArray();
    }

    public static final class Builder implements Buildable<ShortID> {

        private Random  random;
        private String  alphabet;
        private Long    reduceTime;
        private Integer version;
        private Integer clusterWorkerId;

        public Random getRandom() {
            return random;
        }

        public void setRandom(Random random) {
            this.random = random;
        }

        public String getAlphabet() {
            return alphabet;
        }

        public void setAlphabet(String alphabet) {
            this.alphabet = alphabet;
        }

        public Long getReduceTime() {
            return reduceTime;
        }

        public void setReduceTime(Long reduceTime) {
            this.reduceTime = reduceTime;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        public Integer getClusterWorkerId() {
            return clusterWorkerId;
        }

        public void setClusterWorkerId(Integer clusterWorkerId) {
            this.clusterWorkerId = clusterWorkerId;
        }

        @Override
        public ShortID build() {
            if(null == random) {
                random = new SecureRandom();
            }

            if(null == alphabet) {
                alphabet = DEFAULT_ALPHABET;
            }else {
                if(alphabet.length() != DEFAULT_ALPHABET.length()) {
                   throw new IllegalStateException("The alphabet's length must be " + DEFAULT_ALPHABET.length());
                }
            }

            if(null == reduceTime) {
                reduceTime = DEFAULT_REDUCE_TIME;
            }

            if(null == version) {
                version = DEFAULT_VERSION;
            }else  {
                if(version < 1 || version > 16) {
                    throw new IllegalStateException("The version must between 1 and 16");
                }
            }

            if(null == clusterWorkerId) {
                clusterWorkerId = 0;
            }

            return new ShortID(random, alphabet, reduceTime, version, clusterWorkerId);
        }
    }
}
