package com.dyz.gameserver.msg.processor.banker;

import com.context.ErrorCode;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.logic.RoomLogic;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;
import com.dyz.gameserver.msg.response.ErrorResponse;
import com.dyz.persist.util.GlobalUtil;

import net.sf.json.JSONObject;

/**
 * 庄家是否继续坐庄
 * @author Administrator
 *
 */
public class GoOnZuoZhuangMsgProcssor extends MsgProcessor implements INotAuthProcessor{

	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		JSONObject json = JSONObject.fromObject(request.getString());
		int type = (int) json.get("type");
		if(GlobalUtil.checkIsLogin(gameSession)){
			Avatar avatar = gameSession.getRole(Avatar.class);
			if(avatar != null){
				if(avatar.avatarVO.getRoomId() != 0){
					RoomLogic roomLogic = RoomManager.getInstance().getRoom(avatar.avatarVO.getRoomId());
					roomLogic.qiangZhuang(type+"");
				}else{
					gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000012));
				}
			}else{
				gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000002));
			}
		}else{
			gameSession.destroyObj();
		}
	}

}
