package com.dyz.gameserver.msg.response.banker;

import java.io.IOException;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

import net.sf.json.JSONObject;

public class XiaZhuResponse extends ServerResponse{

	public XiaZhuResponse(int status,int score,int uuid) {
		super(status, ConnectAPI.PJ_XIAZHU_RESPONSE);
		JSONObject json = new JSONObject();
		json.put("score", score);
		json.put("uuid", uuid);
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
