package com.dyz.gameserver.net;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.dyz.gameserver.bootstrap.GameServer;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;

/**
 * 前段
 * @author Administrator
 *
 */
public class MinaMsgHandler extends IoHandlerAdapter{
	
//	private static final Logger logger = LoggerFactory.getLogger(MinaMsgHandler.class);
	
//	@Override
//	public void sessionCreated(IoSession session) throws Exception {
//
//	}
	@Override
	public void sessionOpened(IoSession session) throws Exception{
		new GameSession(session);
		//logger.info("a session create from ip {}",session.getRemoteAddress());
	}
	
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		ClientRequest clientRequest = (ClientRequest) message;
		
		//性能优化(10-25)
		int msgCode = clientRequest.getMsgCode();
		if(msgCode == 1000){//客户端请求断开链接
			session.close(true);
			
		}else{
			GameSession gameSession = GameSession.getInstance(session);
			if (gameSession == null) {
				session.close(true);
				return;
			}
			GameServer.msgDispatcher.dispatchMsg(gameSession,clientRequest);
		}
		
		//优化前
		/*if (gameSession == null) {
			//logger.info("gameSession == null");
			return;
		}
		GameServer.msgDispatcher.dispatchMsg(gameSession,clientRequest);*/
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		//强制退出
		sessionClosed(session);//性能优化(10-25)
//		System.out.println(cause.getMessage());
//		logger.error("服务器出错 {}",cause.getMessage());
		//cause.printStackTrace();
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		super.sessionIdle(session, status);//性能优化(10-25)
	}

	/**
	 * 关闭SESSION
	 * @param session
	 * @throws Exception
     */
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		//logger.info("a session closed ip:{}",session.getRemoteAddress());
		GameSession gameSession = GameSession.getInstance(session);
		if(gameSession !=null){
			gameSession.close();
		}
		super.sessionClosed(session);//性能优化(10-25)
		session.close(true);//性能优化(10-25)
	}
	
	
}
