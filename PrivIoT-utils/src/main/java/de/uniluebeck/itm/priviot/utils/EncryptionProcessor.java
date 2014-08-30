package de.uniluebeck.itm.priviot.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

import org.apache.commons.codec.binary.Base64;

import de.uniluebeck.itm.priviot.utils.data.EncryptionParameters;
import de.uniluebeck.itm.priviot.utils.data.generated.PrivacyDataPackage;
import de.uniluebeck.itm.priviot.utils.encryption.EncryptionException;
import de.uniluebeck.itm.priviot.utils.encryption.cipher.AsymmetricCipherer;
import de.uniluebeck.itm.priviot.utils.encryption.cipher.CiphererFactory;
import de.uniluebeck.itm.priviot.utils.encryption.cipher.SymmetricCipherer;

/**
 * Encapsulates the process of encryption and serialization of data packages.
 */
public abstract class EncryptionProcessor {
    
    /**
     * Creates an EncryptedSensorDataPackage out of a given content.
     * The content is symmetrically encrypted with the given algorithm and key size.
     * The used symmetric key and initialization vector are encrypted asymmetrically with given algorithm and key size.
     * For asymmetric Encryption, the public key of the recipient is needed.
     * @param content                         Content of data package.
     * @param sensorUriPseudonym              URI with the Pseudonym for the sensor.
     * @param encryptionParameters            parameters for asymmetric and symmetric encryption.
     * @param publicKeyRecipient              public key of the recipient
     * @return
     * @throws EncryptionException
     */
    public static PrivacyDataPackage createPrivacyDataPackage(String content,
            String sensorUriPseudonym,
            EncryptionParameters encryptionParameters,
            byte[] publicKeyRecipient) throws EncryptionException {
        
        PrivacyDataPackage dataPackage = new PrivacyDataPackage();
        SymmetricCipherer symmetricCipherer;
        AsymmetricCipherer asymmetricCipherer;
        byte[] symmetricKey;
        byte[] encryptedKey;
        byte[] ciphertext;
        String symmetricAlgorithmCode;
        
        // get name of symmetric algorithm. Throws EncryptionException if algorithm is unknown
        symmetricAlgorithmCode = encryptionParameters.getSymmetricAlgorithmCode();
        
        // initialize symmetric and asymmetric cipherers
        
        try {
            symmetricCipherer = CiphererFactory.createSymmetricCipherer(encryptionParameters.getSymmetricEncryptionAlgorithm());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new EncryptionException("Symmetric algorithm not supported: " + encryptionParameters.getSymmetricEncryptionAlgorithm(), e);
        }
        if (symmetricCipherer == null) {
            throw new EncryptionException("Symmetric algorithm not supported: " + encryptionParameters.getSymmetricEncryptionAlgorithm());
        }
        try {
            symmetricCipherer.initialize(encryptionParameters.getSymmetricEncryptionBitStrength());
        } catch (InvalidAlgorithmParameterException e) {
            throw new EncryptionException("BitStrength for symmetric encryption not supported: " + encryptionParameters.getSymmetricEncryptionBitStrength(), e);
        }
        
        try {
            asymmetricCipherer = CiphererFactory.createAsymmetricCipherer(encryptionParameters.getAsymmetricEncryptionAlgorithm());
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            throw new EncryptionException("Asymmetric algorithm not supported: " + encryptionParameters.getAsymmetricEncryptionAlgorithm(), e);
        }
        if (asymmetricCipherer == null) {
            throw new EncryptionException("Asymmetric algorithm not supported: " + encryptionParameters.getAsymmetricEncryptionAlgorithm());
        }
        try {
            asymmetricCipherer.initialize(encryptionParameters.getAsymmetricEncryptionBitStrength());
        } catch (InvalidAlgorithmParameterException e) {
            throw new EncryptionException("BitStrength for asymmetric encryption not supported: " + encryptionParameters.getAsymmetricEncryptionBitStrength(), e);
        }
        try {
            asymmetricCipherer.setPublicKeyFromByteArray(publicKeyRecipient);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new EncryptionException("Invalid public key of recipient", e);
        }
        
        // generate key and get is as byte-array
        
        symmetricCipherer.generateKey();
        
        symmetricKey = symmetricCipherer.getKeyAsByteArray();
        
        // encrypt content with symmetric cipherer
        
        try {
            ciphertext = symmetricCipherer.encrypt(content.getBytes());
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | ShortBufferException
                | InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Error during symmetric encryption of content", e);
        }
        
        // encrypt key vector with asymmetric cipherer
        
        try {
            encryptedKey = asymmetricCipherer.encrypt(symmetricKey);
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | ShortBufferException
                | InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Error during asymmetric encryption of symmetric key", e);
        }
        
        // base64 encode ciphertext, initialization vector and encryptedKey
        
        String ciphertextStr = Base64.encodeBase64String(ciphertext);
        String initializationVectorStr = Base64.encodeBase64String(symmetricCipherer.getIvAsByteArray());
        String encrpytedKeyStr = Base64.encodeBase64String(encryptedKey);
        
        // build data package
        
        dataPackage.setSensorUri(sensorUriPseudonym);
        dataPackage.setSymmetricEncryptionAlgorithmCode(symmetricAlgorithmCode);
        dataPackage.setEncryptedContent(ciphertextStr);
        dataPackage.setInitializationVector(initializationVectorStr);
        dataPackage.setEncryptedSymmetricKey(encrpytedKeyStr);
        
        return dataPackage;
    }
    
    /**
     * Decrypts the content of a PrivacyDataPackage.
     * Therefore first the encrypted key and the encrypted initialization vector are decrypted with asymmetric encryption.
     * For asymmetric encryption the private key is needed.
     * TODO: what to do, if dataPackage.getAsymmetricEncryptionMethod() does not fit to privateKey?
     * @param dataPackage
     * @param privateKey
     * @return
     * @throws EncryptionException
     */
    public static byte[] getContentOfPrivacyDataPackage(PrivacyDataPackage dataPackage,
											    		PrivateKey privateKey) throws EncryptionException {
        SymmetricCipherer symmetricCipherer;
        AsymmetricCipherer asymmetricCipherer;
        byte[] encrpytedSymmetricKey;
        byte[] initializationVector;
        byte[] encryptedContent;
        byte[] decryptedsymmetricKey;
        byte[] decryptedContent;
        
        String asymmetricEncryptionAlgorithm = EncryptionParameters.getAsymmetricEncryptionAlgorithmByPrivateKey(privateKey);
    	int asymmetricEncryptionBitStrength = EncryptionParameters.getAsymmetricEncryptionBitStrengthByPrivateKey(privateKey);
        
        // get EncrpytionParameters. Throws EncryptionException if dataPackage.getEncryptionAlgorithm() is not valid
        EncryptionParameters encryptionParameters = new EncryptionParameters(dataPackage.getSymmetricEncryptionAlgorithmCode(), 
        		                                                             asymmetricEncryptionAlgorithm, 
        		                                                             asymmetricEncryptionBitStrength);
        
        // initialize symmetric and asymmetric cipherers
        
        try {
            symmetricCipherer = CiphererFactory.createSymmetricCipherer(encryptionParameters.getSymmetricEncryptionAlgorithm());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new EncryptionException("Symmetric algorithm not supported: " + encryptionParameters.getSymmetricEncryptionAlgorithm(), e);
        }
        try {
            symmetricCipherer.initialize(encryptionParameters.getSymmetricEncryptionBitStrength());
        } catch (InvalidAlgorithmParameterException e) {
            throw new EncryptionException("BitStrength for symmetric encryption not supported: " + encryptionParameters.getSymmetricEncryptionBitStrength(), e);
        }
        
        try {
            asymmetricCipherer = CiphererFactory.createAsymmetricCipherer(encryptionParameters.getAsymmetricEncryptionAlgorithm());
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            throw new EncryptionException("Asymmetric algorithm not supported: " + encryptionParameters.getAsymmetricEncryptionAlgorithm(), e);
        }
        try {
            asymmetricCipherer.initialize(encryptionParameters.getAsymmetricEncryptionBitStrength());
        } catch (InvalidAlgorithmParameterException e) {
            throw new EncryptionException("BitStrength for asymmetric encryption not supported: " + encryptionParameters.getAsymmetricEncryptionBitStrength(), e);
        }
        asymmetricCipherer.setPrivateKey(privateKey);
        
        // Base64 decode encrypted symmetric key, initialization vector and 
        
        if (!Base64.isBase64(dataPackage.getEncryptedSymmetricKey())) {
        	throw new EncryptionException("encrpyted symmetric key in data package is not base64 encoded");
        }
        encrpytedSymmetricKey = Base64.decodeBase64(dataPackage.getEncryptedSymmetricKey());
        
        if (!Base64.isBase64(dataPackage.getInitializationVector())) {
        	throw new EncryptionException("initialization vector in data package is not base64 encoded");
        }
        initializationVector = Base64.decodeBase64(dataPackage.getInitializationVector());
        
        if (!Base64.isBase64(dataPackage.getEncryptedContent())) {
        	throw new EncryptionException("encrpyted content in data package is not base64 encoded");
        }
        encryptedContent = Base64.decodeBase64(dataPackage.getEncryptedContent());
        
        // decrypt symmetric key
        
        try {
            decryptedsymmetricKey = asymmetricCipherer.decrypt(encrpytedSymmetricKey);
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Error during asymmetric decryption of symmetric key", e);
        }
        
        // configure symmetric encryption
        
        try {
            symmetricCipherer.setKeyFromByteArray(decryptedsymmetricKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new EncryptionException("Bad symmetric key", e);
        }
        try {
            symmetricCipherer.setIvFromByteArray(initializationVector);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new EncryptionException("Bad initialization vector", e);
        }
        
        // decrypt content
        
        try {
            decryptedContent = symmetricCipherer.decrypt(encryptedContent);
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Error during decryption of content", e);
        }
        
        return decryptedContent;
    }

}
