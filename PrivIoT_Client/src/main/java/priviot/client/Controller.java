package priviot.client;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioSocketChannel;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestEncoder.ErrorDataEncoderException;

import priviot.utils.PseudonymizationProcessor;
import priviot.utils.pseudonymization.PseudonymizationException;

/**
 * Controls the work of the Client.
 */
public class Controller {
	
	private static final String PSEUDONYM_URI = "http://www.pseudonym.com/";
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	private String sensorUri;
	private byte[] sensorSecret;
	private int sensorUpdateInterval;
	private String sspHttpHost;
	private String sspHttpRequestPath;
	
	private Thread worker;
	
	private Channel connector;

	/**
	 * Constructor.
	 * 
	 * @param sensorUri
	 * @param sensorSecret
	 * @param sensorUpdateInterval
	 * @param sspHttpUri
	 * @param sspHttpPort
	 */
	public Controller(String sensorUri, byte[] sensorSecret, int sensorUpdateInterval, String sspHttpHost, String sspHttpRequestPath, int sspHttpPort) {
		this.sensorUri = sensorUri;
		this.sensorSecret = sensorSecret;
		this.sensorUpdateInterval = sensorUpdateInterval;
		this.sspHttpHost = sspHttpHost;
		this.sspHttpRequestPath = sspHttpRequestPath;
		
		// setup HTTP client
		ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
                                                                   Executors.newCachedThreadPool());
  
        ClientBootstrap bootstrap = new ClientBootstrap(factory);
  
        bootstrap.setPipelineFactory(new HttpChannelPipelineFactory());
          
        bootstrap.setOption("tcpNoDelay", true);
	    bootstrap.setOption("keepAlive", true);
	    
	    log.info("Connect HTTP client to " + sspHttpHost + ", port " + sspHttpPort);
	    
	    InetSocketAddress address = new InetSocketAddress(sspHttpHost, sspHttpPort);
	    log.info("address: " + address.getAddress());
	    if (address.isUnresolved()) {
	    	log.info("unresolved");
	    }
	    else {
	    	log.info("resolved");
	    }
	    
	    ChannelFuture future = bootstrap.connect(address);
	    
	    if (!future.awaitUninterruptibly().isSuccess()) {
	    	log.error("Failed to connect to server");
	    	return;
	    }
	    
	    connector = (NioSocketChannel)future.getChannel();
	    
	    if (connector.isConnected()) {
	    	log.info("Connected to server");
	    }
	}
	
	/**
	 * Let the Controller start.
	 */
	public boolean start() {
		if (!connector.isConnected()) {
			return false;
		}
		
		worker = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						requestStatus();
					} catch (ErrorDataEncoderException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					try {
						Thread.sleep(sensorUpdateInterval * 1000);
					} catch (InterruptedException e) {
						log.error("Worker thread interrupted", e);
						break;
					}
				}
			}
		});
		
		worker.start();
		
		return true;
	}
	
	/**
	 * Requests the status of the specified sensor from the Smart Service Proxy.
	 * @throws ErrorDataEncoderException 
	 */
	private void requestStatus() throws ErrorDataEncoderException {
		final String BOUNDARY = "---------------------------21018220672108554064107170109";
		
		// create actual pseudonym
		String pseudonym = PSEUDONYM_URI;
		try {
			pseudonym += PseudonymizationProcessor.generateHmac256Pseudonym(sensorUri, sensorUpdateInterval, sensorSecret);
		} catch (PseudonymizationException e) {
			log.error("Couldn't create pseudonym for sensor " + sensorUri);
			return;
		}
		
		log.info("Send HTTP request for sensor " + pseudonym);
		
		DefaultHttpDataFactory factory = new DefaultHttpDataFactory();
		
		// send http request to ssp
		HttpRequest request = new DefaultHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.POST, sspHttpRequestPath);
		
		
		// First try: Build a request similar to that one sent by the website /services/sparql-endpoint
		// this causes a null pointer exception on the server because he can't find body http data "query" in SparqlEndpoint
		
        request.headers().set(HttpHeaders.Names.HOST, sspHttpHost);
        request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        //request.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        request.headers().set(HttpHeaders.Names.CONTENT_TYPE, "multipart/form-data; boundary=" + BOUNDARY);
        request.headers().set(HttpHeaders.Names.ACCEPT, "application/rdf+xml");
        
        String contentStr = BOUNDARY + "\n"
		          + "Content-Disposition: form-data; name=\"query\"\n\n"
		          + "SELECT ?point WHERE {\n"
		          + "<" + pseudonym + ">"
		          + " <http://example.org/itm-geo-test#hasPosition> ?position .\n"
	              + "?position <http://www.opengis.net/ont/geosparql#asWKT> ?point .\n"
                + "}"
                + "\n" + BOUNDARY + "--";
        ChannelBuffer channelBuffer = ChannelBuffers.copiedBuffer(contentStr, StandardCharsets.UTF_8);
        request.setContent(channelBuffer);
        request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, channelBuffer.readableBytes());
        log.debug("send message to " + sspHttpHost + "/" + sspHttpRequestPath + ":\n" + contentStr);
        
		
		// Second try: Build a request with HttpPostRequestEncoder
		// This causes a java.lang.IllegalArgumentException: unsupported message type: class org.jboss.netty.handler.codec.http.multipart.HttpPostRequestEncoder
		// in SocketSendBufferPool.acquire()
		/*
		String contentStr = "SELECT ?point WHERE {\n"
				          + "<" + pseudonym + ">"
				          + " <http://example.org/itm-geo-test#hasPosition> ?position .\n"
			              + "?position <http://www.opengis.net/ont/geosparql#asWKT> ?point .\n"
		                  + "}";
		                  
		HttpPostRequestEncoder bodyRequestEncoder =
				new HttpPostRequestEncoder(factory, request, true); // true => multipart
		
		request.headers().set(HttpHeaders.Names.HOST, sspHttpHost);
        request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        request.headers().set(HttpHeaders.Names.ACCEPT, "XML");
        
        // add Form attribute
        bodyRequestEncoder.addBodyAttribute("getform", "POST");
        bodyRequestEncoder.addBodyAttribute("Content-Disposition", "form-data; name=\"query\"");
        bodyRequestEncoder.addBodyAttribute("query", contentStr);
        
        bodyRequestEncoder.finalizeRequest();
        */
        
        log.debug("send message to " + sspHttpHost + "/" + sspHttpRequestPath);
        log.debug("version: " + request.getProtocolVersion());
        log.debug("method: " + request.getMethod());
        
        log.debug("headers:");
        for (Entry<String, String> entry : request.headers().entries()) {
        	log.debug(entry.getKey() + ": " + entry.getValue());
        }
        
        byte[] payload = new byte[request.getContent().readableBytes()];
        request.getContent().getBytes(0, payload);
		log.debug("content:\n" + new String(payload));
        
    	connector.write(request);
    	
    	// for seconds try
    	//connector.write(bodyRequestEncoder);
	}
}
