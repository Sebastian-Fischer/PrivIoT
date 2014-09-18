package de.uniluebeck.itm.priviot.coapwebserver.service;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.CoapResponse;
import de.uniluebeck.itm.ncoap.message.MessageCode;
import de.uniluebeck.itm.ncoap.message.MessageType;
import de.uniluebeck.itm.priviot.utils.data.PrivIoTContentFormat;

/**
 * This component has two tasks to perform:
 * 1. It can communicate with a CoAP Provacy Proxy to register the CoAP-Webserver.
 * 2. It can communicate with a Smart Service Proxy to retreive it's X509 certificate.
 * 
 * For communication a {@link CoapClient} is used.
 */
public class CoapRegisterClient {
    private static String urlPathCertificate = "/certificate";
    private static String urlPathRegistry = "/registry";
    
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
    private String urlSSP;
    private int portSSP;
    private String urlCPP;
    private int portCPP;
    
    /** Can send CoAP requests using the CoapRegisterClient */
    private CoapClientApplication coapClientApplication;
    
    private CoapRegisterClientObserver observer;
    
    /**
     * Constructor.
     * 
     * @param observer  The observer who will receive events
     * @param coapClientApplication  The CoapClientApplication object
     * @param urlSSP   The host url of the Smart Service Proxy
     * @param portSSP  The port of the Smart Service Proxy
     * @param urlCPP   The host url of the CoAP Privacy Proxy
     * @param portCPP  The port of the CoAP Privacy Proxy
     */
    public CoapRegisterClient(CoapRegisterClientObserver observer,
    		CoapClientApplication coapClientApplication,
            String urlSSP, int portSSP, String urlCPP, int portCPP) {
        
    	this.observer = observer;
        this.coapClientApplication = coapClientApplication;
        this.urlSSP = urlSSP;
        this.portSSP = portSSP;
        this.urlCPP = urlCPP;
        this.portCPP = portCPP;
    }
    
    /**
     * Sends a certificate request to the Smart Service Proxy to get it's X509 certificate.
     * @throws URISyntaxException
     * @throws UnknownHostException
     */
    public void sendCertificateRequest() throws URISyntaxException, UnknownHostException {
        // Create CoAP request
        final URI webserviceURI = new URI ("coap", null, urlSSP, portSSP, urlPathCertificate, "", null);
        
        MessageType.Name messageType = MessageType.Name.CON;
        
        CoapRequest coapRequest = new CoapRequest(messageType, MessageCode.Name.GET, webserviceURI, false);
        coapRequest.setAccept(PrivIoTContentFormat.APP_X509CERTIFICATE);
        
        // Set recipient (webservice host)
        InetSocketAddress recipient;
        recipient = new InetSocketAddress(InetAddress.getByName(urlSSP), portSSP);
        
        CoapClient coapClient = new CoapClient();
        
        log.info("send certificate request to " + recipient.getAddress());
        
        // Send the CoAP request
        coapClientApplication.sendCoapRequest(coapRequest, coapClient, recipient);
        
        Futures.addCallback(coapClient.getResponseFuture(), new FutureCallback<CoapResponse>() {
        	@Override
			public void onSuccess(CoapResponse response) {
        		// parse received certificate
        		
        		log.debug("Received response to certificate request");
        		
        		if (response.getContentFormat() != PrivIoTContentFormat.APP_X509CERTIFICATE) {
        			log.error("Received response to certificate request with unexpected content format: " + response.getContentFormat());
        			return;
        		}
        		
        		Certificate certificate = null;
        		X509Certificate x509certificate = null;
        		CertificateFactory certificateFactory;
                
                byte[] coapPayload = new byte[response.getContent().readableBytes()];
                response.getContent().getBytes(0, coapPayload);
                
                byte[] encodedCertificate = Base64.decodeBase64(new String(coapPayload));
        	
				try {
					certificateFactory = CertificateFactory.getInstance("X.509");
				
					certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(encodedCertificate));
				} catch (CertificateException e) {
					log.error("Failure during parsing of received certificate", e);
					return;
				}
        		
        		if (!(certificate instanceof X509Certificate)) {
        			log.error("Received certificate is no X.509 certificate");
        			return;
        		}
        		
        		x509certificate = (X509Certificate)certificate;
                
        		log.debug("Parsed response to certificate request");
        		
                if (observer != null) {
                    observer.receivedCertificate(webserviceURI, x509certificate);
                }
			}
        	
        	@Override
			public void onFailure(Throwable e) {
			}
        });
    }
    
    /**
     * Sends the registration request to the CoAP Privacy Proxy.
     * The proxy will react with a registration as observer to the available webservices.
     * @throws URISyntaxException
     * @throws UnknownHostException
     */
    public void sendRegisterRequestToCPP() throws URISyntaxException, UnknownHostException {
    	
    	sendRegisterRequest(urlCPP, portCPP, urlPathRegistry, urlSSP.getBytes());
    }
    
    /**
     * Sends the registration request to the CoAP Privacy Proxy.
     * The proxy will react with a registration as observer to the available webservices.
     * @throws URISyntaxException
     * @throws UnknownHostException
     */
    public void sendRegisterRequestToSSP() throws URISyntaxException, UnknownHostException {
    	
    	sendRegisterRequest(urlSSP, portSSP, urlPathRegistry, null);
    }
    
    /**
     * Sends the registration request to the CoAP Privacy Proxy.
     * The proxy will react with a registration as observer to the available webservices.
     * @throws URISyntaxException
     * @throws UnknownHostException
     */
    private void sendRegisterRequest(String host, int port, String path, byte[] content) throws URISyntaxException, UnknownHostException {
        // Create CoAP request
        URI webserviceURI = new URI ("coap", null, host, port, path, "", null);
        
        MessageType.Name messageType = MessageType.Name.CON;
        
        CoapRequest coapRequest = new CoapRequest(messageType, MessageCode.Name.POST, webserviceURI, false);
        
        if (content != null) {
        	coapRequest.setContent(urlSSP.getBytes());
        }
        
        // Set recipient (webservice host)
        InetSocketAddress recipient;
        recipient = new InetSocketAddress(InetAddress.getByName(host), port);
        
        CoapClient coapClient = new CoapClient();
        
        // Send the CoAP request
        coapClientApplication.sendCoapRequest(coapRequest, coapClient, recipient);
        
        log.debug("register request sent to: " + recipient.getAddress() + ":" + recipient.getPort());
    }
    
}
