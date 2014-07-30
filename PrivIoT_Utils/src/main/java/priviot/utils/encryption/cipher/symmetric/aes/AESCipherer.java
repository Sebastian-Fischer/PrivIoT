package priviot.utils.encryption.cipher.symmetric.aes;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

import priviot.utils.encryption.cipher.SymmetricCipherer;

/**
 * Encapsulates symmetric cipher implementation of AES algorithm.
 */
public class AESCipherer implements SymmetricCipherer {

    private static String algorithmName = "AES/CBC/PKCS5Padding";
    private static String keySpecName = "AES";
    private static String randomName = "SHA1PRNG";
    
    private SecretKey key;
    private IvParameterSpec initializationVector;
    private KeyGenerator keyGenerator;
    private Cipher aes;
    private SecureRandom secureRandom;
    
    private int keysize = 0;
    
    public AESCipherer() throws NoSuchAlgorithmException, NoSuchPaddingException {
        aes = Cipher.getInstance(algorithmName);
        keyGenerator = KeyGenerator.getInstance(keySpecName);
        secureRandom = SecureRandom.getInstance(randomName);
    }
    
    @Override
    public void initialize(int keysize) throws InvalidAlgorithmParameterException {
        this.keysize = keysize;

        //TODO: check how long an iv is with different key sizes
        byte[] initializationVectorBytes = new byte[16]; 
        secureRandom.nextBytes(initializationVectorBytes);
        initializationVector = new IvParameterSpec(initializationVectorBytes);
        
        keyGenerator.init(keysize, secureRandom);
    }

    @Override
    public void generateKey() {
        key = keyGenerator.generateKey();
    }
    
    @Override
    public String getConfiguration() {
        return ("Algorithm: " + aes.getAlgorithm() + ", Provider: " + aes.getProvider() + ", Keysize: " + keysize);
    }

    @Override
    public byte[] getKeyAsByteArray() throws NoSuchAlgorithmException {
        if (key == null) {
            return new byte[0];
        }
        
        return key.getEncoded();
    }

    @Override
    public void setKeyFromByteArray(byte[] keyBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        key = new SecretKeySpec(keyBytes, keySpecName);
    }

    @Override
    public byte[] getIvAsByteArray() throws NoSuchAlgorithmException {
        return initializationVector.getIV();
    }

    @Override
    public void setIvFromByteArray(byte[] ivBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        initializationVector = new IvParameterSpec(ivBytes);
    }

    @Override
    public byte[] encrypt(byte[] plaintext) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException,
            ShortBufferException, InvalidAlgorithmParameterException {
        aes.init(Cipher.ENCRYPT_MODE, key, initializationVector);
        
        return aes.doFinal(plaintext);
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        aes.init(Cipher.DECRYPT_MODE, key, initializationVector);
        
        return aes.doFinal(ciphertext);
    }

    @Override
    public String getAlgorithm() {
        return aes.getAlgorithm();
    }

    @Override
    public int getKeySize() {
        return keysize;
    }

}
