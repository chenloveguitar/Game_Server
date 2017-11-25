package com.dyz.gameserver.msg.processor.location;

import java.net.URL;

import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;
import com.dyz.persist.util.GlobalUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
/**
 * 获得用户经纬度
 * @author Administrator
 *
 */
public class LocationMsgProcessor extends MsgProcessor implements INotAuthProcessor{

	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		if(GlobalUtil.checkIsLogin(gameSession)) {
			JSONObject json = JSONObject.fromObject(request.getString());
			//纬度
			double latitude = (double) json.get("latitude");
			//经度
			double longitude = (double) json.get("longitude");
			Avatar avatar = gameSession.getRole(Avatar.class);
			
			String str = "";
//		    String add = getAdd("31.325152", "120.558957");  
		    String add = getAdd(latitude+"", longitude+""); 
		    JSONObject jsonObject = JSONObject.fromObject(add);  
		    JSONArray jsonArray = JSONArray.fromObject(jsonObject.getString("addrList"));  
		    JSONObject j_2 = JSONObject.fromObject(jsonArray.get(0));  
		    String allAdd = j_2.getString("admName");  
		    String arr[] = allAdd.split(",");  
		    String strArr = j_2.getString("addr"); 
		    String name = j_2.getString("name"); 
		    for(int i = 0;i < arr.length;i++){
		    	str += arr[i];
		    }
//		    System.out.println("省："+arr[0]+"\n市："+arr[1]+"\n区："+arr[2]);  
		    avatar.avatarVO.setLocation("经纬度定位："+str+strArr+name);
		}
		else{
			gameSession.destroyObj();
		}
	}
	/**
	 * 
	 * @param lat纬度
	 * @param log经度
	 * @return
	 */
	public static String getAdd(String lat , String log){  
	    //lat 小  log  大  
	    //参数解释: 纬度,经度 type 001 (100代表道路，010代表POI，001代表门址，111可以同时显示前三项)  
	    String urlString = "http://gc.ditu.aliyun.com/regeocoding?l="+lat+","+log+"&type=010";  
	    String res = "";     
	    try {     
	        URL url = new URL(urlString);    
	        java.net.HttpURLConnection conn = (java.net.HttpURLConnection)url.openConnection();    
	        conn.setDoOutput(true);    
	        conn.setRequestMethod("POST");    
	        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(),"UTF-8"));    
	        String line;    
	       while ((line = in.readLine()) != null) {    
	           res += line+"\n";    
	     }    
	        in.close();    
	    } catch (Exception e) {    
	        System.out.println("error in wapaction,and e is " + e.getMessage());    
	    }   
//	    System.out.println(res);  
	    return res;    
	}  
}
