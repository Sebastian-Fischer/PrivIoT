package de.uniluebeck.itm.priviot.cpp.data;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an entry in the {@link Registry}.
 * 
 * Contains the information about one CoAP-Webserver.
 */
public class RegistryEntry {
    /** The URL of the Webserver */
    private URI webserver;
    
    /** 
     * The URL of the Smart Service Proxy,
     * where the sensor data of the Webservices
     * should be forwarded to.
     */
    private URI ssp;
    
    /**
     * The url of every CoAP-Webservice, 
     * that is located at the CoAP-Webserver.
     */
    private Set<URI> webservices = new HashSet<URI>();
    
    
    public RegistryEntry(URI webserver, URI ssp) {
        this.webserver = webserver;
        this.ssp = ssp; 
    }
    
    public URI getWebserver() {
        return webserver;
    }
    
    public void setWebserver(URI webserver) {
        this.webserver = webserver;
    }
    
    public URI getSSP() {
        return ssp;
    }
    
    public void setSSP(URI ssp) {
        this.ssp = ssp;
    }

    public Set<URI> getWebservices() {
        return webservices;
    }
    
    public void addWebservice(URI webservice) {
        if (webservice != null) {
            webservices.add(webservice);
        }
    }
    
    /**
     * Returns true, if the given webservice path is
     * a relative path of a registered webservice
     * of this Webserver.
     * @param webservicePath
     * @return
     */
    public boolean containsWebservice(String webservicePath) {
        for (URI uri : webservices) {
            if (uri.getPath().equals(webservicePath)) {
                return true;
            }
        }
        return false;
    }
}
