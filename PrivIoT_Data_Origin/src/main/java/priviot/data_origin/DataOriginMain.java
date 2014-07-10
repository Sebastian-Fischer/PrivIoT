package priviot.data_origin;

import priviot.data_origin.controller.SimpleIntegerSensorTCPServiceController;

public class DataOriginMain {

	private static String version = "0.1";
	
	public static void main(String[] args) {
		System.out.println("PrivIoT - Data Origin (version " + version + ")");
		
		System.out.println("start SimpleIntegerSensor and TCPService...");
		initializeSimpleIntegerTCPServiceController();
		System.out.println("start SimpleIntegerSensor and TCPService - done");
	}
	
	private static void initializeSimpleIntegerTCPServiceController() {
		SimpleIntegerSensorTCPServiceController controller = new SimpleIntegerSensorTCPServiceController();
		
		controller.initialize();
		
		controller.start();
	}

}
