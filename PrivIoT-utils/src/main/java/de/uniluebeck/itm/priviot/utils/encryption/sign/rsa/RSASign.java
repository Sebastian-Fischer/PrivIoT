package de.uniluebeck.itm.priviot.utils.encryption.sign.rsa;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import de.uniluebeck.itm.priviot.utils.encryption.sign.Sign;

public class RSASign implements Sign {

	private PublicKey publicKey;
	private PrivateKey privateKey;
	
	private KeyFactory keyFactory;
	private Signature signature;
	private KeyPairGenerator keyPairGenerator;
	private SecureRandom secureRandom;
	
	private int keysize = 0;
	
	public RSASign() throws NoSuchAlgorithmException {
		signature = Signature.getInstance("SHA1withRSA");
		keyFactory = KeyFactory.getInstance("RSA");
		keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		secureRandom = new SecureRandom();
	}
	
	@Override
	public void initialize(int keysize) throws InvalidAlgorithmParameterException {
		this.keysize = keysize;
		
		// F4 is the 4th Fermat value. This is commonly used in RSA and has a high security and efficiency
		RSAKeyGenParameterSpec parameter = new RSAKeyGenParameterSpec(keysize, RSAKeyGenParameterSpec.F4);
		
		keyPairGenerator.initialize(parameter, secureRandom);
	}

	@Override
	public void generateKeys() {
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		publicKey = keyPair.getPublic();
		privateKey = keyPair.getPrivate();
	}

	@Override
	public byte[] getPublicKeyAsByteArray() throws NoSuchAlgorithmException {
		if (publicKey == null) {
			return new byte[0];
		}
		
		return publicKey.getEncoded();
	}

	@Override
	public void setPublicKeyFromByteArray(byte[] publicKeyBytes)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
		publicKey = keyFactory.generatePublic(keySpec);
	}

	@Override
	public byte[] sign(byte[] plaintext) throws InvalidKeyException, SignatureException {
		signature.initSign(privateKey, secureRandom);
		
		signature.update(plaintext);
		
		return signature.sign();
	}

	@Override
	public boolean verifySignature(byte[] plaintext, byte[] _signature) throws InvalidKeyException, SignatureException {
		signature.initVerify(publicKey);
		
		signature.update(plaintext);
		
		return signature.verify(_signature);
	}
	
	@Override
	public String getConfiguration() {
		return ("Algorithm: " + signature.getAlgorithm() + ", Provider: " + signature.getProvider() + ", Keysize: " + keysize);
	}
	
	@Override
	public byte[] getPrivateKeyAsByteArray() throws NoSuchAlgorithmException {
		if (privateKey == null) {
			return new byte[0];
		}
		
		return privateKey.getEncoded();
	}

	@Override
	public void setPrivateKeyFromByteArray(byte[] privateKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeySpec keySpec = new X509EncodedKeySpec(privateKeyBytes);
		privateKey = keyFactory.generatePrivate(keySpec);
	}

}
