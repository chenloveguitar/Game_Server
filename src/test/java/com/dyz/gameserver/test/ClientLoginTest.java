package com.dyz.gameserver.test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyz.gameserver.pojo.RoomVO;
import com.dyz.persist.util.JsonUtilTool;

public class ClientLoginTest {
	public static final String IP_ADDR = "localhost";//服务器地址 
	public static final int PORT = 10122;//服务器端口号  
	private static Logger logger = LoggerFactory.getLogger(ClientLoginTest.class);
    public static void main(String[] args) {  
        System.out.println("客户端启动...");  
        System.out.println("当接收到服务器端字符为 \"OK\" 的时候, 客户端将终止\n");
        Socket socket = null;
        try {
        	socket = new Socket(IP_ADDR, PORT);  
        	DataInputStream input = new DataInputStream(socket.getInputStream());  
        	//向服务器端发送数据  
        	DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        	while (true) {
        		//创建一个流套接字并将其连接到指定主机上的指定端口号
	            //读取服务器端数据  
//				ObjectOutputStream  out = new ObjectOutputStream (socket.getOutputStream());
	            System.out.println("请输入code: \t");
	            String code = new BufferedReader(new InputStreamReader(System.in)).readLine();
	            System.out.println("请输入对象类型: \t");
	            String json = new BufferedReader(new InputStreamReader(System.in)).readLine();
				//登录操作，不同操作不同的ConnectAPI.CREATEROOM_REQUEST值    消息处理方式
				ClientSendRequest loginSend = new ClientSendRequest(Integer.valueOf(code));
				loginSend.output.writeUTF(json);
				out.write(loginSend.entireMsg().array());//
				serverCallBack(input);
        	} 
        } catch (Exception e) {
        		System.out.println("客户端异常:" + e.getMessage()); 
    	} finally {
    		if (socket != null) {
    			try {
					socket.close();
				} catch (IOException e) {
					socket = null; 
					System.out.println("客户端 finally 异常:" + e.getMessage()); 
				}
    		}
        }  
    }

	public static void serverCallBack(DataInputStream input){
		try {
			System.out.println("服务器端返回过来的是: " +input.readByte());
			int len = input.readInt();
			System.out.println("数据包长度:"+len);
			int code = input.readInt();
			System.out.println("服务器返回的状态码:"+code);
//			String ret = input.readUTF();
//			byte[] b = new byte[len];
//			String string = input.readLine();
//			System.out.println("服务器端返回过来的是: " + string);
			// 如接收到 "OK" 则断开连接
//			if ("OK".equals(ret)) {
//				System.out.println("客户端将关闭连接");
//			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}  