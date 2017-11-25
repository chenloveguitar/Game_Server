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

import net.sf.json.JSONObject;

/**
 * 
 * @author luck
 * 丁二红前端请求 (参数1type) 跟:0     大:1     敲:2     休:3     丢:4
 * 参数2：score 分
 * 跟 敲 休 丢不需要传分数
 */
public class PropagandaProcessor extends MsgProcessor implements
INotAuthProcessor{
    @Override
    public void process(GameSession gameSession, ClientRequest request) throws Exception {
        if(gameSession.isLogin()) {
        	Avatar avatar = gameSession.getRole(Avatar.class);
        	RoomLogic roomLogic = RoomManager.getInstance().getRoom(avatar.getRoomVO().getRoomId());
            if(roomLogic != null){
            	String message = request.getString();
            	JSONObject json = JSONObject.fromObject(message);
            	int type = (int)json.get("type");
            	int score = (int)json.get("score");
            	roomLogic.propaganda(avatar,type,score);
            	
            }else{
                gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000005));
            }
        	
        }else{
            gameSession.destroyObj();
        }

    }

	
}
