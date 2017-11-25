package com.dyz.gameserver.test;

import java.net.InetSocketAddress;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.junit.Before;
import org.junit.Test;

public class GameServerTest {
	private IoAcceptor ioAcceptor = null;
	private static final int PORT = 9999;
	@Before
	public void init() throws Exception {
		ioAcceptor = new NioSocketAcceptor();
	}
	@Test
	public void testName() throws Exception {
		//设置处理器
		ioAcceptor.setHandler(new MyHandler());
		//绑定服务器端口号
		ioAcceptor.bind(new InetSocketAddress(PORT));
		ioAcceptor.getFilterChain().addLast("test", new ProtocolCodecFilter(new TextLineCodecFactory()));
	}
}
