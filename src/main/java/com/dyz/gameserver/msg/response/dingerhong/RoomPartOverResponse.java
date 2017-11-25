package com.dyz.gameserver.msg.response.dingerhong;

import java.io.IOException;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

/**
 * 
 * 整个房间中的单局结束返回信息
 * @author luck
 *
 */
public class RoomPartOverResponse extends ServerResponse  {

	public RoomPartOverResponse(int status, String str) {
		super(status, ConnectAPI.PARTOVER_RESPONSE);
		if(status >0){
            try {
                output.writeUTF(str);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
           	 output.close();
			}
        }
	}

}
