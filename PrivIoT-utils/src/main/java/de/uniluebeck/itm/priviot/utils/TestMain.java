package de.uniluebeck.itm.priviot.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import de.uniluebeck.itm.priviot.utils.data.DataPackageParsingException;
import de.uniluebeck.itm.priviot.utils.data.EncryptionAlgorithmCodes;
import de.uniluebeck.itm.priviot.utils.data.EncryptionParameters;
import de.uniluebeck.itm.priviot.utils.data.PrivacyDataPackageMarshaller;
import de.uniluebeck.itm.priviot.utils.data.PrivacyDataPackageUnmarshaller;
import de.uniluebeck.itm.priviot.utils.data.generated.PrivacyDataPackage;
import de.uniluebeck.itm.priviot.utils.encryption.EncryptionException;
import de.uniluebeck.itm.priviot.utils.encryption.EncryptionHelper;
import de.uniluebeck.itm.priviot.utils.encryption.cipher.AsymmetricCipherer;
import de.uniluebeck.itm.priviot.utils.encryption.cipher.CiphererFactory;
import de.uniluebeck.itm.priviot.utils.encryption.cipher.SymmetricCipherer;
import de.uniluebeck.itm.priviot.utils.encryption.cipher.asymmetric.elgamal.ElgamalCipherer;
import de.uniluebeck.itm.priviot.utils.encryption.cipher.asymmetric.rsa.RSACipherer;
import de.uniluebeck.itm.priviot.utils.encryption.cipher.symmetric.aes.AESCipherer;
import de.uniluebeck.itm.priviot.utils.pseudonymization.HMacSha256PseudonymGenerator;
import de.uniluebeck.itm.priviot.utils.pseudonymization.PseudonymizationException;


public class TestMain {

    public static void main(String[] args) {
        System.out.println("== Test PrivIoT_Utils ==\n");
        
        System.out.println("-- Test serialization of EncryptedSensorDataPackage");
        if (testPrivacyDataPackage()) {
            System.out.println("=> Serialization of EncryptedSensorDataPackage works! :)");
        }
        else {
            System.out.println("=> Serialization of EncryptedSensorDataPackage doesn't work! :(");
            return;
        }
        
        System.out.println();
        System.out.println("-- Test AES encryption");
        if (testAESEncryption()) {
            System.out.println("=> AES encryption works! :)");
        }
        else {
            System.out.println("=> AES encryption doesn't work! :(");
            return;
        }        
        
        System.out.println();
        System.out.println("-- Test RSA encryption");
        if (testRSAEncryption()) {
            System.out.println("=> RSA Encryption with works! :)");
        }
        else {
            System.out.println("=> RSA Encryption doesn't work! :(");
            return;
        }
        
        boolean doTestElgamal = false;
        if (doTestElgamal) {
            System.out.println();
            System.out.println("-- Test ElGamal encryption");
            if (testElgamalEncryption()) {
                System.out.println("=> ElGamal Encryption works! :)");
            }
            else {
                System.out.println("=> ElGamal Encryption doesn't work! :(");
                return;
            }
        }
        
        System.out.println();
        System.out.println("-- Test HMac256PseudonymGenerator");
        if (testHmac256PseudonymGenerator()) {
        	System.out.println("=> HMac256PseudonymGenerator works! :)");
        }
        else {
        	System.out.println("=> HMac256PseudonymGenerator doesn't work! :(");
        	return;
        }
        
        System.out.println();
        System.out.println("-- Test PseudonymizationProcessor");
        if (testPseudonymizationProcessor()) {
        	System.out.println("=> PseudonymizationProcessor works! :)");
        }
        else {
        	System.out.println("=> PseudonymizationProcessor doesn't work! :(");
        	return;
        }
        
        System.out.println();
        System.out.println("-- Test EncryptionProcessor");
        if (testEncryptionProcessor()) {
        	System.out.println("=> EncryptionProcessor works! :)");
        }
        else {
        	System.out.println("=> EncryptionProcessor doesn't work! :(");
        	return;
        }
    }
    
    private static boolean testPrivacyDataPackage() {
    	String sensorUri = "www.pseudonym.com/xyz";
    	String encryptionAlgorithmCode = EncryptionAlgorithmCodes.AES_256_CBC;
        byte[] content = "blabla".getBytes();
        byte[] iv = {-43, 120, 2};
        byte[] key = "keykey".getBytes();
        
        PrivacyDataPackage dataPackage = new PrivacyDataPackage();
        dataPackage.setSensorUri(sensorUri);
        dataPackage.setSymmetricEncryptionAlgorithmCode(encryptionAlgorithmCode);
        dataPackage.setEncryptedContent(Base64.encodeBase64String(content));
        dataPackage.setInitializationVector(Base64.encodeBase64String(iv));
        dataPackage.setEncryptedSymmetricKey(Base64.encodeBase64String(key));
        
        printDataPackage(dataPackage);
        
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
           PrivacyDataPackageMarshaller.marshal(dataPackage, outStream);
        }
        catch (JAXBException | XMLStreamException e) {
            e.printStackTrace();
            return false;
        }
        
        System.out.println("XML representation:");
        System.out.println(outStream.toString());
        
        ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
        PrivacyDataPackage dataPackage2;
        try {
            dataPackage2 = PrivacyDataPackageUnmarshaller.unmarshal(inStream);
        } catch (JAXBException | XMLStreamException e) {
            e.printStackTrace();
            return false;
        }
        if (dataPackage2 == null) {
            System.out.println("Error");
        }
        
        System.out.println("Parsed data package:");
        printDataPackage(dataPackage2);
        
        if (dataPackage.getSensorUri().equals(dataPackage2.getSensorUri()) &&
            dataPackage.getSymmetricEncryptionAlgorithmCode().equals(dataPackage2.getSymmetricEncryptionAlgorithmCode()) &&
            dataPackage.getEncryptedContent().equals(dataPackage2.getEncryptedContent()) &&
            dataPackage.getInitializationVector().equals(dataPackage2.getInitializationVector()) &&
            dataPackage.getEncryptedSymmetricKey().equals(dataPackage2.getEncryptedSymmetricKey())) {
            return true;
        }
        else {
            System.out.println("Error: Packages are not equal");
            return false;
        }
    }
    
    private static boolean testAESEncryption() {
        SymmetricCipherer cipherer = createAESCipherer();
        int keysize = 256;
        String plaintextInStr = "Plaintextmessage Plaintextmessage Plaintextmessage";
        byte[] plaintextIn = plaintextInStr.getBytes();
        byte[] ciphertext;
        byte[] plaintextOut;
        
        try {
            if (EncryptionHelper.getMaxKeySizeAES() <= 128) {
                EncryptionHelper.printRestrictionMessage();
                return false;
            }
        } catch (NoSuchAlgorithmException e) {
            System.out.println("AES algorithm not supported");
            return false;
        }
        
        Date startInit = new Date();
        try {
            cipherer.initialize(keysize);
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("Exception during initialize: " + e.getMessage());
            return false;
        }
        Date endInit = new Date();
        
        Date startGen = new Date();
        cipherer.generateKey();
        Date endGen = new Date();
        
        byte[] keyBytes = cipherer.getKeyAsByteArray();
        
        System.out.println("initialized cipherer with keysize " + keysize + " Bit in " + (endInit.getTime() - startInit.getTime()) + " ms");
        System.out.println("generated " + keysize + " Bit Key (In praxis " + (keyBytes.length * 8) + " Bit) in " + (endGen.getTime() - startGen.getTime()) + " ms");
        
        System.out.println("Algorithm configuration: " + cipherer.getConfiguration());
        
        try {
            ciphertext = cipherer.encrypt(plaintextIn);
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | ShortBufferException | InvalidAlgorithmParameterException e) {
            System.out.println("Exception during encrypt: " + e.getMessage());
            return false;
        }
        
        System.out.println("Encrypted '" + Arrays.toString(plaintextIn) + "'");
        System.out.println("          ('" + plaintextInStr + "')");
        System.out.println("      as: '" + Arrays.toString(ciphertext) + "'");
        
        
        SymmetricCipherer cipherer2 = createAESCipherer();
        try {
            cipherer2.initialize(cipherer.getKeySize());
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("Exception during initialize: " + e.getMessage());
            return false;
        }
        try {
            cipherer2.setIvFromByteArray(cipherer.getIvAsByteArray());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Exception during setIvFromByteArray: " + e.getMessage());
            return false;
        }
        try {
            cipherer2.setKeyFromByteArray(cipherer.getKeyAsByteArray());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Exception during setKeyFromByteArray: " + e.getMessage());
            return false;
        }
        
        
        try {
            plaintextOut = cipherer2.decrypt(ciphertext);
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException e) {
            System.out.println("Exception during decrypt: " + e.getMessage());
            return false;
        }
        
        System.out.println("Decrypted '" + Arrays.toString(ciphertext) + "'");
        System.out.println("      to: '" + Arrays.toString(plaintextOut) + "'");
        System.out.println("          ('" + new String(plaintextOut) + "')");
        
        return Arrays.equals(plaintextIn, plaintextOut);
    }
    
    private static boolean testRSAEncryption() {
        String plaintextInStr = "rsamessage rsamessage rsamessage rsamessage rsamessage rsamessage";
        int keysize = 1024;
        RSACipherer cipherer;
        
        try {
            cipherer = new RSACipherer();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            System.out.println("Exception during construct in RSA test: " + e.getMessage());
            return false;
        }
        
        return testAsymmetricEncryption(cipherer, keysize, plaintextInStr);
    }
    
    private static boolean testElgamalEncryption() {
        String plaintextInStr = "elgamalmessage elgamalmessage elgamalmessage elgamalmessage elgamalmessage";
        int keysize = 1024;
        ElgamalCipherer cipherer;
        
        try {
            cipherer = new ElgamalCipherer();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            System.out.println("Exception during construct in RSA test: " + e.getMessage());
            return false;
        }
        
        return testAsymmetricEncryption(cipherer, keysize, plaintextInStr);
    }
    
    /**
     * tests the given asymmetric encryption.
     * The plaintext is enrypted and the resulting ciphertext decrypted again. The result is compared with the plaintext.
     * @param keysize Length of the keys in bit
     * @param plaintextInStr Plaintext to encrypt
     * @return true, if result equals plaintext.
     */
    private static boolean testAsymmetricEncryption(AsymmetricCipherer cipherer, int keysize, String plaintextInStr) {
        byte[] plaintextIn = plaintextInStr.getBytes();
        byte[] ciphertext;
        byte[] plaintextOut;
        
        Date startInit = new Date();
        try {
            cipherer.initialize(keysize);
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("Exception during initialize: " + e.getMessage());
            return false;
        }
        Date endInit = new Date();
        
        Date startGen = new Date();
        cipherer.generateKey();
        Date endGen = new Date();
        
        byte[] publicKeyBytes = cipherer.getPublicKeyAsByteArray();
        
        System.out.println("inititlized cipherer with keysize " + keysize + " Bit in " + (endInit.getTime() - startInit.getTime()) + " ms");
        System.out.println("generated " + keysize + " Bit Public Key (In praxis " + (publicKeyBytes.length * 8) + " Bit) in " + (endGen.getTime() - startGen.getTime()) + " ms");
        
        System.out.println("Algorithm configuration: " + cipherer.getConfiguration());
        
        try {
            ciphertext = cipherer.encrypt(plaintextIn);
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | ShortBufferException | InvalidAlgorithmParameterException e) {
            System.out.println("Exception during encrypt: " + e.getMessage());
            return false;
        }
        
        System.out.println("Encrypted '" + Arrays.toString(plaintextIn) + "'");
        System.out.println("          ('" + plaintextInStr + "')");
        System.out.println("      as: '" + Arrays.toString(ciphertext) + "'");
        
        try {
            plaintextOut = cipherer.decrypt(ciphertext);
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException e) {
            System.out.println("Exception during decrypt: " + e.getMessage());
            return false;
        }
        
        System.out.println("Decrypted '" + Arrays.toString(ciphertext) + "'");
        System.out.println("      to: '" + Arrays.toString(plaintextOut) + "'");
        System.out.println("          ('" + new String(plaintextOut) + "')");
        
        return Arrays.equals(plaintextIn, plaintextOut);
    }
    
    private static boolean testHmac256PseudonymGenerator() {
    	HMacSha256PseudonymGenerator generator = new HMacSha256PseudonymGenerator();
    	String text = "plaintextplaintextplaintext";
    	
    	try {
			generator.inititialize();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Exception during initialization of HMac256PseudonymGenerator: " + e.getMessage());
			return false;
		}
    	
    	byte[] secret = generator.createSecret();
    	
    	System.out.println("Generate two HMAC-256");
    	System.out.println("  for plaintext '" + text + "'");
    	System.out.println("  with secret " + Arrays.toString(secret));
    	
    	String pseudonym;
    	try {
			pseudonym = generator.generatePseudonym(text, secret);
		} catch (InvalidKeyException e) {
			System.out.println("Exception during generation of pseudonym: " + e.getMessage());
			return false;
		}
    	
    	System.out.println("Pseudonym is: " + pseudonym);
    	
    	return true;
    }
    
    private static boolean testPseudonymizationProcessor() {
    	String plaintext = "plaintextplaintextplaintextplaintext";
    	byte[] secret;
    	int timePeriod = 4;
    	
    	System.out.println("timePeriod: " + timePeriod);
    	System.out.println("Plaintext: '" + plaintext + "'");
    	
		try {
			secret = PseudonymizationProcessor.generateHmac256Secret();
		} catch (PseudonymizationException e) {
			System.out.println(e.getMessage());
			return false;
		}
    	
    	System.out.println("Generated secret: " + Arrays.toString(secret));
    	
    	String pseudonym1;
		try {
			pseudonym1 = PseudonymizationProcessor.generateHmac256Pseudonym(plaintext, timePeriod, secret);
		} catch (PseudonymizationException e) {
			System.out.println(e.getMessage());
			return false;
		}
    	
    	System.out.println("First Pseudonym: '" + pseudonym1 + "'");
    	
    	String pseudonym2;
		try {
			pseudonym2 = PseudonymizationProcessor.generateHmac256Pseudonym(plaintext, timePeriod, secret);
		} catch (PseudonymizationException e) {
			System.out.println(e.getMessage());
			return false;
		}
    	
    	System.out.println("Second Pseudonym: '" + pseudonym2 + "'");
    	
    	System.out.println("Sleep 5 seconds...");
    	
    	try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			System.out.println("Interrupted: " + e.getMessage());
		}
    	
    	String pseudonym3;
		try {
			pseudonym3 = PseudonymizationProcessor.generateHmac256Pseudonym(plaintext, timePeriod, secret);
		} catch (PseudonymizationException e) {
			System.out.println(e.getMessage());
			return false;
		}
    	
    	System.out.println("Third Pseudonym: '" + pseudonym3 + "'");
    	
    	return (pseudonym1.equals(pseudonym2) && !pseudonym1.equals(pseudonym3));
    }
    
    private static boolean testEncryptionProcessor() {
    	String content = "plaintextplaintextplaintextplaintextplaintextplaintextplaintextplaintextplaintext";
    	String sensorUri = "www.sensor.com/sensor1";
    	String symmetricEncryptionAlgorithmCode = EncryptionAlgorithmCodes.AES_256_CBC;
    	String certificatePathStr = "src/main/resources/test.cert";
    	String privateKeyPathStr = "src/main/resources/test_private_key.der";
    	
    	// load certificate
    	X509Certificate certificate;
		try {
			certificate = loadCertificateFromFile(certificatePathStr);
		} catch (CertificateException | IOException e) {
			System.out.println(e.getMessage());
			return false;
		}
		
		System.out.println("loaded certificate from file");
		
		// get public key from certificate
    	PublicKey publicKey = certificate.getPublicKey();
    	
    	// get encryption parameters from algorithm code and public key
    	EncryptionParameters encryptionParameters;
		try {
			encryptionParameters = new EncryptionParameters(symmetricEncryptionAlgorithmCode, 
					                                        publicKey);
		} catch (EncryptionException e) {
			System.out.println(e.getMessage());
			return false;
		}
		
		System.out.println("Encrpytion parameters:");
		System.out.println("  symmetric encryption algorithm: " + encryptionParameters.getAsymmetricEncryptionAlgorithm());
		System.out.println("  symmetric encryption bit strength: " + encryptionParameters.getAsymmetricEncryptionBitStrength());
		System.out.println("  asymmetric encryption algorithm: " + encryptionParameters.getSymmetricEncryptionAlgorithm());
		System.out.println("  asymmetric encryption bit strength: " + encryptionParameters.getSymmetricEncryptionBitStrength());
    	
    	byte[] sensorSecret;
    	try {
    		sensorSecret = PseudonymizationProcessor.generateHmac256Secret();
		} catch (PseudonymizationException e) {
			System.out.println(e.getMessage());
			return false;
		}
    	
    	String sensorUriPseudonym = "www.pseudonym.com/";
    	try {
    		sensorUriPseudonym += PseudonymizationProcessor.generateHmac256Pseudonym(sensorUri, 10, sensorSecret);
		} catch (PseudonymizationException e) {
			System.out.println(e.getMessage());
			return false;
		}
    	
    	System.out.println("generated sensor URI pseudonym: " + sensorUriPseudonym);
    	
    	System.out.println("generate PrivacyDataPackage with content: " + content);
    	
    	PrivacyDataPackage dataPackage;
    	try {
    		dataPackage = EncryptionProcessor.createPrivacyDataPackage(content, 
                                                                       sensorUriPseudonym, 
                                                                       encryptionParameters, 
                                                                       publicKey.getEncoded());
		} catch (EncryptionException e) {
			System.out.println(e.getMessage());
			return false;
		}
    	
    	// load certificate
    	PrivateKey privateKey;
		try {
			privateKey = loadPrivateKeyFromFile(privateKeyPathStr);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			System.out.println(e.getMessage());
			return false;
		}
		
		System.out.println("loaded private key from file");
    	
		byte[] decryptedContent;
    	try {
    		decryptedContent = EncryptionProcessor.getContentOfPrivacyDataPackage(dataPackage, privateKey);
		} catch (EncryptionException e) {
			System.out.println(e.getMessage());
			return false;
		}
    	
    	System.out.println("Decrypted content: " + new String(decryptedContent));
    	
    	return content.equals(new String(decryptedContent));
    }
    
    private static X509Certificate loadCertificateFromFile(String certificatePathStr) throws CertificateException, IOException {    	
        Path certPath = Paths.get(certificatePathStr);
        
        if (!(new File(certificatePathStr)).exists()) {
            System.out.println("Certificate file not found. Please check the path given in configuration: '" + certificatePathStr + "'");
            return null;
        }
        
    	final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        
        ByteArrayInputStream stream = new ByteArrayInputStream(Files.readAllBytes(certPath));
        
        final Collection<? extends Certificate> certs =
             (Collection<? extends Certificate>) certFactory.generateCertificates(stream);
        
        if (certs.size() == 0) {
        	System.out.println("No certificate found in file '" + certificatePathStr + "'");
        	return null;
        }
        
        if (certs.size() > 1) {
        	System.out.println("More than one certificate found in file '" + certificatePathStr + "'. Load first one.");
        }
        
    	Certificate cert = certs.iterator().next();
    	if (cert instanceof X509Certificate) {    		
    		return (X509Certificate)cert;
    	}
    	else {
    		System.out.println("Certificate in file '" + certificatePathStr + "' is not a X.509 certificate");
    		return null;
    	}        
    }
    
    private static PrivateKey loadPrivateKeyFromFile(String privateKeyPathStr) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    	File file = new File(privateKeyPathStr);
    	
    	if (!new File(privateKeyPathStr).exists()) {
    		System.out.println("Private key file does not exist: '" + privateKeyPathStr + "'");
    		return null;
    	}
    	
    	if (!file.getName().endsWith(".der")) {
    		System.out.println("Private key file has to be in .der format");
    		return null;
    	}
    	
    	// get private key of SSP
    	
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int)file.length()];
        dis.readFully(keyBytes);
        dis.close();
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        
        return privateKey;
    }
    
    private static SymmetricCipherer createAESCipherer() {
        try {
            return  new AESCipherer();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            System.out.println("Exception during construct in AES test: " + e.getMessage());
            return null;
        }
    }
    
    private static AsymmetricCipherer createRSACipherer() {
        try {
            return  new RSACipherer();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            System.out.println("Exception during construct in RSA test: " + e.getMessage());
            return null;
        }
    }
    
    private static void printDataPackage(PrivacyDataPackage dataPackage) {
        System.out.println("Attributes:");
        System.out.println("SensorUri: " + dataPackage.getSensorUri());
        System.out.println("EncryptionAlgorithmCode: " + dataPackage.getSymmetricEncryptionAlgorithmCode());
        System.out.println("encryptedInitializationVector: " + new String(dataPackage.getInitializationVector()));
        System.out.println("encryptedKey: " + new String(dataPackage.getEncryptedSymmetricKey()));
        System.out.println("encryptedContent: " + new String(dataPackage.getEncryptedContent()));
    }

}
