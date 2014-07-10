package priviot.data_origin.sensor;

import priviot.data_origin.data.SensorData;

/**
 * Interface for classes, that need to observe a Sensor.
 */
public interface SensorObserver {
	/**
	 * Sends new data from the Sensor to the SensorObserver.
	 * @param data The new Data as RDFData.
	 */
	void publishData(SensorData data);
}
