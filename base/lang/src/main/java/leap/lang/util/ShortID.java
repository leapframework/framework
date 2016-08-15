package leap.lang.util;

import leap.lang.time.StopWatch;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * /**
 * Short id generator. Url-friendly. Non-predictable. Cluster-compatible.
 *
 * <p/>
 * Inspired from <a href="https://github.com/dylang/shortid">shortid</a>
 */
public class ShortID {

    private static Random random = new SecureRandom();

    private static String alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-";

    // Ignore all milliseconds before a certain time to reduce the size of the date entropy without sacrificing uniqueness.
    // This number should be updated every year or so to keep the generated id short.
    // To regenerate `new Date() - 0` and bump the version. Always bump the version!
    private static final long REDUCE_TIME = 1403265799803L;

    // don't change unless we change the algos or REDUCE_TIME
    // must be an integer and less than 16
    private static int version = 6;

    // if you are using cluster or multiple servers use this to make each instance
    // has a unique value for worker
    // Note: I don't know if this is automatically set when using third
    // party cluster solutions such as pm2.
    private static int clusterWorkerId = 0;

    // Counter is used when shortid is called multiple times in one second.
    private static volatile int counter;

    // Remember the last time shortid was called in case counter is needed.
    private static volatile long previousSeconds;

    /**
     * Generate unique id and returns it.
     */
    public static String generate() {
        String str = "";

        long seconds = (long)Math.floor((System.currentTimeMillis() - REDUCE_TIME) * 0.001);

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

    private static String encode(int number) {
        int loopCounter = 0;
        boolean done = false;

        String str = "";

        int index;
        while (!done) {
            index = ( (number >> (4 * loopCounter)) & 0x0f ) | randomByte();
            str = str + lookup( index );
            done = number < (Math.pow(16, loopCounter + 1 ) );
            loopCounter++;
        }
        return str;
    }

    private static int randomByte() {
        byte[] bytes = new byte[1];
        random.nextBytes(bytes);
        return bytes[0] & 0x30;
    }

    private static char lookup(int index) {
        char[] alphabetShuffled = getShuffled();
        return alphabetShuffled[index];
    }

    private static char[] shuffled;

    private static char[] getShuffled() {
        if (null != shuffled) {
            return shuffled;
        }
        shuffled = shuffle();
        return shuffled;
    }

    private static char[] shuffle() {
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

    public static void main(String[] args) {
        StopWatch sw = StopWatch.startNew();

        int num = 1000000;
        int maxlen = 0;
        Set<String> ids = new HashSet<>(num);
        for(int i=0;i<num;i++) {
            String s = generate();
            //maxlen = Math.max(maxlen, s.length());
        }

        System.out.println("max len: " + maxlen);
        System.out.println("size:" + ids.size());
        System.out.println("time: " + sw.getElapsedMilliseconds());
    }
}
