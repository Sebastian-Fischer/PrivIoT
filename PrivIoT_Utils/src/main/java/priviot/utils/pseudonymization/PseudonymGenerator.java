package priviot.utils.pseudonymization;

/**
 * Interface for all PseudonymGenerators.
 * A PseudonymGenerator generates pseudonyms for a given string.
 */
public interface PseudonymGenerator {
	/**
	 * Initializes the generator
	 */
	void inititialize();
	
	/**
	 * Generates a new pseudonym for a given value
	 * @param value an arbitrary String
	 * @return Pseudonym for the value
	 */
	String generatePseudonym(String value);
}
