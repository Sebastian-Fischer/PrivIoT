package priviot.data_origin.service;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import priviot.utils.data.transfer.DataPackage;

import com.google.common.primitives.Longs;
import com.google.common.util.concurrent.SettableFuture;

import de.uniluebeck.itm.ncoap.application.client.Token;
import de.uniluebeck.itm.ncoap.application.server.webservice.ObservableWebservice;
import de.uniluebeck.itm.ncoap.application.server.webservice.WrappedResourceStatus;
import de.uniluebeck.itm.ncoap.application.server.webservice.linkformat.LinkAttribute;
import de.uniluebeck.itm.ncoap.application.server.webservice.linkformat.LongLinkAttribute;
import de.uniluebeck.itm.ncoap.message.CoapMessage;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.CoapResponse;
import de.uniluebeck.itm.ncoap.message.MessageCode;
import de.uniluebeck.itm.ncoap.message.MessageType;
import de.uniluebeck.itm.ncoap.message.options.ContentFormat;

/**
 * Webservice over the COAP protocol.
 * Is used by COAPService. 
 * 
 * The COAPWebservice is observable for clients.
 */
public class CoapWebservice  extends ObservableWebservice<DataPackage> {
	public static long DEFAULT_CONTENT_FORMAT = ContentFormat.TEXT_PLAIN_UTF8;
	
	private Logger log = Logger.getLogger(CoapWebservice.class.getName());
	private ScheduledFuture periodicUpdateFuture;

    private Map<Long, String> templates;
    
    private long updateIntervalMillis;
    
    private volatile DataPackage dataPackage;
    
    /**
     * Constructor
     * @param path Path where the Webservice is registered
     * @param updateInterval Interval of resource update in seconds
     */
    public CoapWebservice(String path, int updateInterval) {
    	super(path, null);
    	
    	updateIntervalMillis = updateInterval * 1000;

        this.templates = new HashMap<>();

        //add support for utf-8 plain-text content
        addContentFormat(
                ContentFormat.TEXT_PLAIN_UTF8,
                "%s"
        );

        //add support for xml content
        addContentFormat(
                ContentFormat.APP_XML,
                "%s"
        );
        
        //add support for rdf/xml content
	    addContentFormat(
	            ContentFormat.APP_RDF_XML,
	            "%s"
	    );
    }
    
    public void updateRdfSensorData(DataPackage dataPackage) {
    	log.info("update sensor data: " + dataPackage);
    	this.dataPackage = dataPackage;
    }
    
    private void addContentFormat(long contentFormat, String template){
        this.templates.put(contentFormat, template);
        this.setLinkAttribute(new LongLinkAttribute(LinkAttribute.CONTENT_TYPE, contentFormat));
    }

    @Override
    public void setScheduledExecutorService(ScheduledExecutorService executorService){
        super.setScheduledExecutorService(executorService);
        schedulePeriodicResourceUpdate();
    }

    @Override
    public MessageType.Name getMessageTypeForUpdateNotification(InetSocketAddress remoteEndpoint, Token token) {
        return MessageType.Name.CON;
    }


    @Override
    public byte[] getEtag(long contentFormat) {
        if (getResourceStatus() == null) {
            return Longs.toByteArray(contentFormat << 56);
        }
        
        return Longs.toByteArray(getResourceStatus().toString().hashCode() | (contentFormat << 56));
    }


    @Override
    public void updateEtag(DataPackage resourceStatus) {
        //nothing to do here...
    }


    private void schedulePeriodicResourceUpdate(){
        this.periodicUpdateFuture = getScheduledExecutorService().scheduleAtFixedRate(new Runnable(){

            @Override
            public void run() {
                try{
                    setResourceStatus(dataPackage, updateIntervalMillis / 1000);
                    log.info("New status of resource " + getPath() + ": " + getResourceStatus());
                }
                catch(Exception ex){
                    log.error("Exception while updating status...", ex);
                }
            }
        },0, updateIntervalMillis, TimeUnit.MILLISECONDS);
    }


    @Override
    public void processCoapRequest(SettableFuture<CoapResponse> responseFuture, CoapRequest coapRequest,
                                   InetSocketAddress remoteAddress) {
        try{
            if(coapRequest.getMessageCodeName() == MessageCode.Name.GET){
                processGet(responseFuture, coapRequest);
            }

            else {
                CoapResponse coapResponse = new CoapResponse(coapRequest.getMessageTypeName(),
                        MessageCode.Name.METHOD_NOT_ALLOWED_405);
                String message = "Service does not allow " + coapRequest.getMessageCodeName() + " requests.";
                coapResponse.setContent(message.getBytes(CoapMessage.CHARSET), ContentFormat.TEXT_PLAIN_UTF8);
                responseFuture.set(coapResponse);
            }
        }
        catch(Exception ex){
            responseFuture.setException(ex);
        }
    }


    private void processGet(SettableFuture<CoapResponse> responseFuture, CoapRequest coapRequest)
            throws Exception {

        //Retrieve the accepted content formats from the request
        Set<Long> contentFormats = coapRequest.getAcceptedContentFormats();

        //If accept option is not set in the request, use the default (TEXT_PLAIN_UTF8)
        if(contentFormats.isEmpty())
            contentFormats.add(DEFAULT_CONTENT_FORMAT);

        //Generate the payload of the response (depends on the accepted content formats, resp. the default
        WrappedResourceStatus resourceStatus = null;
        Iterator<Long> iterator = contentFormats.iterator();
        long contentFormat = DEFAULT_CONTENT_FORMAT;

        while(resourceStatus == null && iterator.hasNext()){
            contentFormat = iterator.next();
            resourceStatus = getWrappedResourceStatus(contentFormat);
        }

        //generate the CoAP response
        CoapResponse coapResponse;

        //if the payload could be generated, i.e. at least one of the accepted content formats (according to the
        //requests accept option(s)) is offered by the Webservice then set payload and content format option
        //accordingly
        if(resourceStatus != null){
            coapResponse = new CoapResponse(coapRequest.getMessageTypeName(), MessageCode.Name.CONTENT_205);
            coapResponse.setContent(resourceStatus.getContent(), contentFormat);

            coapResponse.setEtag(resourceStatus.getEtag());
            coapResponse.setMaxAge(resourceStatus.getMaxAge());

            if(coapRequest.isObserveSet())
                coapResponse.setObserveOption(0);
        }

        //if no payload could be generated, i.e. none of the accepted content formats (according to the
        //requests accept option(s)) is offered by the Webservice then set the code of the response to
        //400 BAD REQUEST and set a payload with a proper explanation
        else{
            coapResponse = new CoapResponse(coapRequest.getMessageTypeName(), MessageCode.Name.NOT_ACCEPTABLE_406);

            StringBuilder payload = new StringBuilder();
            payload.append("Requested content format(s) (from requests ACCEPT option) not available: ");
            for(long acceptedContentFormat : coapRequest.getAcceptedContentFormats())
                payload.append("[").append(acceptedContentFormat).append("]");

            coapResponse.setContent(payload.toString()
                    .getBytes(CoapMessage.CHARSET), ContentFormat.TEXT_PLAIN_UTF8);
        }

        //Set the response future with the previously generated CoAP response
        responseFuture.set(coapResponse);

    }


    @Override
    public void shutdown() {
        log.info("Shutdown service " + getPath() + ".");
        boolean futureCanceled = this.periodicUpdateFuture.cancel(true);
        log.info("Future canceled: " + futureCanceled);
    }


    @Override
    public byte[] getSerializedResourceStatus(long contentFormat) {
        log.debug("Try to create payload (content format: " + contentFormat + ")");

        if (getResourceStatus() == null) {
            return new byte[0];
        }
        
        String ressourceStatusString = "";
        if (contentFormat == ContentFormat.TEXT_PLAIN_UTF8) {
            ressourceStatusString = getResourceStatus().toUTF8();
        }
        else if (contentFormat == ContentFormat.APP_XML || contentFormat == ContentFormat.APP_RDF_XML) {
            ressourceStatusString = getResourceStatus().toXML();
        }
        else {
            // contentFormat not supported
            return null;
        }
        
        log.info("Ressource Status: " + ressourceStatusString);

        String template = templates.get(contentFormat);

        if(template == null)
            return null;

        else
            return String.format(template, ressourceStatusString).getBytes(CoapMessage.CHARSET);
    }
}
