package eu.spitfire.ssp.backends.external.coap;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads and stores the private key and the certificate of the SSP.
 * 
 * @author Sebatian Fischer
 */
public class KeyStore {
	/** Key in configuration for the path of the private key */
	private static final String CONFIG_KEY_KEY_PATH = "ssp.privatekeypath";
	/** Key in configuration for the path of the certificate */
	private static final String CONFIG_KEY_CERTIFICATE_PATH = "ssp.certificatepath";
	
	/** type of the private key */
	private static final String KEY_TYPE = "RSA";
	
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	private PrivateKey privateKey;
	
	private X509Certificate certificate;
	
	
	/**
	 * Constructor.
	 * Loads the private key and the certificate from the path, 
	 * that is configured in given config.
	 * 
	 * @param config The program's configuration
	 */
	public KeyStore(Configuration config) {
		String keyPath = config.getString(CONFIG_KEY_KEY_PATH);
		String certificatePath = config.getString(CONFIG_KEY_CERTIFICATE_PATH);
		
		try {
			certificate = loadCertificateFromFile(certificatePath);
			
			log.info("X.509 certificate loaded from file '" + certificatePath + "': " + certificate.getSubjectX500Principal().getName());
		}
		catch (Exception e) {
			log.error("Couldn't load certificate from " + certificatePath, e);
		}
		
		try {
			privateKey = loadPrivateKeyFromFile(keyPath);
			
			log.info("Private key loaded from file '" + keyPath + "'");
		} catch (Exception e) {
			log.error("Couldn't load private key from " + keyPath + ". Decryption of received messages will not work", e);			
		}
	}
	
	/**
	 * Returns the private key of the SSP.
	 * 
	 * @return Private key or null if loading of key failed.
	 */
	public PrivateKey getPrivateKey() {
		return privateKey;
	}
	
	/**
	 * Returns the certificate of the SSP.
	 * 
	 * @return Certificate or null if loading of certificate failed.
	 */
	public X509Certificate getCertificate() {
		return certificate;
	}
	
	/**
     * Loads the X.509 certificate from given file.
     * 
     * @param certificatePathStr The path to the certificate file
     * @return The certificate, or null if given file does not exists or contains no X.509 certificate
     * 
     * @throws CertificateException 
     * @throws IOException 
     */
    private X509Certificate loadCertificateFromFile(String certificatePathStr) throws CertificateException, IOException {
    	if (certificatePathStr == null || certificatePathStr.isEmpty()) {
    		log.error("No certificate path set in configuration");
    	}
    	
        Path certPath = Paths.get(certificatePathStr);
        
        if (!(new File(certificatePathStr)).exists()) {
            log.error("Certificate file not found. Please check the path given in configuration: '" + certificatePathStr + "'");
            return null;
        }
        
    	final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        
        ByteArrayInputStream stream = new ByteArrayInputStream(Files.readAllBytes(certPath));
        
        final Collection<? extends Certificate> certs =
             (Collection<? extends Certificate>) certFactory.generateCertificates(stream);
        
        if (certs.size() == 0) {
        	log.error("No certificate found in file '" + certificatePathStr + "'");
        	return null;
        }
        
        if (certs.size() > 1) {
        	log.warn("More than one certificate found in file '" + certificatePathStr + "'. Load first one.");
        }
        
    	Certificate cert = certs.iterator().next();
    	if (cert instanceof X509Certificate) {    		
    		return (X509Certificate)cert;
    	}
    	else {
    		log.error("Certificate in file '" + certificatePathStr + "' is not a X.509 certificate");
    		return null;
    	}        
    }
    
	/**
     * Loads the private key of the SSP from given file.
     * 
     * @param privateKeyPathStr The path to the private key file
     * @return The private key
     * 
     * @throws IOException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeySpecException 
     */
    private PrivateKey loadPrivateKeyFromFile(String privateKeyPathStr) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    	File file = new File(privateKeyPathStr);
    	
    	if (!new File(privateKeyPathStr).exists()) {
    		log.error("Private key file does not exist: '" + privateKeyPathStr + "'");
    		return null;
    	}
    	
    	if (!file.getName().endsWith(".der")) {
    		log.error("Private key file has to be in .der format");
    		return null;
    	}
    	
    	// get private key of SSP
    	
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int)file.length()];
        dis.readFully(keyBytes);
        dis.close();
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_TYPE);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        
        return privateKey;
    }
}
