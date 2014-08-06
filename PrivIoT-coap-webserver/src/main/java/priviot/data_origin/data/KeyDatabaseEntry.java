package priviot.data_origin.data;

/**
 * An entry in the KeyDatabase.
 * Contains the URL of a Smart Service Proxy and it's public key
 */
public class KeyDatabaseEntry {
    
    /** The URL */
    private String url = "";
    
    /** The public key */
    private byte[] publicKey = new byte[0];
    
    /** Encryption algorithm */
    private String encryptionAlgorithm = "";
    
    
    public KeyDatabaseEntry(String url, byte[] publicKey) {
        this.url = url;
        this.publicKey = publicKey;
    }
    
    
    public String getURL() {
        return url;
    }
    
    public void setURL(String url) {
        this.url = url;
    }
    
    public byte[] getPublicKey() {
        return publicKey;
    }
    
    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }


    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }


    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }
}
