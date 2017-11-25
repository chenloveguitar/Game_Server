package com.dyz.gameserver.msg.response.dingerhong;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

/**
 * 把牌组传入后代处理之后，传入前端
 * @author luck
 *
 */
public class DisassembleResponse extends ServerResponse{

	/**
	 * 
	 * @param status
	 * @param preCode  第一组
	 * @param afterCode 第二组
	 */
	public DisassembleResponse(int status, String preCode , String afterCode) {
		super(status, ConnectAPI.DISASSEMBLE_RESPONSE);
		if(status >0){
            try {
            	JSONObject json = new JSONObject();
            	json.put("preCode", preCode);
            	//json.put("afterCode", afterCode);
                output.writeUTF(json.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
           	 output.close();
			}
        }
	}

}
