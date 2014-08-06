package priviot.data_origin.service;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import priviot.data_origin.data.KeyDatabaseEntry;
import priviot.utils.data.transfer.PrivIoTContentFormat;
import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.CoapResponse;
import de.uniluebeck.itm.ncoap.message.MessageCode;
import de.uniluebeck.itm.ncoap.message.MessageType;

/**
 * This component has two tasks to perform:
 * 1. It can communicate with a CoAP Provacy Proxy to register the CoAP-Webserver.
 * 2. It can communicate with a Smart Service Proxy to retreive it's X509 certificate.
 * 
 * For communication a {@link CoapClient} is used.
 */
public class CoapRegisterClient implements Observer {
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
        
        this.urlSSP = urlSSP;
        this.portSSP = portSSP;
        this.urlCPP = urlCPP;
        this.portCPP = portCPP;
        
        coapClient = new CoapClient();
        coapClient.addObserver(this);
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
        // Create CoAP request
        URI webserviceURI = new URI ("coap", null, urlCPP, portCPP, urlPathRegistry, "", null);
        
        MessageType.Name messageType = MessageType.Name.CON;
        
        CoapRequest coapRequest = new CoapRequest(messageType, MessageCode.Name.GET, webserviceURI, false);
        
        // Set recipient (webservice host)
        InetSocketAddress recipient;
        recipient = new InetSocketAddress(InetAddress.getByName(urlCPP), portCPP);
        
        // Send the CoAP request
        coapClientApplication.sendCoapRequest(coapRequest, coapClient, recipient);
    }

    //TODO: change to own observer class
    @Override
    public void update(Observable coapClientObs, Object coapResponseObj) {
        if (coapClient != coapClientObs) {
            return;
        }
        
        CoapResponse coapResponse = (CoapResponse)coapResponseObj;
        
        URI locationUri;
        try {
            locationUri = coapResponse.getLocationURI();
        } catch (URISyntaxException e) {
            log.error("location uri of received message is bad formed");
            return;
        }
        String baseUri = locationUri.getHost();
        
        log.info("received message from: " + locationUri + " (host: " + baseUri + ")");
        
        // if this is answer to certificate request
        if (coapResponse.getContentFormat() == PrivIoTContentFormat.APP_X509CERTIFICATE) {
            //TODO: parse X509Certificate
            // maybe like this: http://www.oracle.com/technetwork/articles/javase/dig-signature-api-140772.html
            X509Certificate certificate;
            
            if (observer != null) {
                observer.receivedCertificate(locationUri, certificate);
            }
        }
      
    }
    
}
