package com.dyz.gameserver.test.codec;

import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * 解码器
 * @author jumili
 *
 */
public class MyDecoder extends CumulativeProtocolDecoder{
	private Charset charset;
	private static final AttributeKey DECODER = new AttributeKey(MyDecoder.class, "decoder");
	private int maxLineLength = 1024;
	public MyDecoder() {
	}
	public MyDecoder(Charset charset) {
		this.charset = charset;
	}
	@Override
	protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		return false;
	}
}
