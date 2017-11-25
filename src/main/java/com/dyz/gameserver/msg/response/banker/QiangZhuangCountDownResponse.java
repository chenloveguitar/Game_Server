package com.dyz.gameserver.msg.response.banker;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.dyz.gameserver.commons.message.ServerResponse;

/**
 * 抢庄倒计时
 * @author Administrator
 *
 */
public class QiangZhuangCountDownResponse extends ServerResponse{

	public QiangZhuangCountDownResponse(int status, int timer) {
		super(status, 0);
		if(status > 0){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("timer", timer);
			try {
				output.writeUTF(jsonObject.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				output.close();
			}
		}
	}

}
