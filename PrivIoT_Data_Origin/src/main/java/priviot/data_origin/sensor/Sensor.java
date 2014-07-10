package priviot.data_origin.sensor;

import java.util.ArrayList;
import java.util.List;

import priviot.data_origin.data.SensorData;

/**
 * Superclass for all Sensors.
 * 
 * A Sensor frequently publishes RDFData to registered SensorObservers.
 * 
 * A child class has to set updateFrequency and sensorURI.
 * A child class has to implement getAndPublicSensorData.
 */
public abstract class Sensor implements Runnable {
	/** List of Observers for the Sensor */
	private List<SensorObserver> observers = new ArrayList<SensorObserver>();
	
	/**
	 * Specifies the frequency in which updates of RDFData will be published by this Sensor.
	 * The value is given in seconds.
	 */
	private int updateFrequency = 0;
	
	private String sensorURI = "";
	
	/**
	 * Adds an observer to the Sensor.
	 * The observer will frequently get information as RDFData.
	 * @param observer The SensorObserver to add
	 */
	public void addObserver(SensorObserver observer) {
		if (observer != null) {
			if (!observers.contains(observer)) {
				observers.add(observer);
			}
		}
	}
	
	/**
	 * Removes a registered observer from the List of Observers.
	 * @param observer The SensorObserver to remove
	 */
	public boolean removeObserver(SensorObserver observer) {
		return observers.remove(observer);
	}
	
 	/** 
	 * Returns the updateFrequency of this Sensor.
	 * The value specifies the frequency in which updates of RDFData will be published by this Sensor.
	 * It is given in seconds.
	 * @return The updateFrequency
	 */
	public int getUpdateFrequency() {
		return updateFrequency;
	}
	
	protected void setUpdateFrequency(int updateFrequency) {
		this.updateFrequency = updateFrequency;
	}
	
	/** Returns the URI of the sensor */
	public String getSensorURI() {
		return sensorURI;
	}
	
	/** Sets the URI of the sensor */
	protected void setSensorURI(String sensorURI) {
		this.sensorURI = sensorURI;
	}
	
	@Override
	public void run() {
		while(true) {
			getAndPublishSensorData();
			
			try {
				Thread.sleep(updateFrequency * 1000);
			} catch (InterruptedException e) {
				System.out.println("Sensor: Thread interrupted.");
			}
		}
	}
	
	/**
	 * Notifies the registered observers and publishes the new data to them.
	 * @param newData New RDFData of the Sensor.
	 */
	protected void notifyObservers(SensorData newData) {
		if (newData != null) {
			for (SensorObserver observer : observers) {
				observer.publishData(newData);
			}
		}
	}
	
	/**
	 * The sensor gets the data of the sensor device and publishes it.
	 */
	protected abstract void getAndPublishSensorData();
	
	
}
