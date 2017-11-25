package com.dyz.gameserver.msg.response.joinroom;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;
import com.dyz.gameserver.pojo.AvatarVO;
import com.dyz.persist.util.JsonUtilTool;

import java.io.IOException;
import java.util.List;
/**
 * 开始游戏时发送所有玩家信息
 * @author Administrator
 *
 */
public class JoinAvatarVOResponse extends ServerResponse {

	public JoinAvatarVOResponse(int status,String players) {
		super(status,ConnectAPI.JOIN_AVATARVO_RESPONSE);
		try {
			if(status>0){
					output.writeUTF(JsonUtilTool.toJson(players));
			}
			else{
				output.writeUTF(players.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
       	 output.close();
		}
		//entireMsg();
	}

}
