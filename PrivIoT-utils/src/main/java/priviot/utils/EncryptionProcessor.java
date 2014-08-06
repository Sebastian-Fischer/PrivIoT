package priviot.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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
     * @param content
     * @param symmetricEncryptionAlgorithm
     * @param symmetricEncryptionKeySize
     * @param asymmetricEncryptionAlgorithm
     * @param asymmetricEncryptionKeySize
     * @param publicKeyRecipient
     * @return
     * @throws EncryptionException
     */
    public static EncryptedSensorDataPackage createEncryptedDataPackage(String content, 
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
        try {
            symmetricCipherer.initialize(symmetricEncryptionKeySize);
        } catch (InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Keysize for symmetric encryption not supported: " + symmetricEncryptionKeySize, e);
        }
        
        try {
            asymmetricCipherer = CiphererFactory.createAsymmetricCipherer(asymmetricEncryptionAlgorithm);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            throw new EncryptionException("Asymmetric algorithm not supported: " + asymmetricEncryptionAlgorithm, e);
        }
        try {
            asymmetricCipherer.initialize(asymmetricEncryptionKeySize);
        } catch (InvalidAlgorithmParameterException e) {
            throw new EncryptionException("Keysize for asymmetric encryption not supported: " + asymmetricEncryptionKeySize, e);
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
            throw new EncryptionException("Error during asymmetric encryption of symmetric key", e);
        }
        
        // build data package
        
        dataPackage.setAsymmetricEncryptionMethod(asymmetricCipherer.getUsedAlgorithm());
        dataPackage.setAsymmetricEncryptionBitStrength(asymmetricEncryptionKeySize);
        dataPackage.setSymmetricEncryptionMethod(symmetricCipherer.getUsedAlgorithm());
        dataPackage.setSymmetricEncryptionBitStrength(symmetricEncryptionKeySize);
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
    public static String getContentOfEncryptedDataPackage(EncryptedSensorDataPackage dataPackage, byte[] privateKey) throws EncryptionException {
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
        try {
            asymmetricCipherer.setPrivateKeyFromByteArray(privateKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new EncryptionException("Bad private key", e);
        }
        
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
        
        return new String(decryptedContent);
    }

}