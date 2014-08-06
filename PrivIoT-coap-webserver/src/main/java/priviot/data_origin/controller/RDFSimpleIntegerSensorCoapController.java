package priviot.data_origin.controller;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;

import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.application.server.CoapServerApplication;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.MessageCode;
import de.uniluebeck.itm.ncoap.message.MessageType;
import priviot.data_origin.data.SensorData;
import priviot.data_origin.data.SimpleIntegerSensorData;
import priviot.data_origin.sensor.SimpleIntegerSensor;
import priviot.data_origin.service.CoapClient;
import priviot.data_origin.service.CoapSensorWebservice;
import priviot.utils.data.transfer.DataPackage;
import priviot.utils.data.transfer.RdfModelDataPackage;

/**
 * A Controller, that connects the SimpleIntegerSensor with the CoapSensorWebservice.
 * The data of the sensor is transformed into a apache jena rdf model before passed to the CoapSensorWebservice.
 */
public class RDFSimpleIntegerSensorCoapController extends Controller {
    
    /** Port the coap application listens to */
    private static final int OWN_PORT = 5684;
    /** URI of the host */
	private static final String HOST_URI = "coap://localhost";
	/** URI of the sensor */
	private static final String SENSOR_URI = "/sensor1";
	/** frequency in which new values are published by the sensor in seconds */
	private static final int UPDATE_FREQUENCY = 3;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	/** Web service over the COAP protocol */
	CoapSensorWebservice coapWebservice;
	
	/** Listens to a local port. Web services can be registered here. */
	CoapServerApplication coapServerApplication;
	
	CoapClientApplication coapClientApplication;
	
	@Override
	public void initialize() {
		
		log.info("initialize SimpleIntegerSensor " + SENSOR_URI);
		sensor = new SimpleIntegerSensor(SENSOR_URI, UPDATE_FREQUENCY);
		sensor.addObserver(this);
		
		log.info("initialize CoAP web service at URL '" + SENSOR_URI + "'");
		coapServerApplication = new CoapServerApplication(OWN_PORT);
		
		coapWebservice = new CoapSensorWebservice(SENSOR_URI, 3);
		
		coapServerApplication.registerService(coapWebservice);
		
		log.info("initialize CoAP client application");
		
		coapClientApplication = new CoapClientApplication();
		
		log.info("initialization complete");
	}
	
	@Override
	public void registerAtServer() {
	    
	    //TODO: read how the register request works
	    
	    String hostCoapProxy = "127.0.0.1";
	    int portCoapProxy = 5683;
	    String path = "/";
	    
	    log.info("send observe request to " + hostCoapProxy + ":" + portCoapProxy + " with path " + path);
        
        try {
            sendCoapProxyRegisterRequest(hostCoapProxy, portCoapProxy, path, "");
        } catch (UnknownHostException e) {
            log.error("Unknown host: '" + hostCoapProxy + ":" + portCoapProxy);
        } catch (URISyntaxException e) {
            log.error("syntax error: " + e.getMessage());
        }
	}

	@Override
	public void receivedSensorData(SensorData data) {
		if (!(data instanceof SimpleIntegerSensorData)) {
			log.error("new sensor data available but it is not a SimpleIntegerSensorData");
			return;
		}
		
		// cast sensor data
		SimpleIntegerSensorData simpleIntegerSensorData = (SimpleIntegerSensorData)data;
		
		// transform data to apache jena RDF model
		Model model = ModelFactory.createDefaultModel();
		Resource sensor1 = model.createResource(HOST_URI + SENSOR_URI);
		Statement s = model.createLiteralStatement(sensor1, OWL.hasValue, simpleIntegerSensorData.getData());
		model.add(s);
		
		// encapsulate the rdf model in a data package
		DataPackage dataPackage = new RdfModelDataPackage(model);
		
		coapWebservice.updateRdfSensorData(dataPackage);
	}
	
	private CoapClient sendCoapProxyRegisterRequest(String uriHost, int uriPort, String uriPath, String uriQuery) throws URISyntaxException, UnknownHostException {
        // Create CoAP request
        URI webserviceURI = new URI ("coap", null, uriHost, uriPort, uriPath, uriQuery, null);
    
        MessageType.Name messageType = MessageType.Name.CON;
    
        CoapRequest coapRequest = new CoapRequest(messageType, MessageCode.Name.GET, webserviceURI, false);
        
        coapRequest.setObserve();
        
        // Set recipient (webservice host)
        InetSocketAddress recipient;
        recipient = new InetSocketAddress(InetAddress.getByName(uriHost), uriPort);
    
        CoapClient dataOriginCoapClient = new CoapClient();
        
        // Send the CoAP request
        coapClientApplication.sendCoapRequest(coapRequest, dataOriginCoapClient, recipient);
        
        return dataOriginCoapClient;
    }
}
