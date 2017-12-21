package com.dyz.gameserver.test;

import com.alibaba.fastjson.JSONObject;
import com.dyz.gameserver.pojo.LoginVO;
import com.dyz.gameserver.pojo.RoomVO;

public class CreateData {
	public static void main(String[] args) {
		LoginVO loginVO = new LoginVO();
		loginVO.setOpenId("1");
		loginVO.setCity("成都");
		loginVO.setIP("222.209.32.192");
		loginVO.setNickName("Hey,Alien");
		loginVO.setProvince("四川");
		loginVO.setSex(1);
		loginVO.setUnionid("10001");
		String json = JSONObject.toJSONString(loginVO);
		System.out.println(json);
		
		RoomVO roomVO = new RoomVO();
		roomVO.setTianShuiCoinType(50);
		json = JSONObject.toJSONString(roomVO);
		System.out.println(json);
	}

}
