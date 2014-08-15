package priviot.coapwebserver.service;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.CoapResponse;
import de.uniluebeck.itm.ncoap.message.MessageCode;
import de.uniluebeck.itm.ncoap.message.MessageType;

import priviot.utils.data.transfer.PrivIoTContentFormat;

/**
 * This component has two tasks to perform:
 * 1. It can communicate with a CoAP Provacy Proxy to register the CoAP-Webserver.
 * 2. It can communicate with a Smart Service Proxy to retreive it's X509 certificate.
 * 
 * For communication a {@link CoapClient} is used.
 */
public class CoapRegisterClient implements CoapClientObserver {
    private static String urlPathCertificate = "/certificate";
    private static String urlPathRegistry = "/registry";
    
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
    private String urlSSP;
    private int portSSP;
    private String urlCPP;
    private int portCPP;
    
    /** Can send CoAP requests using the CoapRegisterClient */
    private CoapClientApplication coapClientApplication;
    
    /** Used to send CoAP requests */
    private CoapClient coapClient;
    
    private CoapRegisterClientObserver observer;
    
    private boolean isInitialized = false;
    
    /**
     * Constructor.
     * 
     * @param coapClientApplication  The CoapClientApplication object
     * @param urlSSP   The host url of the Smart Service Proxy
     * @param portSSP  The port of the Smart Service Proxy
     * @param urlCPP   The host url of the CoAP Privacy Proxy
     * @param portCPP  The port of the CoAP Privacy Proxy
     */
    public CoapRegisterClient(CoapClientApplication coapClientApplication,
            String urlSSP, int portSSP, String urlCPP, int portCPP) {
        
        this.coapClientApplication = coapClientApplication;
        this.urlSSP = urlSSP;
        this.portSSP = portSSP;
        this.urlCPP = urlCPP;
        this.portCPP = portCPP;
        
        coapClient = new CoapClient();
    }
    
    private void initialize() {
        coapClient.addObserver(this);
        
        isInitialized = true;
    }
    
    public void setObserver(CoapRegisterClientObserver observer) {
        this.observer = observer;
    }
    
    /**
     * Sends a certificate request to the Smart Service Proxy to get it's X509 certificate.
     * @throws URISyntaxException
     * @throws UnknownHostException
     */
    public void sendCertificateRequest() throws URISyntaxException, UnknownHostException {
        if (!isInitialized) {
            initialize();
        }
        
        // Create CoAP request
        URI webserviceURI = new URI ("coap", null, urlSSP, portSSP, urlPathCertificate, "", null);
        
        MessageType.Name messageType = MessageType.Name.CON;
        
        CoapRequest coapRequest = new CoapRequest(messageType, MessageCode.Name.GET, webserviceURI, false);
        
        // Set recipient (webservice host)
        InetSocketAddress recipient;
        recipient = new InetSocketAddress(InetAddress.getByName(urlSSP), portSSP);
        
        // Send the CoAP request
        coapClientApplication.sendCoapRequest(coapRequest, coapClient, recipient);
    }
    
    /**
     * Sends the registration request to the CoAP Privacy Proxy.
     * The proxy will react with a registration as observer to the available webservices.
     * @throws URISyntaxException
     * @throws UnknownHostException
     */
    public void sendRegisterRequest() throws URISyntaxException, UnknownHostException {
        if (!isInitialized) {
            initialize();
        }
        
        // Create CoAP request
        URI webserviceURI = new URI ("coap", null, urlCPP, portCPP, urlPathRegistry, "", null);
        
        MessageType.Name messageType = MessageType.Name.CON;
        
        CoapRequest coapRequest = new CoapRequest(messageType, MessageCode.Name.POST, webserviceURI, false);
        coapRequest.setContent("localhost".getBytes());
        
        // Set recipient (webservice host)
        InetSocketAddress recipient;
        recipient = new InetSocketAddress(InetAddress.getByName(urlCPP), portCPP);
        
        // Send the CoAP request
        coapClientApplication.sendCoapRequest(coapRequest, coapClient, recipient);
        
        log.info("REGISTER request sent to: " + recipient.getAddress() + ":" + recipient.getPort());
    }

    @Override
    public void receivedResponse(CoapResponse coapResponse) {        
        URI locationUri;
        try {
            locationUri = coapResponse.getLocationURI();
        } catch (URISyntaxException e) {
            log.error("location uri of received message is bad formed");
            return;
        }
        if (locationUri == null) {
            log.error("location uri is null");
            return;
        }
        if (coapResponse.getMessageCode() != MessageCode.Name.CONTENT_205.getNumber()) {
            log.info("Received message code " + coapResponse.getMessageCode());
            return;
        }
        String baseUri = locationUri.getHost();
        
        log.info("RECEIVED message from: " + locationUri + " (host: " + baseUri + ")");
        
        // if this is answer to certificate request
        if (coapResponse.getContentFormat() == PrivIoTContentFormat.APP_X509CERTIFICATE) {
            //TODO: parse X509Certificate
            // maybe like this: http://www.oracle.com/technetwork/articles/javase/dig-signature-api-140772.html
            X509Certificate certificate = null;
            
            log.warn("TODO: Parse the received X509Certificate");
            
            if (observer != null) {
                observer.receivedCertificate(locationUri, certificate);
            }
        }
      
    }
    
}
