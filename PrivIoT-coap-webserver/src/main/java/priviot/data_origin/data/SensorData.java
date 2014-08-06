package priviot.data_origin.data;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Superclass for a data set of a sensor.
 */
public abstract class SensorData {
	/** The URI of the sensor */
	protected String sensorURI;
	
	/** Lifetime of the data in seconds */
	protected int lifetime;
	
	/** Returns the URI of the sensor */
	public String getSensorURI() {
		return sensorURI;
	}
	
	/** Sets the URI of the sensor */
	public void setSensorURI(String sensorURI) {
		this.sensorURI = sensorURI;
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
