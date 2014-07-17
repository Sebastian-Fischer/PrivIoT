package priviot.utils.data.transfer;

import java.io.ByteArrayOutputStream;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;


/**
 * A DataPackage that simply contains the sensor data.
 */
public class RdfModelDataPackage implements DataPackage {

	private static final long serialVersionUID = 1L;
	
	private Logger log = Logger.getLogger(RdfModelDataPackage.class.getName());
	
	/** The data set of this package */
	private Model rdfModel;
	
	public RdfModelDataPackage() {
	}
	
	public RdfModelDataPackage(Model rdfModel) {
	    this.rdfModel = rdfModel;
	}

	/** Returns the data */
	public Model getRdfModel() {
		return rdfModel;
	}

	/** Sets the data */
	public void setRdfModel(Model rdfModel) {
		this.rdfModel = rdfModel;
	}

	@Override
	public String toUTF8() {
		String rdfModelString = "";
	
		if (rdfModel != null) {
            try {
    	        ByteArrayOutputStream outStream = new ByteArrayOutputStream((int)rdfModel.size());
    	        rdfModel.write(outStream, "TURTLE");
    	        rdfModelString  = outStream.toString();
            }
            catch (Exception e) {
            	log.error("Exception during utf8 serialization: " + e.getMessage());
            }
		}
		else {
		    rdfModelString = "null";
		}
        
		return "RdfModelDataPackage:\n" + rdfModelString;
	}

	@Override
	public String toXML() {
		String rdfModelString = "";
		
		if (rdfModel != null) {
            try {
    	        ByteArrayOutputStream outStream = new ByteArrayOutputStream((int)rdfModel.size());
    	        rdfModel.write(outStream, "RDF/XML");
    	        rdfModelString  = outStream.toString();
            }
            catch (Exception e) {
            	log.error("Exception during utf8 serialization: " + e.getMessage());
            }
		}
		else {
		    rdfModelString = "null";
		}
        
        return "<RdfModelDataPackage>\n" + rdfModelString + "\n</RdfModelDataPackage>";
	}
	
}
