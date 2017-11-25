package com.dyz.gameserver.msg.response.banker;

import java.io.IOException;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

import net.sf.json.JSONObject;

public class GameTypeResponse extends ServerResponse{

	public GameTypeResponse(int status,int uuid,int type) {
		super(status, ConnectAPI.PJ_GAMETYPE_RESPONSE);
		JSONObject json = new JSONObject();
		json.put("uuid", uuid);
		json.put("type", type);
		
		if(status > 0){
			 try {
				output.writeUTF(json.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
		 	output.close();
			}
		 }
	}

}
