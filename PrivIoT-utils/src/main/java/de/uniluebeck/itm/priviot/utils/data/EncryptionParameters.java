package de.uniluebeck.itm.priviot.utils.data;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.apache.commons.codec.EncoderException;

import de.uniluebeck.itm.priviot.utils.encryption.EncryptionException;
import de.uniluebeck.itm.priviot.utils.encryption.cipher.asymmetric.rsa.RSACipherer;
import de.uniluebeck.itm.priviot.utils.encryption.cipher.symmetric.aes.AESCipherer;

/**
 * Encapsulates the parameters needed for encryption.
 */
public class EncryptionParameters {
    private String symmetricEncryptionAlgorithm;
    private int symmetricEncryptionKeyBitStrength; 
    private String asymmetricEncryptionAlgorithm;
    private int asymmetricEncryptionKeyBitStrength;
    
    /**
     * Initializes an EncryptionParameters object.
     * 
     * @param symmetricEncryptionAlgorithm see for example {@link AESChiperer#getAlgorithm()}
     * @param symmetricEncryptionBitStrength   Key size in bit. For example 128
     * @param asymmetricEncryptionAlgorithm see for example {@link RSAChiperer#getAlgorithm()}
     * @param asymmetricEncryptionBitStrength  Key size in bit. For example 1024
     */
    public EncryptionParameters(String symmetricEncryptionAlgorithm, 
                                int symmetricEncryptionBitStrength,
                                String asymmetricEncryptionAlgorithm,
                                int asymmetricEncryptionBitStrength) {
        this.symmetricEncryptionAlgorithm = symmetricEncryptionAlgorithm;
        this.symmetricEncryptionKeyBitStrength = symmetricEncryptionBitStrength;
        this.asymmetricEncryptionAlgorithm = asymmetricEncryptionAlgorithm;
        this.asymmetricEncryptionKeyBitStrength = asymmetricEncryptionBitStrength;
    }
    
    /**
     * Initializes an EncryptionParameters object.
     * 
     * @param symmetricEncryptionAlgorithmCode  see {@link EncryptionAlgorithmCodes}
     * @param asymmetricEncryptionAlgorithm  see for example {@link RSAChiperer#getAlgorithm()}
     * @param asymmetricEncryptionBitStrength   BitStrength of algorithm. For example 1024
     */
    public EncryptionParameters(String symmetricEncryptionAlgorithmCode,
			    				String asymmetricEncryptionAlgorithm,
			    				int asymmetricEncryptionBitStrength) {
    	setSymmetricParameters(symmetricEncryptionAlgorithmCode);
    	this.asymmetricEncryptionAlgorithm = asymmetricEncryptionAlgorithm;
        this.asymmetricEncryptionKeyBitStrength = asymmetricEncryptionBitStrength;
    }
    
    public EncryptionParameters(String symmetricEncryptionAlgorithmCode,
    		                    PublicKey publicKey) throws EncryptionException {
    	setSymmetricParameters(symmetricEncryptionAlgorithmCode);
    	
    	this.asymmetricEncryptionAlgorithm = getAsymmetricEncryptionAlgorithmByPublicKey(publicKey);
    	this.asymmetricEncryptionKeyBitStrength = getAsymmetricEncryptionBitStrengthByPublicKey(publicKey);
    }

    public String getSymmetricEncryptionAlgorithm() {
        return symmetricEncryptionAlgorithm;
    }

    /**
     * Sets the encryption algorithm for symmetric encryption.
     * 
     * @param symmetricEncryptionAlgorithm  See for example {@link AESCipherer#getAlgorithm()}
     */
    public void setSymmetricEncryptionAlgorithm(
            String symmetricEncryptionAlgorithm) {
        this.symmetricEncryptionAlgorithm = symmetricEncryptionAlgorithm;
    }

    public int getSymmetricEncryptionBitStrength() {
        return symmetricEncryptionKeyBitStrength;
    }

    public void setSymmetricEncryptionBitStrength(int symmetricEncryptionBitStrength) {
        this.symmetricEncryptionKeyBitStrength = symmetricEncryptionBitStrength;
    }

    public String getAsymmetricEncryptionAlgorithm() {
        return asymmetricEncryptionAlgorithm;
    }

    /**
     * Sets the encryption algorithm for asymmetric encryption.
     * 
     * @param asymmetricEncryptionAlgorithm  See for example {@link RSACipherer#getAlgorithm()}
     */
    public void setAsymmetricEncryptionAlgorithm(
            String asymmetricEncryptionAlgorithm) {
        this.asymmetricEncryptionAlgorithm = asymmetricEncryptionAlgorithm;
    }

    public int getAsymmetricEncryptionBitStrength() {
        return asymmetricEncryptionKeyBitStrength;
    }

    public void setAsymmetricEncryptionBitStrength(int asymmetricEncryptionBitStrength) {
        this.asymmetricEncryptionKeyBitStrength = asymmetricEncryptionBitStrength;
    }
    
    /**
     * Sets the symmetric encryption parameters associated with an encryptionAlgorithm name.
     * 
     * @param encryptionAlgorithmName  see {@link EncryptionAlgorithmCodes}
     */
    public void setSymmetricParameters(String encryptionAlgorithmName) {
    	if (EncryptionAlgorithmCodes.AES_128_CBC.equals(encryptionAlgorithmName)) {
    		this.symmetricEncryptionAlgorithm = AESCipherer.getAlgorithm();
    		this.symmetricEncryptionKeyBitStrength = 128;
    	}
    	else  if (EncryptionAlgorithmCodes.AES_256_CBC.equals(encryptionAlgorithmName)) {
    		this.symmetricEncryptionAlgorithm = AESCipherer.getAlgorithm();
    		this.symmetricEncryptionKeyBitStrength = 256;
    	}
    }
    
    /**
     * Returns the algorithm name for symmetric encryption that indicates 
     * both algorithm and key size.
     * 
     * @return The algorithm code (see {@link EncryptionAlgorithmCodes})
     * @throws EncryptionException 
     */
    public String getSymmetricAlgorithmCode() throws EncryptionException {
    	if (symmetricEncryptionAlgorithm.equals(AESCipherer.getAlgorithm())) {
    		if (symmetricEncryptionKeyBitStrength == 128) {
    			return EncryptionAlgorithmCodes.AES_128_CBC;
    		}
    		else if (symmetricEncryptionKeyBitStrength == 256) {
    			return EncryptionAlgorithmCodes.AES_256_CBC;
    		}
    		else {
    			throw new EncryptionException("Unknown or invalid symmetric encrpytion algorithm: " + symmetricEncryptionAlgorithm + ", " + symmetricEncryptionKeyBitStrength);
    		}
    	}
    	else {
    		throw new EncryptionException("Unknown or invalid symmetric encrpytion algorithm: " + symmetricEncryptionAlgorithm + ", " + symmetricEncryptionKeyBitStrength);
    	}
    }
    
    public static String getAsymmetricEncryptionAlgorithmByPublicKey(PublicKey publicKey) throws EncryptionException {
    	if (publicKey instanceof RSAPublicKey) {    		
    		return RSACipherer.getAlgorithm();
    	}
    	else {
    		throw new EncryptionException("Public key algorithm not supported: " + publicKey.getAlgorithm());
    	}
    }
    
    public static int getAsymmetricEncryptionBitStrengthByPublicKey(PublicKey publicKey) throws EncryptionException {
    	if (publicKey instanceof RSAPublicKey) {
    		return ((RSAPublicKey)publicKey).getModulus().bitLength();
    	}
    	else {
    		throw new EncryptionException("Public key algorithm not supported: " + publicKey.getAlgorithm());
    	}
    }
    
    public static String getAsymmetricEncryptionAlgorithmByPrivateKey(PrivateKey privateKey) throws EncryptionException {
    	if (privateKey instanceof RSAPrivateKey) {    		
    		return RSACipherer.getAlgorithm();
    	}
    	else {
    		throw new EncryptionException("Private key algorithm not supported: " + privateKey.getAlgorithm());
    	}
    }
    
    public static int getAsymmetricEncryptionBitStrengthByPrivateKey(PrivateKey privateKey) throws EncryptionException {
    	if (privateKey instanceof RSAPrivateKey) {
    		return ((RSAPrivateKey)privateKey).getModulus().bitLength();
    	}
    	else {
    		throw new EncryptionException("Private key algorithm not supported: " + privateKey.getAlgorithm());
    	}
    }
}
