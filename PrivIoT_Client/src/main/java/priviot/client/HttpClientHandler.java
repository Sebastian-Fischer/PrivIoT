package priviot.client;

import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class HttpClientHandler extends SimpleChannelHandler {
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	
	public HttpClientHandler() {
		
	}

	@Override
	public void messageReceived(ChannelHandlerContext context, MessageEvent event) {
		log.info("message received");
		
		log.debug(event.getMessage().getClass());
		
		if (event.getMessage() instanceof HttpResponse) {
			HttpResponse response = (HttpResponse)event.getMessage();
			
			log.debug("status: " + response.getStatus());
			
			log.debug("headers:");
			for (Entry<String, String> entry : response.headers().entries()) {
				log.debug(entry.getKey() + ": " + entry.getValue());
			}
			
			byte[] payload = new byte[response.getContent().readableBytes()];
			response.getContent().getBytes(0, payload);
			log.debug("content:\n" + new String(payload));
		}
	}
	
	 @Override
	 public void exceptionCaught(ChannelHandlerContext context, ExceptionEvent event) {
		 log.error("Network Exception", event.getCause());
	 }
}
