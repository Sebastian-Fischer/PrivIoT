package de.uniluebeck.itm.priviot.coapwebserver.sensor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.priviot.coapwebserver.data.SensorData;

/**
 * Superclass for all Sensors.
 * 
 * A Sensor frequently publishes RDFData to registered SensorObservers.
 * 
 * A child class has to set updateFrequency and sensorURI.
 * A child class has to implement getAndPublicSensorData.
 */
public abstract class Sensor {
	/** List of Observers for the Sensor */
	private List<SensorObserver> observers = new ArrayList<SensorObserver>();
	
	private ScheduledExecutorService scheduledExecutorService;
	
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	/**
	 * Specifies the frequency in which updates of RDFData will be published by this Sensor.
	 * The value is given in seconds.
	 */
	private int updateFrequency = 0;
	
	private String sensorUriPath = "";
	
	/** The secret of the sensor, used to create the pseudonyms */
	private byte[] secret;
	
	
	protected void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
	    this.scheduledExecutorService = scheduledExecutorService;
	}
	
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
	public String getSensorUriPath() {
		return sensorUriPath;
	}
	
	/** Sets the URI of the sensor */
	protected void setSensorUriPath(String sensorUriPath) {
		this.sensorUriPath = sensorUriPath;
	}
	
	/** Sets the secret of the sensor, used to create the pseudonyms */
	public byte[] getSecret() {
		return secret;
	}

	/** Sets the secret of the sensor, used to create the pseudonyms */
	public void setSecret(byte[] secret) {
		this.secret = secret;
	}
	
	/**
	 * Starts the sensor.
	 * From this moment on it will publish sensor values all updateFrequency seconds.
	 * Calls start(updateFrequency)
	 */
	public void start() {
		start(updateFrequency);
	}
	
	/**
	 * Starts the sensor.
	 * From this moment on it will publish sensor values all updateFrequency seconds.
	 * The sensor will first sleep until actual time is minimum minTimeDifference after the last update time.
	 * 
	 * @param minTimeDifference  Maximum time difference in seconds after the last update time. 
	 *                           If not needed set to updateFrequency.
	 */
	public void start(long maxTimeDifference) {
	    if (scheduledExecutorService == null) {
	        log.error("scheduledExecutorService not initialized. Can't start Sensor");
	    }
	    
	    // find a good start point
	    long sleepTime = 0;
	    long updateFrequencyMilli = updateFrequency * 1000;
	    long maxTimeDifferenceMilli = maxTimeDifference * 1000;
	    long modulo = (new Date()).getTime() % (updateFrequencyMilli);
	    
	    if (modulo > maxTimeDifferenceMilli) {
			sleepTime = (updateFrequencyMilli - modulo) + 1000; // 1000 for tolerance
			log.debug(modulo + " seconds after last update time. Sensor starts after " + sleepTime + " milliseconds");
		}
	    else {
	    	log.debug(modulo + " seconds after last update time.");
	    }
	    
	    scheduledExecutorService.scheduleAtFixedRate(new Runnable(){

            @Override
            public void run() {
                try{
                    getAndPublishSensorData();
                }
                catch(Exception e){
                    log.error("Exception while updating sensor value", e);
                }
            }
        },sleepTime, updateFrequencyMilli, TimeUnit.MILLISECONDS);
    }
	
	/**
	 * Notifies the registered observers and publishes the new data to them.
	 * @param newData New RDFData of the Sensor.
	 */
	protected void notifyObservers(SensorData newData) {
		if (newData != null) {
			for (SensorObserver observer : observers) {
				observer.publishData(this, newData);
			}
		}
	}
	
	/**
	 * The sensor gets the data of the sensor device and publishes it.
	 */
	protected abstract void getAndPublishSensorData();
	
}
