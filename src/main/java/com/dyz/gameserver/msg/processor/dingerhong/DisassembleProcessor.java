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

/**
 * 传入前端拆分过后的牌组
 * @author luck
 *
 *传入字符串  构造:拆分后的牌  1,2,3,4（12组合，34组合  不分大小）
 */
public class DisassembleProcessor extends MsgProcessor implements
INotAuthProcessor{

	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		 if(gameSession.isLogin()) {
			    //得到角色信息
	        	Avatar avatar = gameSession.getRole(Avatar.class);
	        	//得到房间的类型，并且在getRoom的方法中封装了游戏规则1,2，3
	        	RoomLogic roomLogic = RoomManager.getInstance().getRoom(avatar.getRoomVO().getRoomId());
	            if(roomLogic != null){
	            	//判断如果已经拆分
	            	if(!avatar.isDetached()){
	            		//获得字符串的信息
	            		String message = request.getString();
	            		//调用拆牌后传入的方法
	            		roomLogic.disassemble(avatar,message);
	            		
	            	}
	            }else{
	                gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000005));
	            }
	        }else{
	            gameSession.destroyObj();
	        }
	}

}
