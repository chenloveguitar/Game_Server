package com.dyz.gameserver.msg.response.banker;

import java.io.IOException;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;
import com.dyz.gameserver.pojo.RoomVO;

import net.sf.json.JSONObject;

public class FaPaiResponse extends ServerResponse{

	public FaPaiResponse(int status, int [][]paiArray,RoomVO roomVO) {
		super(status, ConnectAPI.PJ_FAPAI_RESPONSE);
		JSONObject json = new JSONObject();
		json.put("paiArray", paiArray);
		json.put("cardNum", roomVO.getCardNum());
		if(status > 0){
			try {
				output.writeUTF(json.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				output.close();
			}
		}
	}

}
