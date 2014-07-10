package priviot.utils.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data class that contains one data set of a sensor in RDF format.
 */
public class RDFSensorData implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/** The data set in RDF format */
	private List<RDFData> dataSet = new ArrayList<RDFData>();
	
	/** Lifetime of this data set */
	private int lifetime = 0;
	
	public RDFSensorData() {
	}

	/** Returns the whole data set */
	public List<RDFData> getRDFData() {
		return dataSet;
	}

	/** Sets the whole data set */
	public void setRDFData(List<RDFData> rdfDataSet) {
		this.dataSet = rdfDataSet;
	}
	
	/** Adds a RDFData object to the data set. */
	public void addRDFData(RDFData rdfData) {
		if (rdfData != null) {
			dataSet.add(rdfData);
		}
	}

	/** Returns the lifetime of this data set */
	public int getLifetime() {
		return lifetime;
	}

	/** Sets the lifetime of this data set */
	public void setLifetime(int lifetime) {
		this.lifetime = lifetime;
	}
	
	public String toString() {
		if (dataSet.isEmpty()) {
			return "RDFSensorData( no data , lifetime: " + lifetime + " sec. )";
		}
		else if (dataSet.size() == 1) {
			RDFData rdfData = dataSet.get(0);
			String rdfDataString = rdfData.getSubject() + " " + rdfData.getPredicate() + " " + rdfData.getObject();
			return "RDFSensorData( " + rdfDataString + ", lifetime: " + lifetime + " sec. )"; 
		}
		else {
			return "RDFSensorData( " + dataSet.size() + " rdf data, lifetime: " + lifetime + " sec. )";
		}
	}
}
