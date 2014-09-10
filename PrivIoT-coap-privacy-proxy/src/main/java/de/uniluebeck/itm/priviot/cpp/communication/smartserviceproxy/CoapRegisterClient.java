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
	
	private static String QUERY_ADD = "add=";
	private static String QUERY_REMOVE = "remove=";
    
    private String urlPathRegistry;
    
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
    public CoapRegisterClient(CoapClientApplication coapClientApplication, int portSSP, String urlPathRegistry) {
        this.coapClientApplication = coapClientApplication;
        this.portSSP = portSSP;
        this.urlPathRegistry = urlPathRegistry;
    }
    
    /**
     * Sends the registration request to the Smart Service Proxy.
     * Registers the whole webserver with all it's webservices.
     * The proxy will react with a registration as observer to the available webservices.
     * @throws URISyntaxException
     * @throws UnknownHostException
     */
    public void registerWebserver(String hostNameSSP) throws URISyntaxException, UnknownHostException {
    	sendPostRequestToSSP(hostNameSSP, "");
    }
    
    /**
     * Sends a registration request to the Smart Service Proxy.
     * Registers only the webservice given by pathWebservice.
     * @param hostNameSSP
     * @param pathWebservice
     * @throws URISyntaxException
     * @throws UnknownHostException
     */
    public void registerWebservice(String hostNameSSP, String pathWebservice) throws URISyntaxException, UnknownHostException {
    	String query =  QUERY_ADD + pathWebservice;
    			
    	sendPostRequestToSSP(hostNameSSP, query);
    }
    
    /**
     * Sends an unregistration request to the Smart Service Proxy.
     * Unregisters only the webservice given by pathWebservice.
     * @param hostNameSSP
     * @param pathWebservice
     * @throws URISyntaxException
     * @throws UnknownHostException
     */
    public void unregisterWebservice(String hostNameSSP, String pathWebservice) throws URISyntaxException, UnknownHostException {
    	String query =  QUERY_REMOVE + pathWebservice;
    			
    	sendPostRequestToSSP(hostNameSSP, query);
    }
    
    private void sendPostRequestToSSP(String hostNameSSP, String query) throws URISyntaxException, UnknownHostException {
    	URI uriSSP = new URI ("coap", null, hostNameSSP, portSSP, urlPathRegistry, query, null);
    	
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
