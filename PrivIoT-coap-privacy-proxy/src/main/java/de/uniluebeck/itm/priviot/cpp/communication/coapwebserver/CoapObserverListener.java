package de.uniluebeck.itm.priviot.cpp.communication.coapwebserver;

import java.net.URI;

import org.jboss.netty.buffer.ChannelBuffer;

import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.CoapResponse;

/**
 * A CoapObserverListener receives events from the {@link CoapObserver}
 */
public interface CoapObserverListener {
	/**
	 * Is called, whenever the {@link CoapObserver} received the new status of it's CoAP-Webservice.
	 * The content and meta information are given as parameters.
	 * @param uriWebservice   URI of the CoAP-Webservice
	 * @param contentFormat   Content format in CoAP content format constants (see {@link de.uniluebeck.itm.ncoap.message.options.ContentFormat})
	 * @param content         The Content of the new status
	 * @param contentLifetimeSeconds  The lifetime of the new status in seconds
	 */
    public void receivedActualStatus(final URI uriWebservice, long contentFormat, final ChannelBuffer content, long contentLifetimeSeconds);
}
