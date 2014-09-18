package de.uniluebeck.itm.priviot.coapwebserver.controller;

import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.configuration.Configuration;
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
import de.uniluebeck.itm.priviot.coapwebserver.service.CoapRegisterClient;
import de.uniluebeck.itm.priviot.coapwebserver.service.CoapRegisterClientObserver;
import de.uniluebeck.itm.priviot.coapwebserver.service.CoapSensorWebservice;
import de.uniluebeck.itm.priviot.utils.PseudonymizationProcessor;
import de.uniluebeck.itm.priviot.utils.data.EncryptionParameters;
import de.uniluebeck.itm.priviot.utils.encryption.cipher.asymmetric.rsa.RSACipherer;
import de.uniluebeck.itm.priviot.utils.encryption.cipher.symmetric.aes.AESCipherer;
import de.uniluebeck.itm.priviot.utils.pseudonymization.Secret;
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
    
    /** URI of the host */
    private static final String HOST_URI = "coap://localhost";
    
    
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
    
    /** If false, the encryption of sensor values is disabled. 
     *  In this case the sensor values are send raw and not in a privacyData package.
     */
    private boolean doEncrypt;
    
    /** Base path of sensors. The sensors will be for example <HOST_URI><sensorBasePath>/1 */
    private String sensorBasePath;
    
    /** URI for pseudonyms */
    private String pseudonymUriHost;
    
    private int numberOfThreads;
    
    private int numberOfSensors;
    
    /** default frequency in which new values are published by the sensor in seconds */
    private int sensorDefaultUpdateFrequency;
    
    /** if true, a random number is added to the default update frequency */
    private boolean sensorAddRandomToUpdateFrequency;
    
    /** Maximum changing in one random step of the sensor values langitude and latitude */
    private double maxChange;
    
    /** Listens to a local port. Web servers can be registered here. */
    private CoapServerApplication coapServerApplication;
    
    /** Can send CoAP requests using the CoapRegisterClient */
    private CoapClientApplication coapClientApplication;
    
    /** Used to retreive the certificate of teh SSP and to register the application at the CPP */
    private CoapRegisterClient coapRegisterClient;
    
    /** One observable webservice for each sensor */
    private volatile List<CoapSensorWebservice> coapSensorWebservices;
    
    /** Sensors that are linked with a CoapSensorWebservice */
    private volatile List<Sensor> sensors;
    
    /** Stores public keys of Smart Service Proxies. Methods are thread safe */
    private KeyDatabase keyDatabase;
    
    /** Parameters for encryption (used algorithms and keysizes) */
    private EncryptionParameters encryptionParameters;
    
    private Configuration config;
    
    /**
     * Constructor.
     * 
     * Starts Sensors and Webservices and connects them.
     * Starts the coap components.
     * 
     * @param config   The programs configuration
     */
    public CoapWebserverController(Configuration config) {
    	this.config = config;
    	
    	numberOfThreads = config.getInt("threads");
    	sensorBasePath = config.getString("sensorbasepath");
    	pseudonymUriHost = config.getString("pseudonymuri");
    	doEncrypt = config.getBoolean("doencrypt");
    	numberOfSensors = config.getInt("sensor.count");
    	sensorDefaultUpdateFrequency = config.getInt("sensor.updatefrequency");
    	sensorAddRandomToUpdateFrequency = config.getBoolean("sensor.updatefrequencyrandom");
    	maxChange = config.getDouble("sensor.maxchange");
    	int ownPort = config.getInt("port");
    	String urlSSP = config.getString("ssp.host");
    	int portSSP = config.getInt("ssp.port");
    	String urlCPP = config.getString("cpp.host");
    	int portCPP = config.getInt("cpp.port");
    	int aesBitStrength = config.getInt("encryption.aesstrength");
    	
    	if (!doEncrypt) {
    		log.info("Encryption is deactivated");
    	}    	
    	
    	coapServerApplication = new CoapServerApplication(ownPort);
        coapClientApplication = new CoapClientApplication();
        
        sensors = new ArrayList<Sensor>();
        coapSensorWebservices = new ArrayList<CoapSensorWebservice>();
        
        keyDatabase = new KeyDatabase();
        
        if (doEncrypt) {
        	encryptionParameters = new EncryptionParameters(AESCipherer.getAlgorithm(), aesBitStrength,
                                                           RSACipherer.getAlgorithm(), 1024);
        }
        else {
        	encryptionParameters = new EncryptionParameters("", 0, "", 0);
        }
        
        createSensorsAndWebservices();
        
        coapRegisterClient = new CoapRegisterClient(this, coapClientApplication, urlSSP, portSSP, urlCPP, portCPP);
        
        // register custom jena datatype
        TypeMapper.getInstance().registerDatatype(WktLiteral.getInstance());
    }
    
    /**
     * Starts the processing of the application by sending the certificate request to SSP.
     */
    public void start() {
    	if (doEncrypt) {
	        // send the certificate request to the SSP
	        try {
	            coapRegisterClient.sendCertificateRequest();
	        } catch (UnknownHostException | URISyntaxException e) {
	            log.error("Exception during sendCertificateRequest: " + e.getLocalizedMessage());
	        }
    	}
    	else {
    		// send register request directly to Smart Service Proxy
            try {
                coapRegisterClient.sendRegisterRequestToSSP();
            } catch (UnknownHostException | URISyntaxException e) {
                log.error("Exception during sendRegisterRequest: " + e.getMessage());
            }
    	}
    }
    
    private void createSensorsAndWebservices() {
        // Create a thread pool that executes the sensor processing
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("CoAP Webserver Sensor Thread#%d").build();
        ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(numberOfThreads, threadFactory);
        
        for (int i = 1; i <= numberOfSensors; i++) {
        	Sensor sensor = createGeographicalSensor(i, executorService);
        	
	        sensor.addObserver(this);
	        
	        // create a webservice for the sensor
	        CoapSensorWebservice coapWebservice = new CoapSensorWebservice(sensor.getSensorUriPath(), sensor.getUpdateFrequency(),
	                                                                        encryptionParameters, keyDatabase);
	        
	        sensors.add(sensor);
	        coapSensorWebservices.add(coapWebservice);
	        
	        coapServerApplication.registerService(coapWebservice);
	        
	        sensor.start(5);
        }
    }
    
    private Sensor createGeographicalSensor(int index, ScheduledThreadPoolExecutor executorService) {
    	String sensorPath = sensorBasePath + String.valueOf(index);
    	
    	log.info("initialize sensor: " + sensorPath);
    	
    	// if there is a special updateFrequency given in config take that one
    	int updateFrequency;
    	try {
    		updateFrequency = config.getInt("sensor" + index + ".updateFrequency");
    		log.debug("initialize sensor " + sensorPath + " with special updateFrequency " + updateFrequency);
    	}
    	catch (Exception e) {
    		updateFrequency = sensorDefaultUpdateFrequency;
    		if (sensorAddRandomToUpdateFrequency) {
    		    // add random number, maximum 10 percent of updateFrequency
    		    updateFrequency += (int)Math.round((Math.random() * 0.1 * updateFrequency));
    		}
    		log.debug("no updatefrequency given for " + sensorPath + ". Use default value" + 
    		          (sensorAddRandomToUpdateFrequency ? " + random: " : ": ") + 
    		          updateFrequency);
    	}
    	
    	// if there is a special secret given in config take that one
    	byte[] secret = null;
    	try {
    		String secretBase64 = config.getString("sensor" + index + ".secret");
    		if (!secretBase64.isEmpty()) {
    			secret = Secret.decodeBase64Secret(secretBase64);
        		log.debug("initialize sensor " + sensorPath + " with special secret");
    		}
    	}
    	catch (Exception e) {
    	}
    	if (secret == null) {
    		log.debug("no special secret given for " + sensorPath + ". Generate new random secret.");
    		// if no special secret, create one (default behavior)
    		try {
				secret = PseudonymizationProcessor.generateHmac256Secret();
			} catch (PseudonymizationException e1) {
				log.error("Error while generating secret for " + sensorPath, e1);
				return null;
			}
    	}
    	
    	double startLatitude;
    	try {
    		startLatitude = config.getDouble("sensor" + index + ".latitude");
    		log.debug("initialize " + sensorPath + " at latitude " + startLatitude);
    	}
    	catch (Exception e) {
    		log.debug("no latitude value given for " + sensorPath + ". Use default value.");
    		startLatitude = 10.6802434;
    	}
    	
    	double startLongitude;
    	try {
    		startLongitude = config.getDouble("sensor" + index + ".longitude");
    		log.info("initialize " + sensorPath + " at longitude " + startLongitude);
    	}
    	catch (Exception e) {
    		log.debug("no longitude value given for " + sensorPath + ". use default value.");
    		startLongitude = 53.8686906;
    	}
    	
    	log.info("initialize sensor " + sensorPath + "." + 
    	         " updateFrequency: " + updateFrequency + 
    	         " startLongitude: " + startLongitude +
    	         " startLatitude: " + startLatitude +
    	         " maxChange: " + maxChange);
    	
        // create and initialize a GeographicSensor and it's Webservice
        GeographicSensor sensor = new GeographicSensor(sensorPath, updateFrequency, 
        		                                       startLongitude, startLatitude, 
        		                                       maxChange, executorService);
        sensor.setSecret(secret);
        
        return sensor;
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
    	
    	ResourceStatus resourceStatus;
    	
    	if (doEncrypt) {
	    	// create the pseudonym for the actual time slot
	    	String sensorPseudonym;
	    	try {
				sensorPseudonym = PseudonymizationProcessor.generateHmac256Pseudonym(sensorURI, data.getLifetime(), sensor.getSecret());
			} catch (PseudonymizationException e) {
				log.error("Error during Pseudonymization of new sensor data", e);
				return;
			}
	    	sensorPseudonym = pseudonymUriHost + sensorPseudonym;
	    	
	    	// initialize Apache Jena RDF model
	    	Model model = ModelFactory.createDefaultModel();
	    	model.setNsPrefix("pseudonym", pseudonymUriHost);
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
	        
	        // for debug output
	        StringWriter stringWriter = new StringWriter();
	        model.write(stringWriter, "N3");
	        String rdfModelStr = stringWriter.getBuffer().toString();
	        log.debug("New sensor data available:\n" + rdfModelStr);
	        
	        resourceStatus = new ResourceStatus(sensorPseudonym, model, data.getLifetime());
    	}
    	else {
    		Model model = ModelFactory.createDefaultModel();
	    	model.setNsPrefix("sn", HOST_URI + sensorBasePath + "#");
	    	Resource resSensor = model.createResource(sensorURI);
	    	
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
	        
	        resourceStatus = new ResourceStatus(sensorURI, model, data.getLifetime());
    	}
        
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
    	String wktLiteralValue = WktLiteral.toValueString(sensorData.getLatitude(), sensorData.getLongitude());
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
            coapRegisterClient.sendRegisterRequestToCPP();
        } catch (UnknownHostException | URISyntaxException e) {
            log.error("Exception during sendRegisterRequest: " + e.getMessage());
        }
    }
    
    
}
