package priviot.data_origin.controller;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;

import de.uniluebeck.itm.ncoap.application.server.CoapServerApplication;
import priviot.data_origin.data.SensorData;
import priviot.data_origin.data.SimpleIntegerSensorData;
import priviot.data_origin.sensor.SimpleIntegerSensor;
import priviot.data_origin.service.CoapWebservice;
import priviot.utils.data.transfer.DataPackage;
import priviot.utils.data.transfer.RdfModelDataPackage;

/**
 * A Controller, that connects the SimpleIntegerSensor with the CoapWebservice.
 * The data of the sensor is transformed into a apache jena rdf model before passed to the CoapWebservice.
 */
public class RDFSimpleIntegerSensorCoapController extends Controller {

	private static final String hostURI = "http://localhost";
	
	/** URI of the sensor */
	private static final String sensorURI = "/sensor1";
	/** frequency in which new values are published by the sensor in seconds */
	private static final int updateFrequency = 3;
	
	private static Logger log = Logger.getLogger(RDFSimpleIntegerSensorCoapController.class.getName());
	
	/** Web service over the COAP protocol */
	CoapWebservice coapWebservice;
	
	/** Listens to a local port. Web services can be registered here. */
	CoapServerApplication coapServerApplication;
	
	
	@Override
	public void initialize() {
		
		log.info("initialize SimpleIntegerSensor " + sensorURI);
		sensor = new SimpleIntegerSensor(sensorURI, updateFrequency);
		sensor.addObserver(this);
		
		log.info("initialize COAP web service at URL '" + sensorURI + "'");
		coapServerApplication = new CoapServerApplication();
		
		coapWebservice = new CoapWebservice(sensorURI, 3);
		
		coapServerApplication.registerService(coapWebservice);
		
		log.info("initialization complete");
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
		Resource sensor1 = model.createResource(hostURI + sensorURI);
		Statement s = model.createLiteralStatement(sensor1, OWL.hasValue, simpleIntegerSensorData.getData());
		model.add(s);
		
		// encapsulate the rdf model in a data package
		DataPackage dataPackage = new RdfModelDataPackage(model);
		
		coapWebservice.updateRdfSensorData(dataPackage);
	}
}
