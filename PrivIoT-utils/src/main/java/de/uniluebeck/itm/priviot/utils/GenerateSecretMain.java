package de.uniluebeck.itm.priviot.utils;

import de.uniluebeck.itm.priviot.utils.pseudonymization.PseudonymizationException;
import de.uniluebeck.itm.priviot.utils.pseudonymization.PseudonymizationProcessor;
import de.uniluebeck.itm.priviot.utils.pseudonymization.Secret;

public class GenerateSecretMain {

	public static void main(String[] args) {
		System.out.println("PrivIoT Utils - Generation of Secret");
		
		System.out.println("This will generate a pseudorandom secret for the use in a pseudonym");
		
		byte[] secret;
		try {
			secret = PseudonymizationProcessor.generateHmac256Secret();
		} catch (PseudonymizationException e) {
			System.out.println("Error during generation of secret: " + e.getMessage());
			return;
		}
		
		String secretStr = Secret.encodeBase64Secret(secret);
		
		System.out.println("The secret (as Base64 encoded string) is:\n" + secretStr);
	}

}
