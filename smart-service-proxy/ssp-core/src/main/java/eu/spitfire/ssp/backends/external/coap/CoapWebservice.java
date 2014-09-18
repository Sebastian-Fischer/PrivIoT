package eu.spitfire.ssp.backends.external.coap;

import eu.spitfire.ssp.backends.generic.DataOrigin;

import java.net.URI;

/**
 * Wrapper class to make external CoAP Web Services that act as data origins for the SSPs cache.
 *
 * @author Oliver Kleine
 */
public class CoapWebservice extends DataOrigin<URI> {

    /**
     * Creates a new instance of {@link eu.spitfire.ssp.backends.generic.DataOrigin}
     *
     * @param identifier the identifier for this {@link eu.spitfire.ssp.backends.generic.DataOrigin}
     */
    public CoapWebservice(URI identifier) {
        super(identifier, identifier);
    }

    /**
     * Returns <code>true</code>
     * @return <code>true</code>
     */
    @Override
    public boolean isObservable() {
        return true;
    }

    @Override
    public int hashCode() {
        return this.getIdentifier().hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if(object == null | !(object instanceof CoapWebservice)){
            return false;
        }

        CoapWebservice other = (CoapWebservice) object;
        return this.getIdentifier().equals(other.getIdentifier());
    }
}
