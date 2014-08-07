package priviot.coapwebserver.data;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Encapsulates a Jena RDF model and it's lifetime
 */
public class JenaRdfModelWithLifetime {
    private Model rdfModel;
    private int lifetime;
    
    /**
     * Constructor
     * @param rdfModel  Jena RDF model 
     * @param lifetime  lifetime in seconds
     */
    public JenaRdfModelWithLifetime(Model rdfModel, int lifetime) {
        this.setRdfModel(rdfModel);
        this.setLifetime(lifetime);
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
