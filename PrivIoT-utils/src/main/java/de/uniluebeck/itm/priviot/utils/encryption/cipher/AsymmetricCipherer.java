package de.uniluebeck.itm.priviot.utils.encryption.cipher;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

/**
 * Interface for asymmetric cipher implementations.
 */
public abstract class AsymmetricCipherer extends Cipherer {
	/**
	 * Encrypts a plaintext with the private Key
	 * @param plaintext The plaintext
	 * @return encrypted bytes
	 */
	public abstract byte[] encryptWithPrivateKey(byte[] plaintext) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException;
	
	/**
	 * Decrypts a encrypted text with the public key
	 * @param encrypted The encrypted text to decrypt
	 * @return plaintext bytes
	 */
	public abstract byte[] decryptWithPublicKey(byte[] ciphertext) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException;
	
	/**
	 * Returns the public key as an array of bytes to send it to another party.
	 * @return The public key
	 */
	public abstract byte[] getPublicKeyAsByteArray();
	
	/**
	 * Sets the public key to the key given in publicKeyBytes.
	 * If Cipherer.generateKey has been called before, the generated public key will be overwritten.
	 * If Cipherer.generateKey has not been called, only encryption and decryption with the public key is possible.
	 * @param publicKeyBytes The new public key as array of bytes
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public abstract void setPublicKeyFromByteArray(byte[] publicKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException;
	
	/**
     * Returns the private key as an array of bytes to save it securely.
     * @return The private key
     */
    public abstract byte[] getPrivateKeyAsByteArray();
    
    /**
     * Sets private key.
     * If Cipherer.generateKey has been called before, the generated private key will be overwritten.
     * If Cipherer.generateKey has not been called, only encryption and decryption with the private key is possible.
     */
    public abstract void setPrivateKey(PrivateKey privateKey);
    
    /**
     * Sets the private key to the key given in privateKeyBytes.
     * If Cipherer.generateKey has been called before, the generated private key will be overwritten.
     * If Cipherer.generateKey has not been called, only encryption and decryption with the private key is possible.
     * @param publicKeyBytes The new private key as array of bytes
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public abstract void setPrivateKeyFromByteArray(byte[] privateKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException;
}
