package com.dyz.gameserver.test;

import java.net.InetSocketAddress;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTest {
	private static final Logger logger = LoggerFactory.getLogger(ClientTest.class.getClass());
	private static final String HOST = "localhost";
	private static final int PORT = 10122;

	@Test
	public void testClient() {
		IoConnector ioConnector = new NioSocketConnector();
		ConnectFuture connectFuture = ioConnector.connect(new InetSocketAddress(HOST, PORT));
		connectFuture.addListener(new MyIoFutureListener());
	}
	
	private static class MyIoFutureListener implements IoFutureListener<IoFuture>{

		@Override
		public void operationComplete(IoFuture future) {
			logger.info("operationComplete...");
		}
		
	}

}
