package com.dyz.gameserver.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.dyz.gameserver.net.codec.GameProtocolcodecFactory;
import com.dyz.gameserver.net.codec.MsgProtocol;

public class MinaClient {

	private static final String HOST = "localhost";
	private static final int PORT = 10122; 
	public static void main(String[] args) throws Exception {
		IoConnector connector = new NioSocketConnector();
		connector.setHandler(new MyHandler());
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new GameProtocolcodecFactory()));
		ConnectFuture connect = connector.connect(new InetSocketAddress(HOST, PORT));
		connect.awaitUninterruptibly();//等待连接创建完毕
		IoSession session = connect.getSession();
		while(true) {
			System.out.println("请输入code: \t");
			String code = new BufferedReader(new InputStreamReader(System.in)).readLine();
			System.out.println("请输入对象类型: \t");
			String json = new BufferedReader(new InputStreamReader(System.in)).readLine();
			byte[] body = json.getBytes();
			/* 标志 byte 长度short */
	        int length = MsgProtocol.flagSize+MsgProtocol.lengthSize+MsgProtocol.msgCodeSize+ body.length;
	        IoBuffer buf = IoBuffer.allocate(length);
	        buf.put(MsgProtocol.defaultFlag);//flag
	        buf.putInt(body.length+MsgProtocol.msgCodeSize);//lengh
	        buf.putInt(Integer.valueOf(code));
	        buf.put(body);
	        buf.flip();
			session.write(buf);
		}
	}
}
