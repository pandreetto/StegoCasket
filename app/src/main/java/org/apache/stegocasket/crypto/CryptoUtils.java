package org.apache.stegocasket.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

import org.apache.stegocasket.R;
import org.apache.stegocasket.core.SecretException;
import org.apache.stegocasket.core.SecretManager;

public class CryptoUtils {

    private static String PBE_ALGO = "PBEWITHSHAANDTWOFISH-CBC";

    private static String MAIN_ALGO = "AES";

    private static String CIPHER_PARAMS = "AES/CBC/PKCS7Padding";

    private static byte[] SALT = { 0, 0xF, 1, 0xE, 2, 0xD, 3, 0xC, 4, 0xB, 5, 0xA, 6, 9, 7, 8 };

    private static int INT_COUNT = 1000;

    private static int KEY_LENGTH = 256;

    private static byte[] INITVECT = { 9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF, 0, 1, 2, 3, 4, 5, 6, 7, 8 };

    public static Cipher setupCipher(int type, String pwd)
            throws SecretException {
        try {
            PBEKeySpec pbeKey = new PBEKeySpec(pwd.toCharArray(), SALT, INT_COUNT, KEY_LENGTH);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBE_ALGO);
            SecretKey sKey = factory.generateSecret(pbeKey);
            SecretKey aesKey = new SecretKeySpec(sKey.getEncoded(), MAIN_ALGO);

            Cipher cipher = Cipher.getInstance(CIPHER_PARAMS);
            cipher.init(type, aesKey, new IvParameterSpec(INITVECT));

            return cipher;
        } catch (Exception ex) {
            Log.e(SecretManager.class.getName(), ex.getMessage(), ex);
            /*
            TODO define error string
             */
            //throw new SecretException(R.string.nosetupchiper);
            throw new SecretException(0);
        }
    }
}