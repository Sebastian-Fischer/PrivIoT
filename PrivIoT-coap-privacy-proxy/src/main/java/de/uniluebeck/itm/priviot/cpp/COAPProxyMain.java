package de.uniluebeck.itm.priviot.cpp;

import java.io.File;
import java.net.MalformedURLException;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.priviot.cpp.controller.Controller;

public class COAPProxyMain {

	private final static String version = "0.1";
	
	private static final String LOG_CONFIG_FILE_NAME = "log4j.xml";
	private static final String CONFIG_FILE_NAME = "cpp.properties";
	
	
	private static Logger log = LoggerFactory.getLogger(COAPProxyMain.class.getName());
	
	private static Configuration config;
	
	
	public static void main(String[] args) {
		System.out.println("PrivIoT - COAP Proxy (version " + version + ")");
		
		System.out.println("Configure logging");
		try {
            configureLogging();
        } catch (MalformedURLException | FactoryConfigurationError e) {
            e.printStackTrace();
        }
		System.out.println("Configure logging: done");
		
		log.info("read configuration");
		try {
			config = new PropertiesConfiguration(CONFIG_FILE_NAME);
		} catch (ConfigurationException e) {
			log.error("error during configuration", e);
		}
		log.info("read configuration: done");
		
		log.info("start controller");
		Controller controller = new Controller(config);
		controller.start();
		log.info("start controller: done");
	}
	
	private static void configureLogging() throws MalformedURLException, FactoryConfigurationError {
	    File configFile = new File(LOG_CONFIG_FILE_NAME);
	    
	    if(configFile.exists()){
	        System.out.println("Configure logging from file '" + LOG_CONFIG_FILE_NAME + "'.");
	        DOMConfigurator.configure(configFile.toURI().toURL());
	    }
	    else {
	        System.out.println("No config file found. Use default configuration.");
	        BasicConfigurator.configure();
	    }
	}

}
