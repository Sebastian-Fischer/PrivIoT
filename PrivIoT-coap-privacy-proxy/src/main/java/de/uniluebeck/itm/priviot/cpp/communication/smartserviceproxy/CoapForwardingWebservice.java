package de.uniluebeck.itm.priviot.cpp.communication.smartserviceproxy;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

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
import de.uniluebeck.itm.priviot.utils.data.PrivacyDataPackageMarshaller;
import de.uniluebeck.itm.priviot.utils.data.generated.PrivacyDataPackage;

/**
 * One CoAP-Webservice for every Original CoAP-Webservice that sends the data.
 * 
 * The CoapForwardingWebservice forwards the received {@link PrivacyDataPackage} of the CoAP-Webservice to the SSP.
 * 
 * When a SSP asks for the resource /.well-known/core, 
 * he finds the CoapForwardingWebservice as observable webservice.
 */
public class CoapForwardingWebservice  extends ObservableWebservice<PrivacyDataPackage> {
	private Logger log = Logger.getLogger(CoapForwardingWebservice.class.getName());

    private Map<Long, String> templates;
    
    private boolean isRegisteredAtSSP = false;
    
    /**
     * Constructor
     * @param path Path where the Webservice is registered
     * @param updateInterval Interval of resource update in seconds
     */
    public CoapForwardingWebservice(String path) {
    	super(path, null);

        this.templates = new HashMap<>();

        //add support for xml content
        addContentFormat(ContentFormat.APP_XML, "%s");
    }
    
    public void updateRdfSensorData(PrivacyDataPackage dataPackage, long contentLifetimeSeconds) {
    	log.debug("New sensor data available to forward");
    	setResourceStatus(dataPackage, contentLifetimeSeconds);
    }
    
    private void addContentFormat(long contentFormat, String template){
        this.templates.put(contentFormat, template);
        this.setLinkAttribute(new LongLinkAttribute(LinkAttribute.CONTENT_TYPE, contentFormat));
    }

    @Override
    public void setScheduledExecutorService(ScheduledExecutorService executorService){
        super.setScheduledExecutorService(executorService);
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
    public void updateEtag(PrivacyDataPackage resourceStatus) {
        //nothing to do here...
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

        //Generate the payload of the response (depends on the accepted content formats, resp. the default
        WrappedResourceStatus resourceStatus = null;
        Iterator<Long> iterator = contentFormats.iterator();
        long contentFormat = 0;

        while(resourceStatus == null && iterator.hasNext()){
            contentFormat = iterator.next();
            if (contentFormat == ContentFormat.APP_XML) {
            	resourceStatus = getWrappedResourceStatus(contentFormat);
            }
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
    }


    @Override
    public byte[] getSerializedResourceStatus(long contentFormat) {
        log.debug("Try to create payload for " + getPath() + " (content format: " + contentFormat + ")");

        if (getResourceStatus() == null) {
        	log.debug("resource status of " + getPath() + " is empty. return null.");
            return null;
        }
        
        String ressourceStatusString = "";
        if (contentFormat == ContentFormat.APP_XML) {
            
        	// serialize privacyDataPackage
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            try {
               PrivacyDataPackageMarshaller.marshal(getResourceStatus(), outStream);
            }
            catch (JAXBException | XMLStreamException e) {
                log.error("Failure during serialization of PrivacyDataPackage", e);
                return null;
            }
            
            ressourceStatusString = outStream.toString();
        }
        else {
            // contentFormat not supported
        	log.debug("Content format " + contentFormat +  " for " + getPath() + " not supported. return null");
            return null;
        }
        
        log.debug("Ressource Status: " + ressourceStatusString);

        String template = templates.get(contentFormat);

        if(template == null)
            return null;

        else
            return String.format(template, ressourceStatusString).getBytes(CoapMessage.CHARSET);
    }

	public boolean isRegisteredAtSSP() {
		return isRegisteredAtSSP;
	}

	public void setRegisteredAtSSP(boolean isRegisteredAtSSP) {
		this.isRegisteredAtSSP = isRegisteredAtSSP;
	}
}
