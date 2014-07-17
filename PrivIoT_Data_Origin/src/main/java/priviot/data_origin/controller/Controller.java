package priviot.data_origin.controller;

import priviot.data_origin.data.SensorData;
import priviot.data_origin.sensor.Sensor;
import priviot.data_origin.sensor.SensorObserver;

/**
 * Superclass for all Controllers.
 * 
 * The Controller connects Sensor with and Service.
 * Therefore it registers itself as a SensorObserver at the Sensor.
 * Every Data that is received from the Sensor is published to the Service.
 * 
 * A child class has to override receivedSensorData.
 */
public abstract class Controller implements SensorObserver {
	
	/** Sensor, that generates data or reads data from a sensor device */
	protected Sensor sensor;
	
	/** Thread in which the Sensor runs as Runnable */ 
	private Thread sensorThread;
	
	/**
	 * Initializes Service and Sensor.
	 */
	public abstract void initialize();
	
	/**
	 * Creates a thread for the Sensor and starts it.
	 * The Sensor is passed as Runnable.
	 */
	public void start() {
		sensorThread = new Thread(sensor);
		
		sensorThread.start();
	}
	
	/**
	 * Receives RDFData from the Sensor.
	 * Is called every time the Sensor publishes new data.
	 * The Controller has to pass this data to the service.
	 * @param data New RDFData of the Sensor
	 */
	public abstract void receivedSensorData(SensorData data);

	@Override
	public void publishData(SensorData data) {
		receivedSensorData(data);
	}
}
