package de.uniluebeck.itm.priviot.coapwebserver.service;

import java.net.URI;
import java.security.cert.X509Certificate;

/**
 * Interface for observers to CoapRegisterClient
 */
public interface CoapRegisterClientObserver {
    public void receivedCertificate(URI fromUri, X509Certificate certificate);
}
