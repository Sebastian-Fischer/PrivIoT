package de.uniluebeck.itm.priviot.cpp.communication.smartserviceproxy;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.MessageCode;
import de.uniluebeck.itm.ncoap.message.MessageType;
import de.uniluebeck.itm.priviot.cpp.communication.CoapClient;

/**
 * The CoapRegisterClient registers the Coap Privacy Proxy at the Smart Service Proxy, 
 * using the CoapClient.
 */
public class CoapRegisterClient {
    
    private static String urlPathRegistry = "/registry";
    
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
    private int portSSP;
    
    /** Can send CoAP requests using the CoapRegisterClient */
    private CoapClientApplication coapClientApplication;

    /**
     * Constructor.
     * 
     * @param coapClientApplication  The CoapClientApplication object
     * @param urlSSP   The host url of the Smart Service Proxy
     */
    public CoapRegisterClient(CoapClientApplication coapClientApplication, int portSSP) {
        this.coapClientApplication = coapClientApplication;
        this.portSSP = portSSP;
    }
    
    /**
     * Sends the registration request to the Smart Service Proxy.
     * The proxy will react with a registration as observer to the available webservices.
     * @throws URISyntaxException
     * @throws UnknownHostException
     */
    public void sendRegisterRequest(String hostNameSSP) throws URISyntaxException, UnknownHostException {
        // Create CoAP request
        URI uriSSP = new URI ("coap", null, hostNameSSP, portSSP, urlPathRegistry, "", null);
        
        MessageType.Name messageType = MessageType.Name.CON;
        
        CoapRequest coapRequest = new CoapRequest(messageType, MessageCode.Name.POST, uriSSP, false);
        
        // Set recipient (webservice host)
        InetSocketAddress recipient;
        recipient = new InetSocketAddress(InetAddress.getByName(hostNameSSP), portSSP);
        
        CoapClient coapClient = new CoapClient();
        
        // Send the CoAP request
        coapClientApplication.sendCoapRequest(coapRequest, coapClient, recipient);
    }
}
