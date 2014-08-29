package de.uniluebeck.itm.priviot.coapwebserver.sensor;

import de.uniluebeck.itm.priviot.coapwebserver.data.SensorData;

/**
 * Interface for classes, that need to observe a Sensor.
 */
public interface SensorObserver {
	/**
	 * Sends new data from the Sensor to the SensorObserver.
	 * @param data The new Data as RDFData.
	 */
	void publishData(Sensor sensor, SensorData data);
}
