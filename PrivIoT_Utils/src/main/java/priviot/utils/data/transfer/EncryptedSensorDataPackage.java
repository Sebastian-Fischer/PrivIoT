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
    
    /** Asymmetric encryption method used to encrypt the key */
    private String asymmetricEncryptionMethod = "";
    /** Bit strength used with asymmetricEncryptionMethod to encrypt the key */
    private int asymmetricEncryptionBitStrength = 0;
    
    /** Symmetric encryption method used to encrypt the content */
    private String symmetricEncryptionMethod = "";
    /** Bit strength used with symmetricEncryptionMethod to encrypt the content */
    private int symmetricEncryptionBitStrength = 0;
    
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
    public static EncryptedSensorDataPackage createInstanceFromXMLString(String xmlString) throws NumberFormatException, 
     SAXException, DataPackageParsingException {
        EncryptedSensorDataPackage dataPackage = new EncryptedSensorDataPackage();
        dataPackage.fromXMLString(xmlString);
        
        return dataPackage;
    }

    public String getAsymmetricEncryptionMethod() {
        return asymmetricEncryptionMethod;
    }

    public void setAsymmetricEncryptionMethod(String asymmetricEncryptionMethod) {
        this.asymmetricEncryptionMethod = asymmetricEncryptionMethod;
    }
    
    public String getSymmetricEncryptionMethod() {
        return symmetricEncryptionMethod;
    }

    public void setSymmetricEncryptionMethod(String symmetricEncryptionMethod) {
        this.symmetricEncryptionMethod = symmetricEncryptionMethod;
    }

    public int getAsymmetricEncryptionBitStrength() {
        return asymmetricEncryptionBitStrength;
    }

    public void setAsymmetricEncryptionBitStrength(int asymmetricEncryptionBitStrength) {
        this.asymmetricEncryptionBitStrength = asymmetricEncryptionBitStrength;
    }
    
    public int getSymmetricEncryptionBitStrength() {
        return symmetricEncryptionBitStrength;
    }

    public void setSymmetricEncryptionBitStrength(int symmetricEncryptionBitStrength) {
        this.symmetricEncryptionBitStrength = symmetricEncryptionBitStrength;
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
     *   <asymmetricEncryptionMethod>[asymmetricEncryptionMethod}</asymmetricEncryptionMethod>
     *   <asymmetricEncryptionBitStrength>[asymmetricEncryptionBitStrength]</asymmetricEncryptionBitStrength>
     *   <symmetricEncryptionMethod>[symmetricEncryptionMethod]</symmetricEncryptionMethod>
     *   <symmetricEncryptionBitStrength>[symmetricEncryptionBitStrength]</symmetricEncryptionBitStrength>
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
        
        Element elementAsymmetricEncryptionMethod = document.createElement("asymmetricEncryptionMethod");
        elementAsymmetricEncryptionMethod.appendChild( document.createTextNode(asymmetricEncryptionMethod) );
        rootElement.appendChild(elementAsymmetricEncryptionMethod);
        
        Element elementAsymmetricEncryptionBitStrength = document.createElement("asymmetricEncryptionBitStrength");
        elementAsymmetricEncryptionBitStrength.appendChild( document.createTextNode(String.valueOf(asymmetricEncryptionBitStrength)) );
        rootElement.appendChild(elementAsymmetricEncryptionBitStrength);
        
        Element elementSymmetricEncryptionMethod = document.createElement("symmetricEncryptionMethod");
        elementSymmetricEncryptionMethod.appendChild( document.createTextNode(symmetricEncryptionMethod) );
        rootElement.appendChild(elementSymmetricEncryptionMethod);
        
        Element elementSymmetricEncryptionBitStrength = document.createElement("symmetricEncryptionBitStrength");
        elementSymmetricEncryptionBitStrength.appendChild( document.createTextNode(String.valueOf(symmetricEncryptionBitStrength)) );
        rootElement.appendChild(elementSymmetricEncryptionBitStrength);
        
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
     *   <asymmetricEncryptionMethod>[asymmetricEncryptionMethod}</asymmetricEncryptionMethod>
     *   <asymmetricEncryptionBitStrength>[asymmetricEncryptionBitStrength]</asymmetricEncryptionBitStrength>
     *   <symmetricEncryptionMethod>[symmetricEncryptionMethod]</symmetricEncryptionMethod>
     *   <symmetricEncryptionBitStrength>[symmetricEncryptionBitStrength]</symmetricEncryptionBitStrength>
     *   <encryptedContent>[encryptedContent]</encryptedContent>
     *   <encryptedInitializationVector>[encryptedInitializationVector]</encryptedInitializationVector>
     *   <encryptedKey>[encryptedKey]</encryptedKey>
     * </encrypted>
     * Here <name> is a XML-Tag and [name] is the content of an attribute.
     * @param xmlString
     * @throws SAXException, NumberFormatException
     */
    private boolean fromXMLString(String xmlString) throws SAXException, 
     NumberFormatException, DataPackageParsingException {
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
            throw new DataPackageParsingException("Empty xml document");
        }
        
        Node rootElement = document.getChildNodes().item(0);
        
        if (rootElement.getChildNodes().getLength() != 7) {
            throw new DataPackageParsingException("Wrong number of child nodes in root element");
        }
        if (!"asymmetricEncryptionMethod".equals(rootElement.getChildNodes().item(0).getNodeName())) {
            throw new DataPackageParsingException("Excepted node asymmetricEncryptionMethod");
        }
        if (!"asymmetricEncryptionBitStrength".equals(rootElement.getChildNodes().item(1).getNodeName())) {
            throw new DataPackageParsingException("Excepted node asymmetricEncryptionBitStrength");
        }
        if (!"symmetricEncryptionMethod".equals(rootElement.getChildNodes().item(2).getNodeName())) {
            throw new DataPackageParsingException("Excepted node symmetricEncryptionMethod");
        }
        if (!"symmetricEncryptionBitStrength".equals(rootElement.getChildNodes().item(3).getNodeName())) {
            throw new DataPackageParsingException("Excepted node symmetricEncryptionBitStrength");
        }
        if (!"encryptedContent".equals(rootElement.getChildNodes().item(4).getNodeName())) {
            throw new DataPackageParsingException("Excepted node encryptedContent");
        }
        if (!"encryptedInitializationVector".equals(rootElement.getChildNodes().item(5).getNodeName())) {
            throw new DataPackageParsingException("Excepted node encryptedInitializationVector");
        }
        if (!"encryptedKey".equals(rootElement.getChildNodes().item(6).getNodeName())) {
            throw new DataPackageParsingException("Excepted node encryptedKey");
        }
        
        Node elementAsymmetricEncryptionMethod = rootElement.getChildNodes().item(0);
        Node elementAsymmetricEncryptionBitStrength = rootElement.getChildNodes().item(1);
        Node elementSymmetricEncryptionMethod = rootElement.getChildNodes().item(2);
        Node elementSymmetricEncryptionBitStrength = rootElement.getChildNodes().item(3);
        Node elementEncryptedContent = rootElement.getChildNodes().item(4);
        Node elementEncryptedIV = rootElement.getChildNodes().item(5);
        Node elementEncryptedKey = rootElement.getChildNodes().item(6);
        
        if (elementAsymmetricEncryptionMethod.getChildNodes().getLength() > 1) {
            throw new DataPackageParsingException("Unexpected content in node asymmetricEncryptionMethod");
        }
        if (elementAsymmetricEncryptionBitStrength.getChildNodes().getLength() != 1) {
            throw new DataPackageParsingException("Unexpected content in node asymmetricEncryptionBitStrength");
        }
        if (elementSymmetricEncryptionMethod.getChildNodes().getLength() > 1) {
            throw new DataPackageParsingException("Unexpected content in node symmetricEncryptionMethod");
        }
        if (elementSymmetricEncryptionBitStrength.getChildNodes().getLength() != 1) {
            throw new DataPackageParsingException("Unexpected content in node symmetricEncryptionBitStrength");
        }
        if (elementEncryptedContent.getChildNodes().getLength() != 1) {
            throw new DataPackageParsingException("Unexpected content in node encryptedContent");
        }
        if (elementEncryptedIV.getChildNodes().getLength() != 1) {
            throw new DataPackageParsingException("Unexpected content in node encryptedInitializationVector");
        }
        if (elementEncryptedKey.getChildNodes().getLength() != 1) {
            throw new DataPackageParsingException("Unexpected content in node encryptedKey");
        }
        
        asymmetricEncryptionMethod = elementAsymmetricEncryptionMethod.getTextContent();
        asymmetricEncryptionBitStrength = Integer.parseInt(elementAsymmetricEncryptionBitStrength.getTextContent());
        symmetricEncryptionMethod = elementSymmetricEncryptionMethod.getTextContent();
        symmetricEncryptionBitStrength = Integer.parseInt(elementSymmetricEncryptionBitStrength.getTextContent());
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
