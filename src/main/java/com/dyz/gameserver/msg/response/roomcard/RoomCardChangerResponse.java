package com.dyz.gameserver.msg.response.roomcard;

import java.io.IOException;
import com.alibaba.fastjson.JSONObject;
import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;



public class RoomCardChangerResponse extends ServerResponse{

	public RoomCardChangerResponse(int status, int msgCode) {
		 super(status, ConnectAPI.ROOMCARDCHANGER_RESPONSE);
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
	public RoomCardChangerResponse(int status, JSONObject msgCode) {
		 super(status, ConnectAPI.P_J_DANJUJIESUAN_RESPONSE);
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
