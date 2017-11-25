package com.dyz.gameserver.msg.processor.dingerhong;

import com.context.ErrorCode;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.logic.RoomLogic;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;
import com.dyz.gameserver.msg.response.ErrorResponse;
import com.dyz.gameserver.msg.response.dingerhong.AddDustpanScoreResponse;

/**
 * 
 * 1：开始第一局时选择簸簸数
 * 2:  中途簸簸数用完时，添加簸簸数
 * @author luck
 *
 */
public class AddDustpanScoreProcessor extends MsgProcessor implements
INotAuthProcessor  {

	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		if(gameSession.isLogin()) {
			Avatar avatar = gameSession.getRole(Avatar.class);
			RoomLogic roomLogic = RoomManager.getInstance().getRoom(avatar.getRoomVO().getRoomId());
			if(roomLogic != null){
				int addDustpan = Integer.parseInt(request.getString());
				if(addDustpan == -1){
					//拒绝添加簸簸数。则直接结束游戏
//					roomLogic.dissolveRoom();
				}
				else{
					String code = roomLogic.addDustpan(avatar,addDustpan);
					//返回 0 表示失败    其他表示添加成功且代表添加玩家和数量拼接    0,100
					for (int i = 0; i < roomLogic.getPlayerList().size(); i++) {
						roomLogic.getPlayerList().get(i).getSession().sendMsg(new AddDustpanScoreResponse(1,code));
					}
				}
			}else{
				gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000005));
			}
			
		}else{
			gameSession.destroyObj();
		}
	}

}
