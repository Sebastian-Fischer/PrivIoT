package priviot.utils.data.transfer;

/**
 * This DataPackage contains a encrypted version of the SensorData and the encrypted key.
 */
public class EncryptedRdfModelDataPackage implements DataPackage {

	private static final long serialVersionUID = 1L;
	
	/** The data set of this package. Encrypted. */
	private byte[] encryptedSensorData;
	
	/** The key needed to decrypt encryptedSensorData. Encrypted with another secret key */
	private byte[] encryptedKey;
	
	public EncryptedRdfModelDataPackage() {
	}
	
	public EncryptedRdfModelDataPackage(byte[] encryptedSensorData, byte[] encryptedKey) {
	    this.encryptedSensorData = encryptedSensorData;
	    this.encryptedKey = encryptedKey;
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
	public String toUTF8() {
		return "EncryptedRdfModelDataPackage: ( " + 
	           "encryptedSensorData: '" + encryptedSensorData + "', " + 
			   "encryptedKey: '" + encryptedKey + "' )";
	}

	@Override
	public String toXML() {
		return "<EncryptedRdfModelDataPackage>\n" + 
	           "\t<encryptedSensorData>" + encryptedSensorData + "</encryptedSensorData>\n" + 
			   "\t<encryptedKey>" + encryptedKey + "</encryptedKey>\n" +
	           "</EncryptedRdfModelDataPackage>";
	}
}
