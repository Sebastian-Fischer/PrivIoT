package priviot.utils.data.transfer;

import java.io.Serializable;

/**
 * This DataPackage contains a encrypted version of the SensorData and the encrypted key.
 */
public class EncryptedSensorDataPackage extends DataPackage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** The data set of this package. Encrypted. */
	private byte[] encryptedSensorData;
	
	/** The key needed to decrypt encryptedSensorData. Encrypted with another secret key */
	private byte[] encryptedKey;
	
	public EncryptedSensorDataPackage() {
	}

	/** Returns the data */
	public byte[] getSensorData() {
		return encryptedSensorData;
	}

	/** Sets the data */
	public void setSensorData(byte[] encryptedSensorData) {
		this.encryptedSensorData = encryptedSensorData;
	}

	/** Returns the encrypted key needed to decrypt encryptedSensorData */
	public byte[] getEncryptedKey() {
		return encryptedKey;
	}

	/** Sets the encrypted key needed to decrypt encryptedSensorData */
	public void setEncryptedKey(byte[] encryptedKey) {
		this.encryptedKey = encryptedKey;
	}

	@Override
	public String toString() {
		return "EncryptedSensorDataPackage";
	}
}
