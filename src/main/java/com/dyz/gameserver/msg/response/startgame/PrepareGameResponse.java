package com.dyz.gameserver.msg.response.startgame;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

/**
 * Created by kevin on 2016/6/22.
 */
public class PrepareGameResponse extends ServerResponse {
	/**
	 *
	 * @param status
	 * @param  avatarIndex 准备人的索引
     */
	public PrepareGameResponse(int status,int avatarIndex) {
		super(status, ConnectAPI.PrepareGame_MSG_RESPONSE);
		try {
			JSONObject json = new JSONObject();
			json.put("avatarIndex", avatarIndex);
			json.put("beForePaiZu", "[1,2,3,4,5,6]");
			json.put("uuid", "[1,2,3,4,5,6]");
			output.writeUTF(json.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
       	 output.close();
		}
		//entireMsg();
	}
}