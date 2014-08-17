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

import priviot.utils.data.DataPackageParsingException;

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
    
    /** The lifetime of the content in seconds */
    private int contentLifetime = 0;
    
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
    
    public String getAsymmetricEncryptionMethodShort() {
    	String[] parts = asymmetricEncryptionMethod.split("/");
    	if (parts.length == 0) {
    		return "";
    	}
    	else {
    		return parts[0];
    	}
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
    
    public int getContentLifetime() {
        return contentLifetime;
    }

    public void setContentLifetime(int contentLifetime) {
        this.contentLifetime = contentLifetime;
    }
    
    /**
     * Returns the data of the object as byte array in the following form:
     * <encrypted>
     *   <asymmetricEncryptionMethod>[asymmetricEncryptionMethod}</asymmetricEncryptionMethod>
     *   <asymmetricEncryptionBitStrength>[asymmetricEncryptionBitStrength]</asymmetricEncryptionBitStrength>
     *   <symmetricEncryptionMethod>[symmetricEncryptionMethod]</symmetricEncryptionMethod>
     *   <symmetricEncryptionBitStrength>[symmetricEncryptionBitStrength]</symmetricEncryptionBitStrength>
     *   <contentLifetime>[contentLifetime]<contentLifetime>
     *   <encryptedContent>[encryptedContent]</encryptedContent>
     *   <encryptedInitializationVector>[encryptedInitializationVector]</encryptedInitializationVector>
     *   <encryptedKey>[encryptedKey]</encryptedKey>
     * </encrypted>
     * Here <name> is a XML-Tag and [name] is the content of an attribute.
     * @return
     * @throws DOMException
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
        
        Element elementContentLifetime = document.createElement("contentLifetime");
        elementContentLifetime.appendChild( document.createTextNode(Integer.toString(contentLifetime)));
        rootElement.appendChild(elementContentLifetime);
        
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
     *   <contentLifetime>[contentLifetime]<contentLifetime>
     *   <encryptedContent>[encryptedContent]</encryptedContent>
     *   <encryptedInitializationVector>[encryptedInitializationVector]</encryptedInitializationVector>
     *   <encryptedKey>[encryptedKey]</encryptedKey>
     * </encrypted>
     * Here <name> is a XML-Tag and [name] is the content of an attribute.
     * @param xmlString
     * @throws SAXException, NumberFormatException, DataPackageParsingException
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
        
        document.getDocumentElement().normalize();
        
        Node rootElement = document.getChildNodes().item(0);
        
        Node elementAsymmetricEncryptionMethod = null;
        Node elementAsymmetricEncryptionBitStrength = null;
        Node elementSymmetricEncryptionMethod = null;
        Node elementSymmetricEncryptionBitStrength = null;
        Node elementContentLifetime = null;
        Node elementEncryptedContent = null;
        Node elementEncryptedIV = null;
        Node elementEncryptedKey = null;
        
        for (int i = 0; i < rootElement.getChildNodes().getLength(); i++) {
            Node node = rootElement.getChildNodes().item(i);
            
            if ("asymmetricEncryptionMethod".equals(node.getNodeName())) {
                elementAsymmetricEncryptionMethod = node;
            }
            else if ("asymmetricEncryptionBitStrength".equals(node.getNodeName())) {
                elementAsymmetricEncryptionBitStrength = node;
            }
            else if ("symmetricEncryptionMethod".equals(node.getNodeName())) {
                elementSymmetricEncryptionMethod = node;
            }
            else if ("symmetricEncryptionBitStrength".equals(node.getNodeName())) {
                elementSymmetricEncryptionBitStrength = node;
            }
            else if ("contentLifetime".equals(node.getNodeName())) {
                elementContentLifetime = node;
            }
            else if ("encryptedContent".equals(node.getNodeName())) {
                elementEncryptedContent = node;
            }
            else if ("encryptedInitializationVector".equals(node.getNodeName())) {
                elementEncryptedIV = node;
            }
            else if ("encryptedKey".equals(node.getNodeName())) {
                elementEncryptedKey = node;
            }
        }
        
        if (elementAsymmetricEncryptionMethod == null) {
            throw new DataPackageParsingException("Missing node: asymmetricEncryptionMethod");
        }
        if (elementAsymmetricEncryptionBitStrength == null) {
            throw new DataPackageParsingException("Missing node: asymmetricEncryptionBitStrength");
        }
        if (elementSymmetricEncryptionMethod == null) {
            throw new DataPackageParsingException("Missing node: symmetricEncryptionMethod");
        }
        if (elementSymmetricEncryptionBitStrength == null) {
            throw new DataPackageParsingException("Missing node: symmetricEncryptionBitStrength");
        }
        if (elementContentLifetime == null) {
            throw new DataPackageParsingException("Missing node: contentLifetime");
        }
        if (elementEncryptedContent == null) {
            throw new DataPackageParsingException("Missing node: encryptedContent");
        }
        if (elementEncryptedIV == null) {
            throw new DataPackageParsingException("Missing node: encryptedInitializationVector");
        }
        if (elementEncryptedKey == null) {
            throw new DataPackageParsingException("Missing node: encryptedKey");
        }
        
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
        if (elementContentLifetime.getChildNodes().getLength() != 1) {
            throw new DataPackageParsingException("Unexpected content in node contentLifetime");
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
        contentLifetime = Integer.parseInt(elementContentLifetime.getTextContent());
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
           transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
           transformer.setOutputProperty(OutputKeys.METHOD, "xml");
           transformer.setOutputProperty(OutputKeys.INDENT, "yes");
           transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
           transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
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
