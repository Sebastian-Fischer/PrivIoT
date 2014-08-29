package de.uniluebeck.itm.priviot.utils.encryption.cipher;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public abstract class SymmetricCipherer extends Cipherer {
    /**
     * Returns the key as an array of bytes to send it to another party.
     * @return The key
     */
    public abstract byte[] getKeyAsByteArray();
    
    /**
     * Sets the key to the key given in keyBytes.
     * If Cipherer.generateKey has been called before, the generated key will be overwritten.
     * @param keyBytes The new key as array of bytes
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public abstract void setKeyFromByteArray(byte[] keyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException;
    
    /**
     * Returns the initialization vector as an array of bytes to send it to another party.
     * @return The initialization vector
     */
    public abstract byte[] getIvAsByteArray();
    
    /**
     * Sets the initialization vector to the initialization vector given in ivBytes.
     * If Cipherer.intialize has been called before, the generated initialization vector will be overwritten.
     * @param ivBytes The new initialization vector as array of bytes
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public abstract void setIvFromByteArray(byte[] ivBytes) throws NoSuchAlgorithmException, InvalidKeySpecException;
}
