package priviot.coapwebserver.data;

import java.net.URI;

/**
 * An entry in the KeyDatabase.
 * Contains the URL of a Smart Service Proxy and it's public key
 */
public class KeyDatabaseEntry {
    
    /** The URI */
    private URI uri;
    
    /** The public key */
    private byte[] publicKey = new byte[0];
    
    /** Encryption algorithm */
    private String encryptionAlgorithm = "";
    
    
    public KeyDatabaseEntry(URI uri, byte[] publicKey) {
        this.uri = uri;
        this.publicKey = publicKey;
    }
    
    
    public URI getURI() {
        return uri;
    }
    
    public void setURI(URI uri) {
        this.uri = uri;
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
