package priviot.utils.encryption.cipher;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;

/**
 * Interface for cipher implementations
 * A Cipherer can encapsulate a symmetric or asymmetric algorithm.
 */
public abstract class Cipherer {
	/**
	 * Initializes the Cipherer with a keysize.
	 * @param keysize Length of Key in Bit, e.g. 256, 512, 1024, 2048
	 * @throws InvalidAlgorithmParameterException
	 */
	public abstract void initialize(int keysize) throws InvalidAlgorithmParameterException;
	
	/**
	 * Generates a new symmetric key or a new asymmetric key pair
	 */
	public abstract void generateKey();
	
	/**
	 * Encrypts a plaintext with the Key (public key in asymmetric encryption)
	 * @param plaintext The plaintext
	 * @return encrypted bytes
	 */
	public abstract byte[] encrypt(byte[] plaintext) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, ShortBufferException, InvalidAlgorithmParameterException;
	
	/**
	 * Decrypts a encrypted text with the Key (private key in asymmetric encryption)
	 * @param encrypted The encrypted text to decrypt
	 * @return plaintext bytes
	 */
	public abstract byte[] decrypt(byte[] ciphertext) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException;
	
	/**
	 * Returns the configuration of this class as String.
	 * Configuration for example contains keysize, encryption algorithms, padding and implementation provider.
	 * @return configuration
	 */
	public abstract String getConfiguration();
	
	/**
     * Returns the algorithm as a standard formated String of the java crypto API.
     * @return algorithm name
	 */
	public abstract String getUsedAlgorithm();
	
	/**
	 * Return the key size.
	 * @return key size
	 */
	public abstract int getKeySize();
}
