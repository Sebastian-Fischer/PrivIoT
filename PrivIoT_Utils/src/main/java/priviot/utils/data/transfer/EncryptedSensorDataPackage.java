package priviot.utils.data.transfer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import org.apache.commons.codec.binary.Base64;

/**
 * Represents a data package with encrypted sensor data.
 */
public class EncryptedSensorDataPackage {
    
    /** Method used to encrypt the content */
    private String encryptionMethod = "";
    /** Bit strength used with encryptionMethod to encrypt the content */
    private int encryptionBitStrength = 0;
    
    /** 
     * The content, encrypted with the symmetric key using encrpytionMethod  
     */
    private byte[] encryptedContent = new byte[0];
    
    /** 
     * The initialization vector used to encrypt the content. 
     * The initialization vector is encrypted with the public key of the receiver 
     */
    private byte[] encryptedInitializationVector = new byte[0];
    
    /** 
     * The symmetric key used to encrypt the content. 
     * The key is encrypted with the public key of the receiver. 
     */
    private byte[] encryptedKey = new byte[0];

        
    public EncryptedSensorDataPackage() {
    }
    
    /**
     * Creates an EncryptedSensorDataPackage from a xml string.
     * @throws SAXException 
     * @throws NumberFormatException 
     */
    public EncryptedSensorDataPackage(String xmlString) throws NumberFormatException, SAXException {
        fromXMLString(xmlString);
    }

    public String getEncryptionMethod() {
        return encryptionMethod;
    }

    public void setEncryptionMethod(String encryptionMethod) {
        this.encryptionMethod = encryptionMethod;
    }

    public int getEncryptionBitStrength() {
        return encryptionBitStrength;
    }

    public void setEncryptionBitStrength(int encryptionBitStrength) {
        this.encryptionBitStrength = encryptionBitStrength;
    }

    public byte[] getEncryptedContent() {
        return encryptedContent;
    }

    public void setEncryptedContent(byte[] encryptedContent) {
        this.encryptedContent = encryptedContent;
    }
    
    public byte[] getEncryptedInitializationVector() {
        return encryptedInitializationVector;
    }

    public void setEncryptedInitializationVector(
            byte[] encryptedInitializationVector) {
        this.encryptedInitializationVector = encryptedInitializationVector;
    } 

    public byte[] getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(byte[] encryptedKey) {
        this.encryptedKey = encryptedKey;
    }
    
    /**
     * Returns the data of the object as byte array in the following form:
     * <encrypted>
     *   <encryptionMethod>[encryptionMethod]</encryptionMethod>
     *   <encryptionBitStrength>[encryptionBitStrength]</encryptionBitStrength>
     *   <encryptedContent>[encryptedContent]</encryptedContent>
     *   <encryptedInitializationVector>[encryptedInitializationVector]</encryptedInitializationVector>
     *   <encryptedKey>[encryptedKey]</encryptedKey>
     * </encrypted>
     * Here <name> is a XML-Tag and [name] is the content of an attribute.
     * @return
     */
    public String toXMLString() throws DOMException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return "";
        }
        Document document = builder.newDocument();
        
        Element rootElement = document.createElement("encrypted");
        
        Element elementEncryptionMethod = document.createElement("encryptionMethod");
        elementEncryptionMethod.appendChild( document.createTextNode(getEncryptionMethod()) );
        rootElement.appendChild(elementEncryptionMethod);
        
        Element elementEncryptionBitStrength = document.createElement("encryptionBitStrength");
        elementEncryptionBitStrength.appendChild( document.createTextNode(String.valueOf(getEncryptionBitStrength())) );
        rootElement.appendChild(elementEncryptionBitStrength);
        
        Element elementEncryptedContent = document.createElement("encryptedContent");
        elementEncryptedContent.appendChild( document.createTextNode(Base64.encodeBase64String(encryptedContent)) );
        rootElement.appendChild(elementEncryptedContent);
        
        Element elementEncryptedIV = document.createElement("encryptedInitializationVector");
        elementEncryptedIV.appendChild( document.createTextNode(Base64.encodeBase64String(encryptedInitializationVector)) );
        rootElement.appendChild(elementEncryptedIV);
        
        Element elementEncryptedKey = document.createElement("encryptedKey");
        elementEncryptedKey.appendChild( document.createTextNode(Base64.encodeBase64String(encryptedKey)) );
        rootElement.appendChild(elementEncryptedKey);
        
        document.appendChild(rootElement);
        
        return getStringFromDocument(document);
    }
    
    /**
     * Creates an EncryptedSensorDataPackage from a string containing xml data in the format:
     * <encrypted>
     *   <encryptionMethod>[encryptionMethod]</encryptionMethod>
     *   <encryptionBitStrength>[encryptionBitStrength]</encryptionBitStrength>
     *   <encryptedContent>[encryptedContent]</encryptedContent>
     *   <encryptedInitializationVector>[encryptedInitializationVector]</encryptedInitializationVector>
     *   <encryptedKey>[encryptedKey]</encryptedKey>
     * </encrypted>
     * Here <name> is a XML-Tag and [name] is the content of an attribute.
     * @param xmlString
     * @throws SAXException, NumberFormatException
     */
    private boolean fromXMLString(String xmlString) throws SAXException, NumberFormatException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return false;
        }
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlString.getBytes());
        Document document;
        try {
            document = builder.parse( inputStream );
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
        if (document == null) {
            return false;
        }
        
        if (document.getChildNodes().getLength() != 1) {
            return false;
        }
        
        Node rootElement = document.getChildNodes().item(0);
        
        if (rootElement.getChildNodes().getLength() != 5) {
            return false;
        }
        if (!"encryptionMethod".equals(rootElement.getChildNodes().item(0).getNodeName())) {
            return false;
        }
        if (!"encryptionBitStrength".equals(rootElement.getChildNodes().item(1).getNodeName())) {
            return false;
        }
        if (!"encryptedContent".equals(rootElement.getChildNodes().item(2).getNodeName())) {
            return false;
        }
        if (!"encryptedInitializationVector".equals(rootElement.getChildNodes().item(3).getNodeName())) {
            return false;
        }
        if (!"encryptedKey".equals(rootElement.getChildNodes().item(4).getNodeName())) {
            return false;
        }
        
        Node elementEncryptionMethod = rootElement.getChildNodes().item(0);
        Node elementEncryptionBitStrength = rootElement.getChildNodes().item(1);
        Node elementEncryptedContent = rootElement.getChildNodes().item(2);
        Node elementEncryptedIV = rootElement.getChildNodes().item(3);
        Node elementEncryptedKey = rootElement.getChildNodes().item(4);
        
        if (elementEncryptionMethod.getChildNodes().getLength() != 1) {
            return false;
        }
        if (elementEncryptionBitStrength.getChildNodes().getLength() != 1) {
            return false;
        }
        if (elementEncryptedContent.getChildNodes().getLength() != 1) {
            return false;
        }
        if (elementEncryptedIV.getChildNodes().getLength() != 1) {
            return false;
        }
        if (elementEncryptedKey.getChildNodes().getLength() != 1) {
            return false;
        }
        
        encryptionMethod = elementEncryptionMethod.getTextContent();
        encryptionBitStrength = Integer.parseInt(elementEncryptionBitStrength.getTextContent());
        encryptedContent = Base64.decodeBase64(elementEncryptedContent.getTextContent());
        encryptedInitializationVector = Base64.decodeBase64(elementEncryptedIV.getTextContent());
        encryptedKey = Base64.decodeBase64(elementEncryptedKey.getTextContent());
        
        return true;
    }
    
    private String getStringFromDocument(Document doc)
    {
        try
        {
           DOMSource domSource = new DOMSource(doc);
           StringWriter writer = new StringWriter();
           StreamResult result = new StreamResult(writer);
           TransformerFactory tf = TransformerFactory.newInstance();
           Transformer transformer = tf.newTransformer();
           transformer.transform(domSource, result);
           return writer.toString();
        }
        catch(TransformerException ex)
        {
           ex.printStackTrace();
           return null;
        }
    }
}
