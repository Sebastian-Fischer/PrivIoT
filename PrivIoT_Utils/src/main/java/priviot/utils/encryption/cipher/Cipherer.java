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
public interface Cipherer {
	/**
	 * Initializes the Cipherer with a keysize.
	 * @param keysize Length of Key in Bit, e.g. 256, 512, 1024, 2048
	 * @throws InvalidAlgorithmParameterException
	 */
	public void initialize(int keysize) throws InvalidAlgorithmParameterException;
	
	/**
	 * Generates a new symmetric key or a new asymmetric key pair
	 */
	public void generateKey();
	
	/**
	 * Encrypts a plaintext with the Key (public key in asymmetric encryption)
	 * @param plaintext The plaintext
	 * @return encrypted bytes
	 */
	public byte[] encrypt(byte[] plaintext) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, ShortBufferException, InvalidAlgorithmParameterException;
	
	/**
	 * Decrypts a encrypted text with the Key (private key in asymmetric encryption)
	 * @param encrypted The encrypted text to decrypt
	 * @return plaintext bytes
	 */
	public byte[] decrypt(byte[] ciphertext) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException;
	
	/**
	 * Returns the configuration of this class as String.
	 * Configuration for example contains keysize, encryption algorithms, padding and implementation provider.
	 * @return configuration
	 */
	public String getConfiguration();
	
	/**
	 * Returns the algorithm as a standard formated String of the java crypto API.
	 * @return algorithm name
	 */
	public String getAlgorithm();
	
	/**
	 * Return the key size.
	 * @return key size
	 */
	public int getKeySize();
}
