package com.dyz.gameserver.test.codec;

import java.nio.charset.Charset;

import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author jumili
 *
 */
public class MyEncoder extends ProtocolEncoderAdapter{
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Charset charset;
	
	private static final AttributeKey ENCODER = new AttributeKey(MyEncoder.class, "encoder");
	
	private int maxLineLength = 1024;
	
	public MyEncoder() {}
	
	public MyEncoder(Charset charset) {
		this.charset = charset;
	}
	
	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		logger.info("message的真实类型："+message.getClass().getName());
	}
}