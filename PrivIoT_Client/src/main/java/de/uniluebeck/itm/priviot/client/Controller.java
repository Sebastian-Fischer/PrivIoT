package de.uniluebeck.itm.priviot.client;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
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

import de.uniluebeck.itm.priviot.utils.PseudonymizationProcessor;
import de.uniluebeck.itm.priviot.utils.pseudonymization.PseudonymizationException;

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
	
	private ClientBootstrap bootstrap;
	
	private InetSocketAddress address;

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
		
		log.info("Observing the sensor " + sensorUri);
		
		// setup HTTP client
		ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
                                                                   Executors.newCachedThreadPool());
  
        bootstrap = new ClientBootstrap(factory);
  
        bootstrap.setPipelineFactory(new HttpChannelPipelineFactory());
          
        bootstrap.setOption("tcpNoDelay", true);
	    bootstrap.setOption("keepAlive", true);
	    
	    log.info("Connect HTTP client to " + sspHttpHost + ", port " + sspHttpPort);
	    
	    address = new InetSocketAddress(sspHttpHost, sspHttpPort);
	    log.info("address: " + address.getAddress());
	    if (address.isUnresolved()) {
	    	log.info("unresolved");
	    }
	    else {
	    	log.info("resolved");
	    }
	    
	    
	}
	
	/**
	 * Let the Controller start.
	 */
	public boolean start() {		
		worker = new Thread(new Runnable() {
			@Override
			public void run() {
				// minimum time in milliseconds since previous update time
				// During this time the sensor data has to come from CoAP-Webserver to SSP's database
				long minTimeDifferencePrevious = 5000;
				// minimum time in milliseconds until next update time
				// During this time the request has to be answered by the SSP
				long minTimeDifferenceNext = 1000;
				
				log.debug("sensor update interval is " + sensorUpdateInterval + " seconds");
				
				sleepUntilInTimeWindow(sensorUpdateInterval * 1000, minTimeDifferencePrevious, minTimeDifferenceNext);
				
				log.info("Start requesting status");
				
				while (true) {
					try {
						if (connector == null) {
							if (!connectToServer()) {
								return;
							}
						} else if(!connector.isConnected()) {
							log.debug("Reconnect to server");
							if (!connectToServer()) {
								return;
							}
						}
					    
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
	 * Connects to the server and waits for the connection.
	 * @return false on failure.
	 */
	private boolean connectToServer() {
		ChannelFuture future = bootstrap.connect(address);
	    
	    if (!future.awaitUninterruptibly().isSuccess()) {
	    	log.error("Failed to connect to server");
	    	return false;
	    }
	    
	    connector = (NioSocketChannel)future.getChannel();
	    
	    if (connector.isConnected()) {
	    	log.info("Connected to server");
	    }
	    else {
	    	log.error("Not connected to server");
	    	return false;
	    }
	    
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
		
		// send http request to ssp
		HttpRequest request = new DefaultHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.POST, sspHttpRequestPath);
		
        request.headers().set(HttpHeaders.Names.HOST, sspHttpHost);
        request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        request.headers().set(HttpHeaders.Names.CONTENT_TYPE, "multipart/form-data; boundary=" + BOUNDARY);
        request.headers().set(HttpHeaders.Names.ACCEPT, "application/sparql-results+xml");
        
        String contentStr = "SELECT ?point WHERE {\r\n"
				          + "<" + pseudonym + "> <http://example.org/itm-geo-test#hasPosition> ?position .\r\n"
			              + "?position <http://www.opengis.net/ont/geosparql#asWKT> ?point .\r\n"
		                  + "}";
        String multipartContentStr = createMultipartContent(contentStr, BOUNDARY);
        
        ChannelBuffer channelBuffer = ChannelBuffers.copiedBuffer(multipartContentStr, StandardCharsets.UTF_8);
        request.setContent(channelBuffer);
        
        request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, channelBuffer.readableBytes());
        
        log.debug("send sparql message to " + sspHttpHost + "/" + sspHttpRequestPath);
        
    	connector.write(request);
	}
	
	private String createMultipartContent(String content, String boundary) {
		return "--" + boundary + "\r\n" +
			   "Content-Disposition: form-data; name=\"query\"\r\n\r\n" +
	           content + 
	           "\r\n--" + boundary + "--\r\n";
	}
	
	/**
	 * Ever sensorUpdateInterval the pseudonym of the requested data changes.
	 * That means, within this time difference the CoAP-Webserver has to create and 
	 * send the new sensor data to the SSP,
	 * the SSP has to save the data and the Client has to ask for the data.
	 * This method sleeps until the the actual time is in interval 
	 * (sensorUpdateIntervalMilli + minTimeDifference, sensorUpdateIntervalMilli - minTimeDifference).
	 * @param sensorUpdateIntervalMilli  The time interval in milliseconds,
	 *                                   in which new sensor data is published and the pseudonym changes.
	 * @param minTimeDifferencePrevoius  The minimum time in milliseconds since previous update time.
	 * @param minTimeDifferenceNext      The minimum time in milliseconds until next update time.
	 */
	private void sleepUntilInTimeWindow(long sensorUpdateIntervalMilli, long minTimeDifferencePrevoius, long minTimeDifferenceNext) {
		// find good start point
		long modulo = (new Date()).getTime() % (sensorUpdateIntervalMilli);
		
		Date lastUpdate = new Date((new Date()).getTime() - modulo);
		Date nextUpdate = new Date((new Date()).getTime() - modulo + sensorUpdateIntervalMilli);
		
		// sleep until we are 0 to minTimeDifferencePrevoius milliseconds after the actual sensor update time
		// and maxTimeDifference before the next sensor update time
		long sleepTime = 0;
		if (modulo < minTimeDifferencePrevoius) {
			sleepTime = minTimeDifferencePrevoius - modulo;
			log.info(modulo + " seconds after last update time (" + lastUpdate + "). Sleep " + sleepTime + " milliseconds");
		}
		else if (modulo > sensorUpdateIntervalMilli - minTimeDifferenceNext) {
			sleepTime = sensorUpdateIntervalMilli + minTimeDifferenceNext - modulo;
			log.info((sensorUpdateIntervalMilli - modulo) + " before next update time (" + nextUpdate + "). Sleep " + sleepTime + " milliseconds");
		}
		else {
			log.info("next update time is " + nextUpdate);
		}
		if (sleepTime > 0) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				log.error("Worker thread interrupted", e);
				return;
			}
		}
	}
}
