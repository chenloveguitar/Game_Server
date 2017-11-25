package com.dyz.gameserver.msg.response.dingerhong;

import java.io.IOException;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

/**
 * 整个房间结束 返回消息
 * @author luck
 *
 */
public class RoomTotalOverResponse extends ServerResponse  {

	/**
	 * 
	 * @param status
	 * @param str  每个玩家得分,格式(玩家1得分,玩家2得分,玩家3得分,玩家4得分)根据房间玩家索引顺序
	 */
	public RoomTotalOverResponse(int status, String str) {
		super(status, ConnectAPI.TOTALOVER_RESPONSE);
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
