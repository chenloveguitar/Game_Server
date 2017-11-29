package com.dyz.gameserver.test;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpTest {

	public static void main(String[] args) throws Exception {
//		String regexp = "<li id=\"[0-9a-zA-Z_]+\" url=\"([^\"]+)\">";
		String regexp = "<input type=\"checkbox\" value=\"([^\"]+)\"";
		List<String> urls = new ArrayList<String>();
		Pattern pattern = Pattern.compile(regexp);
		StringBuilder html = new StringBuilder();
		FileInputStream inputStream = new FileInputStream("C:\\Users\\jumili\\Desktop\\电影列表.html");
		byte[] bytes = new byte[100];
		int len = 0;
		while((len = inputStream.read(bytes)) != -1){
			html.append(new String(bytes, 0, len, "utf-8"));
		}
		Matcher matcher = pattern.matcher(html);
		int url = matcher.groupCount();
		while(matcher.find()){
			System.out.println(matcher.group(1));
			urls.add(matcher.group(1));
		}
		System.out.println(urls.size());
		System.out.println(url);
	}
}
