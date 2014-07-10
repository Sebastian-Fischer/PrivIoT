package priviot.data_origin.service;

import priviot.utils.data.transfer.DataPackage;

/**
 * Superclass for all Services.
 * 
 * A Service opens a server and allows clients to register as observer.
 * Every new data that is available is sent to those clients.
 * 
 * Additionally a client can access the actual data set.
 * Therefore the Service every time holds the newest data set.
 */
public abstract class Service {
	protected DataPackage actualData;
	
	/**
	 * Is called every time new sensor data is received by a Controller.
	 * The Service has to publish the data to registered clients.
	 * The received data can be accessed in actualData.
	 */
	protected abstract void receivedSensorData();
	
	/**
	 * Notifies the Service, that a new data set is available.
	 * The Service will publish the data to registered clients.
	 * The new Data is saved at the Service as actual data set.
	 * @param newData The new Sensor data set
	 */
	public void publishData(DataPackage newData) {
		actualData = newData;
		receivedSensorData();
	}
	
	/**
	 * Starts the service.
	 */
	public abstract void startService();
}
