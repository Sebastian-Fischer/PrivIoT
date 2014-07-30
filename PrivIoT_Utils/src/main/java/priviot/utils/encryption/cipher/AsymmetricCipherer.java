package priviot.utils.encryption.cipher;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

/**
 * Interface for asymmetric cipher implementations.
 */
public interface AsymmetricCipherer extends Cipherer {
	/**
	 * Encrypts a plaintext with the private Key
	 * @param plaintext The plaintext
	 * @return encrypted bytes
	 */
	public byte[] encryptWithPrivateKey(byte[] plaintext) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException;
	
	/**
	 * Decrypts a encrypted text with the public key
	 * @param encrypted The encrypted text to decrypt
	 * @return plaintext bytes
	 */
	public byte[] decryptWithPublicKey(byte[] ciphertext) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException;
	
	/**
	 * Returns the public key as an array of bytes to send it to another party.
	 * @return The public key
	 * @throws NoSuchAlgorithmException
	 */
	public byte[] getPublicKeyAsByteArray() throws NoSuchAlgorithmException;
	
	/**
	 * Sets the public key to the key given in publicKeyBytes.
	 * If Cipherer.generateKey has been called before, the generated public key will be overwritten.
	 * If Cipherer.generateKey has not been called, only encryption and decryption with the public key is possible.
	 * @param publicKeyBytes The new public key as array of bytes
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public void setPublicKeyFromByteArray(byte[] publicKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException;
}
