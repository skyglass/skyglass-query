package skyglass.data.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * @author jwa
 */
public class CryptStringUtil {

    //========================================================================//
    // STATIC
    //========================================================================//

    // do not use Log4j here because this class goes into the Codestation client

    final static private String CLOSE_BRACKET = "}";

    // basic des encryption
    // THIS ALGORITHM IS IN PRODUCTION!!! DO NOT CHANGE!!!
    final static private int ITERATION_COUNT = 1000;
    final static private int PBE_SALT_LENGTH = 8;
    final static private int PBE_PWD_LENGTH = 8;
    final static private String PBE_ALG = "PBEWithMD5AndDES";
    final static private String PBE_OPEN_BRACKET = "pbe{";

    // triple des encryption
    final static private String DESEDE_ALG = "DESede";
    final static private String DESEDE_OPEN_BRACKET = "DESede{";

    static private CryptStringUtil instance = null;

    //--------------------------------------------------------------------------
    static synchronized public CryptStringUtil getInstance() {
        if (instance == null) {
            instance = new CryptStringUtil();
        }
        return instance;
    }

    //--------------------------------------------------------------------------
    static public String encrypt(String value)
    throws GeneralSecurityException, IOException {
        return getInstance().getEncryptedValue(value);
    }

    //--------------------------------------------------------------------------
    static public String decrypt(String cryptValue)
    throws GeneralSecurityException {
        return getInstance().getDecryptedValue(cryptValue);
    }

    /**
     * Check if the value is encrypted and can be decrypted using this algorithm.
     */
    static public boolean isEncrypted(String value)
    throws GeneralSecurityException {
        if (value == null) {
            return false;
        }
        return !value.equals(getInstance().decryptValueOnce(value));
    }

    //========================================================================//
    // INSTANCE
    //========================================================================//

    //--------------------------------------------------------------------------
    private CryptStringUtil(){
    }

    //--------------------------------------------------------------------------
    // default/universal methods

    //--------------------------------------------------------------------------
    public String getEncryptedValue(String value)
    throws GeneralSecurityException, IOException  {
        String result;

        // default encryption is triple des
        result = encryptPBE(value);

        return result;
    }

    //--------------------------------------------------------------------------
    public String getDecryptedValue(String cryptValue)
    throws GeneralSecurityException {
        int count = 0;
        String value = cryptValue;
        while (isEncrypted(value) && count < 100) {
            value = decryptValueOnce(value);
            count++;
        }
        return value;
    }
    
    //--------------------------------------------------------------------------
    private String decryptValueOnce(String cryptValue)
    throws GeneralSecurityException {
        String result;
        if (cryptValue != null && cryptValue.endsWith(CLOSE_BRACKET)) {

            if (cryptValue.startsWith(PBE_OPEN_BRACKET)) {
                result = decryptPBE(cryptValue);
            }
            else if (cryptValue.startsWith(DESEDE_OPEN_BRACKET)) {
                result = decryptDESede(cryptValue);
            }
            else {
                result = cryptValue;
            }
        }
        else {
            result = cryptValue;
        }

        return result;
    }

    //--------------------------------------------------------------------------
    // Single PBE-DES methods

    //--------------------------------------------------------------------------
    // THIS ALGORITHM IS IN PRODUCTION!!! DO NOT CHANGE!!!
    public String encryptPBE(String value)
    throws GeneralSecurityException, IOException {
        String result;
        if (value != null) {
            byte[] data = value.getBytes("UTF-8");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // generate random salt
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            byte[] salt = new byte[PBE_SALT_LENGTH];
            sr.nextBytes(salt);

            // generate random password
            byte[] passByte = new byte[PBE_PWD_LENGTH];
            sr.nextBytes(passByte);
            for (int i = 0; i < passByte.length; i++) {
                // ascii password using 'a' - 'z'
                passByte[i] = (byte)((passByte[i] % 26) + 97);
            }
            char[] password = new String(passByte, "US-ASCII").toCharArray();

            // set up key
            PBEKeySpec keyspec = new PBEKeySpec(password, salt, ITERATION_COUNT);
            SecretKey key = SecretKeyFactory.getInstance(PBE_ALG).generateSecret(keyspec);

            Cipher cipher = Cipher.getInstance(PBE_ALG);
            PBEParameterSpec paramspec = new PBEParameterSpec(salt, ITERATION_COUNT);
            cipher.init(Cipher.ENCRYPT_MODE, key, paramspec/*, sr*/);

            for (int i = 0; i < salt.length; i++) {
                baos.write(salt[i]);
                baos.write(passByte[i]);
            }
            baos.write(passByte, salt.length, passByte.length - salt.length);

            baos.write(cipher.doFinal(data));

            keyspec.clearPassword();

            result = PBE_OPEN_BRACKET
                     + new String(Base64.encodeBase64(baos.toByteArray()), "US-ASCII")
                     + CLOSE_BRACKET;

            try {
                baos.close();
            }
            catch (IOException ioe) {
                // swallow
            }
        }
        else {
            result = null;
        }
        return result;
    }

    //--------------------------------------------------------------------------
    // THIS ALGORITHM IS IN PRODUCTION!!! DO NOT CHANGE!!!
    public String decryptPBE(String cryptValue)
    throws GeneralSecurityException {
        String result;
        if (cryptValue != null && cryptValue.startsWith(PBE_OPEN_BRACKET) && cryptValue.endsWith(CLOSE_BRACKET)) {

            String strippedCrypt = cryptValue.substring(PBE_OPEN_BRACKET.length(), cryptValue.length() - CLOSE_BRACKET.length());
            byte[] data;

            try {
                data = Base64.decodeBase64(strippedCrypt.getBytes("US-ASCII"));
            }
            catch (UnsupportedEncodingException impossible) {
                throw new AssertionError(impossible);
            }

            if (data.length < PBE_SALT_LENGTH + PBE_PWD_LENGTH) {
                throw new IllegalArgumentException("Invalid Encrypted String");
            }

            byte[] passByte = new byte[PBE_PWD_LENGTH];
            byte[] salt = new byte[PBE_SALT_LENGTH];

            System.arraycopy(data, PBE_SALT_LENGTH*2, passByte, PBE_SALT_LENGTH, PBE_PWD_LENGTH - PBE_SALT_LENGTH);
            for (int i = 0; i < PBE_SALT_LENGTH; i++) {
                salt[i] = data[i*2];
                passByte[i] = data[i*2 + 1];
            }

            char[] password;
            try {
                password = new String(passByte, "UTF-8").toCharArray();
            }
            catch (UnsupportedEncodingException impossible) {
                throw new AssertionError(impossible);
            }

            PBEKeySpec keyspec = new PBEKeySpec(password, salt, ITERATION_COUNT);
            SecretKey key = SecretKeyFactory.getInstance(PBE_ALG).generateSecret(keyspec);

            PBEParameterSpec paramspec = new PBEParameterSpec(salt, ITERATION_COUNT);
            Cipher cipher = Cipher.getInstance(PBE_ALG);
            cipher.init(Cipher.DECRYPT_MODE, key, paramspec);
            byte[] output = cipher.doFinal(data, PBE_SALT_LENGTH +PBE_PWD_LENGTH, data.length - PBE_SALT_LENGTH - PBE_PWD_LENGTH);

            keyspec.clearPassword();

            try {
                result = new String(output, "UTF-8");
            }
            catch (UnsupportedEncodingException impossible) {
                throw new AssertionError(impossible);
            }
        }
        else {
            result = cryptValue;
        }

        return result;
    }

    //--------------------------------------------------------------------------
    // Triple DES methods

    // NOT IN 3.0.0 OR 3.0.1

    //--------------------------------------------------------------------------
    private byte[] encodeKeyBytes(byte[] keyBytes) {
        byte[] encKeyBytes = new byte[keyBytes.length];
        System.arraycopy(keyBytes, 0, encKeyBytes, 0, keyBytes.length);

        // 1) bunch of xor-ing
        for (int i = 0; i < encKeyBytes.length; i++) {
            encKeyBytes[i] ^= (i*7)%128;
            if (i > 1) {
                encKeyBytes[i] ^= keyBytes[i-1];
            }
        }

        // 2) swap every other byte with companion from other end of array
        for (int i = 0; i < encKeyBytes.length / 2; i+=2) {
            int index1 = i;
            int index2 = encKeyBytes.length-1 - i;

            byte temp = encKeyBytes[index1];
            encKeyBytes[index1] = encKeyBytes[index2];
            encKeyBytes[index2] = temp;
        }

        // 3) reverse bits around in every third byte
        for (int i = 1; i < encKeyBytes.length; i+=3) {
            byte reversebyte = 0;
            for (int j = 0; j < 8 ; j++) {
                reversebyte |= ((encKeyBytes[i] >> j) & 0x1) << 7 - j;
            }
            encKeyBytes[i] = reversebyte;
        }

//        // We should probably use a psuedo generator that we provide
//        //                  to ensure the generator algorithm doesn't change in the future
//        //                  A MTTwister implementation should be stronger and faster than normal RAND anyway
//        // 4) xor with seed-based psuedo-random numbers
//        int seedindex = encKeyBytes.length / 4;
//        Random rand = new Random(encKeyBytes[seedindex]*29 + encKeyBytes.length);
//        byte[] masks = new byte[encKeyBytes.length];
//        rand.nextBytes(masks);
//        for (int i = 0; i < encKeyBytes.length; i++) {
//            if (i != seedindex) {
//                encKeyBytes[i] ^= masks[i];
//            }
//        }

        return encKeyBytes;
    }

    //--------------------------------------------------------------------------
    private byte[] decodeKeyBytes(byte[] encKeyBytes) {
        byte[] keyBytes = new byte[encKeyBytes.length];
        System.arraycopy(encKeyBytes, 0, keyBytes, 0, encKeyBytes.length);

//        // 4) xor with seed-based psuedo-random numbers
//        int seedindex = encKeyBytes.length / 4;
//        Random rand = new Random(encKeyBytes[seedindex]*29 + encKeyBytes.length);
//        byte[] masks = new byte[keyBytes.length];
//        rand.nextBytes(masks);
//        for (int i = 0; i < keyBytes.length; i++) {
//            if (i != seedindex) {
//                keyBytes[i] ^= masks[i];
//            }
//        }

        // 3) reverse-swap bits in every third byte back to original location
        for (int i = 1; i < keyBytes.length; i+=3) {
            byte reversebyte = 0;
            for (int j = 0; j < 8 ; j++) {
                reversebyte |= ((keyBytes[i] >> j) & 0x1) << 7 - j;
            }
            keyBytes[i] = reversebyte;
        }

        // 2) reverse-swap every other byte back to original location
        for (int i = 0; i < keyBytes.length / 2; i+=2) {
            int index1 = i;
            int index2 = keyBytes.length-1 - i;

            byte temp = keyBytes[index1];
            keyBytes[index1] = keyBytes[index2];
            keyBytes[index2] = temp;
        }

        // 1) bunch of reverse xor-ing
        for (int i = 0; i < keyBytes.length; i++) {
            keyBytes[i] ^= (i*7)%128;
            if (i > 1) {
                keyBytes[i] ^= keyBytes[i-1];
            }
        }

        return keyBytes;
    }

    //--------------------------------------------------------------------------
    public String encryptDESede(String value)
    throws GeneralSecurityException, IOException {
        String result;
        if (value != null) {
            byte[] plainBytes = value.getBytes("UTF-8");
            byte[] encKeyBytes   = null;
            byte[] cryptBytes = null;
            byte[] data       = null;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // generate random key
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            KeyGenerator keygen = KeyGenerator.getInstance(DESEDE_ALG);
            keygen.init(sr);
            SecretKey key = keygen.generateKey();
            encKeyBytes = encodeKeyBytes(key.getEncoded());

            Cipher cipher = Cipher.getInstance(DESEDE_ALG);
            cipher.init(Cipher.ENCRYPT_MODE, key, sr);
            cryptBytes = cipher.doFinal(plainBytes);

            // write out encoded key and crypt
            baos.write(encKeyBytes);
            baos.write(cryptBytes);
            data = baos.toByteArray();
            result = DESEDE_OPEN_BRACKET
                     + new String(Base64.encodeBase64(data), "US-ASCII")
                     + CLOSE_BRACKET;
            try {
                baos.close();
            }
            catch (IOException ioe) {
                // swallow
            }
        }
        else {
            result = null;
        }
        return result;
    }

    //--------------------------------------------------------------------------
    public String decryptDESede(String cryptToken)
    throws GeneralSecurityException {
        String result;
        if (cryptToken != null && cryptToken.startsWith(DESEDE_OPEN_BRACKET) && cryptToken.endsWith(CLOSE_BRACKET)) {
            String strippedToken = cryptToken.substring(DESEDE_OPEN_BRACKET.length(), cryptToken.length() - CLOSE_BRACKET.length());

            byte[] data;
            try {
                data = Base64.decodeBase64(strippedToken.getBytes("US-ASCII"));
            }
            catch (UnsupportedEncodingException impossible) {
                throw new AssertionError(impossible);
            }
            byte[] encKeyBytes= new byte[DESedeKeySpec.DES_EDE_KEY_LEN];
            byte[] cryptBytes = new byte[data.length - encKeyBytes.length];
            byte[] plainBytes = null;

            // extract encoded-key and crypt data arrays
            System.arraycopy(data, 0, encKeyBytes, 0, encKeyBytes.length);
            System.arraycopy(data, encKeyBytes.length, cryptBytes, 0, cryptBytes.length);

            // regenerate key
//            KeySpec keyspec = new DESedeKeySpec(decodeKeyBytes(encKeyBytes));
//            SecretKeyFactory keyfactory = SecretKeyFactory.getInstance(PBE3DES_ALG);
//            SecretKey key = keyfactory.generateSecret(keyspec);
            SecretKey key = new SecretKeySpec(decodeKeyBytes(encKeyBytes), DESEDE_ALG);

            // decrypt string
            Cipher cipher = Cipher.getInstance(DESEDE_ALG);
            cipher.init(Cipher.DECRYPT_MODE, key);
            plainBytes = cipher.doFinal(cryptBytes);

            try {
                result = new String(plainBytes, "UTF-8");
            }
            catch (UnsupportedEncodingException impossible) {
                throw new AssertionError(impossible);
            }
        }
        else {
            result = cryptToken;
        }

        return result;
    }

}
