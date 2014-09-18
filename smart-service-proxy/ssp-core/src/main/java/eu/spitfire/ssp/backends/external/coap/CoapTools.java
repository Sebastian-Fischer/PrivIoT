package eu.spitfire.ssp.backends.external.coap;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.security.PrivateKey;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.uniluebeck.itm.ncoap.message.CoapResponse;
import de.uniluebeck.itm.ncoap.message.options.ContentFormat;
import de.uniluebeck.itm.priviot.utils.EncryptionProcessor;
import de.uniluebeck.itm.priviot.utils.data.EncryptionParameters;
import de.uniluebeck.itm.priviot.utils.data.PrivacyDataPackageUnmarshaller;
import de.uniluebeck.itm.priviot.utils.data.generated.PrivacyDataPackage;
import de.uniluebeck.itm.priviot.utils.encryption.EncryptionException;
import eu.spitfire.ssp.utils.Language;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides static tools to process CoAP messages
 *
 * @author Oliver Kleine
 */
public abstract class CoapTools {

    private static Logger log = LoggerFactory.getLogger(CoapTools.class.getName());

    /**
     * Reads the content of the given {@link de.uniluebeck.itm.ncoap.message.CoapResponse} and deserializes that content
     * into a {@link com.hp.hpl.jena.rdf.model.Model} according to the
     * {@link de.uniluebeck.itm.ncoap.message.options.OptionValue.Name#CONTENT_FORMAT}.
     *
     * @param coapResponse the {@link de.uniluebeck.itm.ncoap.message.CoapResponse} to read the content from.
     * @param keyStore The {@link eu.spitfire.ssp.backends.external.coap.KeyStore} that saves the private key.
     *                 The keyStore is needed only, if coapResponse contains encrypted content.
     *
     * @return a {@link com.hp.hpl.jena.rdf.model.Model} that contains the triples from the given
     * {@link de.uniluebeck.itm.ncoap.message.CoapResponse}s content
     */
    public static Model getModelFromCoapResponse(CoapResponse coapResponse, KeyStore keyStore){

        try{
            Model resourceStatus = ModelFactory.createDefaultModel();

            //read payload from CoAP response
            byte[] coapPayload = new byte[coapResponse.getContent().readableBytes()];
            coapResponse.getContent().getBytes(0, coapPayload);
            
            long contentFormat = coapResponse.getContentFormat();
            
            //fischer: unmarshall and decrypt payload
            if (coapResponse.getContentFormat() == ContentFormat.APP_XML) {
            	return getModelFromXmlCoapResponse(coapPayload, keyStore);
            }
            else {
	            Language language = Language.getByCoapContentFormat(contentFormat);
	
	            if(language == null)
	                return null;
	
	            resourceStatus.read(new ByteArrayInputStream(coapPayload), null, language.lang);
	            return resourceStatus;
            }
        }

        catch(Exception ex){
            log.error("Could not read content from CoAP response!", ex);
            return null;
        }
    }
    
    /**
     * Returns the alternative locationUri if there exist one in the CoapResponse
     * @return Alternative locationUri or null, if none exist
     */
    public static URI getAlternativeLocationUri(CoapResponse coapResponse) {    	
    	try{
            //read payload from CoAP response
            byte[] coapPayload = new byte[coapResponse.getContent().readableBytes()];
            coapResponse.getContent().getBytes(0, coapPayload);
            
            if (coapResponse.getContentFormat() == ContentFormat.APP_XML) {
            	ByteArrayInputStream inStream = new ByteArrayInputStream(coapPayload);
            	
            	// unmarshall PrivacyDataPackage
            	PrivacyDataPackage privacyDataPackage;
                try {
                	privacyDataPackage = PrivacyDataPackageUnmarshaller.unmarshal(inStream);
                } catch (JAXBException | XMLStreamException e) {
                    return null;
                }
            	
            	return new URI(privacyDataPackage.getSensorUri());
            }
            else {
            	return null;
            }
    	}
        catch(Exception ex){
            return null;
        }
    }
    
    /**
     * Tries to unmarshall the xml payload of a CoAP response.
     * Returns the RDF Model, if unmarshalling was successful.
     * 
     * @author Sebastian Fischer
     * 
     * @param xmlCoapPayload
     * @param keyStore
     * @return
     */
    private static Model getModelFromXmlCoapResponse(byte[] xmlCoapPayload, KeyStore keyStore) {
    	if (xmlCoapPayload.length == 0) {
    		log.error("CoAP payload is empty");
    		return null;
    	}
    	
    	Model model = getModelFromPrivacyDataPackage(xmlCoapPayload, keyStore);
    	
    	if (model == null) {
    		log.error("Could not read xml content from CoAP response!");
    	}
    	
    	return model;
    }
    
    /**
     * If xmlCoapPayload is the xml representation of a PrivacyDataPackage,
     * this method unmarshalls the xml and decrypts the content of the package.
     * 
     * @author Sebastian Fischer
     * 
     * @param xmlCoapPayload
     * @param keyStore
     * @return  The content of the PrivacyDataPackage
     */
    private static Model getModelFromPrivacyDataPackage(byte[] xmlCoapPayload, KeyStore keyStore) {
    	ByteArrayInputStream inStream = new ByteArrayInputStream(xmlCoapPayload);
    	PrivacyDataPackage privacyDataPackage;
    	Model resourceStatus = ModelFactory.createDefaultModel();
    	
    	// unmarshall PrivacyDataPackage
        try {
        	privacyDataPackage = PrivacyDataPackageUnmarshaller.unmarshal(inStream);
        } catch (JAXBException | XMLStreamException e) {
            log.error("XML CoAP payload is not a PrivacyDataPackage");
            return null;
        }
        
        // get the SSP's private key
        PrivateKey privateKey = keyStore.getPrivateKey();
        
        // get encryption parameters from algorithm code and private key
    	EncryptionParameters encryptionParameters;
		try {
			encryptionParameters = new EncryptionParameters(privacyDataPackage.getSymmetricEncryptionAlgorithmCode(), 
					                                        privateKey);
		} catch (EncryptionException e) {
			log.error("Error in PrivacyDataPackage or private key", e);
			return null;
		}
		
		// decrypt
		byte[] decryptedContent;
    	try {
    		decryptedContent = EncryptionProcessor.getContentOfPrivacyDataPackage(privacyDataPackage, privateKey);
		} catch (EncryptionException e) {
			log.error("Error during decryption of PrivacyDataPackage", e);
			return null;
		}
    	
    	
    	Language language = Language.getByCoapContentFormat(privacyDataPackage.getContentFormat());

        if(language == null) {
            return null;
        }
        
        log.debug("fischer: decrypted content:\n" + new String(decryptedContent));

        // create model
        resourceStatus.read(new ByteArrayInputStream(decryptedContent), null, language.lang);
        return resourceStatus;
    }   
    
}
