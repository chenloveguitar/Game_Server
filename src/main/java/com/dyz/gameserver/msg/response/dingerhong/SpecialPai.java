package com.dyz.gameserver.msg.response.dingerhong;

import java.io.IOException;

import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

public class SpecialPai extends ServerResponse{

	public SpecialPai(int status, String pai) {
		super(status, ConnectAPI.SPECIAL_PAI);
		if(status >0){
            try {
                output.writeUTF(pai);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
           	 output.close();
			}
        }
	}

}
