package de.uniluebeck.itm.priviot.utils.certificates;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;

import javax.security.auth.x500.X500Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains methods to load and verify a X.509 certificates and private keys
 */
public class CertificateProcessor {
    /** type of the private keys */
    private static final String KEY_TYPE = "RSA";
    
    private static Logger log = LoggerFactory.getLogger(CertificateProcessor.class.getName());
    
    /**
     * Loads the X.509 certificate from given file.
     * 
     * @param certificatePathStr The path to the certificate file
     * @return The certificate, or null if given file does not exists or contains no X.509 certificate
     * 
     * @throws CertificateException 
     * @throws IOException 
     */
    public static X509Certificate loadCertificateFromFile(String certificatePathStr) throws CertificateException, IOException {
        return loadCertificateFromFile(certificatePathStr, false);
    }
    
    private static X509Certificate loadCertificateFromFile(String certificatePathStr, boolean silent) throws CertificateException, IOException {
        if (certificatePathStr == null || certificatePathStr.isEmpty()) {
            if (!silent) log.error("loadCertificateFromFile called with empty certificatePathStr");
        }
        
        Path certPath = Paths.get(certificatePathStr);
        
        if (!(new File(certificatePathStr)).exists()) {
            if (!silent) log.error("Certificate file not found. Please check the path given in configuration: '" + certificatePathStr + "'");
            return null;
        }
        
        final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        
        ByteArrayInputStream stream = new ByteArrayInputStream(Files.readAllBytes(certPath));
        
        final Collection<? extends Certificate> certs =
             (Collection<? extends Certificate>) certFactory.generateCertificates(stream);
        
        if (certs.size() == 0) {
            if (!silent) log.error("No certificate found in file '" + certificatePathStr + "'");
            return null;
        }
        
        if (certs.size() > 1) {
            if (!silent) log.warn("More than one certificate found in file '" + certificatePathStr + "'. Load first one.");
        }
        
        Certificate cert = certs.iterator().next();
        if (cert instanceof X509Certificate) {          
            return (X509Certificate)cert;
        }
        else {
            if (!silent) log.error("Certificate in file '" + certificatePathStr + "' is not a X.509 certificate");
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
    public static PrivateKey loadPrivateKeyFromFile(String privateKeyPathStr) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        if (privateKeyPathStr == null || privateKeyPathStr.isEmpty()) {
            log.error("loadPrivateKeyFromFile called with empty privateKeyPathStr");
        }
        
        File file = new File(privateKeyPathStr);
        
        if (!new File(privateKeyPathStr).exists()) {
            log.error("Private key file does not exist: '" + privateKeyPathStr + "'");
            return null;
        }
        
        if (!file.getName().endsWith(".der")) {
            log.error("Private key file has to be in .der format");
            return null;
        }
        
        // get private key
        
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
    
    public static boolean verifyCertificate(X509Certificate certificate, String certificatesPath, String trustedCertificatesPath) {
        return verifyCertificate(certificate, certificatesPath, trustedCertificatesPath, 0);
    }
    
    private static boolean verifyCertificate(X509Certificate certificate, String certificatesPath, String trustedCertificatesPath, int recursionCounter) {
        if (recursionCounter == 100) {
            log.error("Error in verification of certificate: Reached maximum recusions. Please check Certificate-Chain!");
            return false;
        }
        
        // check date of certificate
        try {
            certificate.checkValidity();
        } catch (Exception e) {
            return false;
        }
        
        String subject = getCommonName(certificate.getSubjectX500Principal());
        String issuer = getCommonName(certificate.getIssuerX500Principal());
        
        String issuerCertificatePath;
        // self-signed certificate - assume issuer is a trusted top-level CA
        if (subject.equals(issuer)) {
            issuerCertificatePath = trustedCertificatesPath + "/" + issuer + ".pem";
        }
        // normal certificate
        else {
            issuerCertificatePath = certificatesPath + "/" + issuer + ".pem";
        }
        
        X509Certificate isserCertificate;
        try {
            isserCertificate = loadCertificateFromFile(issuerCertificatePath, true);
        } catch (CertificateException e) {
            log.error("Error during verification of certificate for " + subject + ": Certificate of issuer in " + issuerCertificatePath + " has errors", e);
            return false;
        } catch (IOException e) {
            log.error("Error during verification of certificate for " + subject + ": Certificate of issuer in " + issuerCertificatePath + " caused IOException", e);
            return false;
        }
        if (isserCertificate == null) {
            // if not found search again in trusted certificates
            issuerCertificatePath = trustedCertificatesPath + "/" + issuer + ".pem";
            
            try {
                isserCertificate = loadCertificateFromFile(issuerCertificatePath, true);
            } catch (CertificateException e) {
                log.error("Error during verification of certificate for " + subject + ": Certificate of issuer in " + issuerCertificatePath + " has errors", e);
                return false;
            } catch (IOException e) {
                log.error("Error during verification of certificate for " + subject + ": Certificate of issuer in " + issuerCertificatePath + " caused IOException", e);
                return false;
            }
            
            if (isserCertificate == null) {
                log.error("Error during verification of certificate for " + subject + ": Certificate of issuer " + issuer + " not found in " + certificatesPath + " and " + trustedCertificatesPath);
                return false;
            }
        }
        
        try {
            certificate.verify(isserCertificate.getPublicKey());
        } catch (InvalidKeyException | CertificateException
                | NoSuchAlgorithmException | NoSuchProviderException
                | SignatureException e) {
            log.error("Error during verification of certificate for " + subject + " with private key of issuer " + issuer, e);
            return false;
        }
        
        if (subject.equals(issuer)) {
            // recursion end
            return true;
        }
        else {
            return verifyCertificate(isserCertificate, certificatesPath, trustedCertificatesPath, recursionCounter + 1);
        }
    }
    
    /**
     * Returns the common name (CN) from the certificates SubjectX500Principal.
     * 
     * @param certificate
     * @return The common name, or empty String if not present
     */
    public static String getCommonName(X500Principal x500principal) {
        String[] principals = x500principal.getName().split(",");
        
        for (String principal : principals) {
            String[] parts = principal.split("=");
            
            if (parts.length == 2) {
                if (parts[0].equals("CN")) {
                    return parts[1];
                }
            }
        }
        
        return "";
    }
}
