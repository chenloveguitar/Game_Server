package com.dyz.gameserver.msg.response.banker;

import java.io.IOException;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;
/**
 * 发送抢庄成功消息
 * @author Administrator
 *
 */
public class QiangZhuangSucceedResponse extends ServerResponse{

	public QiangZhuangSucceedResponse(int status) {
		super(status, ConnectAPI.PJ_QIANGZHUANGSUCCEED_RESPONSE);
		 if(status > 0){
			 try {
				output.writeUTF("");
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
		 	output.close();
			}
		 }
	}
}
