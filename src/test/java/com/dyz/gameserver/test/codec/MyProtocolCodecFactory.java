package com.dyz.gameserver.test.codec;

import java.nio.charset.Charset;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * 协议编解码器工厂
 * @author jumili
 *
 */
public class MyProtocolCodecFactory implements ProtocolCodecFactory{

	private final MyEncoder encoder;

    private final MyDecoder decoder;
	
	public MyProtocolCodecFactory() {
        this(Charset.defaultCharset());
    }

	public MyProtocolCodecFactory(Charset charset) {
		encoder = new MyEncoder(charset);
		decoder = new MyDecoder(charset);
	}
	
	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return this.encoder;
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		return this.decoder;
	}
	
}