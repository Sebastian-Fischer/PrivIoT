package priviot.data_origin;

import org.apache.log4j.Logger;

import priviot.data_origin.controller.RDFSimpleIntegerSensorCoapController;

public class DataOriginMain {

	private static String version = "0.1";
	
	private static Logger log = Logger.getLogger(DataOriginMain.class.getName());
	
	public static void main(String[] args) {
		System.out.println("PrivIoT - Data Origin (version " + version + ")");
		
		System.out.println("Configure logging");
		configureLogging();
		System.out.println("Configure logging: done");		
		
		log.info("start controller");
		initializeSimpleIntegerCoapController();
		log.info("start controller: done");
	}
	
	private static void configureLogging() {
		org.apache.log4j.BasicConfigurator.configure();
	}
	
	private static void initializeSimpleIntegerCoapController() {
		RDFSimpleIntegerSensorCoapController controller = new RDFSimpleIntegerSensorCoapController();
		
		controller.initialize();
		
		controller.start();
	}

}
