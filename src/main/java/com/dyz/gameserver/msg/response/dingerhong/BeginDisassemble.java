package com.dyz.gameserver.msg.response.dingerhong;

import java.io.IOException;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

/**
 * 当检测到当前玩家叫牌之后就结束了，告诉前端开始拆牌
 * @author luck
 *
 */
public class BeginDisassemble  extends ServerResponse{

	public BeginDisassemble(int status, int msgCode) {
		super(status, ConnectAPI.BEGINDISASSEMBLE_RESPONSE);
		if(status >0){
            try {
				output.writeUTF(""+msgCode);
            } catch (IOException e) {
                e.printStackTrace();
			}finally {
           	 output.close();
			}
        }
	}

}
