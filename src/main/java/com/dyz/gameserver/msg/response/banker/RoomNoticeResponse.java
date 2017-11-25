package com.dyz.gameserver.msg.response.banker;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;
import com.dyz.gameserver.pojo.RoomVO;

/**
 * 房间通知返回信息
 * @author Administrator
 *
 */


public class RoomNoticeResponse extends ServerResponse{public RoomNoticeResponse(int status,int roomId,int roomType,RoomVO roomVO) {
		super(status,ConnectAPI.PJ_ROOMNOTICE_RESPONSE);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("roomId", roomVO.getRoomId());
		jsonObject.put("roomType", roomVO.getRoomType());
		jsonObject.put("roomVO", roomVO);
		
		if(status > 0){
			try {
				output.writeUTF(jsonObject.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				output.close();
			}
		}
	}
 
}


