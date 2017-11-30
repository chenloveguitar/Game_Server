package com.dyz.gameserver.test.client.handler;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyClientHandler implements IoHandler{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		logger.info("sessionCreated...");
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		logger.info("sessionOpened...");
		
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		logger.info("sessionClosed...");
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		logger.info("sessionIdle...");
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		logger.info("exceptionCaught...");
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		logger.info("messageReceived...");
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		logger.info("messageSent...");
	}

	@Override
	public void inputClosed(IoSession session) throws Exception {
		logger.info("inputClosed...");
	}
	
}