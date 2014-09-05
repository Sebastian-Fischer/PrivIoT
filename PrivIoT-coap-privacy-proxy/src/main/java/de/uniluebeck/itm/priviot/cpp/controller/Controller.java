package de.uniluebeck.itm.priviot.cpp.controller;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import de.uniluebeck.itm.priviot.cpp.data.WebserviceEntry;
import de.uniluebeck.itm.priviot.utils.data.PrivacyDataPackageUnmarshaller;
import de.uniluebeck.itm.priviot.utils.data.generated.PrivacyDataPackage;

/**
 * The Controller connects the data origin with the smart service proxy.
 * 
 * A registration of a data origin is answered with a observe request.
 * 
 * All data that arrives from the data origin is forwarded to the smart service proxy.
 */
public class Controller implements CoapRegistryWebserviceListener,
        CoapObserverListener {
    
    /** Root path of forwarding webservices. The concrete webservices are <basePathForwarding>/1, <basePathForwarding>/2 */
    private  String basePathForwarding;
    
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
    /** Saves the registered Webservers with their webservices */
    private volatile Registry registry;
    
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
    
    
    public Controller(Configuration config) {
        this.basePathForwarding = config.getString("forwadingpath");
        int ownPortSSPs = config.getInt("port.ownssp");
        int ownPortWebservers = config.getInt("port.owncoapwebserver");
        this.portSSP = config.getInt("port.ssp");
        this.portWebserver = config.getInt("port.coapwebserver");
        
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
        
        coapRegistryWebservice = new CoapRegistryWebservice(coapClientApplication, portSSP, portWebserver);
        coapRegistryWebservice.setListener(this);
        coapServerApplicationWebservers.registerService(coapRegistryWebservice);
        
        log.info("CoapRegistryWebservice started");
        
        coapRegisterClient = new CoapRegisterClient(coapClientApplication, portSSP);
        
        coapForwardingWebservices = new ArrayList<CoapForwardingWebservice>();
    }
    
    @Override
    public void registeredNewWebserver(URI uriWebserver, URI uriSSP) {
        log.info("Registered new webserver: " + uriWebserver.getHost() + " with SSP " + uriSSP.getHost());
        
        registry.addEntry(new RegistryEntry(uriWebserver, uriSSP));
    }
    
    @Override
    public void registeredNewWebservice(URI uriWebservice) {
        log.info("Registered new webservice " + uriWebservice.getHost() + uriWebservice.getPath() + ". send observe request.");
        
        RegistryEntry registryEntry = registry.getEntryByWebservice(uriWebservice);
        if (registryEntry == null) {
        	log.error("Registered Webservice from unknown webserver");
        	return;
        }
        
        // create and start a CoapForwardingWebservice for this web service
        String path = basePathForwarding + coapForwardingWebservices.size() + 1;
        CoapForwardingWebservice coapForwardingWebservice = new CoapForwardingWebservice(path);
        coapServerApplicationSSPs.registerService(coapForwardingWebservice);
        log.info("Registered new forwarding webservice: " + coapForwardingWebservice.getPath());
        
        registryEntry.addWebservice(new WebserviceEntry(uriWebservice, coapForwardingWebservice));
        
        // register as observer
        try {
            coapObserver.registerAsObserver(uriWebservice);
        } catch (UnknownHostException e) {
            log.error("Registered Webservice is unknown host", e);
            return;
        }
        
        // send registration to Smart Service Proxy
        // The proxy will then register itself as observer at the CoapForwardingWebservice
        try {
            coapRegisterClient.sendRegisterRequest(registryEntry.getSSP().getHost());
        } catch (UnknownHostException | URISyntaxException e) {
            log.error("Registration at Smart Service Proxy failed", e);
        }
    }

    @Override
    public void receivedActualStatus(final URI uriWebservice, long contentFormat,
            final ChannelBuffer content, long contentLifetime) {        
        RegistryEntry registryEntry = registry.getEntryByWebservice(uriWebservice);
        if (registryEntry == null) {
            log.error("received actual status of not registered werbservice: '" + uriWebservice.getHost() + uriWebservice.getPath() + "'");
        }
        
        // get URI of Smart Service Proxy to forward the status message
        URI uriSSP = registryEntry.getSSP();
        
        log.info("Forward received status from '" + uriWebservice.getHost() + uriWebservice.getPath() + 
                  "' to SSP '" + uriSSP.getHost() + ":" + uriSSP.getPort() + "'");
        
        // convert content from ChannelBuffer to InputStream
        byte[] coapPayload = new byte[content.readableBytes()];
        content.toByteBuffer().get(coapPayload, content.readerIndex(), content.readableBytes());
        final ByteArrayInputStream inStream = new ByteArrayInputStream(coapPayload);
        
        log.debug("content:\n" + new String(coapPayload));
        
        // unmarshall PrivacyDataPackage
        PrivacyDataPackage dataPackage;
        try {
        	dataPackage = PrivacyDataPackageUnmarshaller.unmarshal(inStream);
        } catch (JAXBException | XMLStreamException e) {
            log.error("CoAP payload is not a PrivacyDataPackage");
            return;
        }
        
        // find the CoapForwardingWebservice for the web service
        CoapForwardingWebservice coapForwardingWebservice = getForwardingWebserviceForWebservice(uriWebservice);
        if (coapForwardingWebservice == null) {
            log.error("No CoapForwardingWebservice for Smart Service Proxy '" + uriSSP.getHost() + "'");
            return;
        }
        
        // push status to CoapForwardingWebservice
        coapForwardingWebservice.updateRdfSensorData(dataPackage, contentLifetime);
    }
    
    /**
     * Searches for the corresponding {@link CoapForwardingWebservice} for a given
     * original CoAP-Webservice.
     * @param uriWebservice
     * @return The {@link CoapForwardingWebservice} or null if CoAP-Webservice is not registered.
     */
    private CoapForwardingWebservice getForwardingWebserviceForWebservice(URI uriWebservice) {
    	RegistryEntry registryEntry = registry.getEntryByWebservice(uriWebservice);
    	
    	if (registryEntry == null) {
    		return null;
    	}
    	
    	WebserviceEntry webserviceEntry = registryEntry.getWebservice(uriWebservice);
    	
    	if (webserviceEntry == null) {
    		return null;
    	}
    	
    	return webserviceEntry.getCoapForwardingWebservice();
    }
}
