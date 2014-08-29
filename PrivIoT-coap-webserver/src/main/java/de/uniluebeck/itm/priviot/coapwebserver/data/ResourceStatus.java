package de.uniluebeck.itm.priviot.coapwebserver.data;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Represents the status of a sensor.
 * Encapsulates a sensor's URI or URI pseudonym, a Jena RDF model and it's lifetime
 */
public class ResourceStatus {
	private String sensorUri;
    private Model rdfModel;
    private int lifetime;
    
    /**
     * Constructor
     * @param rdfModel  Jena RDF model 
     * @param lifetime  lifetime in seconds
     */
    public ResourceStatus(String sensorUri, Model rdfModel, int lifetime) {
    	this.sensorUri = sensorUri;
        this.rdfModel = rdfModel;
        this.lifetime = lifetime;
    }

    public String getSensorUri() {
        return sensorUri;
    }

    public void setSensorUri(String sensorUri) {
        this.sensorUri = sensorUri;
    }
    
    public Model getRdfModel() {
        return rdfModel;
    }

    public void setRdfModel(Model rdfModel) {
        this.rdfModel = rdfModel;
    }

	public int getLifetime() {
		return lifetime;
	}

	public void setLifetime(int lifetime) {
		this.lifetime = lifetime;
	}
}
