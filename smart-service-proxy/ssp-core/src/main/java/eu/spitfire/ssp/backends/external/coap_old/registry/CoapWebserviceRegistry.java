//package eu.spitfire.ssp.backends.external.coap.registry;
//
//import com.google.common.collect.Multimap;
//import com.google.common.util.concurrent.FutureCallback;
//import com.google.common.util.concurrent.Futures;
//import com.google.common.util.concurrent.ListenableFuture;
//import com.google.common.util.concurrent.SettableFuture;
//
//import de.uniluebeck.itm.ncoap.application.client.CoapClientApplication;
//import de.uniluebeck.itm.ncoap.application.server.CoapServerApplication;
//import de.uniluebeck.itm.ncoap.application.server.webservice.linkformat.EmptyLinkAttribute;
//import de.uniluebeck.itm.ncoap.application.server.webservice.linkformat.LinkAttribute;
//import de.uniluebeck.itm.ncoap.message.CoapRequest;
//import de.uniluebeck.itm.ncoap.message.MessageCode;
//import de.uniluebeck.itm.ncoap.message.MessageType;
//import de.uniluebeck.itm.ncoap.message.options.ContentFormat;
//
//import eu.spitfire.ssp.backends.external.coap.CoapBackendComponentFactory;
//import eu.spitfire.ssp.backends.external.coap.CoapWebserviceResponseProcessor;
//import eu.spitfire.ssp.backends.generic.Registry;
//import eu.spitfire.ssp.backends.generic.messages.InternalResourceStatusMessage;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.net.InetAddress;
//import java.net.InetSocketAddress;
//import java.net.URI;
//import java.net.URISyntaxException;
//
//import java.util.Collections;
//import java.util.Set;
//import java.util.TreeSet;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * Created with IntelliJ IDEA.
// * User: olli
// * Date: 18.09.13
// * Time: 18:30
// * To change this template use File | Settings | File Templates.
// */
//public class CoapRegistry extends Registry<URI> {
//
//    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
//
//    private CoapBackendComponentFactory backendComponentFactory;
//    private CoapClientApplication coapClientApplication;
//    private ExecutorService internalTasksExecutorService;
//
//    public CoapRegistry(CoapBackendComponentFactory backendComponentFactory) {
//        super(backendComponentFactory);
//        this.backendComponentFactory = backendComponentFactory;
//        this.coapClientApplication = backendComponentFactory.getCoapClientApplication();
//        this.internalTasksExecutorService = backendComponentFactory.getInternalTasksExecutor();
//
//        CoapRegistrationWebservice registrationWebservice = new CoapRegistrationWebservice(this, internalTasksExecutorService);
//        this.backendComponentFactory.getCoapServerApplication()
//                                    .registerService(registrationWebservice);
//
//        log.info("Registered CoAP registration Webservice ({})", registrationWebservice.getPath());
//    }
//
//
//    /**
//     * Invocation of this method causes the framework to send a CoAP request to the .well-known/core service
//     * at the given remote address on port 5683. All services from the list returned by that service are
//     * considered data origins to be registered at the SSP.
//     *
//     * @param remoteAddress the IP address of the host offering CoAP Webservices as data origins
//     */
//    public ListenableFuture<Set<URI>> processRegistration(final InetAddress remoteAddress){
//
//        try{
//            log.info("Process registration request from {}.", remoteAddress.getHostAddress());
//
//            final SettableFuture<Set<URI>> result = SettableFuture.create();
//
//             //Send request for .well-known/core resource
//            WellKnownCoreProcessor responseProcessor = new WellKnownCoreProcessor(internalTasksExecutorService);
//            sendWellKnownCoreRequest(remoteAddress, CoapServerApplication.DEFAULT_COAP_SERVER_PORT, responseProcessor);
//
//            final ListenableFuture<Multimap<String, LinkAttribute>> wellKnownCoreFuture =
//                    responseProcessor.getWellKnownCoreFuture();
//
//            //register the resources from all services listed in .well-known/core
//            Futures.addCallback(wellKnownCoreFuture, new FutureCallback<Multimap<String, LinkAttribute>>() {
//
//                @Override
//                public void onSuccess(Multimap<String, LinkAttribute> webservices) {
//
//                    SettableFuture<Set<URI>> resourceRegistrationFuture =
//                            registerResources(webservices, remoteAddress.getHostAddress());
//
//                    Futures.addCallback(resourceRegistrationFuture, new FutureCallback<Set<URI>>() {
//
//                        @Override
//                        public void onSuccess(Set<URI> uris) {
//                            result.set(uris);
//                        }
//
//                        @Override
//                        public void onFailure(Throwable throwable) {
//                            result.setException(throwable);
//                        }
//
//                    }, internalTasksExecutorService);
//                }
//
//                @Override
//                public void onFailure(Throwable throwable) {
//                    result.setException(throwable);
//                }
//
//            });
//
//            return result;
//        }
//
//        catch(Exception ex){
//            SettableFuture<Set<URI>> future = SettableFuture.create();
//            future.setException(ex);
//            return future;
//        }
//
//    }
//
//
////    private ListenableFuture<Multimap<String, LinkAttribute>> discoverWebservices(InetAddress remoteAddress)
////            throws URISyntaxException {
////
////        //Send request for .well-known/core resource
////        WellKnownCoreProcessor responseProcessor = new WellKnownCoreProcessor(internalTasksExecutorService);
////        sendWellKnownCoreRequest(remoteAddress, CoapServerApplication.DEFAULT_COAP_SERVER_PORT, responseProcessor);
////
////        return responseProcessor.getWellKnownCoreFuture();
////
////    }
//
//
//    private void sendWellKnownCoreRequest(InetAddress remoteHost, int remotePort,
//                                          WellKnownCoreProcessor responseProcessor) throws URISyntaxException {
//
//        URI uri = new URI("coap", null, null, -1, "/.well-known/core", null, null);
//        CoapRequest coapRequest = new CoapRequest(MessageType.Name.CON, MessageCode.Name.GET, uri);
//
//        InetSocketAddress remoteEndpoint = new InetSocketAddress(remoteHost, remotePort);
//        coapClientApplication.sendCoapRequest(coapRequest, responseProcessor, remoteEndpoint);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    private SettableFuture<Set<URI>> registerResources(final Multimap<String, LinkAttribute> webservices, String host){
//
//        final SettableFuture<Set<URI>> result = SettableFuture.create();
//        final AtomicInteger counter = new AtomicInteger(0);
//
//        final Set<URI> registeredResources = Collections.synchronizedSet(new TreeSet<URI>());
//
//        for(final String webservicePath : webservices.keySet()){
//
//            try{
//                final URI webserviceUri = new URI("coap", null, host, -1, webservicePath, null, null);
//
//                CoapWebserviceResponseProcessor responseProcessor;
//                if(webservices.get(webservicePath).contains(new EmptyLinkAttribute(LinkAttribute.OBSERVABLE, null)))
//                    responseProcessor = new CoapWebserviceUpdateNotificationProcessor(
//                            this.backendComponentFactory, webserviceUri, webserviceUri);
//                else
//                    responseProcessor = new CoapWebserviceResponseProcessor(this.backendComponentFactory,
//                        webserviceUri, webserviceUri);
//
//
//                final ListenableFuture<InternalResourceStatusMessage> resourceStatusFuture =
//                        responseProcessor.getResourceStatusFuture();
//
//                CoapRequest coapRequest = new CoapRequest(MessageType.Name.CON, MessageCode.Name.GET, webserviceUri);
//                coapRequest.setAccept(ContentFormat.APP_SHDT, ContentFormat.APP_RDF_XML, ContentFormat.APP_N3,
//                        ContentFormat.APP_TURTLE);
//
//                coapRequest.setObserve();
//
//                coapClientApplication.sendCoapRequest(coapRequest, responseProcessor,
//                        new InetSocketAddress(host, CoapServerApplication.DEFAULT_COAP_SERVER_PORT));
//
//                Futures.addCallback(resourceStatusFuture, new FutureCallback<InternalResourceStatusMessage>() {
//
//                    @Override
//                    public void onSuccess(InternalResourceStatusMessage resourceStatusMessage) {
//
//                        ListenableFuture<URI> registrationFuture = registerDataOrigin(webserviceUri,
//                                resourceStatusMessage.getModel(), resourceStatusMessage.getExpiry());
//
//                        Futures.addCallback(registrationFuture, new FutureCallback<URI>() {
//
//                            @Override
//                            public void onSuccess(URI uri) {
//                                registeredResources.add(webserviceUri);
//
//                                if(counter.incrementAndGet() == webservices.keySet().size())
//                                    result.set(registeredResources);
//                            }
//
//                            @Override
//                            public void onFailure(Throwable throwable) {
//                                log.error("Could not register \"{}\"!", webservicePath, throwable);
//
//                                if(counter.incrementAndGet() == webservices.keySet().size())
//                                    result.set(registeredResources);
//                            }
//
//                        }, internalTasksExecutorService);
//                    }
//
//                    @Override
//                    public void onFailure(Throwable throwable) {
//                        log.error("Could not register \"{}\"!", webservicePath, throwable);
//
//                        if(counter.incrementAndGet() == webservices.keySet().size())
//                            result.set(registeredResources);
//                    }
//
//                }, internalTasksExecutorService);
//
//
//            }
//
//            catch (Exception ex) {
//                log.error("Could not register \"{}\"!", webservicePath, ex);
//
//                if(counter.incrementAndGet() == webservices.keySet().size())
//                    result.set(registeredResources);
//            }
//        }
//
//        return result;
//    }
//
//}
//
