package com.dyz.gameserver.msg.response.banker;

import java.io.IOException;

/**
 * 当前玩家取庄家
 */

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

import net.sf.json.JSONObject;

public class ZhuangJiaQueRenRepsonse extends ServerResponse{

	public ZhuangJiaQueRenRepsonse(int status,int uuid) {
		super(status, ConnectAPI.PJ_ZHUANGJIAQUEDING_RESPONSE);
		if(status > 0){
			JSONObject json = new JSONObject();
			json.put("uuid", uuid);
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
