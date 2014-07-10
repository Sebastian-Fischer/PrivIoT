package priviot.utils.data.transfer;

import java.io.Serializable;

import priviot.utils.data.RDFSensorData;

/**
 * A DataPackage that simply contains the sensor data.
 */
public class SimpleSensorDataPackage extends DataPackage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** The data set of this package */
	private RDFSensorData sensorData;
	
	public SimpleSensorDataPackage() {
	}

	/** Returns the data */
	public RDFSensorData getSensorData() {
		return sensorData;
	}

	/** Sets the data */
	public void setSensorData(RDFSensorData sensorData) {
		this.sensorData = sensorData;
	}
	
	public String toString() {
		return "SimpleSensorDataPackage( " + sensorData.toString() + " )";
	}
	
}
