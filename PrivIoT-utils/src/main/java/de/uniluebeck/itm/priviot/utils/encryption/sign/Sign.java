package de.uniluebeck.itm.priviot.utils.encryption.sign;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

/**
 * Interface for signature implementations.
 * A Signaturer encapsulates a signature algorithm.
 */
public interface Sign {
	/**
	 * Initializes the Signaturer with a keysize
	 * @param keysize Length of the keys in Bit. e.g. 1024, 2048
	 */
	public void initialize(int keysize) throws InvalidAlgorithmParameterException;
	
	/**
	 * Generates an asymmetric Key pair.
	 */
	public void generateKeys();
	
	/**
	 * Returns the public key as an array of bytes to send it to another party.
	 * @return The public key
	 * @throws NoSuchAlgorithmException
	 */
	public byte[] getPublicKeyAsByteArray() throws NoSuchAlgorithmException;
	
	/**
	 * Sets the public key to the key given in publicKeyBytes.
	 * If generateKey has been called before, the generated public key will be overwritten.
	 * If generateKey has not been called, only verifySignature is possible.
	 * @param publicKeyBytes The new public key as array of bytes
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public void setPublicKeyFromByteArray(byte[] publicKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException;
	
	/**
	 * Returns the private key as an array of bytes.
	 * @return The private key
	 */
	public byte[] getPrivateKeyAsByteArray() throws NoSuchAlgorithmException;
	
	/**
	 * Sets the private key to the key given in privateKeyBytes.
	 * If generateKey has been called before, the generated private key will be overwritten.
	 * If generateKey has not been called, only sign is possible.
	 * @param privateKeyBytes
	 */
	public void setPrivateKeyFromByteArray(byte[] privateKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException;
	
	/**
	 * Caclulates a hash-value of the plaintext using a cryptographic hash function and encrypts this value with the pivate key.
	 * @param plaintext The plaintext
	 * @return The Signature
	 */
	public byte[] sign(byte[] plaintext) throws InvalidKeyException, SignatureException;
	
	/**
	 * Verifies a given signature of a plaintext.
	 * The signature is decrypted using the public key and compared with a hash value that is created from the plaintext.
	 * The public key that belongs to the private key the signature was created with has to be set before.
	 * @param plaintext The plaintext
	 * @param signature The signature of the plaintext
	 * @return true, if the signature belongs to the plaintext
	 */
	public boolean verifySignature(byte[] plaintext, byte[] signature) throws InvalidKeyException, SignatureException; 
	
	/**
	 * Returns the configuration of this class as String.
	 * Configuration for example contains keysize, hash function, signature algorithm and implementation provider.
	 * @return configuration
	 */
	public String getConfiguration();
	
}
