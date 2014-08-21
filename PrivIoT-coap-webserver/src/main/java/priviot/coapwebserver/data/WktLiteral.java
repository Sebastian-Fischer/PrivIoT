package priviot.coapwebserver.data;

import java.math.BigDecimal;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.graph.impl.LiteralLabel;

/**
 * Custom Jena datatype to support WktLiterals ("POINT(10.695197 54.855072)"^^gsp:wktLiteral)
 * in RDF triples.
 */
public class WktLiteral extends BaseDatatype {
	public static final String TypeURI = "http://www.opengis.net/ont/geosparql#wktLiteral";

    public static final String CRS84 = "<http://www.opengis.net/def/crs/OGC/1.3/CRS84>";

    private static WktLiteral instance = new WktLiteral();
    
    public static WktLiteral getInstance() {
    	return instance;
    }
    
    private WktLiteral() {
        super(WktLiteral.TypeURI);
    }

    /**
     * Convert a value of this datatype out to lexical form.
     */
    public String unparse(Object value) {
        return value.toString();
    }

    /**
     * Parse a lexical form of this datatype to a value
     */
    public Object parse(String lexicalForm) {
        return new TypedValue(String.format("%s %s", WktLiteral.CRS84, lexicalForm), this.getURI());
    }

    /**
     * Compares two instances of values of the given datatype.
     * This does not allow rationals to be compared to other number
     * formats, Lang tag is not significant.
     *
     * @param value1 First value to compare
     * @param value2 Second value to compare
     * @return Value to determine whether both are equal.
     */
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
        return value1.getDatatype() == value2.getDatatype()
                && value1.getValue().equals(value2.getValue());
    }
    
    /**
     * Forms a WktLiteral value String in the form POINT(longitude latitude)
     * from given longitude and latitude.
     * The given values are rounded to 6 decimal places, because more precision is
     * not needed for a geographic point.
     * 
     * @param longitude Longitude coordinate of a geographic point
     * @param latitude  Latitude coordinate (parallel to the equator) of a geographic point
     * @return literal value string
     */
    public static String toValueString(double longitude, double latitude) {
    	// round the values to 6 decimal places
    	String longitudeStr = BigDecimal.valueOf(longitude).setScale(6, BigDecimal.ROUND_HALF_UP).toString();
    	String latitudeStr = BigDecimal.valueOf(latitude).setScale(6, BigDecimal.ROUND_HALF_UP).toString();
    	
    	return "POINT(" + longitudeStr + " " + latitudeStr + ")";
    }
}
