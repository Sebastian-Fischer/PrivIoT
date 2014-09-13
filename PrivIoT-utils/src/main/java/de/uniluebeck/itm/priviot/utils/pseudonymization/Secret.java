package de.uniluebeck.itm.priviot.utils.pseudonymization;

import org.apache.commons.codec.binary.Base64;

/**
 * Abstract class that contains static methods for secrets, 
 * that are needed for pseudonym generation.
 */
public abstract class Secret {
	/**
	 * Decodes a secret, that is encoded as base64 String
	 * 
	 * @param secretBase64String
	 * @return The secret as byte array
	 */
	public static byte[] decodeBase64Secret(String secretBase64String) {
		return Base64.decodeBase64(secretBase64String);
	}
	
	/**
	 * Encodes a secret as base64 String
	 * 
	 * @param secret
	 * @return The secret as base64 encoded String
	 */
	public static String encodeBase64Secret(byte[] secret) {
		return Base64.encodeBase64String(secret);
	}
}
