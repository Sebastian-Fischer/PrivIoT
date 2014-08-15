package priviot.cpp.communication.smartserviceproxy;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import priviot.cpp.communication.CoapClient;
import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.MessageCode;
import de.uniluebeck.itm.ncoap.message.MessageType;

/**
 * The CoapRegisterClient registers the Coap Privacy Proxy at the Smart Service Proxy, 
 * using the CoapClient.
 */
public class CoapRegisterClient {
    
    private static String urlPathRegistry = "/registry";
    
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
    private String urlSSP;
    private static int portSSP = 8080;
    
    /** Can send CoAP requests using the CoapRegisterClient */
    private CoapClientApplication coapClientApplication;
    
    /** Used to send CoAP requests */
    private CoapClient coapClient;

    /**
     * Constructor.
     * 
     * @param coapClientApplication  The CoapClientApplication object
     * @param urlSSP   The host url of the Smart Service Proxy
     */
    public CoapRegisterClient(CoapClientApplication coapClientApplication, CoapClient coapClient, String urlSSP) {
        this.coapClientApplication = coapClientApplication;
        this.coapClient = coapClient;
        
        this.urlSSP = urlSSP;
    }
    
    /**
     * Sends the registration request to the Smart Service Proxy.
     * The proxy will react with a registration as observer to the available webservices.
     * @throws URISyntaxException
     * @throws UnknownHostException
     */
    public void sendRegisterRequest() throws URISyntaxException, UnknownHostException {
        // Create CoAP request
        URI webserviceURI = new URI ("coap", null, urlSSP, portSSP, urlPathRegistry, "", null);
        
        MessageType.Name messageType = MessageType.Name.CON;
        
        CoapRequest coapRequest = new CoapRequest(messageType, MessageCode.Name.GET, webserviceURI, false);
        
        // Set recipient (webservice host)
        InetSocketAddress recipient;
        recipient = new InetSocketAddress(InetAddress.getByName(urlSSP), portSSP);
        
        // Send the CoAP request
        coapClientApplication.sendCoapRequest(coapRequest, coapClient, recipient);
    }
}
