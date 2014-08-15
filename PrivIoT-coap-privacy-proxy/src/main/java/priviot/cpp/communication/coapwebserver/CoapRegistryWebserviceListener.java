package priviot.cpp.communication.coapwebserver;

import java.net.URI;

/**
 * A CoapRegistryWebserviceListener receives events from the CoapRegistryWebservice
 */
public interface CoapRegistryWebserviceListener {
    /**
     * Is called whenever a new webserver is registered.
     * @param uriWebserver The URI of the webserver.
     * @param uriSSP       The URI of the Smart Service Proxy to forward sensor data to.
     */
    public void registeredNewWebserver(URI uriWebserver, URI uriSSP);
    
    /**
     * Is called whenever a new webservice of a webserver is registered.
     * @param uriWebservice
     */
    public void registeredNewWebservice(URI uriWebservice);
}
