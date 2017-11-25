package com.dyz.gameserver.msg.response.dingerhong;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.context.ConnectAPI;
import com.dyz.gameserver.commons.message.ServerResponse;

public class StartGame_DEHResponse extends ServerResponse {
    /**
    *
    * @param status
    * @param dustpan 各个玩家的索引以及，簸箕分数 (index1:120,index2:55)
    * @param bankerId 庄家ID
    * @param paiArray 牌组
    */
   public StartGame_DEHResponse(int status,int[][] paiArray,String dustpan,int bankerId) {
       super(status, ConnectAPI.STARTGAME_DEH_RESPONSE);
       try {
           JSONObject json = new JSONObject();
           json.put("dustpan",dustpan);
           json.put("bankerId",bankerId);
           json.put("paiArray",paiArray);
           output.writeUTF(json.toString());
       } catch (IOException e) {
           e.printStackTrace();
       }
       entireMsg();
   }

}
