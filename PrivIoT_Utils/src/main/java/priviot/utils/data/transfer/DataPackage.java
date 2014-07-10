package priviot.utils.data.transfer;

import java.io.Serializable;

/**
 * A data package that can be transfered via network.
 */
public abstract class DataPackage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public abstract String toString();
}
