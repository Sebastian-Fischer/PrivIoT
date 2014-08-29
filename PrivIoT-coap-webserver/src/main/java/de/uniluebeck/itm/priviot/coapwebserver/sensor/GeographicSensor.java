package de.uniluebeck.itm.priviot.coapwebserver.sensor;

import java.util.concurrent.ScheduledExecutorService;

import de.uniluebeck.itm.priviot.coapwebserver.data.GeographicSensorData;

/**
 * A simulated position sensor, that produces geographic points.
 */
public class GeographicSensor extends Sensor {

	/** longitude coordinate of a geographic point */
	private double longitude;
	/** latitude coordinate (parallel to the equator) of a geographic point */
	private double latitude;
	/** Maximum changing of the latitude and longitude in one step */
	private double maxChange;
	
	/**
	 * Constructor.
	 * Initializes the coordinate with a position in HL, Germany.
	 * Sets the maximum changing of latitude and longitude in one step to 0.001
	 * @param sensorURI The URI of the Sensor
	 * @param updateFrequency The frequency in seconds in which new values are created and published
	 * @param executorService Used to execute the changing and publishing of the sensor value 
	 */
	public GeographicSensor(String sensorURI, int updateFrequency, ScheduledExecutorService executorService) {
		setScheduledExecutorService(executorService);
		setSensorUriPath(sensorURI);
		setUpdateFrequency(updateFrequency);
		
		this.longitude = 53.8686906;
		this.latitude = 10.6802434;
		this.maxChange = 0.001;
	}
	
	@Override
	protected void getAndPublishSensorData() {
		longitude += Math.random() * maxChange;
		latitude += Math.random() * maxChange;
		
		GeographicSensorData sensorData = new GeographicSensorData(getSensorUriPath(), longitude, latitude);
		sensorData.setLifetime(getUpdateFrequency());
		
		// publish SensorData
		notifyObservers(sensorData);
	}

}
