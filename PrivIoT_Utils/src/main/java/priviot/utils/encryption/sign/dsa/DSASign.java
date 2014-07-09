package priviot.utils.encryption.sign.dsa;

import java.math.BigInteger;
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
import java.security.interfaces.DSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;

import priviot.utils.encryption.sign.Sign;

public class DSASign implements Sign {

	private PublicKey publicKey;
	private PrivateKey privateKey;
	
	private KeyFactory keyFactory;
	private Signature signature;
	private KeyPairGenerator keyPairGenerator;
	private SecureRandom secureRandom;
	
	private int keysize = 0;
	
	public DSASign() throws NoSuchAlgorithmException {
		signature = Signature.getInstance("SHA1withDSA");
		keyFactory = KeyFactory.getInstance("DSA");
		keyPairGenerator = KeyPairGenerator.getInstance("DSA");
		secureRandom = new SecureRandom();
	}
	
	@Override
	public void initialize(int keysize) throws InvalidAlgorithmParameterException {
		this.keysize = keysize;
		
		//DSAParameterSpec parameter = new DSAParameterSpec();
		
		keyPairGenerator.initialize(keysize, secureRandom);
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
	
	public void printParameters() {
		BigInteger p = ((DSAPublicKey)publicKey).getParams().getP();
		BigInteger g = ((DSAPublicKey)publicKey).getParams().getG();
		BigInteger q = ((DSAPublicKey)publicKey).getParams().getQ();
		System.out.println("P: BigInteger(" + p.toString(16) + ", 16)");
		System.out.println("G: BigInteger(" + g.toString(16) + ", 16)");
		System.out.println("Q: BigInteger(" + q.toString(16) + ", 16)");
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
