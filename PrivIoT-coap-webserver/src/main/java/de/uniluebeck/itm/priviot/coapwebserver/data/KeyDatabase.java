package de.uniluebeck.itm.priviot.coapwebserver.data;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves public keys of Smart Service Proxies.
 * Methods of the class are thread save.
 */
public class KeyDatabase {
    
    List<KeyDatabaseEntry> entries = new ArrayList<KeyDatabaseEntry>();
    
    public KeyDatabase() {
        
    }
    
    /**
     * Adds a new Entry.
     * Entry must not be null.
     * Attribute url of entry must not be null.
     * @param entry
     */
    public synchronized void addEntry(KeyDatabaseEntry entry) {
        if (entry == null || entry.getURI() == null) {
            return;
        }
        
        entries.add(entry);
    }
    
    /**
     * Returns the entry with the url equal to given argument, if it exist.
     * @param url
     * @return
     */
    public synchronized KeyDatabaseEntry getEntry(URI url) {
        for (KeyDatabaseEntry entry : entries) {
            if (entry.getURI().getHost().equals(url.getHost())) {
                return entry;
            }
        }
        
        return null;
    }
    
    /**
     * Returns a list containing the URLs of all database entries.
     * @return
     */
    public synchronized List<URI> getAllEntryUrls() {
        List<URI> list = new ArrayList<URI>();
        
        for (KeyDatabaseEntry entry : entries) {
            list.add(entry.getURI());
        }
        
        return list;
    }
}
