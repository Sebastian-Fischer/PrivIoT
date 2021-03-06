package de.uniluebeck.itm.priviot.cpp.communication.coapwebserver;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.CoapResponse;
import de.uniluebeck.itm.ncoap.message.MessageCode;
import de.uniluebeck.itm.ncoap.message.MessageType;
import de.uniluebeck.itm.ncoap.message.options.ContentFormat;
import de.uniluebeck.itm.priviot.cpp.communication.CoapClient;
import de.uniluebeck.itm.priviot.cpp.communication.CoapClientListener;

/**
 * The CoapObserver registeres the Coap Privacy Proxy at observable sensor webservices 
 * of registered CoAP-Webservers.
 * 
 * After the observer registration it receives periodically the actual data of the sensors and
 * sends them to the registered CoapObserverListener.
 */
public class CoapObserver {    
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
    /** Can send CoAP requests using the CoapRegisterClient */
    private CoapClientApplication coapClientApplication;
    
    /** The listener receives events */
    private CoapObserverListener listener;
    
    /**
     * Constructor.
     * 
     * @param coapClientApplication  The CoapClientApplication object
     * @param urlSSP   The host url of the Smart Service Proxy
     */
    public CoapObserver(CoapClientApplication coapClientApplication) {
        this.coapClientApplication = coapClientApplication;
    }
    
    public void setListener(CoapObserverListener listener) {
        this.listener = listener;
    }
    
    /**
     * Registers the CoAP Privacy Proxy as observer at a given webservice of a given
     * CoAP-Webserver.
     * @param urlCoapWebserver   Base url of the CoAP-Webserver
     * @param portCoapWebserver  Port of the CoAP-Webserver
     * @param urlPath            Path of the webservice, relative to urlCoapWebserver
     * @throws UnknownHostException 
     */
    public void registerAsObserver(final URI uriWebservice) throws UnknownHostException {
        MessageType.Name messageType = MessageType.Name.CON;
        
        CoapRequest coapRequest = new CoapRequest(messageType, MessageCode.Name.GET, uriWebservice, false);
        
        coapRequest.setAccept(ContentFormat.APP_XML);
        
        coapRequest.setObserve();
        
        // Set recipient (webservice host)
        InetSocketAddress recipient = new InetSocketAddress(InetAddress.getByName(uriWebservice.getHost()), 
                                                            uriWebservice.getPort());
        
        CoapClient coapClient = new CoapClient(uriWebservice);
        
        // Send the CoAP request
        coapClientApplication.sendCoapRequest(coapRequest, coapClient, recipient);
        
        coapClient.addListener(new CoapClientListener() {
            @Override
            public void receivedResponse(URI endpoint, CoapResponse response) {
                if (!response.getMessageCodeName().equals(MessageCode.Name.CONTENT_205)) {
                    log.error("Wrong behavior of webservice. Received message with code " + response.getMessageCodeName());
                    return;
                }
                
                if (response.getContent().readableBytes() == 0) {
                    // empty status
                    return;
                }
                if (listener != null) {
                    listener.receivedActualStatus(endpoint, response.getContentFormat(), response.getContent(), response.getMaxAge());
                }
            }
        });
    }
    
}
