package priviot.coapwebserver.sensor;

import java.util.concurrent.ScheduledExecutorService;

import priviot.coapwebserver.data.SimpleIntegerSensorData;

/**
 * A Sensor with a simple integer as sensor data.
 * 
 * The sensor data is randomly constructed and a random value is added in each cycle.
 */
public class SimpleIntegerSensor extends Sensor {
	
	/** The actual data of the sensor */
	private int actualData = 0;

	/**
	 * Constructor
	 * @param sensorURI The URI of the Sensor
	 * @param updateFrequency The frequency in seconds in which new values are created and published
	 */
	public SimpleIntegerSensor(String sensorURI, int updateFrequency, ScheduledExecutorService executorService) {
	    setScheduledExecutorService(executorService);
		setSensorUriPath(sensorURI);
		setUpdateFrequency(updateFrequency);
		
		actualData = (int)(Math.round(Math.random() * 100));
	}
	
	@Override
	protected void getAndPublishSensorData() {
		// add +- 5
		actualData += 5 - (int)(Math.round(Math.random() * 10));
		
		// generate SensorData object
		SimpleIntegerSensorData sensorData = new SimpleIntegerSensorData(getSensorUriPath());
		sensorData.setData(actualData);
		sensorData.setLifetime(getUpdateFrequency());
		
		// publish SensorData
		notifyObservers(sensorData);
	}
	
}
