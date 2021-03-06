package com.dyz.gameserver.test.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyz.gameserver.test.codec.MyProtocolCodecFactory;
import com.dyz.gameserver.test.server.handler.MyServerHandler;
public class ServerTest {

	private static Logger logger = LoggerFactory.getLogger(ServerTest.class.getClass());
	private static final String HOST = "192.168.0.194";
	private static final int PORT = 9999;
	
	public static void main(String[] args){
		//创建服务器核心对象
		IoAcceptor ioAcceptor = new NioSocketAcceptor();
		//日志过滤器,记录所有信息,如新创建的会话
//		ioAcceptor.getFilterChain().addLast( "logger", new LoggingFilter());
		//编码过滤器，使得服务器可以处理string类型的消息  
		ioAcceptor.getFilterChain().addLast("codec",new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
		//设置处理器
		ioAcceptor.setHandler(new MyServerHandler());
		//设置自定义编码过滤器
//		ioAcceptor.getFilterChain().addLast("codec",new ProtocolCodecFilter(new MyProtocolCodecFactory(Charset.forName("UTF-8"))));
		//设置写空闲时间 单位秒 设置后,每隔5秒将调用handler中的sessionIdle方法
//		ioAcceptor.getSessionConfig().setBothIdleTime(5);
		//设置读缓冲区大小
		ioAcceptor.getSessionConfig().setReadBufferSize( 2048 );
		//设置会话空闲时间单位秒
		ioAcceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, 10 );
		try {
			//绑定地址和端口号
			ioAcceptor.bind(new InetSocketAddress(HOST, PORT));
			logger.info("服务器启动成功！");
		} catch (IOException e) {
			logger.error("创建服务异常!" + e);
		}
	}
}
