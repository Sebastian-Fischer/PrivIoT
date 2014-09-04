package de.uniluebeck.itm.priviot.cpp.communication.coapwebserver;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.application.server.webservice.NotObservableWebservice;
import de.uniluebeck.itm.ncoap.application.server.webservice.linkformat.LinkAttribute;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.CoapResponse;
import de.uniluebeck.itm.ncoap.message.MessageCode;
import de.uniluebeck.itm.ncoap.message.MessageType;
import de.uniluebeck.itm.ncoap.message.options.OptionValue;

/**
 * A CoAP-Webservice, that accepts registrations from CoAP-Webservers.
 * 
 * Uppon receiving a registration, it requests the ressource /.well-known/core of the registered
 * CoAP-Webserver to get information about the servers observable webservices.
 * 
 * The CoapRegistryWebservice uses a {@link WellKnownCoreProcessor} to process responses.
 */
public class CoapRegistryWebservice extends NotObservableWebservice<Void> {
	/** Port of the CoAP Webserver */
    private int portWebserver;
    /** Port of the Smart Service Proxy */
    private int portSSP;
    private static String PATH_CORE_RESSOURCE = "/.well-known/core";
    private static String PATH_REGISTRY_RESSOURCE = "/registry";
    
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
    /** Client used to send requests */
    private CoapClientApplication clientApplication;
    
    /** Used to receive callback event from WellKnownCoreProcessor */
    private ScheduledExecutorService internalTasksExecutor;
    
    /** The listener receives events */
    CoapRegistryWebserviceListener listener;
    
    
    public CoapRegistryWebservice(CoapClientApplication clientApplication, int portSSP, int portWebserver) {
        super(PATH_REGISTRY_RESSOURCE, null, OptionValue.MAX_AGE_DEFAULT);
        
        this.clientApplication = clientApplication;
        this.portSSP = portSSP;
        this.portWebserver = portWebserver;
    }
    
    public void setListener(CoapRegistryWebserviceListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void processCoapRequest(final SettableFuture<CoapResponse> registrationResponseFuture,
                                   final CoapRequest coapRequest, InetSocketAddress remoteAddress) {

        try{
            log.debug("Received CoAP registration message from {}: {}", remoteAddress.getAddress(), coapRequest);

            //Only POST messages are allowed
            if(coapRequest.getMessageCodeName() != MessageCode.Name.POST){
                CoapResponse coapResponse = CoapResponse.createErrorResponse(coapRequest.getMessageTypeName(),
                        MessageCode.Name.METHOD_NOT_ALLOWED_405, "Only POST messages are allowed!");

                registrationResponseFuture.set(coapResponse);
                return;
            }
            
            // get url of the Smart Service Proxy from the message content to forward messages later.
            ChannelBuffer buffer = coapRequest.getContent();
            byte[] readable = new byte[buffer.readableBytes()];
            buffer.toByteBuffer().get(readable, buffer.readerIndex(), buffer.readableBytes());
            
            String urlSSP = new String(readable);
            
            log.debug("content urlSSP is: '" + urlSSP + "'");
            
            final URI uriWebserver = createWebserverURI(remoteAddress.getHostName());
            URI uriSSP;
            try {
                uriSSP = createSSPURI(urlSSP);
            }
            catch (Exception e) {
                log.error("Content is not a ssp uri", e);
                registrationResponseFuture.setException(e);
                return;
            }
            
            // notify listener
            if (listener != null) {
                listener.registeredNewWebserver(uriWebserver, uriSSP);
            }

            //Get the set of available Services on the newly registered Server
            ListenableFuture<Set<URI>> servicesFuture = getAvailableWebservices(remoteAddress.getAddress());

            Futures.addCallback(servicesFuture, new FutureCallback<Set<URI>>() {
                @Override
                public void onSuccess(Set<URI> result) {                        
                    // add available webservices to registry
                    for (URI uriWebservice : result){                        
                        // notify listener
                        if (listener != null) {
                            listener.registeredNewWebservice(uriWebservice);
                        }
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    registrationResponseFuture.setException(t);
                }
            });
        }
        catch(Exception ex){
            registrationResponseFuture.setException(ex);
        }
    }
    
    /**
     * Returns an empty byte array as there is only POST allowed and no content provided. However, this method is only
     * implemented for the sake of completeness and is not used at all by the framework.
     *
     * @param contentFormat the number representing the desired content format
     *
     * @return an empty byte array
     */
    @Override
    public byte[] getSerializedResourceStatus(long contentFormat) {
        return new byte[0];
    }
    
    @Override
    public byte[] getEtag(long contentFormat) {
        return new byte[0];
    }

    @Override
    public void updateEtag(Void resourceStatus) {

    }

    @Override
    public void shutdown() {

    }
    
    private URI createWebserverURI(String hostName) throws URISyntaxException {
        return new URI("coap", null, hostName, portWebserver, "/", null, null);
    }
    
    private URI createWebserverCoreURI(String hostName) throws URISyntaxException {
        return new URI("coap", null, hostName, portWebserver, PATH_CORE_RESSOURCE, null, null);
    }
    
    private URI createWebserviceURI(String hostName, String servicePath) throws URISyntaxException {
        return new URI("coap", null, hostName, portWebserver, "/" + servicePath, null, null);
    }
    
    private URI createSSPURI(String hostName) throws URISyntaxException {
        return new URI("coap", null, hostName, portSSP, "/", null, null);
    }

    /**
     * Sends a request to the ressource /.well-known/core of a webserver.
     * Retreives a list of the URIs of the available webservices.
     * Every webservice is registered in the registry.
     * @param remoteAddress
     * @return
     * @throws Exception
     */
    private ListenableFuture<Set<URI>> getAvailableWebservices(final InetAddress remoteAddress) throws Exception {

        final SettableFuture<Set<URI>> servicesFuture = SettableFuture.create();
        final String remoteHostName = remoteAddress.getHostName();
        final URI uri = createWebserverCoreURI(remoteHostName);
        
        CoapRequest coapRequest = new CoapRequest(MessageType.Name.CON, MessageCode.Name.GET, uri);
        WellKnownCoreProcessor responseProcessor = new WellKnownCoreProcessor(internalTasksExecutor);

        this.clientApplication.sendCoapRequest(coapRequest, responseProcessor, new InetSocketAddress(remoteAddress, portWebserver));

        Futures.addCallback(responseProcessor.getWellKnownCoreFuture(),
                new FutureCallback<Multimap<String, LinkAttribute>>() {

                    @Override
                    public void onSuccess(Multimap<String, LinkAttribute> result) {
                        try{
                            if(result == null){
                                servicesFuture.set(new HashSet<URI>());
                                return;
                            }

                            Set<URI> serviceUris = new HashSet<>(result.keySet().size());
                            for(String servicePath : result.keySet()){
                                URI serviceUri = createWebserviceURI(remoteHostName, servicePath);

                                serviceUris.add(serviceUri);
                            }

                            servicesFuture.set(serviceUris);
                        }
                        catch(Exception ex){
                            log.error("This should never happen!", ex);
                            servicesFuture.setException(ex);
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        log.error("This should never happen!", t);
                        servicesFuture.setException(t);
                    }
                });

        return servicesFuture;
    }
}
