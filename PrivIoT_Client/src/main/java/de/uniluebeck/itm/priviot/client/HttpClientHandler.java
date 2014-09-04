package de.uniluebeck.itm.priviot.client;

import java.io.ByteArrayInputStream;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat;

public class HttpClientHandler extends SimpleChannelHandler {
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	
	public HttpClientHandler() {
		
	}

	@Override
	public void messageReceived(ChannelHandlerContext context, MessageEvent event) {
		log.debug("message received");
		
		if (event.getMessage() instanceof HttpResponse) {
			HttpResponse response = (HttpResponse)event.getMessage();
			
			byte[] payload = new byte[response.getContent().readableBytes()];
			response.getContent().getBytes(0, payload);
			
			log.debug("received:\n" + new String(payload));
			
			ByteArrayInputStream playloadStream = new ByteArrayInputStream(payload);
			
			ResultsFormat format = getJenaFormatByHttpContentType(response.headers().get("Content-Type"));
			if (format == null) {
				log.error("content-format unknown: " + response.headers().get("content-format"));
				return;
			}
			
			log.debug("parse result in format " + format);
			
			ResultSet r = ResultSetFactory.load(playloadStream, format);
			int entries = 0;
		    while ( r.hasNext() ) {
		      QuerySolution soln = r.next();
		      log.info("Received new Status: " + soln);
		      entries++;
		    }
		    if (entries == 0) {
		    	log.info("Received message contains no result sets");
		    }
		}
	}
	
	 @Override
	 public void exceptionCaught(ChannelHandlerContext context, ExceptionEvent event) {
		 log.error("Network Exception", event.getCause());
	 }
	 
	 private ResultsFormat getJenaFormatByHttpContentType(String httpContentType) {
		 if ("application/sparql-results+xml".equals(httpContentType)) {
			 return ResultsFormat.FMT_RS_XML;
		 }
		 else if ("application/sparql-results+json".equals(httpContentType)) {
			 return ResultsFormat.FMT_RS_JSON;
		 }
		 else if ("text/csv".equals(httpContentType)) {
			 return ResultsFormat.FMT_RS_CSV;
		 }
		 else if ("text/tab-separated-values".equals(httpContentType)) {
			 return ResultsFormat.FMT_RS_TSV;
		 }
		 else {
			 return null;
		 }
	 }
}
