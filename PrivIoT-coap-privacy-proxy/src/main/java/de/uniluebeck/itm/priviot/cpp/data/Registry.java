package de.uniluebeck.itm.priviot.cpp.data;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Saves the registrations of CoAP-Webservers with all the needed information.
 */
public class Registry {
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
    private Set<RegistryEntry> entries = new HashSet<RegistryEntry>();
    
    public Registry() {
        
    }
    
    /**
     * Adds an entry to the registry.
     * @param entry
     */
    public synchronized void addEntry(RegistryEntry entry) {
        if (entry != null) {
            entries.add(entry);
            
            log.info("Added webserver: " + entry.getWebserver().getHost());
        }
    }
    
    /**
     * Returns the entry of a given CoAP-Webserver.
     * @param urlWebserver
     * @return The entry or null if none matches.
     */
    public synchronized RegistryEntry getEntry(URI uriWebserver) {
        for (RegistryEntry entry : entries) {
            if (entry.getWebserver().equals(uriWebserver)) {
                return entry;
            }
        }
        
        return null;
    }
    
    /**
     * Returns the entry of a CoAP-Webserver, that contains the
     * given Webservice path.
     * @param urlWebservice 
     * @return The entry or null if none matches.
     */
    public synchronized RegistryEntry getEntryByWebservice(URI uriWebservice) {
        for (RegistryEntry entry : entries) {
            if (entry.getWebserver().getHost().equals(uriWebservice.getHost()) &&
                entry.getWebserver().getPort() == uriWebservice.getPort()) {
                
                if (entry.containsWebservice(uriWebservice.getPath())) {
                    return entry;
                }
            }
            
        }
        
        return null;
    }
}
