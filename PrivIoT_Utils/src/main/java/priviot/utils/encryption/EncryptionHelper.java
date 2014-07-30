package priviot.utils.encryption;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

public class EncryptionHelper {
    
    public static int getMaxKeySizeAES() throws NoSuchAlgorithmException {
        return Cipher.getMaxAllowedKeyLength("AES");
    }
    
    public static void printRestrictionMessage() {
        System.out.println("Key size in standard Java jre is restricted to 128 bit.");
        System.out.println("Please remove the restriction by copying the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files into your local directory <java-home>/lib/security.");
    }
}
