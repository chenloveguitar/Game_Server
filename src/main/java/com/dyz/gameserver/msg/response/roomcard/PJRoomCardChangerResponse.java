package com.dyz.gameserver.msg.response.roomcard;

import java.io.IOException;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

import net.sf.json.JSONObject;
/**
 * 牌九分数改变
 * @author Administrator
 *
 */
public class PJRoomCardChangerResponse extends ServerResponse{

	public PJRoomCardChangerResponse(int status, int msgCode) {
		 super(status, ConnectAPI.PJROOMCARDCHANGER_RESPONSE);
	        if(status >0){
	            try {
	                output.writeUTF(msgCode+"");
	            } catch (IOException e) {
	                e.printStackTrace();
	            } finally {
	           	 output.close();
				}
	        }
		
	}

}
