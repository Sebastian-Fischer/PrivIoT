package priviot.utils.data;

import java.io.Serializable;

/**
 * Data object for RDF data to be send over the network
 */
public class RDFData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String subject = "";
	private String predicate = "";
	private String object = "";
	
	public RDFData() {
	}
	
	public RDFData(String subject, String predicate, String object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getPredicate() {
		return predicate;
	}
	
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	
	public String getObject() {
		return object;
	}
	
	public void setObject(String object) {
		this.object = object;
	}
}
