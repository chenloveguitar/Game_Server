package com.dyz.gameserver.msg.processor.login;

import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.manager.GameSessionManager;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;

/**
 * 退出游戏
 * @author luck
 *
 */
public class LogoutMsgProcessor extends MsgProcessor{

	
	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		String uuid = request.getString();
		//GameSession session = GameSessionManager.getInstance().sessionMap.get("uuid_"+uuid);
		/*if(session.equals(gameSession)){
			//session存在 清除session
			System.out.println(account.getUuid()+"：退出游戏：");
			GameSessionManager.getInstance().removeGameSession(avatar);
//			GameServerContext.remove_offLine_Character(avatar);
//			GameServerContext.remove_onLine_Character(avatar);
		}*/
		System.out.println(uuid+"：退出游戏：");
		GameSessionManager.getInstance().removeGameSession(gameSession.getRole(Avatar.class));
	}

}
