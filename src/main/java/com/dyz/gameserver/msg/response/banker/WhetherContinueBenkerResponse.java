package com.dyz.gameserver.msg.response.banker;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;
/**
 * 庄家是否继续坐庄
 * @author Administrator
 *
 */
public class WhetherContinueBenkerResponse extends ServerResponse{

	public WhetherContinueBenkerResponse(int status) {
		super(status, ConnectAPI.PJ_ZUOZHUANG_RESPONSE);
		if(status > 0){
			try {
				output.writeUTF("");
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				output.close();
			}
		}
	}

}
