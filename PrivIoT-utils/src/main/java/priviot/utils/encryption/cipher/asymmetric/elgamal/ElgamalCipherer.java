package priviot.utils.encryption.cipher.asymmetric.elgamal;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

import  org.bouncycastle.jce.provider.BouncyCastleProvider;

import priviot.utils.encryption.cipher.AsymmetricCipherer;

/**
 * Encapsulates implementation of symmetric cipher using Elgamal algorithm.
 * Implementation used is bouncycastle as provider in java.security.
 * Code is taken from http://opensourcejavaphp.net/java/bouncycastle/org/bouncycastle/jce/provider/test/ElGamalTest.java.html
 * 
 * To make this code work, the static method initializeClass() has to be called.
 * 
 * Important: This needs the policy files of UnlimitedJCEPolicyJDK7.zip (available on oracles website) to be copied in Java-Home. Otherwise their will be an exception with message "Wrong Key or default parameters"
 */
public class ElgamalCipherer extends AsymmetricCipherer {
	
    private static boolean isInitialized;
    
    private static String algorithmName = "ElGamal/ECB/PKCS1Padding";
    
	//TODO: choose very secure g and p parameters. They can be fixed, but we better choose very secure parameters.

    /** Elgamal parameter g for keysize 1024. This is the generator parameter or the base in diffie-hellman and a primitive root of p. */
    private static BigInteger  g1024 = new BigInteger("4c16756db8c5cb2faaf20fbf3d915bc8a7900959c69d5f419827db808e92aa21e238896eb88ff91bc01cc8ce4f81ad67d50affcd8e0e0fb188a757b7cad06e1323ae6c1f8a6dd6b3851692f48cb607a4447e00fd17758382f1fa2aa1a838f26e76b023e999c326fb9acfb0fb9e82f66597b9969412b2c14eba5ee4d0488cf12f", 16);
    /** Elgamal parameter p (sometimes calles q) for keysize 1024. This is the order of the group. */
    private static BigInteger  p1024 = new BigInteger("90e69ce3d3299c18f45ce25f9058657c47c0b406ac2e7fbae70e529041320e8a0d19695c9042ca5e9ecd43da4b23187c9072b980a20bfb901cd2a2baa493d358dca7c7a4ff24d9848734abb77615a37a8c45de49576ae1207055441d7aab44166f25bcb8669b7bccaa98dbab488e7241e2cc13daafc77ea396f4dbe5145db3bf", 16);

    /** Elgamal parameter g for keysize 2048. This is the generator parameter or the base in diffie-hellman and a primitive root of p. */
    private static BigInteger  g2048 = new BigInteger("454d76d6b74f5106cf28fb6658f51f33f85cfbaf34e4e514b0eb6b2de59e3e1e9ea525f048925a11a64b45ccb6d3265bc1487ec279565e4c76ae0ef2ab8e98b01b7273cacc1a65a0cace20c15f5cf1b5247bf03eec14458759cab0b60f67f8aa358935d0f33f1890f264cac9de85a9b52fa21506b9856276217676de27daf85b60d2472d524bc8df77af5277bbe0b9dd972ce0f5275f3aac015358db4591d72347f62fa552d6a7faa56c37f843388e45a8a1cbbdd0c4b553699e10bcd50992309dab46ef6f605e00f635ce33a0cc1ca82f1d6bf92dfbf93ebfb3b41d39bd405700ddbd6ce38bdf231f0ba3df0e80a036886d7c4e2c899f29e9440627d596bdd", 16);
    /** Elgamal parameter p (sometimes calles q) for keysize 2048. This is the order of the group. */
    private static BigInteger  p2048 = new BigInteger("8c9345140d38a20817730097f5e0611de4f53851ba884db755281e0f06363b8deaf4f9b4aec1afb24d0a999ef6ce45ed6a17eadb8cce397055676792f3b67b36f9fd05d8d51f06ae4be5f14c86e0cb6caff847e8ac5871d63302bb922451c0c55c97d20d36c8ec3db90f51c53aed352cd1a2f0dbc615c7d6062699c6facf03367a3d3f8a9909526e31f2f4fc09a1499b5b0dfee270c6a0a306d17cdbd06f3a016f781d65139a06d326f2ac36a0d9a81b1ee97d979e2f85eb5a13e2e2911ffb96748c91040ca3df00d15c3ff985699bdc9abccb2cb854b150541ae27d620910e86b551de9098a089486cd565b0c4ec01c729abaa08b616d1dfce66a2888cab41b", 16);
	
	private PublicKey publicKey;
	private PrivateKey privateKey;
	
	private KeyFactory keyFactory;
	private Cipher cipher;
	private KeyPairGenerator keyPairGenerator;
	private DHParameterSpec params;
	private SecureRandom secureRandom;
	
	private int keysize = 0;
	
	/**
	 * Adds the Bouncycastle provider to java.security.Security
	 */
	public static void initializeClass() {
		Security.addProvider(new BouncyCastleProvider());
		isInitialized = true;
	}
	
	/**
	 * Constructor.
	 * @throws NoSuchAlgorithmException  Algorithm ElGamal/ECB not supported locally
	 * @throws NoSuchProviderException   Provider Bouncycastle is not working
	 * @throws NoSuchPaddingException    Padding PKCS1Padding not supported locally
	 */
	public ElgamalCipherer() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
	    if (!isInitialized) {
	        ElgamalCipherer.initializeClass();
	    }
	    
		keyPairGenerator = KeyPairGenerator.getInstance("ElGamal", "BC");
		cipher = Cipher.getInstance(algorithmName, "BC");
		keyFactory = KeyFactory.getInstance("ElGamal", "BC");
		secureRandom = new SecureRandom();
	}
	
	@Override
	public void initialize(int keysize) throws InvalidAlgorithmParameterException {
		this.keysize = keysize;
		
		params = new DHParameterSpec(getParamP(keysize), getParamG(keysize));
		
		keyPairGenerator.initialize(keysize, secureRandom);
	}
	
	private BigInteger getParamP(int keysize) throws IllegalArgumentException {
		switch (keysize) {
		case 1024:
			return p1024;
		case 2048:
			return p2048;
		default:
			throw new IllegalArgumentException("Bad Keysize: " + keysize);
		}
	}
	
	private BigInteger getParamG(int keysize) throws IllegalArgumentException {
		switch (keysize) {
		case 1024:
			return g1024;
		case 2048:
			return g2048;
		default:
			throw new IllegalArgumentException("Bad Keysize: " + keysize);
		}
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
    public byte[] getPrivateKeyAsByteArray() {
        if (privateKey == null) {
            return new byte[0];
        }
        
        return privateKey.getEncoded();  
    }

    @Override
    public void setPrivateKeyFromByteArray(byte[] privateKeyBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec keySpec = new X509EncodedKeySpec(privateKeyBytes);
        privateKey = keyFactory.generatePrivate(keySpec);
    }

	@Override
	public byte[] encrypt(byte[] plaintext) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, ShortBufferException {		
		cipher.init(Cipher.ENCRYPT_MODE, publicKey, secureRandom);
		
		//return encryptdecryptInternal(cipher, blockSize, plaintext);
		
		return cipher.doFinal(plaintext);
	}
	
	@Override
	public byte[] encryptWithPrivateKey(byte[] plaintext) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		cipher.init(Cipher.ENCRYPT_MODE, privateKey, secureRandom);
		return cipher.doFinal(plaintext);
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
		System.out.println("doFinal(in, position: " + outLen + ", blockSize: " + (in.length - outLen) + ", out, position: " + outLen);
		initializedCipher.doFinal(in, outLen, in.length - outLen, out, outLen);
		
		return out;
	}

	@Override
	public byte[] decrypt(byte[] ciphertext) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		cipher.init(Cipher.DECRYPT_MODE, privateKey, secureRandom);
		return cipher.doFinal(ciphertext);
	}

	@Override
	public byte[] decryptWithPublicKey(byte[] ciphertext) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		cipher.init(Cipher.DECRYPT_MODE, publicKey, secureRandom);
		return cipher.doFinal(ciphertext);
	}

	@Override
	public String getConfiguration() {
		return ("Algorithm: " + cipher.getAlgorithm() + ", Provider: " + cipher.getProvider() + ", Keysize: " + keysize);
	}
	
	public void printParameters() {
		BigInteger p = ((DHPublicKey)publicKey).getParams().getP();
		BigInteger g = ((DHPublicKey)publicKey).getParams().getG();
		System.out.println("P: BigInteger(" + p.toString(16) + ", 16)");
		System.out.println("G: BigInteger(" + g.toString(16) + ", 16)");
	}

	/**
     * Returns the algorithm as a standard formated String of the java crypto API.
     * @return algorithm name
     */
    public static String getAlgorithm() {
        return algorithmName;
    }

    @Override
    public int getKeySize() {
        return keysize;
    }

    @Override
    public String getUsedAlgorithm() {
        return cipher.getAlgorithm();
    }

	@Override
	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}

}
