package com.dyz.gameserver.msg.processor.joinroom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONObject;
import com.context.ErrorCode;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.logic.RoomLogic;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;
import com.dyz.gameserver.msg.response.ErrorResponse;
import com.dyz.gameserver.msg.response.joinroom.GetRoomResponse;
import com.dyz.gameserver.pojo.RoomVO;
import com.dyz.persist.util.GlobalUtil;

/**
 * 获得房间场次
 * @author Administrator
 *
 */
public class GetRoomMsgProcessor extends MsgProcessor implements INotAuthProcessor{
    
	
	////用成list集合 add进去 放在json里面 tostring出去
	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		if(GlobalUtil.checkIsLogin(gameSession)){
			int roomType = Integer.parseInt(request.getString());
			//获取所有房间信息
			Map<Integer, RoomLogic> list = RoomManager.getInstance().getAllRoom(roomType);
			if(list != null){
				//Map<Integer, RoomVO> roomList = new HashMap<>();  
				//定义了List
				List<RoomVO> roomList = new ArrayList<RoomVO>();
				for(Entry<Integer, RoomLogic> entry : list.entrySet()){
					roomList.add(entry.getValue().getRoomVO());
				}
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("roomList",roomList);
				gameSession.sendMsg(new GetRoomResponse(1, jsonObject.toString()));
			}else{
				gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000027));
			}
		}else{
			gameSession.destroyObj();
		}
	}

}
