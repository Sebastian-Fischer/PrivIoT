package de.uniluebeck.itm.priviot.utils.data;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.priviot.utils.data.generated.ObjectFactory;
import de.uniluebeck.itm.priviot.utils.data.generated.PrivacyDataPackage;


/**
 * Tool class to unmarshall the generated class {@link PrivacyDataPackage} 
 */
public class PrivacyDataPackageUnmarshaller {

    private static Logger log = LoggerFactory.getLogger(PrivacyDataPackageUnmarshaller.class.getName());
    private static Unmarshaller unmarshaller;

    static{
        try{
            JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
            unmarshaller = jaxbContext.createUnmarshaller();
        }
        catch(JAXBException ex){
            log.error("Exception during unmarshalling of PrivacyDataPackage", ex);
        }
    }

    public static synchronized PrivacyDataPackage unmarshal(final InputStream xmlStream) throws JAXBException, XMLStreamException {
        
        //create xml event reader for input stream
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(xmlStream);

        return unmarshaller.unmarshal(xmlEventReader, PrivacyDataPackage.class).getValue();
    }
}
