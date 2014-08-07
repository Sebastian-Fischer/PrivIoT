package priviot.coapwebserver;

import org.apache.log4j.Logger;

import priviot.coapwebserver.controller.CoapWebserverController;

public class DataOriginMain {

	private static String version = "0.1";
	
	private static Logger log = Logger.getLogger(DataOriginMain.class.getName());
	
	private static String urlSSP = "localhost";
	private static int portSSP = 8080;
	private static String urlCPP = "localhost";
	private static int portCPP = 8081;
	
	public static void main(String[] args) {
		System.out.println("PrivIoT - Data Origin (version " + version + ")");
		
		System.out.println("Configure logging");
		configureLogging();
		System.out.println("Configure logging: done");		
		
		log.info("start controller");
		startController();
		log.info("start controller: done");
	}
	
	private static void configureLogging() {
		org.apache.log4j.BasicConfigurator.configure();
	}
	
	private static void startController() {
	    CoapWebserverController controller = new CoapWebserverController(urlSSP, portSSP, urlCPP, portCPP);
	    
	    controller.start();
	}

}
