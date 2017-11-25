package com.dyz.gameserver.msg.response.dingerhong;

import java.io.IOException;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

/**
 * 发牌
 * @author luck
 * 
 * 1:发送给自己时
 */
public class PickCard_DEH_Response extends ServerResponse{

	/**
	 * 
	 * @param status
	 * @param str json字符串   
	 *               json.put("data", array)//每个人摸得牌
	 *               json.put("OperaterIndex", nextOperateAvatar);//摸玩牌之后该那个玩家操作
	 */
	public PickCard_DEH_Response(int status,String Str) {
		super(status, ConnectAPI.PICKCARD_DEH_RESPONSE);
		try {
			output.writeUTF(Str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			output.close();
		}
	}

}
