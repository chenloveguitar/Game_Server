package com.dyz.gameserver.test.client;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyz.gameserver.test.client.handler.MyClientHandler;
import com.dyz.gameserver.test.codec.MyProtocolCodecFactory;
import com.dyz.gameserver.test.listener.MyIoFutureListener;

public class ClientTest {
	private static final Logger logger = LoggerFactory.getLogger(ClientTest.class.getClass());
	private static final String HOST = "localhost";
	private static final int PORT = 10122;
	private static final int CONNECT_TIMEOUT = 10000;

	public static void main(String[] args) {
		IoConnector ioConnector = new NioSocketConnector();
		ioConnector.setConnectTimeoutMillis(CONNECT_TIMEOUT);//设置连接超时时间,10秒
		ioConnector.setHandler(new MyClientHandler());
		ioConnector.getFilterChain().addLast("codec",new ProtocolCodecFilter(new MyProtocolCodecFactory(Charset.forName("UTF-8"))));
		ConnectFuture connectFuture = ioConnector.connect(new InetSocketAddress(HOST, PORT));
		connectFuture.awaitUninterruptibly();//等待连接创建完成
		connectFuture.addListener(new MyIoFutureListener());
	}
}
