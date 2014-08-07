package priviot.coapwebserver.data;

/**
 * Sensor data set with a simple integer
 */
public class SimpleIntegerSensorData extends SensorData {

	/** data of this data set */
	private int data;
	
	/**
	 * Constructor.
	 * @param sensorURI The URI of the Sensor
	 */
	public SimpleIntegerSensorData(String sensorURI) {
		setSensorURI(sensorURI);
	}

	/** Returns the data */
	public int getData() {
		return data;
	}

	/** Sets the data */
	public void setData(int data) {
		this.data = data;
	}
}
