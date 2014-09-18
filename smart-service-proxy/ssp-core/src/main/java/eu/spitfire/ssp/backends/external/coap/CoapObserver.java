package eu.spitfire.ssp.backends.external.coap;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hp.hpl.jena.rdf.model.Model;

import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.application.client.Token;
import de.uniluebeck.itm.ncoap.communication.observe.client.UpdateNotificationProcessor;
import de.uniluebeck.itm.ncoap.communication.reliability.outgoing.RetransmissionTimeoutProcessor;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.CoapResponse;
import de.uniluebeck.itm.ncoap.message.MessageCode;
import de.uniluebeck.itm.ncoap.message.MessageType;
import de.uniluebeck.itm.ncoap.message.options.ContentFormat;
import eu.spitfire.ssp.backends.generic.Observer;
import eu.spitfire.ssp.server.internal.messages.responses.ExpiringNamedGraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Date;

/**
 * The {@link CoapObserver} is the component to observe registered
 * CoAP Web Services (as {@link eu.spitfire.ssp.backends.generic.DataOrigin}) and update the SSPs cache according to
 * the observations, i.e. status changes.
 *
 * @author Oliver Kleine
 */
public class CoapObserver extends Observer<URI, CoapWebservice> {

    private Logger log = LoggerFactory.getLogger(CoapObserver.class.getName());
    private CoapClientApplication coapClient;
    
    /** Stores the private key */
    private KeyStore keyStore;

    /**
     * Creates a new instance of {@link eu.spitfire.ssp.backends.generic.Observer}.
     *
     * @param componentFactory the {@link eu.spitfire.ssp.backends.generic.BackendComponentFactory} that provides
     *                         all components to handle instances of
     *                         {@link eu.spitfire.ssp.backends.external.coap.CoapWebservice}.
     */
    protected CoapObserver(CoapBackendComponentFactory componentFactory) {
        super(componentFactory);
        this.coapClient = componentFactory.getCoapClient();
        
        this.keyStore = componentFactory.getKeyStore();
    }

    /**
     * Starting an observation means to send a GET request with the
     * {@link de.uniluebeck.itm.ncoap.message.options.OptionValue.Name#OBSERVE} set to the CoAP Web Service to be
     * observed.
     *
     * @param coapWebservice the {@link eu.spitfire.ssp.backends.external.coap.CoapWebservice} to be observed
     */
    @Override
    public void startObservation(CoapWebservice coapWebservice) {
        try{
            URI webserviceUri = coapWebservice.getIdentifier();
            CoapRequest coapRequest = new CoapRequest(MessageType.Name.CON, MessageCode.Name.GET, webserviceUri);
            //fischer: added new content type and changed the way setAccept is called
            coapRequest.setAccept(ContentFormat.APP_RDF_XML, 
            		              ContentFormat.APP_N3, 
            		              ContentFormat.APP_TURTLE,
            		              ContentFormat.APP_XML);
            coapRequest.setObserve();

            InetAddress remoteAddress = InetAddress.getByName(webserviceUri.getHost());
            int port = webserviceUri.getPort() == -1 ? 5683 : webserviceUri.getPort();
            
            coapClient.sendCoapRequest(coapRequest,
                    new CoapUpdateNotificationProcessor(webserviceUri), new InetSocketAddress(remoteAddress, port)
            );
        }
        catch(Exception ex){
            log.error("Could not start observation of {}!", coapWebservice.getIdentifier(), ex);
        }
    }


    private class CoapUpdateNotificationProcessor implements UpdateNotificationProcessor,
            RetransmissionTimeoutProcessor {


        private URI graphName;


        private CoapUpdateNotificationProcessor(URI graphName) {
            this.graphName = graphName;
        }


        @Override
        public void processRetransmissionTimeout(InetSocketAddress remoteEndpoint, int messageID, Token token) {
            log.error("Request to {} timed out!", graphName);
        }


        @Override
        public boolean continueObservation() {
            return true;
        }


        @Override
        public void processCoapResponse(CoapResponse coapResponse) {
            try{
                Model model = CoapTools.getModelFromCoapResponse(coapResponse, keyStore);
                Date expiry = new Date(System.currentTimeMillis() + coapResponse.getMaxAge() * 1000);

                // fischer: in encrypted sensor data packages there may be an alternative name for the graph
                URI alternativeLocationUri = CoapTools.getAlternativeLocationUri(coapResponse);
                final URI actualGraphName = (alternativeLocationUri == null) ? graphName : alternativeLocationUri;
                
                ExpiringNamedGraph expiringNamedGraph = new ExpiringNamedGraph(actualGraphName, model, expiry);
                ListenableFuture<Void> cacheUpdateResult = updateCache(expiringNamedGraph);

                Futures.addCallback(cacheUpdateResult, new FutureCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        log.debug("Successfully updated graph {}.", actualGraphName);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        log.error("Error while updating graph {}", actualGraphName);
                    }
                });

            }
            catch(Exception ex){
                log.error("Error while processing Update Notification from {}.", graphName, ex);
            }
        }
    }
}
