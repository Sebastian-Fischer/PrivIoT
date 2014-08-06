package priviot.utils.data;

/**
 * Exception that can be thrown durin parsing of xml representation of a data package.
 */
public class DataPackageParsingException extends Exception {
    
    static final long serialVersionUID = 1L;
    
    private String message = "";
    
    public DataPackageParsingException(String message) {
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
