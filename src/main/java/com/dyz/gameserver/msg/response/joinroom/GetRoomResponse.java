package com.dyz.gameserver.msg.response.joinroom;

import java.io.IOException;
import java.util.Map;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;
import com.dyz.gameserver.pojo.RoomVO;
/**
 * 发送场次信息
 * @author Administrator
 *
 */
public class GetRoomResponse extends ServerResponse{

	public GetRoomResponse(int status, String room) {
		super(status, ConnectAPI.PJ_GETROOM_RESPONSE);
		if(status > 0){
			try {
				output.writeUTF(room);
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				output.close();
			}
		}
	}

}
