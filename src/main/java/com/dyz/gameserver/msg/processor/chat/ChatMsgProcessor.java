package com.dyz.gameserver.msg.processor.chat;

import com.context.ErrorCode;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.logic.RoomLogic;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;
import com.dyz.gameserver.msg.response.ErrorResponse;

import net.sf.json.JSONObject;

/**
 * 聊天/图片请求
 * @author luck
 *
 */
public class ChatMsgProcessor extends MsgProcessor implements
INotAuthProcessor {

	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		Avatar avatar = gameSession.getRole(Avatar.class);
		if(avatar!=null){
        RoomLogic roomLogic = RoomManager.getInstance().getRoom(avatar.getRoomVO().getRoomId());
        if(roomLogic != null){
        	JSONObject json = JSONObject.fromObject(request.getString());
        	roomLogic.chatServer(json.toString());
        }else{
            gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000005));
        }}

	}

}
