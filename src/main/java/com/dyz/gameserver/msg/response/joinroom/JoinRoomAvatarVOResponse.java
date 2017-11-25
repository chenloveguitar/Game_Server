package com.dyz.gameserver.msg.response.joinroom;

import java.io.IOException;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;
import com.dyz.gameserver.pojo.AvatarVO;

public class JoinRoomAvatarVOResponse extends ServerResponse {

	public JoinRoomAvatarVOResponse(int status,List<AvatarVO> avatar) {
		super(status,ConnectAPI.JOIN_ROOMAvatarVO_RESPONSE);
		try {
			if(status>0){
				JSONObject json = new JSONObject();
				json.put("avatarVO", avatar);
				output.writeUTF(json.toString());
			}
			else{
				output.writeUTF(avatar.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
       	 output.close();
		}
		//entireMsg();
	}

}
