package de.uniluebeck.itm.priviot.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import de.uniluebeck.itm.priviot.utils.data.DataPackageParsingException;
import de.uniluebeck.itm.priviot.utils.data.transfer.EncryptedSensorDataPackage;
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
        if (testEncryptedSensorDataPackage()) {
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
        System.out.println("-- Test EncryptedSensorDataPackage with AES encrypted content");
        if (testAESEncryptionWithEncryptedSensorDataPackage()) {
            System.out.println("=> EncryptedSensorDataPackage with AES encrypted content works! :)");
        }
        else {
            System.out.println("=> EncryptedSensorDataPackage with AES encrypted content doesn't work! :(");
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
        System.out.println("-- Test EncryptedSensorDataPackage with AES encrypted content and RSA encrypted key");
        if (testRSAWithAESWithEncryptedSensordataPackage()) {
            System.out.println("=> EncryptedSensorDataPackage with AES encrypted content and RSA encrypted key works! :)");
        }
        else {
            System.out.println("=> EncryptedSensorDataPackage with AES encrypted content and RSA encrypted key doesn't work! :(");
            return;
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
    }
    
    private static boolean testEncryptedSensorDataPackage() {
        String asyMethod = "rsa";
        int asyKeySize = 1024;
        String symMethod = "aes";
        int symKeySize = 128;
        int lifetime = 5;
        byte[] content = "blabla".getBytes();
        byte[] iv = {-43, 120, 2};
        byte[] key = "keykey".getBytes();
        
        EncryptedSensorDataPackage dataPackage = new EncryptedSensorDataPackage();
        dataPackage.setAsymmetricEncryptionMethod(asyMethod);
        dataPackage.setAsymmetricEncryptionBitStrength(asyKeySize);
        dataPackage.setSymmetricEncryptionMethod(symMethod);
        dataPackage.setSymmetricEncryptionBitStrength(symKeySize);
        dataPackage.setContentLifetime(lifetime);
        dataPackage.setEncryptedContent(content);
        dataPackage.setEncryptedInitializationVector(iv);
        dataPackage.setEncryptedKey(key);
        
        printDataPackage(dataPackage);
        
        String xml;
        try {
           xml = dataPackage.toXMLString(); 
        }
        catch (DOMException e) {
            e.printStackTrace();
            return false;
        }
        
        System.out.println("XML representation:");
        System.out.println(xml);
        
        EncryptedSensorDataPackage dataPackage2;
        try {
            dataPackage2 = EncryptedSensorDataPackage.createInstanceFromXMLString(xml);
        } catch (NumberFormatException | SAXException | DataPackageParsingException e) {
            e.printStackTrace();
            return false;
        }
        if (dataPackage2 == null) {
            System.out.println("Error");
        }
        
        System.out.println("Parsed data package:");
        printDataPackage(dataPackage2);
        
        if (dataPackage.getAsymmetricEncryptionMethod().equals(dataPackage2.getAsymmetricEncryptionMethod()) &&
            dataPackage.getAsymmetricEncryptionBitStrength() == dataPackage2.getAsymmetricEncryptionBitStrength() &&
            dataPackage.getSymmetricEncryptionMethod().equals(dataPackage2.getSymmetricEncryptionMethod()) &&
            dataPackage.getSymmetricEncryptionBitStrength() == dataPackage2.getSymmetricEncryptionBitStrength() &&
            dataPackage.getContentLifetime() == dataPackage2.getContentLifetime() &&
            Arrays.equals(dataPackage.getEncryptedContent(), dataPackage2.getEncryptedContent()) &&
            Arrays.equals(dataPackage.getEncryptedInitializationVector(), dataPackage2.getEncryptedInitializationVector()) &&
            Arrays.equals(dataPackage.getEncryptedKey(), dataPackage2.getEncryptedKey())) {
            
                byte[] content2 = dataPackage2.getEncryptedContent();
                byte[] iv2 = dataPackage2.getEncryptedInitializationVector();
                byte[] key2 = dataPackage2.getEncryptedKey();
                
                if (Arrays.equals(content, content2) &&
                    Arrays.equals(iv, iv2) &&
                    Arrays.equals(key, key2)) {
                    return true;
                }
                else {
                    System.out.println("Error: Result is not equal to input");
                    return false;
                }
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
    
    private static boolean testAESEncryptionWithEncryptedSensorDataPackage() {        
        SymmetricCipherer aesCipherer = createAESCipherer();
        String message = "Message Message Message Message Message Message Message Message Message Message Message Message Message Message Message Message Message Message";
        byte[] plaintextIn = message.getBytes();
        int keysize = 256;
        
        System.out.println("Message: '" + message + "'");
        System.out.println("will be encrypted with " + AESCipherer.getAlgorithm() + " with keysize " + keysize);
        
        try {
            aesCipherer.initialize(keysize);
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("Exception during initialize: " + e.getMessage());
        }
        
        aesCipherer.generateKey();
        
        
        EncryptedSensorDataPackage dataPackage = new EncryptedSensorDataPackage();
        dataPackage.setAsymmetricEncryptionMethod("");
        dataPackage.setAsymmetricEncryptionBitStrength(0);
        dataPackage.setSymmetricEncryptionMethod(aesCipherer.getUsedAlgorithm());
        dataPackage.setSymmetricEncryptionBitStrength(keysize);
        
        
        byte[] ciphertext;
        byte[] keyBytes = aesCipherer.getKeyAsByteArray();
        byte[] ivBytes = aesCipherer.getIvAsByteArray();
        try {
            ciphertext = aesCipherer.encrypt(plaintextIn);
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | ShortBufferException | InvalidAlgorithmParameterException e) {
            System.out.println("Exception during encrypt: " + e.getMessage());
            return false;
        }
        
        //System.out.println("ciphertext: " + Arrays.toString(ciphertext));
        //System.out.println("iv: " + Arrays.toString(ivBytes));
        //System.out.println("key: " + Arrays.toString(keyBytes));
        
        dataPackage.setEncryptedContent(ciphertext);
        dataPackage.setEncryptedInitializationVector(ivBytes);
        dataPackage.setEncryptedKey(keyBytes);
        
        //printDataPackage(dataPackage);
        
        String xml;
        try {
           xml = dataPackage.toXMLString(); 
        }
        catch (DOMException e) {
            e.printStackTrace();
            return false;
        }
        
        System.out.println("XML representation:");
        System.out.println(xml);
        
        
        EncryptedSensorDataPackage dataPackage2;
        try {
            dataPackage2 = EncryptedSensorDataPackage.createInstanceFromXMLString(xml);
        } catch (NumberFormatException | SAXException | DataPackageParsingException e) {
            e.printStackTrace();
            return false;
        }
        
        //System.out.println("Parsed data package:");
        //printDataPackage(dataPackage2);
        
        //System.out.println("ciphertext: " + Arrays.toString(dataPackage2.getEncryptedContent()));
        //System.out.println("iv: " + Arrays.toString(dataPackage2.getEncryptedInitializationVector()));
        //System.out.println("key: " + Arrays.toString(dataPackage2.getEncryptedKey()));
        
        
        SymmetricCipherer aesCipherer2;
        try {
            aesCipherer2 = CiphererFactory.createSymmetricCipherer(dataPackage2.getSymmetricEncryptionMethod());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            System.out.println("Exception during create symmetric cipherer: " + e.getMessage());
            return false;
        }
        if (aesCipherer2 == null) {
            System.out.println("No symmetric cipherer found for algorithm " + dataPackage2.getSymmetricEncryptionMethod());
            return false;
        }
        try {
            aesCipherer2.initialize(keysize);
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("Exception during initialize: " + e.getMessage());
            return false;
        }
        try {
            aesCipherer2.setIvFromByteArray(dataPackage2.getEncryptedInitializationVector());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Exception during setIvFromByteArray: " + e.getMessage());
            return false;
        }
        try {
            aesCipherer2.setKeyFromByteArray(dataPackage2.getEncryptedKey());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Exception during setKeyFromByteArray: " + e.getMessage());
            return false;
        }
        
        byte[] ciphertext2 = dataPackage2.getEncryptedContent();
        
        System.out.println("will be decrypted with: " + aesCipherer2.getUsedAlgorithm() + " with keysize: " + aesCipherer2.getKeySize());
        
        byte[] plaintextOut;
        try {
            plaintextOut = aesCipherer2.decrypt(ciphertext2);
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException e) {
            System.out.println("Exception during decrypt: " + e.getMessage());
            return false;
        }
        
        System.out.println("Decrypted Message: '" + new String(plaintextOut) + "'");
        
        return (Arrays.equals(plaintextOut, plaintextIn));
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
    
    private static boolean testRSAWithAESWithEncryptedSensordataPackage() {
        SymmetricCipherer aesCipherer = createAESCipherer();
        AsymmetricCipherer rsaCipherer = createRSACipherer();
        AsymmetricCipherer rsaCipherer2 = createRSACipherer(); // cipherer of recipient
        String message = "Message Message Message Message Message Message Message Message Message Message Message Message Message Message Message Message Message Message";
        byte[] plaintextIn = message.getBytes();
        int keysize = 256;
        int rsaKeysize = 1024;
        
        try {
            rsaCipherer2.initialize(rsaKeysize);
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("Exception during initialize of rsa cipherer: " + e.getMessage());
            return false;
        }
        
        rsaCipherer2.generateKey();
        
        byte[] rsaPublicKey = rsaCipherer2.getPublicKeyAsByteArray();
        
        try {
            rsaCipherer.initialize(rsaKeysize);
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("Exception during initialize of rsa cipherer: " + e.getMessage());
        }
        
        System.out.println("RSA public key of recipient: " + Arrays.toString(rsaPublicKey));
        
        try {
            rsaCipherer.setPublicKeyFromByteArray(rsaPublicKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Exception during set public key at sender: " + e.getMessage());
        }
        
        System.out.println("RSA public key set at sender");
        
        System.out.println("Message: '" + message + "'");
        System.out.println("will be encrypted with " + aesCipherer.getUsedAlgorithm() + " with keysize " + keysize);
        
        try {
            aesCipherer.initialize(keysize);
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("Exception during initialize: " + e.getMessage());
        }
        
        aesCipherer.generateKey();

        byte[] key = aesCipherer.getKeyAsByteArray();
        
        System.out.println("Key: '" + Arrays.toString(key) + "'");
        System.out.println("will be encrypted with " + rsaCipherer.getUsedAlgorithm() + " with keysize " + rsaKeysize);
        
        byte[] encryptedKey = new byte[0];
        try {
            encryptedKey = rsaCipherer.encrypt(key);
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | ShortBufferException
                | InvalidAlgorithmParameterException e) {
            System.out.println("Exception during encryption of key: " + e.getMessage());
            return false;
        }
        
        EncryptedSensorDataPackage dataPackage = new EncryptedSensorDataPackage();
        dataPackage.setAsymmetricEncryptionMethod(rsaCipherer.getUsedAlgorithm());
        dataPackage.setAsymmetricEncryptionBitStrength(rsaKeysize);
        dataPackage.setSymmetricEncryptionMethod(aesCipherer.getUsedAlgorithm());
        dataPackage.setSymmetricEncryptionBitStrength(keysize);
        
        
        byte[] ivBytes = aesCipherer.getIvAsByteArray();
        byte[] ciphertext;
        try {
            ciphertext = aesCipherer.encrypt(plaintextIn);
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | ShortBufferException | InvalidAlgorithmParameterException e) {
            System.out.println("Exception during encrypt: " + e.getMessage());
            return false;
        }
        
        dataPackage.setEncryptedContent(ciphertext);
        dataPackage.setEncryptedInitializationVector(ivBytes);
        dataPackage.setEncryptedKey(encryptedKey);
        
        //printDataPackage(dataPackage);
        
        String xml;
        try {
           xml = dataPackage.toXMLString(); 
        }
        catch (DOMException e) {
            e.printStackTrace();
            return false;
        }
        
        System.out.println("XML representation:");
        System.out.println(xml);
        
        
        EncryptedSensorDataPackage dataPackage2;
        try {
            dataPackage2 = EncryptedSensorDataPackage.createInstanceFromXMLString(xml);
        } catch (NumberFormatException | SAXException | DataPackageParsingException e) {
            e.printStackTrace();
            return false;
        }
        
        //System.out.println("Parsed data package:");
        //printDataPackage(dataPackage2);
        
        if (! rsaCipherer2.getUsedAlgorithm().equals(dataPackage2.getAsymmetricEncryptionMethod())) {
            System.out.println("asymmetric encryption method not supported");
            return false;
        }
        if (rsaCipherer2.getKeySize() != dataPackage2.getAsymmetricEncryptionBitStrength()) {
            System.out.println("asymmetric key size does not match asymmetric key pair of recipient");
            return false;
        }
        byte[] decryptedKey = new byte[0];
        try {
            decryptedKey = rsaCipherer2.decrypt(dataPackage2.getEncryptedKey());
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException e) {
            System.out.println("Exception during decryption of key: " + e.getMessage());
            return false;
        }
        System.out.println("Decrypted Key to: " + Arrays.toString(decryptedKey));
        
        SymmetricCipherer aesCipherer2;
        try {
            aesCipherer2 = CiphererFactory.createSymmetricCipherer(dataPackage2.getSymmetricEncryptionMethod());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            System.out.println("Exception during create symmetric cipherer: " + e.getMessage());
            return false;
        }
        if (aesCipherer2 == null) {
            System.out.println("No symmetric cipherer for algorithm: " + dataPackage2.getSymmetricEncryptionMethod());
            return false;
        }
        try {
            aesCipherer2.initialize(keysize);
        } catch (InvalidAlgorithmParameterException e) {
            System.out.println("Exception during initialize of aes cipherer: " + e.getMessage());
            return false;
        }
        if (! aesCipherer2.getUsedAlgorithm().equals(dataPackage2.getSymmetricEncryptionMethod())) {
            System.out.println("symmetric encryption method not supported");
            return false;
        }
        try {
            aesCipherer2.setIvFromByteArray(dataPackage2.getEncryptedInitializationVector());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Exception during setIvFromByteArray: " + e.getMessage());
            return false;
        }
        try {
            aesCipherer2.setKeyFromByteArray(decryptedKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Exception during setKeyFromByteArray: " + e.getMessage());
            return false;
        }
        
        byte[] ciphertext2 = dataPackage2.getEncryptedContent();
        
        System.out.println("will be decrypted with: " + aesCipherer2.getUsedAlgorithm() + " with keysize: " + aesCipherer2.getKeySize());
        
        byte[] plaintextOut;
        try {
            plaintextOut = aesCipherer2.decrypt(ciphertext2);
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException e) {
            System.out.println("Exception during decrypt: " + e.getMessage());
            return false;
        }
        
        System.out.println("Decrypted Message: '" + new String(plaintextOut) + "'");
        
        return (Arrays.equals(plaintextOut, plaintextIn));
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
    
    private static void printDataPackage(EncryptedSensorDataPackage dataPackage) {
        System.out.println("Attributes:");
        System.out.println("asymmetricEncryptionMethod: " + dataPackage.getAsymmetricEncryptionMethod());
        System.out.println("asymmetricEncryptionBitStrength: " + dataPackage.getAsymmetricEncryptionBitStrength());
        System.out.println("symmetricEncryptionMethod: " + dataPackage.getSymmetricEncryptionMethod());
        System.out.println("symmetricEncryptionBitStrength: " + dataPackage.getSymmetricEncryptionBitStrength());
        System.out.println("encryptedContent: " + new String(dataPackage.getEncryptedContent()));
        System.out.println("encryptedInitializationVector: " + new String(dataPackage.getEncryptedInitializationVector()));
        System.out.println("encryptedKey: " + new String(dataPackage.getEncryptedKey()));
    }

}
