package priviot.utils.pseudonymization;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Interface for all PseudonymGenerators.
 * 
 * A PseudonymGenerator generates pseudonyms for a given string.
 */
public interface PseudonymGenerator {
	/**
	 * Initializes the generator
	 */
	void inititialize() throws NoSuchAlgorithmException;
	
	/**
	 * Creates a pseudorandom secret that can be used in generatePseudonym().
	 * @return secret
	 */
	byte[] createSecret();
	
	/**
	 * Generates a new pseudonym for a given value
	 * @param value   An arbitrary String
	 * @param secret  The secret, if needed by the concrete PseudonymGenerator
	 * @return Pseudonym for the value
	 */
	String generatePseudonym(String value, byte[] secret) throws InvalidKeyException;
}
