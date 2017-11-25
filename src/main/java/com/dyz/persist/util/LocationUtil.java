package com.dyz.persist.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import net.sf.json.JSONObject;

public class LocationUtil {

	/**
	 * 通过ip获得地址
	 * @param strIP
	 * @return
	 */
	public static String getAddressByIP(String strIP){ 
	  	String str = "";
	  	if(StringUtil.isEmpty(strIP)){
	  		return "";
	  	}
		try{
	  	    URL url = new URL("http://ip.taobao.com/service/getIpInfo.php?ip="+strIP); 
	  	    URLConnection conn = url.openConnection(); 
	  	    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8")); 
	  	    String line = null; 
	  	    StringBuffer result = new StringBuffer(); 
	  	    while((line = reader.readLine()) != null){ 
	  	      result.append(line); 
	  	    } 
	  	    reader.close(); 
	  	    JSONObject jsonObject = JSONObject.fromObject(result.toString());
	  	    JSONObject json = JSONObject.fromObject(jsonObject.get("data"));
	  	    str += "IP定位：";
	  	    str += (String) json.get("country");
	  	    str += (String) json.get("area");
	  	    str += (String) json.get("region");
	  	    str += (String) json.get("city");
	  	    str += (String) json.get("isp");
	  	  }
	  	  catch( IOException e){ 
	  	    return "读取失败"; 
	  	  }
	  	return str;
	}
}
