package com.dyz.gameserver.msg.processor.login;

import com.context.ErrorCode;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.logic.RoomLogic;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;
import com.dyz.gameserver.msg.response.ErrorResponse;
import com.dyz.gameserver.pojo.RoomVO;


/**
 * 断线重连之后 打牌进行到了什么步骤（该谁出牌，打牌，杠牌/碰牌/胡牌）
 * @author luck
 *
 */
public class LoginReturnInfoMsgProcessor extends MsgProcessor implements
INotAuthProcessor {

	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		
		Avatar avatar = gameSession.getRole(Avatar.class);
		if(avatar != null){
			RoomVO roomVO = avatar.getRoomVO();
			if(roomVO != null && roomVO.getRoomId() != 0){
				RoomLogic roomLogic = RoomManager.getInstance().getRoom(roomVO.getRoomId());
				if(roomLogic != null){
					if(roomVO.getRoomType() == 2){
    			    	roomLogic.LoginReturnInfo(avatar);
    			    }
    			    else if(roomVO.getRoomType() == 4){//架锅断线重连
    			    	roomLogic.LoginReturnInfo_DEH(avatar);
    			    }
//					roomLogic.LoginReturnInfo(avatar);
				}else{
					gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000005));
				}
			}
			else{
				System.out.println("房间已经不存在!");
			}
		}
		else{
			System.out.println("账户未登录或已经掉线!");
		}
	}

}
