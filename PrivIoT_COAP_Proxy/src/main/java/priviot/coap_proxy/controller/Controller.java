package priviot.coap_proxy.controller;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import priviot.coap_proxy.communication.data_origin.DataOriginCoapClient;
import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.MessageCode;
import de.uniluebeck.itm.ncoap.message.MessageType;

/**
 * The Controller connects the data origin with the smart service proxy.
 * 
 * A registration of a data origin is answered with a observe request.
 * 
 * All data that arrives from the data origin is passed to the smart service proxy.
 */
public class Controller extends CoapClientApplication {
    
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
    public Controller() {
        
    }
    
    public void start() {
        String host = "127.0.0.1";
        int port = 5683;
        String path = "/sensor1";
        
        log.info("send observe request to " + host + ":" + port + " with path " + path);
        
        try {
            sendDataOriginObserveRequest(host, port, path, "");
        } catch (UnknownHostException e) {
            log.error("Unknown host: '" + host + ":" + port);
        } catch (URISyntaxException e) {
            log.error("syntax error: " + e.getMessage());
        }
    }
    
    private DataOriginCoapClient sendDataOriginObserveRequest(String uriHost, int uriPort, String uriPath, String uriQuery) throws URISyntaxException, UnknownHostException {
        // Create CoAP request
        URI webserviceURI = new URI ("coap", null, uriHost, uriPort, uriPath, uriQuery, null);
    
        MessageType.Name messageType = MessageType.Name.CON;
    
        CoapRequest coapRequest = new CoapRequest(messageType, MessageCode.Name.GET, webserviceURI, false);
        
        coapRequest.setObserve();
        
        // Set recipient (webservice host)
        InetSocketAddress recipient;
        recipient = new InetSocketAddress(InetAddress.getByName(uriHost), uriPort);
    
        DataOriginCoapClient dataOriginCoapClient = new DataOriginCoapClient();
        
        // Send the CoAP request
        this.sendCoapRequest(coapRequest, dataOriginCoapClient, recipient);
        
        return dataOriginCoapClient;
    }
}
