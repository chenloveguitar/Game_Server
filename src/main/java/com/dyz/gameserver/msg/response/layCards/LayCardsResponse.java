package com.dyz.gameserver.msg.response.layCards;

import java.io.IOException;
import java.util.List;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;
import com.sdicons.json.validator.impl.predicates.Int;

import net.sf.json.JSONObject;
/**
 * 发送躺牌消息
 * @author Administrator
 *
 */
public class LayCardsResponse extends ServerResponse{

	public LayCardsResponse(int status,int avatarId,int[] lieCardsList,List<Integer> putOffCardList,boolean isPengTing) {
	    /**
	     * 必须调用此方法设置消息号
	     *
	     * @param status
	     * @param
	     */
		super(1,ConnectAPI.LAYCARDS_RESPONSE); 
		if(status > 0){
			try {
	            JSONObject jsonObject = new JSONObject();
	            jsonObject.put("lieCardsList",lieCardsList);
	            jsonObject.put("avatarId",avatarId);
	            jsonObject.put("putOffCardList", putOffCardList);
	            jsonObject.put("isPengTing", isPengTing);
	            output.writeUTF(jsonObject.toString());
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		
	}

}
