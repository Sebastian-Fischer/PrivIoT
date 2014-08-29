package de.uniluebeck.itm.priviot.coapwebserver.data;

public class GeographicSensorData extends SensorData {
	/** longitude coordinate of a geographic point */
	private double longitude;
	/** latitude coordinate (parallel to the equator) of a geographic point */
	private double latitude;
	
	public GeographicSensorData(String sensorURI, double longitude, double latitude) {
		setSensorUriPath(sensorURI);
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
}
