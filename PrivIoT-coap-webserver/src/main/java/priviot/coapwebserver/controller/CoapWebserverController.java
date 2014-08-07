package priviot.coapwebserver.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;

import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.application.server.CoapServerApplication;
import priviot.coapwebserver.data.JenaRdfModelWithLifetime;
import priviot.coapwebserver.data.KeyDatabase;
import priviot.coapwebserver.data.KeyDatabaseEntry;
import priviot.coapwebserver.data.SensorData;
import priviot.coapwebserver.data.SimpleIntegerSensorData;
import priviot.coapwebserver.sensor.Sensor;
import priviot.coapwebserver.sensor.SensorObserver;
import priviot.coapwebserver.sensor.SimpleIntegerSensor;
import priviot.coapwebserver.service.CoapRegisterClient;
import priviot.coapwebserver.service.CoapRegisterClientObserver;
import priviot.coapwebserver.service.CoapSensorWebservice;

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
    
    /** Port the coap application listens to */
    private static final int OWN_PORT = 5684;
    /** URI of the host */
    private static final String HOST_URI = "coap://localhost";
    /** URI of the sensor */
    private static final String SENSOR1_URI = "/sensor1";
    /** frequency in which new values are published by the sensor in seconds */
    private static final int SENSOR1_UPDATE_FREQUENCY = 3;
    
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
    /** Listens to a local port. Web services can be registered here. */
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
    public CoapWebserverController(String urlSSP, int portSSP, String urlCPP, int portCPP) {
        coapServerApplication = new CoapServerApplication(OWN_PORT);
        coapClientApplication = new CoapClientApplication();
        
        sensors = new ArrayList<Sensor>();
        coapSensorWebservices = new ArrayList<CoapSensorWebservice>();
        
        createSensorsAndWebservices();
        
        keyDatabase = new KeyDatabase();
        
        coapRegisterClient = new CoapRegisterClient(coapClientApplication, urlSSP, portSSP, urlCPP, portCPP);
    }
    
    /**
     * Starts the processing of the application by sending the certificate request to SSP.
     */
    public void start() {
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
        
        SimpleIntegerSensor sensor1 = new SimpleIntegerSensor(SENSOR1_URI, SENSOR1_UPDATE_FREQUENCY, executorService);
        sensor1.addObserver(this);
        
        CoapSensorWebservice coapWebservice1 = new CoapSensorWebservice(SENSOR1_URI, SENSOR1_UPDATE_FREQUENCY);
        
        sensors.add(sensor1);
        coapSensorWebservices.add(coapWebservice1);
        
        coapServerApplication.registerService(coapWebservice1);
        
        // let the sensor work
        sensor1.start();
    }
    
    

    /**
     * Whenever a sensor has new data available, this method is called.
     * The data is transformed to a jena rdf model and passed to the
     * {@link CoapSensorWebservice} that responsible for the sensor.
     * @param data
     */
    @Override
    public void publishData(SensorData data) {
        //TODO: add support for other data types
        
        // cast sensor data
        SimpleIntegerSensorData simpleIntegerSensorData = (SimpleIntegerSensorData)data;
        
        log.info("new sensor data available for " + data.getSensorURI() + ": " + simpleIntegerSensorData.getData());
        
        // transform data to apache jena RDF model
        Model model = ModelFactory.createDefaultModel();
        Resource sensor1 = model.createResource(HOST_URI + data.getSensorURI());
        Statement s = model.createLiteralStatement(sensor1, OWL.hasValue, simpleIntegerSensorData.getData());
        model.add(s);
        
        JenaRdfModelWithLifetime modelWithLifetime = new JenaRdfModelWithLifetime(model, simpleIntegerSensorData.getLifetime());
        
        CoapSensorWebservice webservice = findWebservice(data.getSensorURI());
        
        if (webservice == null) {
            log.error("No webservice available for data of sensor '" + data.getSensorURI() + "'");
            return;
        }
        
        webservice.updateRdfSensorData(modelWithLifetime);
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
        String baseUri = fromUri.getHost();
        
        byte[] publicKey = certificate.getPublicKey().getEncoded();
        
        // save public key
        keyDatabase.addEntry(new KeyDatabaseEntry(baseUri, publicKey));
        
        // send register request to CoAP Privacy Proxy
        try {
            coapRegisterClient.sendRegisterRequest();
        } catch (UnknownHostException | URISyntaxException e) {
            log.error("Exception during sendRegisterRequest: " + e.getMessage());
        }
    }
    
    
}
