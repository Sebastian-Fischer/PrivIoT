package de.uniluebeck.itm.priviot.client;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestEncoder;

public class HttpChannelPipelineFactory implements ChannelPipelineFactory {

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		
		pipeline.addLast("codec", new HttpClientCodec());
		
		// needed?
		//pipeline.addLast("encoder", new HttpRequestEncoder());
		
		pipeline.addLast("handler", new HttpClientHandler());
		
		return pipeline;
	}

}
