package com.dyz.gameserver.msg.response.banker;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

/**
 * 
       定义：点击抢庄，告诉所有人抢庄的结果：抢了，还是没抢
 * @author Administrator
 * 0,未抢庄，1，已抢庄
 */

public class IsQiangZhuangResponse extends ServerResponse{

	public IsQiangZhuangResponse(int status, String type,int uuid) {
		super(status, ConnectAPI.PJ_ISQIANGZHUANG_RESPONSE);
		JSONObject json = new JSONObject();
		json.put("type", type);
		json.put("uuid", uuid);
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
