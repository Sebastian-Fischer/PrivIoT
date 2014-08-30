package de.uniluebeck.itm.priviot.utils.data;

import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import javanet.staxutils.IndentingXMLEventWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.priviot.utils.data.generated.ObjectFactory;
import de.uniluebeck.itm.priviot.utils.data.generated.PrivacyDataPackage;

/**
 * Tool class to marshall the generated class {@link PrivacyDataPackage} 
 */
public class PrivacyDataPackageMarshaller {

    private static Logger log = LoggerFactory.getLogger(PrivacyDataPackageMarshaller.class.getName());

    private static Marshaller marshaller;
    static{
        try{
            JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);

            marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        }
        catch(JAXBException ex){
            log.error("Exception during marshalling of PrivacyDataPackage", ex);
        }
    }

    public static void marshal(PrivacyDataPackage privacyDataPackage, OutputStream outputStream)
            throws JAXBException, XMLStreamException {

        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        IndentingXMLEventWriter xmlEventWriter =
                new IndentingXMLEventWriter(xmlOutputFactory.createXMLEventWriter(outputStream));

        marshaller.marshal(privacyDataPackage, xmlEventWriter);

    }
}
