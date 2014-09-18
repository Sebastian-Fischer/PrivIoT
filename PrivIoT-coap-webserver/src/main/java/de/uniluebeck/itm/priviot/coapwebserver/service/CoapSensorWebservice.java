package de.uniluebeck.itm.priviot.coapwebserver.service;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import com.google.common.primitives.Longs;
import com.google.common.util.concurrent.SettableFuture;
import com.hp.hpl.jena.rdf.model.Model;

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
import de.uniluebeck.itm.priviot.coapwebserver.data.KeyDatabase;
import de.uniluebeck.itm.priviot.coapwebserver.data.KeyDatabaseEntry;
import de.uniluebeck.itm.priviot.coapwebserver.data.ResourceStatus;
import de.uniluebeck.itm.priviot.utils.EncryptionProcessor;
import de.uniluebeck.itm.priviot.utils.data.EncryptionParameters;
import de.uniluebeck.itm.priviot.utils.data.PrivacyDataPackageMarshaller;
import de.uniluebeck.itm.priviot.utils.data.generated.PrivacyDataPackage;
import de.uniluebeck.itm.priviot.utils.encryption.EncryptionException;

/**
 * Webservice over the COAP protocol.
 * There will be one instance for every sensor.
 * 
 * The resource of the CoapSensorWebservice is the sensor.
 * The resource status is represented by a {@link ResourceStatus} object.
 * 
 * The COAPWebservice is observable for clients.
 */
public class CoapSensorWebservice  extends ObservableWebservice<ResourceStatus> {
	public static long DEFAULT_CONTENT_FORMAT_ENCRYPT = ContentFormat.APP_XML;
	public static long DEFAULT_CONTENT_FORMAT_NOENCRYPT = ContentFormat.APP_RDF_XML;
	
	private Logger log = Logger.getLogger(this.getClass().getName());

    private Map<Long, String> templates;
    
    private long updateIntervalSeconds;
    
    private EncryptionParameters encryptionParameters;
    
    private KeyDatabase keyDatabase;
    
    private boolean doEncrypt;
    
    /**
     * Constructor
     * @param path Path where the Webservice is registered
     * @param updateInterval Interval of resource update in seconds
     */
    public CoapSensorWebservice(String path, int updateInterval,
                                EncryptionParameters encryptionParameters, KeyDatabase keyDatabase) {
    	super(path, null);
    	
    	this.encryptionParameters = encryptionParameters;
    	this.keyDatabase = keyDatabase;
    	
    	updateIntervalSeconds = updateInterval;

        this.templates = new HashMap<>();
        
        doEncrypt = (!encryptionParameters.getSymmetricEncryptionAlgorithm().isEmpty());

        // if encryption activated (default behavior)
        if (doEncrypt) {
        	//add support for xml content
	        addContentFormat(DEFAULT_CONTENT_FORMAT_ENCRYPT, "%s");
        }
        else {
        	//add support for rdf/xml content
	        addContentFormat(ContentFormat.APP_RDF_XML, "%s");
	        
	        //add support for n3 content
	        addContentFormat(ContentFormat.APP_N3, "%s");
	        
	        //add support for turtle content
	        addContentFormat(ContentFormat.APP_TURTLE, "%s");
        }
    }
    
    public void updateResourceStatus(ResourceStatus newResourceStatus) {
    	log.debug("update sensor data for sensor " + getPath() + " with updateInterval " + updateIntervalSeconds);
    	setResourceStatus(newResourceStatus, updateIntervalSeconds);
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
    public void updateEtag(ResourceStatus resourceStatus) {
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
            	log.debug("Reply with METHOD NOT ALLOWED 405");
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
        
        String contentFormatsStr = "";
        for (Long contentFormat : contentFormats) {
        	contentFormatsStr += Long.toString(contentFormat) + ", ";
        }
        log.debug("received get with accepted content formats: " + contentFormatsStr);

        long defaultContentFormat;
        if (doEncrypt) {
    		defaultContentFormat = DEFAULT_CONTENT_FORMAT_ENCRYPT;
    	}
    	else {
    		defaultContentFormat = DEFAULT_CONTENT_FORMAT_NOENCRYPT;
    	}
        
        //If accept option is not set in the request, use the default
        if(contentFormats.isEmpty()) {
        	contentFormats.add(defaultContentFormat);
        }            

        //Generate the payload of the response (depends on the accepted content formats, resp. the default
        WrappedResourceStatus resourceStatus = null;
        Iterator<Long> iterator = contentFormats.iterator();
        long contentFormat = defaultContentFormat;

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
        	log.debug("Reply with CONTENT 205");
            coapResponse = new CoapResponse(coapRequest.getMessageTypeName(), MessageCode.Name.CONTENT_205);
            coapResponse.setContent(resourceStatus.getContent(), contentFormat);
            
            coapResponse.setEtag(resourceStatus.getEtag());
            log.info("max age: " + resourceStatus.getMaxAge());
            coapResponse.setMaxAge(resourceStatus.getMaxAge());

            if(coapRequest.isObserveSet())
                coapResponse.setObserveOption(0);
        }

        //if no payload could be generated, i.e. none of the accepted content formats (according to the
        //requests accept option(s)) is offered by the Webservice then set the code of the response to
        //400 BAD REQUEST and set a payload with a proper explanation
        else{
        	log.debug("Ressource status couldn't be serialized with accepted content formats");
        	log.debug("Reply with NOT ACCEPTABLE 406");
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
        log.debug("Try to create payload (content format: " + contentFormat + ")");

        if (getResourceStatus() == null) {
        	log.debug("getSerializedResourceStatus called while ressource status is null");
            return new byte[0];
        }
        
        String ressourceStatusString = "";
        
        if (doEncrypt) {
        	ressourceStatusString = getSerializedResourceStatusEncrypt(contentFormat);
        }
        else {
        	ressourceStatusString = getSerializedResourceStatusNoEncrypt(contentFormat);
        }
        
        if (ressourceStatusString == null || ressourceStatusString.isEmpty()) {
        	return null;
        }

        String template = templates.get(contentFormat);

        if(template == null) {
            return null;
        }
        else {
        	byte[] res = String.format(template, ressourceStatusString).getBytes(CoapMessage.CHARSET);
        	log.debug("serialized ressource status: " + new String(res));
        	return res;
        }
            
    }
    
    private String getSerializedResourceStatusEncrypt(long contentFormat) {
    	
        if (contentFormat == ContentFormat.APP_XML) {
            
        	String sensorPseudonymUri = getResourceStatus().getSensorUri();
            Model rdfModel = getResourceStatus().getRdfModel();
            
            // content format of the encrypted content
            String language = "N3";
            long innerContentFormat = ContentFormat.APP_N3;
            //String language = "RDF/XML";
            //long innerContentFormat = ContentFormat.APP_RDF_XML;
            //String language = "TURTLE";
            //long innerContentFormat = ContentFormat.APP_TURTLE;
            
            StringWriter stringWriter = new StringWriter();
            rdfModel.write(stringWriter, language);
            String rdfModelStr = stringWriter.getBuffer().toString();
            
            //TODO: get url of the recipient to get the right public key, but how??
            //      This will be a problem, if the CoAP-Webserver wants to communicate with more than one client
            //      The superclass ObservableWebservice does not know it's observers
            List<URI> uriList = keyDatabase.getAllEntryUrls();
            if (uriList.size() == 0) {
                log.error("No Entry in KeyDatabase. Without a public key of the recipient no encrypted data package can be created.");
                return null;
            }
            if (uriList.size() != 1) {
                log.error("More than one Entry in KeyDatabase");
                return null;
            }
            URI uriRecipient = uriList.get(0);
            
            KeyDatabaseEntry keyDatabaseEntry = keyDatabase.getEntry(uriRecipient);
            if (keyDatabaseEntry == null) {
                log.error("No public key knwon for recipient '" + uriRecipient.getHost() + "'");
                return null;
            }
            PublicKey publicKeyRecipient = keyDatabaseEntry.getPublicKey();
            
            // encrypt content and build data package
            PrivacyDataPackage privacyDataPackage;
            try {
                privacyDataPackage = 
                        EncryptionProcessor.createPrivacyDataPackage(rdfModelStr,
	                                                                 sensorPseudonymUri,
	                                                                 innerContentFormat,
	                                                                 encryptionParameters,
	                                                                 publicKeyRecipient);
            } catch (EncryptionException e) {
                log.error(e.getMessage());
                return null;
            }
            
            // serialize privacyDataPackage
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            try {
               PrivacyDataPackageMarshaller.marshal(privacyDataPackage, outStream);
            }
            catch (JAXBException | XMLStreamException e) {
                log.error("Failure during serialization of PrivacyDataPackage", e);
                return null;
            }
            
            return outStream.toString();
        }
        else {
            // contentFormat not supported
            return null;
        }
    }
    
    private String getSerializedResourceStatusNoEncrypt(long contentFormat) {
    	if (contentFormat == ContentFormat.APP_RDF_XML ||
    		contentFormat == ContentFormat.APP_N3 ||
    	    contentFormat == ContentFormat.APP_TURTLE) {
    		
    		// content format of the content
            String language;
            if (contentFormat == ContentFormat.APP_RDF_XML) {
            	language = "RDF/XML";
            }
            else if (contentFormat == ContentFormat.APP_N3) {
            	language = "N3";
            }
            else {
            	language = "TURTLE";
            }
            
            Model rdfModel = getResourceStatus().getRdfModel();
            
            StringWriter stringWriter = new StringWriter();
            rdfModel.write(stringWriter, language);
            return stringWriter.getBuffer().toString();
    	}
    	else {
    		return null;
    	}
    }
}
