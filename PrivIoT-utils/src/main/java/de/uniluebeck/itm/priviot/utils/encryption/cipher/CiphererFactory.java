package de.uniluebeck.itm.priviot.utils.encryption.cipher;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.NoSuchPaddingException;

import de.uniluebeck.itm.priviot.utils.encryption.cipher.asymmetric.elgamal.ElgamalCipherer;
import de.uniluebeck.itm.priviot.utils.encryption.cipher.asymmetric.rsa.RSACipherer;
import de.uniluebeck.itm.priviot.utils.encryption.cipher.symmetric.aes.AESCipherer;


/**
 * Factory class for cipherers.
 */
public abstract class CiphererFactory {
    
    /**
     * Creates an AsymmetricCipherer for the given algorithm name, if one exists.
     * If no Cipherer exists, null is returned.
     * @param algorithmName
     * @return 
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws NoSuchPaddingException
     * @throws IllegalArgumentException
     */
    public static AsymmetricCipherer createAsymmetricCipherer(String algorithmName) throws NoSuchAlgorithmException, 
            NoSuchProviderException, NoSuchPaddingException {
        if (algorithmName == null) {
            return null;
        }
        
        if (algorithmName.equals(ElgamalCipherer.getAlgorithm())) {
            return new ElgamalCipherer();
        }
        
        if (algorithmName.equals(RSACipherer.getAlgorithm())) {
            return new RSACipherer();
        }
        
        return null;
    }
    
    /**
     * Creates a SymmetricCipherer for the given algorithm name, if one exists.
     * If no Cipherer exists, null is returned.
     * @param algorithmName
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public static SymmetricCipherer createSymmetricCipherer(String algorithmName) throws NoSuchAlgorithmException, NoSuchPaddingException {
        if (algorithmName == null) {
            return null;
        }
        
        if (algorithmName.equals(AESCipherer.getAlgorithm())) {
            return new AESCipherer();
        }
        
        return null;
    }
}
