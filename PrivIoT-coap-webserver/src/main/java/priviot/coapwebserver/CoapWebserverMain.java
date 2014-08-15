package priviot.coapwebserver;

import java.io.File;
import java.net.MalformedURLException;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import priviot.coapwebserver.controller.CoapWebserverController;

public class CoapWebserverMain {

	private static String version = "0.1";
	
	private static Logger log = Logger.getLogger(CoapWebserverMain.class.getName());
	
	private static String urlSSP = "localhost";
	private static int portSSP = 8080;
	private static String urlCPP = "localhost";
	private static int portCPP = 8081;
	
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
	    CoapWebserverController controller = new CoapWebserverController(urlSSP, portSSP, urlCPP, portCPP);
	    
	    controller.start();
	}

}
