package priviot.utils.pseudonymization;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * A concrete PseudonymGenerator, that produces pseudonyms using HMAC-SHA256.
 * 
 * See RFC 2104 for HMAC algorithm.
 */
public class HMacSha256PseudonymGenerator implements PseudonymGenerator {

	private static final String ALGORITHM = "HmacSHA256";
	private static final int BLOCK_SIZE = 256;
	
	private Mac mac;
	
	private SecureRandom secureRandom;
	
	
	@Override
	public void inititialize() throws NoSuchAlgorithmException {
		mac = Mac.getInstance(ALGORITHM);
		secureRandom = new SecureRandom();
		
	}
	
	@Override
	public byte[] createSecret() {
		byte[] bytes = new byte[BLOCK_SIZE];
		
		secureRandom.nextBytes(bytes);
		
		return bytes;
	}

	/**
	 * Returns the generated pseudonym as Base64 encoded String
	 */
	@Override
	public String generatePseudonym(String value, byte[] secret) throws InvalidKeyException {
		SecretKeySpec keySpec = new SecretKeySpec(secret, ALGORITHM);
		
		mac.init(keySpec);
		
		byte[] macValue = mac.doFinal(value.getBytes());
		
		// lineLength must be long enough to ensure, there is no end of line
		// otherwise there will be the line separator character always at the same position
		// ulSafeMode is set to true, so there will be - and _ instead of / and + in the pseudonym
		Base64 base64 = new Base64(1000, "#".getBytes(), true);
		
		String pseudonym = base64.encodeAsString(macValue);
		
		// eliminate last line end character
		if (pseudonym.endsWith("#")) {
			pseudonym = pseudonym.substring(0, pseudonym.length() - 1);
		}
		
		return pseudonym;
	}	

}
