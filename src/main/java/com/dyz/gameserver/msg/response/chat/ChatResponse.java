package com.dyz.gameserver.msg.response.chat;

import java.io.IOException;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

import net.sf.json.JSONObject;


public class ChatResponse extends ServerResponse{

	public ChatResponse(int status, String msgCode) {
		super(status, ConnectAPI.CHAT_RESPONSE);
	    try {
			output.writeUTF(msgCode);
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
            output.close();
        }
	}
}
