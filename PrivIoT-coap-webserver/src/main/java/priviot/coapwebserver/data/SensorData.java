package priviot.coapwebserver.data;

/**
 * Superclass for a data set of a sensor.
 */
public abstract class SensorData {
	/** The URI of the sensor */
	protected String sensorUriPath;
	
	/** Lifetime of the data in seconds */
	protected int lifetime;
	
	/** Returns the URI of the sensor */
	public String getSensorUriPath() {
		return sensorUriPath;
	}
	
	/** Sets the URI of the sensor */
	public void setSensorUriPath(String sensorUriPath) {
		this.sensorUriPath = sensorUriPath;
	}
	
	/** Returns the lifetime of the data */
	public int getLifetime() {
		return lifetime;
	}
	
	/** Sets the lifetime of the data */
	public void setLifetime(int lifetime) {
		this.lifetime = lifetime;
	}
}
