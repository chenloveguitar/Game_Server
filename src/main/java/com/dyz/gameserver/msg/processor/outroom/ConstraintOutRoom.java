package com.dyz.gameserver.msg.processor.outroom;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.logic.RoomLogic;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;
import com.dyz.gameserver.msg.response.host.IndexInfosResponse;

/**
 * 强制解散房间
 * @author Administrator
 *
 */
public class ConstraintOutRoom extends MsgProcessor implements
INotAuthProcessor {
	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {

	}
	@Override
	public void handle(GameSession gameSession, ClientRequest request) {
//		JSONObject json = JSONObject.fromObject(request);
		String roomId;
		JSONObject json;
		try {
			roomId = request.getString();
			if(!roomId.isEmpty()){
			RoomLogic roomLogic = RoomManager.getInstance().getRoom(Integer.parseInt(roomId));
			String str = "0";
			if(roomLogic != null){
				if(roomLogic.getPlayCardsLogic() == null){
					json = new JSONObject();
	    			json.put("accountName", "超级管理员");
	    		    json.put("status_code", "0");
	    		    json.put("uuid", 0);
	    		 	json.put("type", "1");
					roomLogic.exitRoomDetail(json);
					str = "1";
				}
//				else if(roomLogic.getRoomVO().getRoomType() == 5){
//					roomLogic.getPlayCardsLogic().endSendInfos(1);
//					str = "1";
//				}
				else if(roomLogic.getRoomVO().getRoomType() != 5){
					roomLogic.setHasDissolve(true);
    				RoomManager.getInstance().getRoom(Integer.parseInt(roomId)).setCount(0);
    				roomLogic.getPlayCardsLogic().settlementData("2");
					str = "1";
				}
				gameSession.sendMsg(new IndexInfosResponse(1,str));
			}
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
