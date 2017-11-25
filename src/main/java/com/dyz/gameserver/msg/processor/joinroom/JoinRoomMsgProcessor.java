package com.dyz.gameserver.msg.processor.joinroom;

import com.context.ErrorCode;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.logic.RoomLogic;
import com.dyz.gameserver.manager.GameSessionManager;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;
import com.dyz.gameserver.msg.response.ErrorResponse;
import com.dyz.persist.util.GlobalUtil;
import net.sf.json.JSONObject;

/**
 * 
 * @author luck
 * 加入房间
 */
public class JoinRoomMsgProcessor extends MsgProcessor implements
		INotAuthProcessor {

	public JoinRoomMsgProcessor() {
	}

	@Override
	public void process(GameSession gameSession, ClientRequest request)
			throws Exception {
		if(GlobalUtil.checkIsLogin(gameSession)) {
			JSONObject json = JSONObject.fromObject(request.getString());
			int roomId = (int)json.get("roomId");
			//if (avatar != null) {
				RoomLogic roomLogic = RoomManager.getInstance().getRoom(roomId);
				if (roomLogic != null) {
					if(roomLogic.getRoomVO().isStartGame()){
						gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000088));
					}else{
					Avatar avatar = gameSession.getRole(Avatar.class);
					if(avatar.avatarVO.getRoomId() == 0){
						
						int rate=roomLogic.getRoomVO().getTianShuiCoinType();
						int koudian=61; 
						if(rate/10==2){
							koudian=300;
						}else if(rate/10==5){
							koudian=1000;
						}
						int fen = 0;
						if(roomLogic.getRoomVO().getMultiplying() == 100){
							fen = 100;
						}
						else if(roomLogic.getRoomVO().getMultiplying() == 300){
							fen = 500;
						}
						else if(roomLogic.getRoomVO().getMultiplying() == 500){
							fen = 1000;
						}
						
						if(roomLogic.getRoomVO().getRoomType()==2 && avatar.avatarVO.getAccount().getRoomcard()>=koudian)
						{
							GameSessionManager.getInstance().sessionMap.put("uuid_"+avatar.getUuId(), gameSession);
							roomLogic.intoRoom(avatar);
							return;
						}else if(roomLogic.getRoomVO().getRoomType()==4 && 
								avatar.avatarVO.getAccount().getRoomcard() >= fen){
							
							GameSessionManager.getInstance().sessionMap.put("uuid_"+avatar.getUuId(), gameSession);
							roomLogic.intoRoom(avatar);
							return;
						}
						else
						{
							gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000014));
						}
						
					
					}
					else if(avatar.avatarVO.getRoomId() == roomId)
						{
							roomLogic.returnBackAction(avatar,false,-1);
							
						}
					
					//boolean joinResult = roomLogic.intoRoom(avatar);
					/*if(joinResult) {
						//system.out.println("加入房间成功");
					}else{
						//system.out.println("加入房间失败");
					}*/
					}
				} else {
					gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000018));
				} 
			//}
			//system.out.println("roomId --> " + roomId);
		}
		else{
			//system.out.println("该用户还没有登录");
			gameSession.destroyObj();
			
		}
	}

}
