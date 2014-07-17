package priviot.coap_proxy.communication.data_origin;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.ncoap.application.client.CoapResponseProcessor;
import de.uniluebeck.itm.ncoap.application.client.Token;
import de.uniluebeck.itm.ncoap.communication.observe.client.UpdateNotificationProcessor;
import de.uniluebeck.itm.ncoap.communication.reliability.outgoing.RetransmissionTimeoutProcessor;
import de.uniluebeck.itm.ncoap.communication.reliability.outgoing.TransmissionInformationProcessor;
import de.uniluebeck.itm.ncoap.message.CoapResponse;

public class DataOriginCoapClient implements CoapResponseProcessor, TransmissionInformationProcessor,
    RetransmissionTimeoutProcessor, UpdateNotificationProcessor {

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
    private AtomicInteger transmissionCounter;
    private AtomicBoolean timedOut;
    
    private AtomicInteger responseCounter;

    /**
     * Constructor.
     * @param expectedNumberOfUpdateNotifications
     */
    public DataOriginCoapClient(){
        this.transmissionCounter = new AtomicInteger(0);
        this.timedOut = new AtomicBoolean(false);
        this.responseCounter = new AtomicInteger(0);
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

    /**
    * Increases the reponse counter by 1, i.e. {@link #getResponseCount()} will return a higher value after
    * invocation of this method.
    *
    * @param coapResponse the response message
    */
    @Override
    public void processCoapResponse(CoapResponse coapResponse) {
        int value = responseCounter.incrementAndGet();
        log.info("Received #{}: {}", value, coapResponse);
        
        ChannelBuffer channelBuffer = coapResponse.getContent();
        byte[] readable = new byte[channelBuffer.readableBytes()];
        channelBuffer.toByteBuffer().get(readable, channelBuffer.readerIndex(), channelBuffer.readableBytes());
        
        String content = new String(readable);
        
        log.info("Content:\n" + content);
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
