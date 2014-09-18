package eu.spitfire.ssp.backends.external.coap;

import com.google.common.util.concurrent.SettableFuture;

import de.uniluebeck.itm.ncoap.application.server.webservice.NotObservableWebservice;
import de.uniluebeck.itm.ncoap.message.CoapMessage;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.CoapResponse;
import de.uniluebeck.itm.ncoap.message.MessageCode;
import de.uniluebeck.itm.ncoap.message.MessageType;
import de.uniluebeck.itm.ncoap.message.options.ContentFormat;
import de.uniluebeck.itm.ncoap.message.options.OptionValue;
import de.uniluebeck.itm.priviot.utils.data.PrivIoTContentFormat;
import eu.spitfire.ssp.backends.external.coap.CoapBackendComponentFactory;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Set;

/**
 * The {@link eu.spitfire.ssp.backends.external.coap.CoapCertificateWebservice} processes incoming
 * GET requests to the "/certificate" URI. Upon reception of a GET requests it responses with the 
 * X.509 certificate of the SSP.
 * The certificate's place is set in config file.
 *
 * @author Sebastian Fischer
 */
public class CoapCertificateWebservice extends NotObservableWebservice<X509Certificate> {

    private Logger log = LoggerFactory.getLogger(CoapCertificateWebservice.class.getName());

    /**
     * Creates a new instance of {@link eu.spitfire.ssp.backends.external.coap.registry.CoapRegistryWebservice}.
     *
     * @param certificatePath  Path to the certificate file of the SSP
     */
    public CoapCertificateWebservice(CoapBackendComponentFactory componentFactory){
        super("/certificate", null, OptionValue.MAX_AGE_DEFAULT);
        
        setResourceStatus(componentFactory.getKeyStore().getCertificate(), OptionValue.MAX_AGE_DEFAULT);
    }
    
    


    @Override
    public void processCoapRequest(final SettableFuture<CoapResponse> registrationResponseFuture,
                                   final CoapRequest coapRequest, InetSocketAddress remoteAddress) {

        try{
            log.info("Received certificate request from {}: {}", remoteAddress.getAddress(), coapRequest);

            //Only POST messages are allowed
            if(coapRequest.getMessageCodeName() != MessageCode.Name.GET){
                CoapResponse coapResponse = CoapResponse.createErrorResponse(coapRequest.getMessageTypeName(),
                        MessageCode.Name.METHOD_NOT_ALLOWED_405, "Only GET messages are allowed!");

                registrationResponseFuture.set(coapResponse);
                return;
            }
            
            Set<Long> contentFormats = coapRequest.getAcceptedContentFormats();
            
            if (!contentFormats.contains(PrivIoTContentFormat.APP_X509CERTIFICATE)) {
            	CoapResponse coapResponse = new CoapResponse(coapRequest.getMessageTypeName(), MessageCode.Name.NOT_ACCEPTABLE_406);

                StringBuilder payload = new StringBuilder();
                payload.append("Requested content format(s) (from requests ACCEPT option) not available: ");
                for(long acceptedContentFormat : coapRequest.getAcceptedContentFormats())
                    payload.append("[").append(acceptedContentFormat).append("]");

                coapResponse.setContent(payload.toString().getBytes(CoapMessage.CHARSET), 
                		                ContentFormat.TEXT_PLAIN_UTF8);
                registrationResponseFuture.set(coapResponse);
                return;
            }
            
            CoapResponse coapResponse = new CoapResponse(MessageType.Name.ACK, MessageCode.Name.CONTENT_205);
            coapResponse.setContent(getSerializedResourceStatus(PrivIoTContentFormat.APP_X509CERTIFICATE), 
            		PrivIoTContentFormat.APP_X509CERTIFICATE);
            
            log.debug("send certificate to: " + remoteAddress.getAddress());
            
            registrationResponseFuture.set(coapResponse);
        }
        catch(Exception ex){
            registrationResponseFuture.setException(ex);
        }
    }

    /**
     * Returns an empty byte array as there is only POST allowed and no content provided. However, this method is only
     * implemented for the sake of completeness and is not used at all by the framework.
     *
     * @param contentFormat the number representing the desired content format
     *
     * @return an empty byte array
     */
    @Override
    public byte[] getSerializedResourceStatus(long contentFormat) {
    	if (contentFormat != PrivIoTContentFormat.APP_X509CERTIFICATE) {
    		return new byte[0];
    	}
    	
        try {
        	byte[] encoded = getResourceStatus().getEncoded();
        	
        	String base64Encoded = Base64.encodeBase64String(encoded);
        	
			return base64Encoded.getBytes();
		} catch (CertificateEncodingException e) {
			log.error("Failure during serialize certificate", e);
			return new byte[0];
		}
    }

    @Override
    public byte[] getEtag(long contentFormat) {
        return new byte[0];
    }

    @Override
    public void updateEtag(X509Certificate resourceStatus) {

    }

    @Override
    public void shutdown() {

    }
}
