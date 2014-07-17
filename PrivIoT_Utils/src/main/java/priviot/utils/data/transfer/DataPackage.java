package priviot.utils.data.transfer;

import java.io.Serializable;

/**
 * A data package that can be transfered via network.
 */
public interface DataPackage extends Serializable {
	
	/**
	 * Transforms the DataPackage into a utf8 string
	 * @return
	 */
	String toUTF8();
	
	/**
	 * Transforms the DataPackage into a xml string
	 * @return
	 */
	String toXML();
}
