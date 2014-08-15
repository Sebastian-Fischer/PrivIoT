package priviot.cpp.communication.coapwebserver;

import java.net.URI;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * A CoapObserverListener receives events from the CoapObserver
 */
public interface CoapObserverListener {
    public void receivedActualStatus(URI uriWebservice, long contentFormat, ChannelBuffer content);
}
