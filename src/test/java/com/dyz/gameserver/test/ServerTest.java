package com.dyz.gameserver.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
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
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class ServerTest {

	private static Logger logger = LoggerFactory.getLogger(ServerTest.class.getClass());
	private static final String HOST = "localhost";
	private static final int PORT = 9999;
	
	public static void main(String[] args){
		//创建服务器核心对象
		IoAcceptor ioAcceptor = new NioSocketAcceptor();
		//设置处理器
		ioAcceptor.setHandler(new ServerTest.MyHandler());
		//编码过滤器，使得服务器可以处理string类型的消息  
		ioAcceptor.getFilterChain().addLast("codec",new ProtocolCodecFilter(new ServerTest.MyProtocolCodecFactory(Charset.forName("UTF-8"))));
		//设置写空闲时间 单位秒 设置后,每隔5秒将调用handler中的sessionIdle方法
//		ioAcceptor.getSessionConfig().setBothIdleTime(5);
		try {
			//绑定地址和端口号
			ioAcceptor.bind(new InetSocketAddress(HOST, PORT));
			logger.info("服务器启动成功！");
		} catch (IOException e) {
			logger.error("创建服务异常!" + e);
		}
	}
	
	/**
	 * 
	 * @author jumili
	 *
	 */
	private static class MyEncoder extends ProtocolEncoderAdapter{
		
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
	
	/**
	 * 解码器
	 * @author jumili
	 *
	 */
	private static class MyDecoder extends CumulativeProtocolDecoder{
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
	
	/**
	 * 协议编解码器工厂
	 * @author jumili
	 *
	 */
	private static class MyProtocolCodecFactory implements ProtocolCodecFactory{

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
	
	/**
	 * 处理器
	 * @author jumili
	 *
	 */
	private static class MyHandler implements IoHandler{

		@Override
		public void sessionCreated(IoSession session) throws Exception {
			session.write("sessionCreated");
			logger.info("sessionCreated......");
		}

		@Override
		public void sessionOpened(IoSession session) throws Exception {
			session.write("sessionOpened");
			logger.info("sessionOpened......");
		}

		@Override
		public void sessionClosed(IoSession session) throws Exception {
			session.write("sessionClosed");
			logger.info("sessionClosed......");
		}

		@Override
		public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
			session.write("sessionIdle");
			logger.info("sessionIdle......");
			
		}

		@Override
		public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
			session.write("exceptionCaught");
			logger.error(cause.getMessage());
			logger.info("exceptionCaught......");
			
		}

		@Override
		public void messageReceived(IoSession session, Object message) throws Exception {
			logger.info("服务端接收到的数据:{}",message);
			logger.info("messageReceived......");
			session.write("1234567890");
		}

		@Override
		public void messageSent(IoSession session, Object message) throws Exception {
			logger.info("服务器发送给客户端的数据{}",message);
			logger.info("messageSent......");
			
		}

		@Override
		public void inputClosed(IoSession session) throws Exception {
			logger.info("inputClosed......");
			session.write("inputClosed");
			
		}
		
	}
}
