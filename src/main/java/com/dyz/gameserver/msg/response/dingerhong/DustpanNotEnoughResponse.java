package com.dyz.gameserver.msg.response.dingerhong;

import java.io.IOException;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

/**
 * 告诉前端簸簸数不够了
 * @author luck
 *
 */
public class DustpanNotEnoughResponse extends ServerResponse{

	public DustpanNotEnoughResponse(int status, int msgCode) {
		super(status, ConnectAPI.DUSTPANNOTENOUGH_RESPONSE);
		if(status >0){
            try {
                output.writeUTF(""+msgCode);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
           	 output.close();
			}
        }
	}

}
