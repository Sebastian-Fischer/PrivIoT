package priviot.cpp.communication.coapwebserver;

import java.net.URI;

/**
 * A CoapRegistryWebserviceListener receives events from the CoapRegistryWebservice
 */
public interface CoapRegistryWebserviceListener {
    /**
     * Is called whenever a new webservice of a webserver is registered.
     * @param uriWebservice
     */
    public void registeredNewWebservice(URI uriWebservice);
}
