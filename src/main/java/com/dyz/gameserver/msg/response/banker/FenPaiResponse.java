package com.dyz.gameserver.msg.response.banker;

import java.io.IOException;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;


/**
 * 分牌接口响应
 * @author Administrator
 *
 */

public class FenPaiResponse extends ServerResponse{

	public FenPaiResponse(int status) {
		super(status, ConnectAPI.PJ_FENPAI_RESPONSE);
		if(status > 0){
			 try {
				output.writeUTF("");
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
		 	output.close();
			}
		 }
	}

}
