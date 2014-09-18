package eu.spitfire.ssp.backends.external.coap;

import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.application.server.CoapServerApplication;
import eu.spitfire.ssp.backends.external.coap.registry.CoapRegistry;
import eu.spitfire.ssp.backends.generic.BackendComponentFactory;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.channel.local.LocalServerChannel;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The {@link eu.spitfire.ssp.backends.external.coap.CoapBackendComponentFactory} provides all components that are
 * either mandatory, i.e. due to inheritance from  {@link eu.spitfire.ssp.backends.generic.BackendComponentFactory} or
 * shared by multiple components, i.e. the {@link de.uniluebeck.itm.ncoap.application.client.CoapClientApplication}
 * and the {@link de.uniluebeck.itm.ncoap.application.server.CoapServerApplication}.
 *
 * @author Oliver Kleine
 */
public class CoapBackendComponentFactory extends BackendComponentFactory<URI, CoapWebservice>{

    private CoapClientApplication coapClient;
    private CoapServerApplication coapServer;
    private CoapRegistry registry;
    private CoapAccessor accessor;
    private CoapObserver observer;
    private CoapCertificateWebservice certificateWebservice;
    private KeyStore keyStore;


    public CoapBackendComponentFactory(Configuration config, LocalServerChannel localChannel,
                                          ScheduledExecutorService internalTasksExecutor, ExecutorService ioExecutor)
            throws Exception {

        super("coap", config, localChannel, internalTasksExecutor, ioExecutor);
        
        // fischer: added key store
        this.keyStore = new KeyStore(config);

        this.coapClient = new CoapClientApplication("SSP CoAP Client");
        //fischer: Test code: changed port, so that SSP can be on same host as CoAP Webserver
        this.coapServer = new CoapServerApplication(CoapServerApplication.DEFAULT_COAP_SERVER_PORT+2);

        this.registry = new CoapRegistry(this);
        this.accessor = new CoapAccessor(this);
        this.observer = new CoapObserver(this);
        
        // fischer: added certificateWebservice. Is this the right place?
        this.certificateWebservice = new CoapCertificateWebservice(this);
    }


    @Override
    public void initialize() throws Exception {
    	// fischer: added certificateWebservice. Is this the right place?
    	coapServer.registerService(certificateWebservice);
    }

    /**
     * Returns the {@link de.uniluebeck.itm.ncoap.application.client.CoapClientApplication} to communicate with
     * external CoAP servers.
     *
     * @return the {@link de.uniluebeck.itm.ncoap.application.client.CoapClientApplication} to communicate with
     * external CoAP servers.
     */
    public CoapClientApplication getCoapClient(){
        return this.coapClient;
    }

    /**
     * Returns the {@link de.uniluebeck.itm.ncoap.application.server.CoapServerApplication} to provide services such as
     * the registry.
     *
     * @return the {@link de.uniluebeck.itm.ncoap.application.server.CoapServerApplication} to provide services such as
     * the registry.
     */
    public CoapServerApplication getCoapServer(){
        return this.coapServer;
    }


    @Override
    public CoapObserver getObserver(CoapWebservice externalWebservice) {
        return this.observer;
    }

    @Override
    public CoapAccessor getAccessor(CoapWebservice externalWebservice) {
        return this.accessor;
    }

    @Override
    public CoapRegistry createRegistry(Configuration config) throws Exception {
        return this.registry;
    }

    @Override
    public CoapRegistry getRegistry() {
        return this.registry;
    }
    
    public KeyStore getKeyStore() {
    	return this.keyStore;
    }

    @Override
    public void shutdown() {
        this.coapServer.shutdown();
    }
}
