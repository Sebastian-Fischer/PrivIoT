package priviot.data_origin.controller;

import priviot.utils.data.RDFData;
import priviot.utils.data.RDFSensorData;
import priviot.utils.data.transfer.SimpleSensorDataPackage;
import priviot.data_origin.data.SensorData;
import priviot.data_origin.data.SimpleIntegerSensorData;
import priviot.data_origin.sensor.SimpleIntegerSensor;
import priviot.data_origin.service.TCPService;

/**
 * Controller, that combines the SimpleIntegerSensor with the TCPService.
 */
public class SimpleIntegerSensorTCPServiceController extends Controller {

	/** URI of the sensor */
	private static final String sensorURI = "sensor.de/sensor1";
	/** frequency in which new values are published by the sensor in seconds */
	private static final int updateFrequency = 3;
	/** TCP port, the server of the TCPService is listen to. */
	private static final int port = 12345;
	
	/** The TCPService that handles the communication with clients */
	private TCPService tcpService;
	
	public SimpleIntegerSensorTCPServiceController() {
	}
	
	@Override
	public void initialize() {		
		SimpleIntegerSensor simpleIntegerSensor = new SimpleIntegerSensor(sensorURI, updateFrequency);
		simpleIntegerSensor.addObserver(this);
		
		sensor = simpleIntegerSensor;
		
		tcpService = new TCPService(port);
		
		tcpService.startService();
	}

	@Override
	public void receivedSensorData(SensorData data) {
		if (!(data instanceof SimpleIntegerSensorData)) {
			return;
		}
		
		// cast sensor data
		SimpleIntegerSensorData simpleIntegerSensorData = (SimpleIntegerSensorData)data;
		
		// create a data package to encapsulate data
		SimpleSensorDataPackage dataPackage = new SimpleSensorDataPackage();
		
		// transform data to RDFSensorData
		RDFSensorData rdfSensorData = new RDFSensorData();
		rdfSensorData.addRDFData(new RDFData(simpleIntegerSensorData.getSensorURI(), "hasValue", Integer.toString(simpleIntegerSensorData.getData())));
		rdfSensorData.setLifetime(data.getLifetime());
		
		// put transformed data into package
		dataPackage.setSensorData(rdfSensorData);
		
		// pass data package to service
		tcpService.publishData(dataPackage);
	}

}
