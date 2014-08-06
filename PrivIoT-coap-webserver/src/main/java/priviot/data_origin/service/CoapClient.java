package priviot.data_origin.service;

import de.uniluebeck.itm.ncoap.application.client.CoapResponseProcessor;
import de.uniluebeck.itm.ncoap.application.client.Token;
import de.uniluebeck.itm.ncoap.communication.reliability.outgoing.RetransmissionTimeoutProcessor;
import de.uniluebeck.itm.ncoap.communication.reliability.outgoing.TransmissionInformationProcessor;
import de.uniluebeck.itm.ncoap.message.CoapResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Client to communicate with the CoAP Proxy over the CoAP protocol.
 * 
 * The CoapRegisterClient has two tasks:
 * 1. Register the CoAP-Webserver at a CoAP Privacy Proxy or a Smart Service Proxy.
 *    As reaction the proxy will register itself as observer at the sensor webservices.
 * 2. Get the X.509 certificate of a Smart Service Proxy to get the public key.
 */
public class CoapClient extends Observable implements CoapResponseProcessor, TransmissionInformationProcessor,
        RetransmissionTimeoutProcessor {
    
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private AtomicBoolean responseReceived;
    private AtomicInteger transmissionCounter;
    private AtomicBoolean timedOut;


    public CoapClient(){
        this.responseReceived = new AtomicBoolean(false);
        this.transmissionCounter = new AtomicInteger(0);
        this.timedOut = new AtomicBoolean(false);
    }

    /**
     * Increases the reponse counter by 1, i.e. {@link #getResponseCount()} will return a higher value after
     * invocation of this method.
     *
     * @param coapResponse the response message
     */
    @Override
    public void processCoapResponse(CoapResponse coapResponse) {
        responseReceived.set(true);
        log.info("Received: {}", coapResponse);
        
        setChanged();
        notifyObservers(coapResponse);
    }

    /**
     * Returns the number of responses received
     * @return the number of responses received
     */
    public int getResponseCount(){
        return this.responseReceived.get() ? 1 : 0;
    }


    @Override
    public void messageTransmitted(InetSocketAddress remoteEndpint, int messageID, Token token,
                                   boolean retransmission) {
        int value = transmissionCounter.incrementAndGet();

        if(retransmission){
            log.info("Transmission #{} for message with ID {} to {} (Token: {})",
                    new Object[]{value, messageID, remoteEndpint, token});
        }
        else{
            log.info("Message with ID {} written to {} (Token: {})",
                    new Object[]{messageID, remoteEndpint, token});
        }
    }


    @Override
    public void processRetransmissionTimeout(InetSocketAddress remoteEndpoint, int messageID, Token token) {
        log.info("Internal timeout for message with ID {} to {} (Token: {})",
                new Object[]{messageID, remoteEndpoint, token});

        timedOut.set(true);
    }


    public boolean isTimedOut(){
        return timedOut.get();
    }
}
