package com.dyz.gameserver.msg.processor.host;

import java.io.IOException;

import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.context.GameServerContext;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;
import com.dyz.gameserver.msg.response.host.IndexInfosResponse;

/**
 * 充值检测
 * @author luck
 *
 */
public class ChongZhiProcessor extends MsgProcessor implements
        INotAuthProcessor {
    @Override
    public void process(GameSession gameSession, ClientRequest request) throws Exception {
    	
    }
	@Override
	public void handle(GameSession gameSession, ClientRequest request) {
		try {
			String type = request.getString();
			int uuid = Integer.parseInt(type);
			Avatar avatar = GameServerContext.getAvatarFromOn(uuid);
			if(avatar == null){
				avatar = GameServerContext.getAvatarFromOff(uuid);
			}
			if(avatar != null && avatar.avatarVO.getRoomId() > 0){
				gameSession.sendMsg(new IndexInfosResponse(1,"0"));
			}else{
				gameSession.sendMsg(new IndexInfosResponse(1,"1"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}    
	}
    
}
