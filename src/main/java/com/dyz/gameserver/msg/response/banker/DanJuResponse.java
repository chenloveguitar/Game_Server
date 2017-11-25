package com.dyz.gameserver.msg.response.banker;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

public class DanJuResponse extends ServerResponse{

	public DanJuResponse(int status,String msg) {
		super(status, ConnectAPI.PJ_DANJUJIESUAN_RESPONSE);
		if(status > 0){
			 try {
				output.writeUTF(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
		 	output.close();
			}
		 }
	}

}
