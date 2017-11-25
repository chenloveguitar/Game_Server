package com.dyz.gameserver.msg.processor.ting;

import com.context.ErrorCode;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.logic.RoomLogic;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;
import com.dyz.gameserver.msg.response.ErrorResponse;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TingPaiMsgProcessor extends MsgProcessor implements
INotAuthProcessor{
    
	/**
	 * 听牌接口
	 */
	
	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		 RoomLogic roomLogic = RoomManager.getInstance().getRoom(gameSession.getRole(Avatar.class).getRoomVO().getRoomId());
		 if(roomLogic != null){
	        	String laypai = request.getString();
	        	JSONObject jsonObject   = JSONObject.fromObject(laypai);
//	        	JSONArray json = jsonObject.getJSONArray("layCards");
//	        	int[] layCards = new int[json.size()];
//	        	if(json.size()>0){
//	        		for(int i = 0;i < json.size();i++){
//	        			layCards[i] = json.getInt(i);
//	        		}
//	        	}
//	            if(layCards.length > 0){
	            	//听牌
			   
			     int layCards = (int) jsonObject.get("layCards");
			   
	             roomLogic.layCards(gameSession.getRole(Avatar.class),layCards);
//	            }
	        }else{
	            gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000005));
	        }
		
	}

}
