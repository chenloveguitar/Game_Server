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
 * 抢庄确认
 * @author Administrator
 *
 */
public class QiangZhuangMsgProcssor extends MsgProcessor implements INotAuthProcessor{

	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		JSONObject object = JSONObject.fromObject(request.getString());
		String type = (String) object.get("trye");
		if(GlobalUtil.checkIsLogin(gameSession)) {  
			Avatar avatar = gameSession.getRole(Avatar.class); //取得玩家对象
			if(avatar != null){
				if(avatar.avatarVO.getRoomId() != 0){
					RoomLogic roomLogic = RoomManager.getInstance().getRoom(avatar.avatarVO.getRoomId());
					roomLogic.qiangZhuangAffirm(avatar,type);
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
