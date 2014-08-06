package priviot.data_origin.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Saves public keys of Smart Service Proxies.
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
    public void addEntry(KeyDatabaseEntry entry) {
        if (entry == null || entry.getURL() == null) {
            return;
        }
        
        entries.add(entry);
    }
    
    /**
     * Returns the entry with the url equal to given argument, if it exist.
     * @param url
     * @return
     */
    public KeyDatabaseEntry getEntry(String url) {
        for (KeyDatabaseEntry entry : entries) {
            if (entry.getURL().equals(url)) {
                return entry;
            }
        }
        
        return null;
    }
    
    /**
     * Returns a list containing the URLs of all database entries.
     * @return
     */
    public List<String> getAllEntryUrls() {
        List<String> list = new ArrayList<String>();
        
        for (KeyDatabaseEntry entry : entries) {
            list.add(entry.getURL());
        }
        
        return list;
    }
}
