package de.uniluebeck.itm.priviot.cpp.data;

import java.net.URI;

import de.uniluebeck.itm.priviot.cpp.communication.smartserviceproxy.CoapForwardingWebservice;

/**
 * A part of the RegistryEntry.
 * Contains the URI of the Web service and the {@link CoAPForwardingWebservice} instance
 * of the CoAP Privacy Proxy, that forwards messages of the web service.
 */
public class WebserviceEntry {
	/**
	 * The URI of the CoAP-Webservice
	 */
	private URI webserviceUri;
	
	/**
	 * The {@link CoAPForwardingWebservice} instance, that forwards messages of the web service
	 */
	private CoapForwardingWebservice coapForwardingWebservice;
	
	public WebserviceEntry(URI webserviceUri, CoapForwardingWebservice coapForwardingWebservice) {
		this.setWebserviceUri(webserviceUri);
		this.setCoapForwardingWebservice(coapForwardingWebservice);
	}

	/**
	 * The URI of the CoAP-Webservice
	 * @return
	 */
	public URI getWebserviceUri() {
		return webserviceUri;
	}

	/**
	 * Sets the URI of the CoAP-Webservice
	 * @param webserviceUri
	 */
	public void setWebserviceUri(URI webserviceUri) {
		this.webserviceUri = webserviceUri;
	}

	/**
	 * The {@link CoAPForwardingWebservice} instance, that forwards messages of the web service
	 * @return
	 */
	public CoapForwardingWebservice getCoapForwardingWebservice() {
		return coapForwardingWebservice;
	}

	/**
	 * Sets the {@link CoAPForwardingWebservice} instance, that forwards messages of the web service
	 * @param coapForwardingWebservice
	 */
	public void setCoapForwardingWebservice(CoapForwardingWebservice coapForwardingWebservice) {
		this.coapForwardingWebservice = coapForwardingWebservice;
	}
	
}
