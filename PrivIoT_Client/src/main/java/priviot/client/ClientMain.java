package priviot.client;

import java.io.File;
import java.net.MalformedURLException;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class ClientMain {

	private static final String version = "0.1";
	
	private static final String SENSOR_URI = "coap://localhost/sensor2";
	//TODO: get random generated secret from sensor
	private static final byte[] SENSOR_SECRET = {84, -96, -33, -95, 19, -64, 61, -41, -46, 108, -35, 56, 99, 25, -29, -53, 57, -25, -59, 123, 10, -75, -91, 40, -66, -9, 66, 9, -77, 70, 74, -53, -41, -102, -20, 15, 118, -68, 12, 70, -71, -2, 90, -52, -53, 113, -98, 46, -21, 13, -67, -87, 24, 73, 28, 115, -28, 120, -13, -78, -86, -104, 104, -121, 15, -25, 61, 54, -88, -64, -42, 106, -80, 65, -107, -54, 19, 94, 8, 8, -19, 0, -95, 38, 121, 92, 2, 38, -14, 9, 91, -75, 14, -61, 121, 47, -85, 63, 119, 9, -32, -42, -94, 32, 71, 0, 99, -36, 17, -124, -18, -128, 74, 63, -75, -46, 52, -66, 56, -127, -98, -64, -56, 51, -78, -81, -18, 29, 26, 25, -71, 48, 25, 101, 56, 5, -90, 11, -13, 116, 70, -112, 63, 63, -109, 98, -127, -78, 118, -53, -109, -116, -7, 127, -66, 123, 73, 69, 66, -19, -48, 100, 9, 4, 92, -47, -113, -104, -92, -15, -9, 53, -1, -74, -103, -31, 35, -26, -113, -14, -116, -84, 110, -119, 11, -113, -62, -39, -10, 79, -20, 114, 47, 3, 40, 61, 117, 45, -83, -65, -83, -55, 75, -26, -64, -44, -47, -65, 127, -126, 73, -78, 112, -94, -7, -120, -4, -60, -71, -96, 106, -56, 85, -128, 109, -69, -65, 80, -20, 56, 110, 28, 18, -24, 45, -25, -101, 112, 90, -59, 121, 123, 49, 8, -124, 17, 40, -128, 98, 61, -8, 84, 20, -72, -119, -2};
	private static final int SENSOR_UPDATE_INTERVAL = 30;
	//private static final String SSP_HTTP_URI = "/127.0.0.1/services/sparql-endpoint";
	private static final String SSP_HTTP_HOST = "localhost";
	private static final String SSP_HTTP_REQUEST_PATH = "/services/sparql-endpoint";
	private static final int SSP_HTTP_PORT = 8080;
	
	private static final String CONFIG_FILE_NAME = "log4j.xml";
	
	private static Logger log = Logger.getLogger(ClientMain.class.getName());
	
	
	public static void main(String[] args) {
		System.out.println("PrivIoT - Client (version " + version + ")");
		
		System.out.println("Configure logging");
		try {
            configureLogging();
        } catch (MalformedURLException | FactoryConfigurationError e) {
            e.printStackTrace();
        }
		System.out.println("Configure logging: done");		
		
		log.info("start controller");
		boolean success = startController();
		if (success) {
			log.info("start controller: done");
		}
		else {
			log.error("start controller: failed");
		}
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
	
	private static boolean startController() {
	    Controller controller = new Controller(SENSOR_URI, SENSOR_SECRET, SENSOR_UPDATE_INTERVAL, SSP_HTTP_HOST, SSP_HTTP_REQUEST_PATH, SSP_HTTP_PORT);
	    
	    return controller.start();
	}
}
