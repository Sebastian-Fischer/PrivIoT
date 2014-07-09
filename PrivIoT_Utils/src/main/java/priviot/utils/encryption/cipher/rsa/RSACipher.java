package priviot.utils.encryption.cipher.rsa;

import java.security.*;
import java.security.spec.*;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import priviot.utils.encryption.cipher.AsymmetricCipher;

/**
 * Encapsulates asymmetric cipher implementation of RSA algorithm.
 * 
 * TODO: Uses Electronic Codebook Mode (ECB), which makes replay attacks easy. Better use Cipher Block Chaining (CBC).
 */
public class RSACipher implements AsymmetricCipher {
	
	private static int blockSize1024 = 117;
	private static int blockSize2048 = 245;
	private static int blockSize4096 = 501;

	private PublicKey publicKey;
	private PrivateKey privateKey;
	
	private KeyFactory keyFactory;
	private Cipher rsa;
	private KeyPairGenerator keyPairGenerator;
	private SecureRandom secureRandom;
	
	private int keysize = 0;
	
	public RSACipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
		rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
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
	public void generateKey() {
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		publicKey = keyPair.getPublic();
		privateKey = keyPair.getPrivate();
	}
	
	public byte[] getPublicKeyAsByteArray() {
		if (publicKey == null) {
			return new byte[0];
		}
		
		return publicKey.getEncoded();		
	}
	
	public void setPublicKeyFromByteArray(byte[] publicKeyBytes) throws InvalidKeySpecException {
		KeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
		publicKey = keyFactory.generatePublic(keySpec);
	}

	@Override
	public byte[] encrypt(byte[] plaintext) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {
		rsa.init(Cipher.ENCRYPT_MODE, publicKey);
		return rsa.doFinal(plaintext);
	}
	
	@Override
	public byte[] encryptWithPrivateKey(byte[] plaintext) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		rsa.init(Cipher.ENCRYPT_MODE, privateKey);
		return rsa.doFinal(plaintext);
	}

	@Override
	public byte[] decrypt(byte[] ciphertext) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		rsa.init(Cipher.DECRYPT_MODE, privateKey);
		return rsa.doFinal(ciphertext);
	}

	@Override
	public byte[] decryptWithPublicKey(byte[] ciphertext) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		rsa.init(Cipher.DECRYPT_MODE, publicKey);
		return rsa.doFinal(ciphertext);
	}

	@Override
	public String getConfiguration() {
		return ("Algorithm: " + rsa.getAlgorithm() + ", Provider: " + rsa.getProvider() + ", Keysize: " + keysize);
	}
	
	private byte[] encryptdecryptInternal(Cipher initializedCipher, int blockSize, byte[] in) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
		//int blockSize = (keysize / 8) / 8;
		//int blockSize = initializedCipher.getBlockSize();
		int outLen = 0;
		int finalOutLen = initializedCipher.getOutputSize(in.length);
		byte[] out = new byte[finalOutLen];
		
		//outLen = initializedCipher.update(in, 0, 2, out, 0);
		
		System.out.println("in buffer: " +  Arrays.toString(in));
		
		System.out.println("outlen will be: " + finalOutLen + ", blocksize is " + blockSize);
		System.out.println("in has size: " + in.length + ", out has size: " + out.length);
		
		for (int block = 0; block < in.length / blockSize; block++) {
			int inputPosition = block * blockSize;
			System.out.println("block " + block + " at in position " + inputPosition);
			byte[] inPart = Arrays.copyOfRange(in, inputPosition, inputPosition + blockSize);
			System.out.println("inputPart with size " + inPart.length + ": " + Arrays.toString(inPart));
			System.out.println("update(in, position: 0, blockSize: " + inPart.length + ", out, position: " + outLen);
			byte[] outPart = initializedCipher.update(inPart);
			if (outPart == null) {
				System.out.println("returned null");
				return new byte[0];
			}
			//int lenUpdate = initializedCipher.update(inPart, 0, inPart.length, out, outLen);
			System.out.println("length returned: " + outPart.length);
			System.out.println("out buffer: " +  Arrays.toString(outPart));
			//outLen += lenUpdate;
		}
		
		System.out.println("in has size: " + in.length + ", out has size: " + out.length);
		int inputPosition = in.length - (in.length / blockSize)*blockSize;
		byte[] inPart = Arrays.copyOfRange(in, inputPosition, inputPosition + blockSize);
		System.out.println("inputPart with size " + inPart.length + ": " + Arrays.toString(inPart));
		System.out.println("doFinal(in, position: 0, blockSize: " + inPart.length + ", out, position: " + outLen);
		initializedCipher.doFinal(inPart, 0, inPart.length, out, outLen);
		
		return out;
	}
	
	private int getBlockSize() {
		switch(keysize) {
		case 1024:
			return blockSize1024;
		case 2048:
			return blockSize2048;
		case 4096:
			return blockSize4096;
		default:
			return 0;
		}
	}

}
