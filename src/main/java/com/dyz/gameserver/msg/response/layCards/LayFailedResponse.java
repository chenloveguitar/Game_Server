package com.dyz.gameserver.msg.response.layCards;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;
import com.sdicons.json.validator.impl.predicates.Int;

public class LayFailedResponse extends ServerResponse{

	public LayFailedResponse(int status,String layPai) {
		super(1,ConnectAPI.LAYFAILED_RESPONSE);
		JSONObject json = new JSONObject();
		if(status > 0){
			json.put("layPai", layPai);
			try {
				output.writeUTF(json.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
