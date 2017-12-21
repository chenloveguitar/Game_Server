package com.dyz.gameserver.test.server.handler;

import java.text.SimpleDateFormat;
import java.util.Date;

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
//		session.write("sessionCreated");
//		logger.info("sessionCreated......");
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
//		session.write("sessionOpened");
//		logger.info("sessionOpened......");
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
//		session.write("sessionClosed");
//		logger.info("sessionClosed......");
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
//		session.write("sessionIdle");
		logger.info( "IDLE " + session.getIdleCount( status ));;
		
	}

	/**
	 * 此方法的作用：
	 * 	以处理在正常处理远程连接过程中引发的异常。
	 */
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
//		session.write("exceptionCaught");
		cause.printStackTrace();
//		logger.info("exceptionCaught......");
		
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
//		logger.info(message.toString());
		logger.info("messageReceived......");
		logger.info("message:"+message);
		String data = message.toString();
		if(data.trim().equalsIgnoreCase("quit")) {//客户端请求关闭与服务器的连接
			session.close(true);//立即关闭
		}
		//输出客户端输入的内容
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		session.write(format.format(new Date()));
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		logger.info("服务器发送给客户端的数据{}",message);
		logger.info("messageSent......");
		
	}

	@Override
	public void inputClosed(IoSession session) throws Exception {
//		logger.info("inputClosed......");
//		session.write("inputClosed");
		
	}
	
}
