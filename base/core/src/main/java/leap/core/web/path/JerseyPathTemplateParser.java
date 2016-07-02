package leap.core.web.path;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import leap.lang.Charsets;
import leap.lang.annotation.Internal;
import leap.lang.codec.Hex;

//This class is a modified copy from <a href="http://jersey.java.net">jersey</a>.

/**
 * Internal parser of this PathTemplate.
 * @see #PathTemplate(String)
 */
@Internal
final class JerseyPathTemplateParser extends JerseyUriTemplateParser {
	
    private static final String[] UNRESERVED = {"0-9", "A-Z", "a-z", "-", ".", "_", "~"};
    private static final String[] SUB_DELIMS = {"!", "$", "&", "'", "(", ")", "*", "+", ",", ";", "="};
    private static final boolean[] PATH_ENCODING_TABLE = initPathEncodingTable();

    private static boolean[] initPathEncodingTable(){
    	List<String> l = new ArrayList<String>();
    	
    	l.addAll(Arrays.asList(UNRESERVED));
    	l.addAll(Arrays.asList(SUB_DELIMS));
    	l.add(":");
    	l.add("@");
    	l.add("/");
    	
    	return initEncodingTable(l);
    }
    
    private static boolean[] initEncodingTable(List<String> allowed) {
        boolean[] table = new boolean[0x80];
        for (String range : allowed) {
            if (range.length() == 1) {
                table[range.charAt(0)] = true;
            } else if (range.length() == 3 && range.charAt(1) == '-') {
                for (int i = range.charAt(0); i <= range.charAt(2); i++) {
                    table[i] = true;
                }
            }
        }

        return table;
    }


    /**
     * Public constructor.
     *
     * @param path the URI path template
     */
    public JerseyPathTemplateParser(final String path) {
        super(path);
    }

    @Override
    
    /**
     * Encode literal characters of a template.
     *
     * @param characters the literal characters
     * @return the encoded literal characters.
     */
    protected String encodeLiteralCharacters(final String s) {
        StringBuilder sb = null;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c < 0x80 && PATH_ENCODING_TABLE[c]) {
                if (sb != null) {
                    sb.append(c);
                }
            } else {

                if (sb == null) {
                    sb = new StringBuilder();
                    sb.append(s.substring(0, i));
                }

                if (c < 0x80) {
                    appendPercentEncodedOctet(sb, c);
                } else {
                    appendUTF8EncodedCharacter(sb, c);
                }
            }
        }

        return (sb == null) ? s : sb.toString();
    }
    
    private static void appendPercentEncodedOctet(StringBuilder sb, int b) {
        sb.append('%');
        sb.append(Hex.HEX_DIGITS[b >> 4]);
        sb.append(Hex.HEX_DIGITS[b & 0x0F]);
    }

    private static void appendUTF8EncodedCharacter(StringBuilder sb, char c) {
        final ByteBuffer bb = Charsets.UTF_8.encode("" + c);

        while (bb.hasRemaining()) {
            appendPercentEncodedOctet(sb, bb.get() & 0xFF);
        }
    }
}