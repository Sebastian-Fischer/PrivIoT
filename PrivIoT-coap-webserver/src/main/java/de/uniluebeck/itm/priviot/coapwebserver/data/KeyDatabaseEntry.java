package de.uniluebeck.itm.priviot.coapwebserver.data;

import java.net.URI;
import java.security.PublicKey;

/**
 * An entry in the KeyDatabase.
 * Contains the URL of a Smart Service Proxy and it's public key
 */
public class KeyDatabaseEntry {
    
    /** The URI */
    private URI uri;
    
    /** The public key */
    private PublicKey publicKey;
    
    /** Encryption algorithm */
    private String encryptionAlgorithm = "";
    
    
    public KeyDatabaseEntry(URI uri, PublicKey publicKey) {
        this.uri = uri;
        this.publicKey = publicKey;
    }
    
    
    public URI getURI() {
        return uri;
    }
    
    public void setURI(URI uri) {
        this.uri = uri;
    }
    
    public PublicKey getPublicKey() {
        return publicKey;
    }
    
    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }


    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }


    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }
}
