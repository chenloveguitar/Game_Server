package com.dyz.gameserver.msg.response.dingerhong;

import java.io.IOException;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

/**
 * 
 * @author luck
 * 1：开始第一局时选择簸簸数
 * 2:  中途簸簸数用完时，添加簸簸数
 * 请求返回
 *
 */
public class AddDustpanScoreResponse extends ServerResponse {

	/**
	 * 
	 * @param status
	 * @param msgCode 0表示增加失败    其他表示添加成功 表示数量,添加玩家的索引    100,0
	 */
	public AddDustpanScoreResponse(int status, String msgCode) {
		super(status, ConnectAPI.ADDDUSTPAN_RESPONSE);
		if(status >0){
            try {
                output.writeUTF(msgCode);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
           	 output.close();
			}
        }
	}

}
