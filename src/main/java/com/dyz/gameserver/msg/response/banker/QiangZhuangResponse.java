package com.dyz.gameserver.msg.response.banker;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;
import com.dyz.gameserver.pojo.RoomVO;

/**
 * 发送抢庄信息
 * @author Administrator
 *
 */
public class QiangZhuangResponse extends ServerResponse{

	public QiangZhuangResponse(int status,RoomVO roomVO) {
		super(status, ConnectAPI.PJ_QIANGZHUANG_RESPONSE);
		if(status > 0){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("cardNum", roomVO.getCardNum());
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
