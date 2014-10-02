package de.uniluebeck.itm.priviot.utils.pseudonymization;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Helper class that provides a method to generate a pseudonym.
 */
public class PseudonymizationProcessor {
	
	/**
	 * Generates a secret that can be used in the HMAC-256 algorithm.
	 * @return secret
	 * @throws PseudonymizationException
	 */
	public static byte[] generateHmac256Secret() throws PseudonymizationException {
		HMacSha256PseudonymGenerator generator = new HMacSha256PseudonymGenerator();
    	
		try {
			generator.inititialize();
		} catch (NoSuchAlgorithmException e) {
			throw new PseudonymizationException("Exception during initialization of HMac256PseudonymGenerator", e);
		}
    	
    	return generator.createSecret();
	}
	
	/**
	 * Helper method that generates a pseudonym with the HMAC-256 algorithm for a given original string 
	 * using a secret.
	 * The actual time is also used to generate the pseudonym. Therefore the actual time is rounded to
	 * timePeriod. This means, that every timePeriod seconds the generated pseudonym for the same 
	 * original string and secret changes. Within the timePeriod the method will produce the same 
	 * pseudonym for the same original string and secret.
	 * 
	 * Example:
	 * timePeriod: 10 seconds
	 * In 13:37:00 generatePseudonym(original, 10, secret) will produce pseudonym1
	 * In 13:37:09 generatePseudonym(original, 10, secret) will produce pseudonym1
	 * In 13:37:10 generatePseudonym(original, 10, secret) will produce pseudonym2
	 * 
	 * @param original    An arbitrary String.
	 * @param timePeriod  timePeriod (in seconds) in which the generated pseudonym will not change.
	 * @param secret      A pseudorandom secret with length 256.
	 * @return
	 */
	public static String generateHmac256Pseudonym(String original, int timePeriod, byte[] secret) throws PseudonymizationException {
		if (original.isEmpty() || timePeriod == 0 || secret.length == 0) {
			throw new PseudonymizationException("Bad input. original length " + original.length() + ", timePeriod " + timePeriod + ", secret length " + secret.length);
		}
		
		Date actDate = new Date();
		// milliseconds since January 1, 1970.
		long milliseconds = actDate.getTime();
		// milliseconds since last update time
		long modulo = milliseconds % (timePeriod*1000);
		// seconds since January 1, 1970 at the last update time
		long roundedSeconds = milliseconds - modulo;
		
		// concatenate
		String plaintext = original + roundedSeconds;
		
		// initialize generator
		HMacSha256PseudonymGenerator generator = new HMacSha256PseudonymGenerator();
		try {
			generator.inititialize();
		} catch (NoSuchAlgorithmException e) {
			throw new PseudonymizationException("Exception during initialization of HMac256PseudonymGenerator", e);
		}
		
		// generate pseudonym
		String pseudonym;
    	try {
			pseudonym = generator.generatePseudonym(plaintext, secret);
		} catch (InvalidKeyException e) {
			throw new PseudonymizationException("Exception during generation of pseudonym", e);
		}
    	
    	return pseudonym;
	}
}
