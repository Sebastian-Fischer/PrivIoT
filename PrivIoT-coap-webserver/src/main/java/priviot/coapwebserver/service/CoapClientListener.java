package priviot.coapwebserver.service;

import java.net.URI;

import de.uniluebeck.itm.ncoap.message.CoapResponse;

/**
 * A CoapClientListener can be added to the CoapClient to received events.
 */
public interface CoapClientListener {
    /** 
     * Is called by the CoapClient for every response it receives.
     * @param enpoint  The recipient of the message the response belongs to.
     *                 Can be null, if recipient has not been set in CoapClient.
     * @param response The response.
     */
    public void receivedResponse(final URI enpoint, final CoapResponse response);
}
