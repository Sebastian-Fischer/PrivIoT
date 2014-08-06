package priviot.coap_proxy;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import priviot.coap_proxy.controller.Controller;

public class COAPProxyMain {

	private static String version = "0.1";
	
	private static Logger log = LoggerFactory.getLogger(COAPProxyMain.class.getName());
	
	public static void main(String[] args) {
		System.out.println("PrivIoT - COAP Proxy (version " + version + ")");
		
		System.out.println("Configure logging");
		configureLogging();
		System.out.println("Configure logging: done");
		
		log.info("start controller");
		Controller controller = new Controller();
		controller.start();
		log.info("start controller: done");
	}
	
	private static void configureLogging() {
		BasicConfigurator.configure();
	}

}
