package com.dyz.gameserver.test;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class MyHandler implements IoHandler {

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		System.out.println("调用---->sessionCreated......");
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		System.out.println("调用---->sessionOpened......");
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		System.out.println("调用---->sessionClosed......");
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		System.out.println("调用---->sessionIdle......");
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		System.out.println("调用---->exceptionCaught......");
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		System.out.println(message.getClass());
		System.out.println("调用---->messageReceived......");
		System.err.println("session id:"+session.getId());
		System.err.println("message:"+message);
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		System.out.println("调用---->messageSent......");
	}

	@Override
	public void inputClosed(IoSession session) throws Exception {
		System.out.println("调用---->inputClosed......");
	}

}
