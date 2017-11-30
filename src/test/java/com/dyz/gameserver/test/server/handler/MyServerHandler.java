package com.dyz.gameserver.test.server.handler;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端处理器
 * @author jumili
 *
 */
public class MyServerHandler implements IoHandler{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
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
