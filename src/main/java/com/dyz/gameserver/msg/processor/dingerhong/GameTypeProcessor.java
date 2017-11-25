package com.dyz.gameserver.msg.processor.dingerhong;

import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.message.ServerResponse;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.logic.RoomLogic;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;

import net.sf.json.JSONObject;

/**
 * 游戏规则由前端发送
 * @author Administrator
 *
 */

public class GameTypeProcessor extends MsgProcessor implements
INotAuthProcessor {
    
	
	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		Avatar avatar = gameSession.getRole(Avatar.class);
		//获得房间id和类型 1,2,3分别是，初，中，高
		RoomLogic roomLogic = RoomManager.getInstance().getRoom(avatar.getRoomVO().getRoomId());
		if(roomLogic != null)
		{
			String message = request.getString();
	    	JSONObject json = JSONObject.fromObject(message);
	    	int type = (int)json.get("type");
	    	int uuid = 0;
	    	try {
				uuid = (int)json.get("uuid");
			} catch (Exception e) {
				uuid = 0;
			}
	    	//调用下注方法
	    	roomLogic.chooseGameType(avatar, type,uuid);
	    	
		}
		
	}
}
