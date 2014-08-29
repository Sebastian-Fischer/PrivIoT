package de.uniluebeck.itm.priviot.coapwebserver.service;

import de.uniluebeck.itm.ncoap.message.CoapResponse;

/**
 * Observer to receive feedback from the CoapClient
 */
public interface CoapClientObserver {
    public void receivedResponse(CoapResponse coapResponse);
}
