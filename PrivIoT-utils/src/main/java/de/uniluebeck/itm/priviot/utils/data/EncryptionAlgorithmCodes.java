package de.uniluebeck.itm.priviot.utils.data;

/**
 * Stirng constants for all encryption algorithms.
 * Each EncryptionAlgorithmCode specifies the algorithm and the key size.
 */
public abstract class EncryptionAlgorithmCodes {
	/**
	 * Symmetric encryption algorithm AES with key size 128 bit and Cipher Block Chaining mode
	 */
	public static final String AES_128_CBC = "AES-128";
	
	/**
	 * Symmetric encryption algorithm AES with key size 256 bit and Cipher Block Chaining mode
	 */
	public static final String AES_256_CBC = "AES-256";
}
