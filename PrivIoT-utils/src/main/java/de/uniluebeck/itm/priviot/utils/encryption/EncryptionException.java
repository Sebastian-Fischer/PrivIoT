package de.uniluebeck.itm.priviot.utils.encryption;

/**
 * Exception that can be thrown if an error occurs during encryption.
 * The EncryptionException encapsulates the original Exception.
 */
public class EncryptionException extends Exception {

    private static final long serialVersionUID = 1L;
    
    private String message = "";
    private Exception originalException;
    
    public EncryptionException(String message, Exception originalException) {
        this.message = message;
        this.originalException = originalException;
    }
    
    public EncryptionException(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        if (originalException == null) {
            return message;
        }
        else {
            return message + ": " + originalException.getMessage();
        }
    }
}
