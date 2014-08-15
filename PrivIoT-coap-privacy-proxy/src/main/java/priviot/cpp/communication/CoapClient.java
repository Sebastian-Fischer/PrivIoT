package priviot.cpp.communication;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;

import de.uniluebeck.itm.ncoap.application.client.CoapResponseProcessor;
import de.uniluebeck.itm.ncoap.application.client.Token;
import de.uniluebeck.itm.ncoap.communication.observe.client.UpdateNotificationProcessor;
import de.uniluebeck.itm.ncoap.communication.reliability.outgoing.RetransmissionTimeoutProcessor;
import de.uniluebeck.itm.ncoap.communication.reliability.outgoing.TransmissionInformationProcessor;
import de.uniluebeck.itm.ncoap.message.CoapResponse;

/**
 * Can be used to process a coap Response and control retransmission of a message.
 * 
 * Because some of the inherited methods don't get the recipient as parameter,
 * an own CoapClient instance is needed for every transmission.
 * The URI of the recipient can be set in constructor if needed.
 * 
 * EventListener can be added in two ways:
 * - Add a SettableFuture<CoapResponse>
 * - Add a CoapClientListener
 * The first is thread save and can handle multiple listeners. The callback method can be called only once,
 * although the recipient answers multiple times (e.g. observation).
 * The second can save the recipient of the message to get it as parameter in the event 
 * receivedCoapResponse(URI, CoapResonse). The event is called for every response.
 */
public class CoapClient implements CoapResponseProcessor, TransmissionInformationProcessor,
    RetransmissionTimeoutProcessor, UpdateNotificationProcessor {

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
    private SettableFuture<CoapResponse> responseFuture;
    
    private AtomicInteger transmissionCounter;
    private AtomicBoolean timedOut;
    
    private AtomicInteger responseCounter;
    
    private URI recipient; 
    
    private CoapClientListener listener;

    /**
     * Constructor.
     */
    public CoapClient(){
        this.transmissionCounter = new AtomicInteger(0);
        this.timedOut = new AtomicBoolean(false);
        this.responseCounter = new AtomicInteger(0);
        this.responseFuture = SettableFuture.create();
    }    
    
    /**
     * Constructor with recipient URI support.
     */
    public CoapClient(URI recipient){
        this.transmissionCounter = new AtomicInteger(0);
        this.timedOut = new AtomicBoolean(false);
        this.responseCounter = new AtomicInteger(0);
        this.responseFuture = SettableFuture.create();
        this.recipient = recipient;
    }
    
    public void addListener(CoapClientListener listener) {
        this.listener = listener;
    }
    
    public SettableFuture<CoapResponse> getResponseFuture(){
        return this.responseFuture;
    }
    
    @Override
    public void messageTransmitted(InetSocketAddress remoteEndpint, int messageID, Token token,
                                   boolean retransmission) {
        int value = transmissionCounter.incrementAndGet();
    
        if(retransmission){
            log.debug("Transmission #{} for message with ID {} to {} (Token: {})",
                      new Object[]{value, messageID, remoteEndpint, token});
        }
        else{
            log.debug("Message with ID {} written to {} (Token: {})",
                      new Object[]{messageID, remoteEndpint, token});
        }
    }
    
    
    @Override
    public void processRetransmissionTimeout(InetSocketAddress remoteEndpoint, int messageID, Token token) {
        log.debug("Internal timeout for message with ID {} to {} (Token: {})",
                  new Object[]{messageID, remoteEndpoint, token});
    
        timedOut.set(true);
    }
    
    
    public boolean isTimedOut(){
        return timedOut.get();
    }

    /**
    * Increases the reponse counter by 1, i.e. {@link #getResponseCount()} will return a higher value after
    * invocation of this method.
    *
    * @param coapResponse the response message
    */
    @Override
    public void processCoapResponse(CoapResponse coapResponse) {
        int value = responseCounter.incrementAndGet();
        log.debug("Received #{}: {}", value, coapResponse);
        
        if (listener != null) {
            listener.receivedResponse(recipient, coapResponse);
        }
        
        responseFuture.set(coapResponse);
    }
    
    /**
     * Returns the number of responses received
     * @return the number of responses received
     */
    public int getResponseCount(){
        return responseCounter.intValue();
    }
    
    @Override
    public boolean continueObservation() {
        return true;
    }
}
