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

import de.uniluebeck.itm.priviot.utils.certificates.CertificateProcessor;

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
			certificate = CertificateProcessor.loadCertificateFromFile(certificatePath);
			
			log.info("X.509 certificate loaded from file '" + certificatePath + "': " + certificate.getSubjectX500Principal().getName());
		}
		catch (Exception e) {
			log.error("Couldn't load certificate from " + certificatePath, e);
		}
		
		try {
			privateKey = CertificateProcessor.loadPrivateKeyFromFile(keyPath);
			
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
}
