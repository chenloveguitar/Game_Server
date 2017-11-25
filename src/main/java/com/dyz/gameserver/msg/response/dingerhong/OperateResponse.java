package com.dyz.gameserver.msg.response.dingerhong;

import java.io.IOException;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;
/**
 * 
 * @author luck
 * 当有玩家跟，大 丢 休 敲请求之后 返回给其他玩家的情况
 */
public class OperateResponse extends ServerResponse {
	
	
	
    /**
     * //1：个人簸箕分数   2： 已下注分数   3：本次下注分数     4：本次操作类型(跟？大？丢？。。)  5：中间池中分数   6：操作玩家索引
	 *	//7：下次操作玩家索引 8：下次操作玩家能够进行的操作
     * @param status
     * @param str    "dustpan", 0  
     * 						 "currentRoundBottomPours", roomVO.getDustpan()
	 *					      "currentRoundBottomPour", currentPool
	 *					     "opreateType", type
	 *					    "scorePool", scorePool
	 *						"avatarIndex", playerList.indexOf(avatar)
	 *						"nextAvatarIndex", getNextAvatarIndex()
	 *	                     operations,    0,1,2,3,4
	 *type为休和丢时  不返回分数相关变化
     */
	public OperateResponse(int status, String str) {
		super(status, ConnectAPI.OPERATE_RESPONSE);
		if(status >0){
            try {
                output.writeUTF(str);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
           	 output.close();
			}
        }
	}


}
