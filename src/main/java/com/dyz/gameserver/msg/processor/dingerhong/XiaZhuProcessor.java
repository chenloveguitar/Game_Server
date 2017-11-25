package com.dyz.gameserver.msg.processor.dingerhong;

import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.logic.RoomLogic;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;

import net.sf.json.JSONObject;

public class XiaZhuProcessor extends MsgProcessor implements
INotAuthProcessor{
    
	/**
	 * 下注接口  用于自己接收参数调用
	 */
	
	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		//ClientRequest  使用byte转换成适合的数据类型
		Avatar avatar = gameSession.getRole(Avatar.class);
		//获得房间id和类型 1,2,3分别是，初，中，高
		RoomLogic roomLogic = RoomManager.getInstance().getRoom(avatar.getRoomVO().getRoomId());
		if(roomLogic != null)
		{
			String message = request.getString();
        	JSONObject json = JSONObject.fromObject(message);
        	//type待定
            //int type = (int)json.get("type");
        	//当前下注的分数
        	int score = (int)json.get("score");
        	//调用下注方法
        	roomLogic.xiaZhu(avatar, score);
        	
		}
	}

}
