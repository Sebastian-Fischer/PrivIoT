package priviot.coapwebserver;

import java.io.File;
import java.net.MalformedURLException;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import de.uniluebeck.itm.ncoap.application.server.CoapServerApplication;

import priviot.coapwebserver.controller.CoapWebserverController;

public class CoapWebserverMain {

	private final static String version = "0.1";
	
	private static Logger log = Logger.getLogger(CoapWebserverMain.class.getName());
	
	private final static int OWN_PORT = CoapServerApplication.DEFAULT_COAP_SERVER_PORT + 1;
	private final static String urlSSP = "localhost";
	private final static int portSSP = CoapServerApplication.DEFAULT_COAP_SERVER_PORT + 2;
	private final static String urlCPP = "localhost";
	private final static int portCPP = CoapServerApplication.DEFAULT_COAP_SERVER_PORT;
	
	private static String CONFIG_FILE_NAME = "log4j.xml";
	
	
	public static void main(String[] args) {
		System.out.println("PrivIoT - CoAP Webserver (version " + version + ")");
		
		System.out.println("Configure logging");
		try {
            configureLogging();
        } catch (MalformedURLException | FactoryConfigurationError e) {
            e.printStackTrace();
        }
		System.out.println("Configure logging: done");		
		
		log.info("start controller");
		startController();
		log.info("start controller: done");
	}
	
	private static void configureLogging() throws MalformedURLException, FactoryConfigurationError {
        File configFile = new File(CONFIG_FILE_NAME);
        
        if(configFile.exists()){
            System.out.println("Configure logging from file '" + CONFIG_FILE_NAME + "'.");
            DOMConfigurator.configure(configFile.toURI().toURL());
        }
        else {
            System.out.println("No config file found. Use default configuration.");
            BasicConfigurator.configure();
        }
    }
	
	private static void startController() {
	    CoapWebserverController controller = new CoapWebserverController(OWN_PORT, urlSSP, portSSP, urlCPP, portCPP);
	    
	    controller.start();
	}

}
