package de.uniluebeck.itm.priviot.coapwebserver.controller;

import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.application.server.CoapServerApplication;
import de.uniluebeck.itm.priviot.coapwebserver.data.GeographicSensorData;
import de.uniluebeck.itm.priviot.coapwebserver.data.KeyDatabase;
import de.uniluebeck.itm.priviot.coapwebserver.data.KeyDatabaseEntry;
import de.uniluebeck.itm.priviot.coapwebserver.data.ResourceStatus;
import de.uniluebeck.itm.priviot.coapwebserver.data.SensorData;
import de.uniluebeck.itm.priviot.coapwebserver.data.SimpleIntegerSensorData;
import de.uniluebeck.itm.priviot.coapwebserver.data.WktLiteral;
import de.uniluebeck.itm.priviot.coapwebserver.sensor.GeographicSensor;
import de.uniluebeck.itm.priviot.coapwebserver.sensor.Sensor;
import de.uniluebeck.itm.priviot.coapwebserver.sensor.SensorObserver;
import de.uniluebeck.itm.priviot.coapwebserver.sensor.SimpleIntegerSensor;
import de.uniluebeck.itm.priviot.coapwebserver.service.CoapRegisterClient;
import de.uniluebeck.itm.priviot.coapwebserver.service.CoapRegisterClientObserver;
import de.uniluebeck.itm.priviot.coapwebserver.service.CoapSensorWebservice;
import de.uniluebeck.itm.priviot.utils.PseudonymizationProcessor;
import de.uniluebeck.itm.priviot.utils.data.EncryptionParameters;
import de.uniluebeck.itm.priviot.utils.encryption.cipher.asymmetric.rsa.RSACipherer;
import de.uniluebeck.itm.priviot.utils.encryption.cipher.symmetric.aes.AESCipherer;
import de.uniluebeck.itm.priviot.utils.pseudonymization.PseudonymizationException;

/**
 * Main controller class of the CoAP-Webserver.
 * The CoapWebserverController initializes the application and registers it at the CoAP Privacy Proxy.
 * 
 * The CoapWebserverController first starts the {@link Sensor}- and {@link CoapSensorWebservice}-instances 
 * and connects them.
 * After that it gets the X509 certificate from the Smart Service Proxy to get and verify it's public key.
 * If the public key is okay, the [@link CoapWebserverController} registers the CoAP-Webserver at the
 * CoAP Privacy Proxy.
 * The CoAP Privacy Proxy will ask for the sensor webservices and register itself as observer at the 
 * {@link CoapSensorWebservice}-instances.
 */
public class CoapWebserverController implements SensorObserver, CoapRegisterClientObserver {
    
    private static final int NUMBER_OF_THREADS = 1;
    
    /** URI of the host */
    private static final String HOST_URI = "coap://localhost";
    /** URI for pseudonyms */
    private static final String PSEUDONYM_URI_HOST = "http://www.pseudonym.com/";
    
    /** URI of the sensor */
    private static final String SENSOR2_URI_PATH = "/sensor1";
    /** frequency in which new values are published by the sensor in seconds */
    private static final int SENSOR2_UPDATE_FREQUENCY = 30;
    
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
    /** Listens to a local port. Web servers can be registered here. */
    private CoapServerApplication coapServerApplication;
    
    /** Can send CoAP requests using the CoapRegisterClient */
    private CoapClientApplication coapClientApplication;
    
    /** Used to retreive the certificate of teh SSP and to register the application at the CPP */
    private CoapRegisterClient coapRegisterClient;
    
    /** One observable webservice for each sensor */
    private List<CoapSensorWebservice> coapSensorWebservices;
    
    /** Sensors that are linked with a CoapSensorWebservice */
    private List<Sensor> sensors;
    
    /** Stores public keys of Smart Service Proxies */
    private KeyDatabase keyDatabase;
    
    /** Parameters for encryption (used algorithms and keysizes) */
    private EncryptionParameters encryptionParameters;
    
    /**
     * Constructor.
     * 
     * Starts Sensors and Webservices and connects them.
     * Starts the coap components.
     * 
     * @param urlSSP   The host url of the Smart Service Proxy
     * @param portSSP  The port of the Smart Service Proxy
     * @param urlCPP   The host url of the CoAP Privacy Proxy
     * @param portCPP  The port of the CoAP Privacy Proxy
     */
    public CoapWebserverController(int ownPort, String urlSSP, int portSSP, String urlCPP, int portCPP) {
    	coapServerApplication = new CoapServerApplication(ownPort);
        coapClientApplication = new CoapClientApplication();
        
        sensors = new ArrayList<Sensor>();
        coapSensorWebservices = new ArrayList<CoapSensorWebservice>();
        
        keyDatabase = new KeyDatabase();
        
        encryptionParameters = new EncryptionParameters(AESCipherer.getAlgorithm(), 256,
                                                        RSACipherer.getAlgorithm(), 1024);
        
        createSensorsAndWebservices();
        
        coapRegisterClient = new CoapRegisterClient(this, coapClientApplication, urlSSP, portSSP, urlCPP, portCPP);
        
        // register custom jena datatype
        TypeMapper.getInstance().registerDatatype(WktLiteral.getInstance());
    }
    
    /**
     * Starts the processing of the application by sending the certificate request to SSP.
     */
    public void start() {
        // send the certificate request to the SSP
        try {
            coapRegisterClient.sendCertificateRequest();
        } catch (UnknownHostException | URISyntaxException e) {
            log.error("Exception during sendCertificateRequest: " + e.getLocalizedMessage());
        }
    }
    
    private void createSensorsAndWebservices() {
        // Create a thread pool that executes the sensor processing
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("CoAP Webserver Sensor Thread#%d").build();
        ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(NUMBER_OF_THREADS, threadFactory);
        
        // create and initialize a GeographicSensor and it's Webservice
        GeographicSensor sensor2 = new GeographicSensor(SENSOR2_URI_PATH, SENSOR2_UPDATE_FREQUENCY, executorService);
        initializeSensorAndCreateWebservice(sensor2);
        sensor2.start(5);
    }
    
    private void initializeSensorAndCreateWebservice(Sensor sensor) {
    	sensor.addObserver(this);
        
    	//TODO: create random secret for both sensors
    	if (sensor.getSensorUriPath().equals(SENSOR2_URI_PATH)) {
	    	log.error("TEST-CODE: Take fixed secret for "  + SENSOR2_URI_PATH);
	    	final byte[] SECRET = {84, -96, -33, -95, 19, -64, 61, -41, -46, 108, -35, 56, 99, 25, -29, -53, 57, -25, -59, 123, 10, -75, -91, 40, -66, -9, 66, 9, -77, 70, 74, -53, -41, -102, -20, 15, 118, -68, 12, 70, -71, -2, 90, -52, -53, 113, -98, 46, -21, 13, -67, -87, 24, 73, 28, 115, -28, 120, -13, -78, -86, -104, 104, -121, 15, -25, 61, 54, -88, -64, -42, 106, -80, 65, -107, -54, 19, 94, 8, 8, -19, 0, -95, 38, 121, 92, 2, 38, -14, 9, 91, -75, 14, -61, 121, 47, -85, 63, 119, 9, -32, -42, -94, 32, 71, 0, 99, -36, 17, -124, -18, -128, 74, 63, -75, -46, 52, -66, 56, -127, -98, -64, -56, 51, -78, -81, -18, 29, 26, 25, -71, 48, 25, 101, 56, 5, -90, 11, -13, 116, 70, -112, 63, 63, -109, 98, -127, -78, 118, -53, -109, -116, -7, 127, -66, 123, 73, 69, 66, -19, -48, 100, 9, 4, 92, -47, -113, -104, -92, -15, -9, 53, -1, -74, -103, -31, 35, -26, -113, -14, -116, -84, 110, -119, 11, -113, -62, -39, -10, 79, -20, 114, 47, 3, 40, 61, 117, 45, -83, -65, -83, -55, 75, -26, -64, -44, -47, -65, 127, -126, 73, -78, 112, -94, -7, -120, -4, -60, -71, -96, 106, -56, 85, -128, 109, -69, -65, 80, -20, 56, 110, 28, 18, -24, 45, -25, -101, 112, 90, -59, 121, 123, 49, 8, -124, 17, 40, -128, 98, 61, -8, 84, 20, -72, -119, -2};
	    	sensor.setSecret(SECRET);
    	}
    	else {
	        // create a secret, that is later used to create the pseudonyms for the sensor
	        byte[] secret;
	        try {
				secret = PseudonymizationProcessor.generateHmac256Secret();
			} catch (PseudonymizationException e) {
				log.error("Error while generating secret", e);
				return;
			}
	        sensor.setSecret(secret);
    	}
        
        // create a webservice for the sensor
        CoapSensorWebservice coapWebservice = new CoapSensorWebservice(sensor.getSensorUriPath(), sensor.getUpdateFrequency(),
                                                                        encryptionParameters, keyDatabase);
        
        sensors.add(sensor);
        coapSensorWebservices.add(coapWebservice);
        
        coapServerApplication.registerService(coapWebservice);
    }

    /**
     * Whenever a sensor has new data available, this method is called.
     * The data is transformed to a jena rdf model and passed to the
     * {@link CoapSensorWebservice} that responsible for the sensor.
     * @param data
     */
    @Override
    public void publishData(Sensor sensor, SensorData data) {
    	log.info("New sensor data from sensor " + sensor.getSensorUriPath());
    	
    	String sensorURI = HOST_URI + sensor.getSensorUriPath();
    	
    	// create the pseudonym for the actual time slot
    	String sensorPseudonym;
    	try {
			sensorPseudonym = PseudonymizationProcessor.generateHmac256Pseudonym(sensorURI, data.getLifetime(), sensor.getSecret());
		} catch (PseudonymizationException e) {
			log.error("Error during Pseudonymization of new sensor data", e);
			return;
		}
    	sensorPseudonym = PSEUDONYM_URI_HOST + sensorPseudonym;
    	
    	// initialize Apache Jena RDF model
    	Model model = ModelFactory.createDefaultModel();
    	model.setNsPrefix("pseudonym", PSEUDONYM_URI_HOST);
    	Resource resSensor = model.createResource(sensorPseudonym);
    	
    	// format and insert the sensor data into the model
        if (data instanceof SimpleIntegerSensorData) {
        	putIntegerSensorDataIntoModel((SimpleIntegerSensorData)data, model, resSensor);
        }
        else if (data instanceof GeographicSensorData) {
        	putGeographicalSensorDataIntoModel((GeographicSensorData)data, model, resSensor);
        }
        else {
        	log.error("Given SensorData not supported: "  + data.getSensorUriPath() + " (" + data.getClass() + ")");
        	return;
        }
        
        StringWriter stringWriter = new StringWriter();
        model.write(stringWriter, "N3");
        String rdfModelStr = stringWriter.getBuffer().toString();
        log.debug("New sensor data available:\n" + rdfModelStr);
        
        ResourceStatus resourceStatus = new ResourceStatus(sensorPseudonym, model, data.getLifetime());
        
        // finds the corresponding web service for the sensor URI
        CoapSensorWebservice webservice = findWebservice(data.getSensorUriPath());
        
        if (webservice == null) {
            log.error("No webservice available for data of sensor '" + data.getSensorUriPath() + "'");
            return;
        }
        
        webservice.updateResourceStatus(resourceStatus);
    }
    
    private void putIntegerSensorDataIntoModel(SimpleIntegerSensorData sensorData, Model model, Resource resSensor) {        
        log.debug("New integer sensor data available for " + sensorData.getSensorUriPath() + ": " + sensorData.getData());
        
        // transform data to Apache Jena RDF model
        Statement s = model.createLiteralStatement(resSensor, OWL.hasValue, sensorData.getData());
        model.add(s);
    }
    
    private void putGeographicalSensorDataIntoModel(GeographicSensorData sensorData, Model model, Resource resSensor) {
    	log.debug("New geographical sensor data available for " + sensorData.getSensorUriPath() + ": (" + 
                  sensorData.getLongitude() + ", " + sensorData.getLatitude());
    	
    	// add namespaces
    	String prefGeosparql = "http://www.opengis.net/ont/geosparql#";
    	String prefSf = "http://www.opengis.net/ont/sf#";
    	String prefItm = "http://example.org/itm-geo-test#";
    	model.setNsPrefix("gsp", prefGeosparql);
    	model.setNsPrefix("sf", prefSf);
    	model.setNsPrefix("itm",prefItm);
    	model.setNsPrefix("rdf", RDF.getURI());
    	model.setNsPrefix("rdfs", RDFS.getURI());
    	
    	// create properties
    	Property propHasPosition = model.createProperty(prefItm + "hasPosition");
    	Property propHasGeometry = model.createProperty(prefGeosparql + "hasGeometry");
    	Property propWKT = model.createProperty(prefGeosparql + "asWKT");
    	
    	// create resources
    	Resource resSensorPosition = model.createResource(resSensor.getURI() + "position");
    	Resource resPoint = model.createResource(prefSf + "Point");
    	resSensor.addProperty(propHasPosition, resSensorPosition);
    	
    	// add triples
    	model.add(propHasPosition, RDF.type, RDF.Property);
    	model.add(propHasPosition, RDFS.subPropertyOf, propHasGeometry);
    	model.add(resSensorPosition, RDF.type, resPoint);
    	
    	// add literal
    	String wktLiteralValue = WktLiteral.toValueString(sensorData.getLongitude(), sensorData.getLatitude());
    	resSensorPosition.addLiteral(propWKT, ResourceFactory.createTypedLiteral(wktLiteralValue, WktLiteral.getInstance()));
    }
    
    private CoapSensorWebservice findWebservice(String sensorURI) {
        for (CoapSensorWebservice webservice : coapSensorWebservices) {
            if (webservice.getPath().equals(sensorURI)) {
                return webservice;
            }
        }
        return null;
    }

    /**
     * Is called by coapRegisterClient when it receives the X509Certificate from the CoAP Privacy Proxy.
     * The public key of the certificate is saved in the key database and
     * a registration request is sent to the Smart Service Proxy.
     */
    @Override
    public void receivedCertificate(URI fromUri, X509Certificate certificate) {
        log.info("received certificate for " + certificate.getSubjectX500Principal().getName());
        
        try {
        	certificate.checkValidity();
        }
        catch (CertificateExpiredException | CertificateNotYetValidException e) {
        	log.error("Received certificate is not valid!");
        }
        
        //TODO: check certificate
        log.warn("TODO: check the received certificate");
        
        // save public key. Method is thread safe.
        keyDatabase.addEntry(new KeyDatabaseEntry(fromUri, certificate.getPublicKey()));
        
        // send register request to CoAP Privacy Proxy
        try {
            coapRegisterClient.sendRegisterRequest();
        } catch (UnknownHostException | URISyntaxException e) {
            log.error("Exception during sendRegisterRequest: " + e.getMessage());
        }
    }
    
    
}