package leap.core.config;

import leap.lang.Out;
import leap.lang.Strings;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

public class JasyptAppPropertyProcessor implements AppPropertyProcessor {

    private static final String ENC_PREFIX = "ENC(";
    private static final String ENC_SUFFIX = ")";
    private static final String PASSWORD_ENV = "jasypt.encryptor.password";

    private final StandardPBEStringEncryptor encryptor;

    public JasyptAppPropertyProcessor() {
        String password = System.getenv(PASSWORD_ENV);
        if (!Strings.isEmpty(password)) {
            encryptor = new StandardPBEStringEncryptor();
            encryptor.setPassword(password);
        } else {
            encryptor = null;
        }
    }

    @Override
    public boolean process(String name, String value, Out<String> newValue) {
        if (null == encryptor) {
            return false;
        }
        if (null == value || !value.startsWith(ENC_PREFIX) || !value.endsWith(ENC_SUFFIX)) {
            return false;
        }
        String ciphertext = value.substring(ENC_PREFIX.length(), value.length() - ENC_SUFFIX.length());
        if (Strings.isEmpty(ciphertext)) {
            newValue.accept(ciphertext);
        } else {
            try {
                newValue.accept(encryptor.decrypt(ciphertext));
            } catch (EncryptionOperationNotPossibleException ex) {
                ex.printStackTrace();
                throw new IllegalStateException("Decryption error property: " + name);
            }
        }
        return true;
    }

}