package priviot.utils.pseudonymization;

/**
 * Exception that can be thrown if an error occurs during pseudonymization.
 * The PseudonymizationException encapsulates the original Exception.
 */
public class PseudonymizationException extends Exception {

    private static final long serialVersionUID = 1L;
    
    private String message = "";
    private Exception originalException;
    
    public PseudonymizationException(String message, Exception originalException) {
        this.message = message;
        this.originalException = originalException;
    }
    
    public PseudonymizationException(String message) {
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
