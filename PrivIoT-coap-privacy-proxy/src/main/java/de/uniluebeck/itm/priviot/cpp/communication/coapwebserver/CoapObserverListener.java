package de.uniluebeck.itm.priviot.cpp.communication.coapwebserver;

import java.net.URI;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * A CoapObserverListener receives events from the CoapObserver
 */
public interface CoapObserverListener {
    public void receivedActualStatus(final URI uriWebservice, long contentFormat, final ChannelBuffer content);
}
