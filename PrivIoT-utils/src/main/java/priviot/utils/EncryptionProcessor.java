package priviot.utils;

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

import priviot.utils.data.transfer.EncryptedSensorDataPackage;
import priviot.utils.encryption.EncryptionException;
import priviot.utils.encryption.cipher.AsymmetricCipherer;
import priviot.utils.encryption.cipher.CiphererFactory;
import priviot.utils.encryption.cipher.SymmetricCipherer;

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
     * @param contentLifetime                 Lifetime of the content in seconds.
     * @param symmetricEncryptionAlgorithm    algorithm for symmetric encryption (e.g. "AES/ECB/PKSC5Padding")
     * @param symmetricEncryptionKeySize      key size for symmetric encryption
     * @param asymmetricEncryptionAlgorithm   algorithm for symmetric encryption (e.g. "RSA/CBC/PKSC1Padding")
     * @param asymmetricEncryptionKeySize     key size for asymmetric encryption
     * @param publicKeyRecipient              public key of the recipient
     * @return
     * @throws EncryptionException
     */
    public static EncryptedSensorDataPackage createEncryptedDataPackage(String content, 
            int contentLifetime,
            String symmetricEncryptionAlgorithm, int symmetricEncryptionKeySize, 
            String asymmetricEncryptionAlgorithm, int asymmetricEncryptionKeySize,
            byte[] publicKeyRecipient) throws EncryptionException {
        
        EncryptedSensorDataPackage dataPackage = new EncryptedSensorDataPackage();
        SymmetricCipherer symmetricCipherer;
        AsymmetricCipherer asymmetricCipherer;
        byte[] symmetricKey;
        byte[] encryptedKey;
        byte[] encryptedInitializationVector;
        byte[] ciphertext;
        
        // initialize symmetric and asymmetric cipherers
        
        try {
            symmetricCipherer = CiphererFactory.createSymmetricCipherer(symmetricEncryptionAlgorithm);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new EncryptionException("Symmetric algorithm not supported: " + symmetricEncryptionAlgorithm, e);
        }
        if (symmetricCipherer == null) {
            throw new EncryptionException("Symmetric algorithm not supported: " + symmetricEncryptionAlgorithm);
        }
        try {
            symmetricCipherer.initialize(symmetricEncryptionKeySize); //TODO: null pointer exception
        } catch (InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Keysize for symmetric encryption not supported: " + symmetricEncryptionKeySize, e);
        }
        
        try {
            asymmetricCipherer = CiphererFactory.createAsymmetricCipherer(asymmetricEncryptionAlgorithm);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            throw new EncryptionException("Asymmetric algorithm not supported: " + asymmetricEncryptionAlgorithm, e);
        }
        if (asymmetricCipherer == null) {
            throw new EncryptionException("Asymmetric algorithm not supported: " + asymmetricEncryptionAlgorithm);
        }
        try {
            asymmetricCipherer.initialize(asymmetricEncryptionKeySize);
        } catch (InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Keysize for asymmetric encryption not supported: " + asymmetricEncryptionKeySize, e);
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
        
        // encrypt key and initialization vector with asymmetric cipherer
        
        try {
            encryptedKey = asymmetricCipherer.encrypt(symmetricKey);
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | ShortBufferException
                | InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Error during asymmetric encryption of symmetric key", e);
        }
        
        try {
            encryptedInitializationVector = asymmetricCipherer.encrypt(symmetricCipherer.getIvAsByteArray());
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | ShortBufferException
                | InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Error during asymmetric encryption of initialization vector", e);
        }
        
        // build data package
        
        dataPackage.setAsymmetricEncryptionMethod(asymmetricCipherer.getUsedAlgorithm());
        dataPackage.setAsymmetricEncryptionBitStrength(asymmetricEncryptionKeySize);
        dataPackage.setSymmetricEncryptionMethod(symmetricCipherer.getUsedAlgorithm());
        dataPackage.setSymmetricEncryptionBitStrength(symmetricEncryptionKeySize);
        dataPackage.setContentLifetime(contentLifetime);
        dataPackage.setEncryptedContent(ciphertext);
        dataPackage.setEncryptedInitializationVector(encryptedInitializationVector);
        dataPackage.setEncryptedKey(encryptedKey);
        
        return dataPackage;
    }
    
    /**
     * Decrypts the content of an EncryptedSensorDataPackage.
     * Therefore first the encrypted key and the encrypted initialization vector are decrypted with asymmetric encryption.
     * For asymmetric encryption the private key is needed.
     * TODO: what to do, if dataPackage.getAsymmetricEncryptionMethod() does not fit to privateKey?
     * @param dataPackage
     * @param privateKey
     * @return
     * @throws EncryptionException
     */
    public static byte[] getContentOfEncryptedDataPackage(EncryptedSensorDataPackage dataPackage, PrivateKey privateKey) throws EncryptionException {
        SymmetricCipherer symmetricCipherer;
        AsymmetricCipherer asymmetricCipherer;
        byte[] decryptedsymmetricKey;
        byte[] decryptedInitializationVector;
        byte[] decryptedContent;
        
        // initialize symmetric and asymmetric cipherers
        
        try {
            symmetricCipherer = CiphererFactory.createSymmetricCipherer(dataPackage.getSymmetricEncryptionMethod());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new EncryptionException("Symmetric algorithm not supported: " + dataPackage.getSymmetricEncryptionMethod(), e);
        }
        try {
            symmetricCipherer.initialize(dataPackage.getSymmetricEncryptionBitStrength());
        } catch (InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Keysize for symmetric encryption not supported: " + dataPackage.getSymmetricEncryptionBitStrength(), e);
        }
        
        try {
            asymmetricCipherer = CiphererFactory.createAsymmetricCipherer(dataPackage.getAsymmetricEncryptionMethod());
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            throw new EncryptionException("Asymmetric algorithm not supported: " + dataPackage.getAsymmetricEncryptionMethod(), e);
        }
        try {
            asymmetricCipherer.initialize(dataPackage.getAsymmetricEncryptionBitStrength());
        } catch (InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Keysize for asymmetric encryption not supported: " + dataPackage.getAsymmetricEncryptionBitStrength(), e);
        }
        asymmetricCipherer.setPrivateKey(privateKey);
        
        // decrypt symmetric key and initialization vector
        
        try {
            decryptedsymmetricKey = asymmetricCipherer.decrypt(dataPackage.getEncryptedKey());
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Error during asymmetric decryption of symmetric key", e);
        }
        
        try {
            decryptedInitializationVector = asymmetricCipherer.decrypt(dataPackage.getEncryptedInitializationVector());
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Error during asymmetric decryption of initialization vector", e);
        }
        
        // configure symmetric encryption
        
        try {
            symmetricCipherer.setKeyFromByteArray(decryptedsymmetricKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new EncryptionException("Bad symmetric key", e);
        }
        try {
            symmetricCipherer.setIvFromByteArray(decryptedInitializationVector);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new EncryptionException("Bad initialization vector", e);
        }
        
        // decrypt content
        
        try {
            decryptedContent = symmetricCipherer.decrypt(dataPackage.getEncryptedContent());
        } catch (InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Error during decryption of content", e);
        }
        
        return decryptedContent;
    }

}
