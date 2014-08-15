package priviot.cpp.controller;

import java.net.URI;
import java.net.UnknownHostException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import priviot.cpp.communication.coapwebserver.CoapObserver;
import priviot.cpp.communication.coapwebserver.CoapObserverListener;
import priviot.cpp.communication.coapwebserver.CoapRegistryWebservice;
import priviot.cpp.communication.coapwebserver.CoapRegistryWebserviceListener;
import priviot.cpp.data.Registry;
import priviot.cpp.data.RegistryEntry;
import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.application.server.CoapServerApplication;

/**
 * The Controller connects the data origin with the smart service proxy.
 * 
 * A registration of a data origin is answered with a observe request.
 * 
 * All data that arrives from the data origin is forwarded to the smart service proxy.
 */
public class Controller implements CoapRegistryWebserviceListener,
        CoapObserverListener {
    private static int OWN_PORT = 8081;
    
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
    /** Saves the registered Webservers with their webservices */
    private Registry registry;
    
    /** Sends observe requests to webservices */
    private CoapObserver coapObserver;
    
    /** Receives registry requests from webservers */
    private CoapRegistryWebservice coapRegistryWebservice;
    
    private CoapClientApplication coapClientApplication;
    
    private CoapServerApplication coapServerApplication;
    
    public Controller() {
        super();
    }
    
    public void start() {
        registry = new Registry();
        
        coapClientApplication = new CoapClientApplication();
        
        coapServerApplication = new CoapServerApplication(OWN_PORT);
        
        coapObserver = new CoapObserver(coapClientApplication);
        coapObserver.setListener(this);
        
        coapRegistryWebservice = new CoapRegistryWebservice(registry, coapClientApplication);
        coapRegistryWebservice.setListener(this);
        
        coapServerApplication.registerService(coapRegistryWebservice);
    }
    
    @Override
    public void registeredNewWebservice(URI uriWebservice) {
        //TODO: get actual ressource status needed before observe?
        
        log.info("registered new webservice. send observe request.");
        
        // register as observer
        try {
            coapObserver.registerAsObserver(uriWebservice);
        } catch (UnknownHostException e) {
            log.error("Registered Webservice is unknown host", e);
        }
    }

    @Override
    public void receivedActualStatus(URI uriWebservice, long contentFormat,
            ChannelBuffer content) {
        
        RegistryEntry registryEntry = registry.getEntryByWebservice(uriWebservice);
        if (registryEntry == null) {
            log.error("received actual status of not registered werbservice: '" + uriWebservice.getHost() + uriWebservice.getPath() + "'");
        }
        
        // get URI of Smart Service Proxy to forward the status message
        URI uriSSP = registryEntry.getSSP();
        
        log.info("FORWARD received status from '" + uriWebservice.getHost() + uriWebservice.getPath() + 
                 "' to SSP '" + uriSSP.getHost() + ":" + uriSSP.getPort() + "'");
        
        byte[] readable = new byte[content.readableBytes()];
        content.toByteBuffer().get(readable, content.readerIndex(), content.readableBytes());
        String contentStr = new String(readable);
        log.info("Content:\n" + contentStr);
        
        //TODO: push status to CoapForwardingWebservice
    }
}
