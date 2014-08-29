package de.uniluebeck.itm.priviot.cpp.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.application.server.CoapServerApplication;
import de.uniluebeck.itm.priviot.cpp.communication.coapwebserver.CoapObserver;
import de.uniluebeck.itm.priviot.cpp.communication.coapwebserver.CoapObserverListener;
import de.uniluebeck.itm.priviot.cpp.communication.coapwebserver.CoapRegistryWebservice;
import de.uniluebeck.itm.priviot.cpp.communication.coapwebserver.CoapRegistryWebserviceListener;
import de.uniluebeck.itm.priviot.cpp.communication.smartserviceproxy.CoapForwardingWebservice;
import de.uniluebeck.itm.priviot.cpp.communication.smartserviceproxy.CoapRegisterClient;
import de.uniluebeck.itm.priviot.cpp.data.Registry;
import de.uniluebeck.itm.priviot.cpp.data.RegistryEntry;
import de.uniluebeck.itm.priviot.utils.data.DataPackageParsingException;
import de.uniluebeck.itm.priviot.utils.data.transfer.EncryptedSensorDataPackage;

/**
 * The Controller connects the data origin with the smart service proxy.
 * 
 * A registration of a data origin is answered with a observe request.
 * 
 * All data that arrives from the data origin is forwarded to the smart service proxy.
 */
public class Controller implements CoapRegistryWebserviceListener,
        CoapObserverListener {
    
    /** Root path of forwarding webservices. The concrete webservices are /forwarding/1, /forwarding/2 */
    private final static String PATH_FORWARDING = "/forwarding/";
    
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
    /** Saves the registered Webservers with their webservices */
    private Registry registry;
    
    /** Sends observe requests to webservices */
    private CoapObserver coapObserver;
    
    /** Receives registry requests from webservers */
    private CoapRegistryWebservice coapRegistryWebservice;
    
    /** Sends registration to Smart Service Proxy */
    private CoapRegisterClient coapRegisterClient;
    
    /** One webservice for every knwon Smart Service Proxy, that forwards all sensor data to the SSP */
    private List<CoapForwardingWebservice> coapForwardingWebservices;
    
    private CoapClientApplication coapClientApplication;
    
    /** 
     * Listens to a local port. web services can be registered here.
     * Used for web services for CoAP-Webservers.
     */
    private CoapServerApplication coapServerApplicationWebservers;
    
    /** 
     * Listens to a local port. Web servers can be registered here.
     * Used for web services for Smart Service Proxies.
     */
    private CoapServerApplication coapServerApplicationSSPs;
    
    /** port of the Smart Service Proxy */
    private int portSSP;
    
    /** port of the CoAP Webserver */
    private int portWebserver;
    
    public Controller(int ownPortWebservers, int ownPortSSPs, int portSSP, int portWebserver) {
        super();
        this.portSSP = portSSP;
        this.portWebserver = portWebserver;
        
        log.info("Open CoAP interface for webservers on port " + ownPortWebservers);
        log.info("Open CoAP interface for Smart Service Proxies on port " + ownPortSSPs);
        
        coapClientApplication = new CoapClientApplication();
        
        coapServerApplicationWebservers = new CoapServerApplication(ownPortWebservers);
    	coapServerApplicationSSPs = new CoapServerApplication(ownPortSSPs);
    	
    	registry = new Registry();
    }
    
    public void start() {        
        coapObserver = new CoapObserver(coapClientApplication);
        coapObserver.setListener(this);
        
        coapRegistryWebservice = new CoapRegistryWebservice(registry, coapClientApplication, portSSP, portWebserver);
        coapRegistryWebservice.setListener(this);
        coapServerApplicationWebservers.registerService(coapRegistryWebservice);
        
        log.info("CoapRegistryWebservice started");
        
        coapRegisterClient = new CoapRegisterClient(coapClientApplication, portSSP);
        
        coapForwardingWebservices = new ArrayList<CoapForwardingWebservice>();
    }
    
    @Override
    public void registeredNewWebserver(URI uriWebserver, URI uriSSP) {
        log.info("Registered new webserver: " + uriWebserver.getHost() + " with SSP " + uriSSP.getHost());
        
        // create and start a CoapForwardingWebservice for this Smart Service Proxy
        String path = PATH_FORWARDING + coapForwardingWebservices.size() + 1;
        CoapForwardingWebservice coapForwardingWebservice = new CoapForwardingWebservice(path, uriSSP);
        coapForwardingWebservices.add(coapForwardingWebservice);
        //TODO: create a new CoapServerApplication for the SSP to separate SSPs from each other
        coapServerApplicationSSPs.registerService(coapForwardingWebservice);
        log.info("Registered new forwarding webservice: " + coapForwardingWebservice.getPath());
        
        // send registration to Smart Service Proxy
        // The proxy will then register itself as observer at the CoapForwardingWebservice
        try {
            coapRegisterClient.sendRegisterRequest(uriSSP.getHost());
        } catch (UnknownHostException | URISyntaxException e) {
            log.error("Registration at Smart Service Proxy failed", e);
        }        
    }
    
    @Override
    public void registeredNewWebservice(URI uriWebservice) {
        log.info("Registered new webservice " + uriWebservice.getHost() + uriWebservice.getPath() + ". send observe request.");
        
        // register as observer
        try {
            coapObserver.registerAsObserver(uriWebservice);
        } catch (UnknownHostException e) {
            log.error("Registered Webservice is unknown host", e);
        }
    }

    @Override
    public void receivedActualStatus(final URI uriWebservice, long contentFormat,
            final ChannelBuffer content) {        
        RegistryEntry registryEntry = registry.getEntryByWebservice(uriWebservice);
        if (registryEntry == null) {
            log.error("received actual status of not registered werbservice: '" + uriWebservice.getHost() + uriWebservice.getPath() + "'");
        }
        
        // get URI of Smart Service Proxy to forward the status message
        URI uriSSP = registryEntry.getSSP();
        
        log.info("Forward received status from '" + uriWebservice.getHost() + uriWebservice.getPath() + 
                  "' to SSP '" + uriSSP.getHost() + ":" + uriSSP.getPort() + "'");
        
        // convert content from ChannelBuffer to EncryptedSensorDataPackage
        byte[] readable = new byte[content.readableBytes()];
        content.toByteBuffer().get(readable, content.readerIndex(), content.readableBytes());
        String contentStr = new String(readable);
        EncryptedSensorDataPackage dataPackage;
        try {
            dataPackage = EncryptedSensorDataPackage.createInstanceFromXMLString(contentStr);
        } catch (NumberFormatException | SAXException | DataPackageParsingException e) {
            log.error("Received content is no valid EncryptedSensorDataPackage", e);
            return;
        }
        
        log.debug("content:\n" + contentStr);
        
        // find the CoapForwardingWebservice for the Smart Service Proxy
        CoapForwardingWebservice coapForwardingWebservice = getForwardingWebserviceForSSP(uriSSP);
        if (coapForwardingWebservice == null) {
            log.error("No CoapForwardingWebservice for Smart Service Proxy '" + uriSSP.getHost() + "'");
            return;
        }
        
        // push status to CoapForwardingWebservice
        coapForwardingWebservice.updateRdfSensorData(dataPackage);
    }
    
    private CoapForwardingWebservice getForwardingWebserviceForSSP(URI uriSSP) {
        for (CoapForwardingWebservice forwardingWebservice : coapForwardingWebservices) {
            if (forwardingWebservice == null) {
                continue;
            }
            
            if (forwardingWebservice.getUriSSP().getHost().equals(uriSSP.getHost()) &&
                forwardingWebservice.getUriSSP().getPort() == uriSSP.getPort()) {
                
                return forwardingWebservice;
            }
        }
        return null;
    }
}
