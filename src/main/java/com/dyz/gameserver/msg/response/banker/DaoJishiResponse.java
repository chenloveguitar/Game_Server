package com.dyz.gameserver.msg.response.banker;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

/**
 * 抢庄倒计时 0抢庄 1押注.2，游戏选择倒计时      time
 * @author Administrator
 *
 */

public class DaoJishiResponse  extends ServerResponse{

	public DaoJishiResponse(int status, int type,int time) {
	
		super(status, ConnectAPI.PJ_DAOJISHI_RESPONSE);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", type);
		jsonObject.put("time", time);
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
