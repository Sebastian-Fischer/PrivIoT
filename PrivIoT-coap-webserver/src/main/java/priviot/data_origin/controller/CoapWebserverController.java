package priviot.data_origin.controller;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;

import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
import de.uniluebeck.itm.ncoap.application.server.CoapServerApplication;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.CoapResponse;
import de.uniluebeck.itm.ncoap.message.MessageCode;
import de.uniluebeck.itm.ncoap.message.MessageType;
import priviot.data_origin.data.KeyDatabase;
import priviot.data_origin.data.KeyDatabaseEntry;
import priviot.data_origin.data.SensorData;
import priviot.data_origin.data.SimpleIntegerSensorData;
import priviot.data_origin.sensor.Sensor;
import priviot.data_origin.sensor.SensorObserver;
import priviot.data_origin.sensor.SimpleIntegerSensor;
import priviot.data_origin.service.CoapRegisterClient;
import priviot.data_origin.service.CoapSensorWebservice;
import priviot.utils.data.transfer.PrivIoTContentFormat;

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
public class CoapWebserverController implements Observer, SensorObserver {
    
    private static String urlPathCertificate = "/certificate";
    private static String urlPathRegistry = "/registry";
    
    /** Port the coap application listens to */
    private static final int OWN_PORT = 5684;
    /** URI of the host */
    private static final String HOST_URI = "coap://localhost";
    /** URI of the sensor */
    private static final String SENSOR1_URI = "/sensor1";
    /** frequency in which new values are published by the sensor in seconds */
    private static final int SENSOR1_UPDATE_FREQUENCY = 3;
    
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    
    private String urlSSP;
    private int portSSP;
    private String urlCPP;
    private int portCPP;
    
    /** Listens to a local port. Web services can be registered here. */
    private CoapServerApplication coapServerApplication;
    
    /** Can send CoAP requests using the CoapRegisterClient */
    private CoapClientApplication coapClientApplication;
    
    /** Used to send CoAP requests */
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
     * Sends Certificate request to SSP.
     * 
     * @param urlSSP   The host url of the Smart Service Proxy
     * @param portSSP  The port of the Smart Service Proxy
     * @param urlCPP   The host url of the CoAP Privacy Proxy
     * @param portCPP  The port of the CoAP Privacy Proxy
     */
    public CoapWebserverController(String urlSSP, int portSSP, String urlCPP, int portCPP) {
        this.urlSSP = urlSSP;
        this.portSSP = portSSP;
        this.urlCPP = urlCPP;
        this.portCPP = portCPP;
        
        coapServerApplication = new CoapServerApplication(OWN_PORT);
        
        createSensorsAndWebservices();
        
        keyDatabase = new KeyDatabase();
        
        coapRegisterClient = new CoapRegisterClient();
        coapRegisterClient.addObserver(this);
        
        try {
            sendCertificateRequest();
        } catch (UnknownHostException | URISyntaxException e) {
            log.error("Exception during sendCertificateRequest: " + e.getLocalizedMessage());
        }
    }
    
    private void createSensorsAndWebservices() {
        SimpleIntegerSensor sensor1 = new SimpleIntegerSensor(SENSOR1_URI, SENSOR1_UPDATE_FREQUENCY);
        sensor1.addObserver(this);
        
        CoapSensorWebservice coapWebservice1 = new CoapSensorWebservice(SENSOR1_URI, SENSOR1_UPDATE_FREQUENCY);
        
        sensors.add(sensor1);
        coapSensorWebservices.add(coapWebservice1);
        
        coapServerApplication.registerService(coapWebservice1);
    }
    
    /**
     * Sends a certificate request to the Smart Service Proxy to get it's X509 certificate.
     * @throws URISyntaxException
     * @throws UnknownHostException
     */
    private void sendCertificateRequest() throws URISyntaxException, UnknownHostException {
        // Create CoAP request
        URI webserviceURI = new URI ("coap", null, urlSSP, portSSP, urlPathCertificate, "", null);
        
        MessageType.Name messageType = MessageType.Name.CON;
        
        CoapRequest coapRequest = new CoapRequest(messageType, MessageCode.Name.GET, webserviceURI, false);
        
        // Set recipient (webservice host)
        InetSocketAddress recipient;
        recipient = new InetSocketAddress(InetAddress.getByName(urlSSP), portSSP);
        
        // Send the CoAP request
        coapClientApplication.sendCoapRequest(coapRequest, coapRegisterClient, recipient);
    }
    
    /**
     * Sends the registration request to the CoAP Privacy Proxy.
     * The proxy will react with a registration as observer to the available webservices.
     * @throws URISyntaxException
     * @throws UnknownHostException
     */
    private void sendRegisterRequest() throws URISyntaxException, UnknownHostException {
        // Create CoAP request
        URI webserviceURI = new URI ("coap", null, urlCPP, portCPP, urlPathRegistry, "", null);
        
        MessageType.Name messageType = MessageType.Name.CON;
        
        CoapRequest coapRequest = new CoapRequest(messageType, MessageCode.Name.GET, webserviceURI, false);
        
        // Set recipient (webservice host)
        InetSocketAddress recipient;
        recipient = new InetSocketAddress(InetAddress.getByName(urlCPP), portCPP);
        
        // Send the CoAP request
        coapClientApplication.sendCoapRequest(coapRequest, coapRegisterClient, recipient);
    }

    //TODO: change to own observer class
    @Override
    public void update(Observable coapRegisterClientObs, Object coapResponseObj) {
        if (coapRegisterClient != coapRegisterClientObs) {
            return;
        }
        
        CoapResponse coapResponse = (CoapResponse)coapResponseObj;
        
        URI locationUri;
        try {
            locationUri = coapResponse.getLocationURI();
        } catch (URISyntaxException e) {
            log.error("location uri of received message is bad formed");
            return;
        }
        String baseUri = locationUri.getHost();
        
        log.info("received message from: " + locationUri + " (host: " + baseUri + ")");
        
        // if this is answer to certificate request
        if (coapResponse.getContentFormat() == PrivIoTContentFormat.APP_X509CERTIFICATE) {
            //TODO: parse X509Certificate
            // maybe like this: http://www.oracle.com/technetwork/articles/javase/dig-signature-api-140772.html
            byte[] publicKey = new byte[0];
            
            // save public key
            keyDatabase.addEntry(new KeyDatabaseEntry(baseUri, publicKey));
            
            // send register request to CoAP Privacy Proxy
            try {
                sendRegisterRequest();
            } catch (UnknownHostException | URISyntaxException e) {
                log.error("Exception during sendRegisterRequest: " + e.getMessage());
            }
        }
      
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
        
        // transform data to apache jena RDF model
        Model model = ModelFactory.createDefaultModel();
        Resource sensor1 = model.createResource(HOST_URI + data.getSensorURI());
        Statement s = model.createLiteralStatement(sensor1, OWL.hasValue, simpleIntegerSensorData.getData());
        model.add(s);
        
        CoapSensorWebservice webservice = findWebservice(data.getSensorURI());
        
        if (webservice == null) {
            log.error("No webservice available for data of sensor '" + data.getSensorURI() + "'");
            return;
        }
        
        webservice.updateRdfSensorData(model);
    }
    
    private CoapSensorWebservice findWebservice(String sensorURI) {
        for (CoapSensorWebservice webservice : coapSensorWebservices) {
            if (webservice.getPath().equals(sensorURI)) {
                return webservice;
            }
        }
        
        return null;
    }
    
    
}
