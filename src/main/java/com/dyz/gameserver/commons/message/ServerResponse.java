package com.dyz.gameserver.commons.message;

import org.apache.mina.core.buffer.IoBuffer;

import com.dyz.gameserver.net.codec.MsgProtocol;

/**
 * 服务端发给客户端的消息。 所有返回给客户端的消息都最好继承于它.<br>
 * 这里封装了基本的输出字节操作。
 * 
 * @author dyz
 * 
 */
public class ServerResponse implements ResponseMsg {
	protected MsgBodyWrap output = MsgBodyWrap.newInstance4Out();
	private int msgCode;
	private int status;
	/**必须调用此方法设置消息号*/
	public ServerResponse(int status,int msgCode) {
		setStatus(status);
		setMsgCode(msgCode);
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setMsgCode(int code) {
		msgCode = code;
	}

	public synchronized  IoBuffer entireMsg() {
		
			byte[] body = output.toByteArray();
			/* 标志 byte 长度short */
			int length = MsgProtocol.flagSize+MsgProtocol.lengthSize+MsgProtocol.msgCodeSize+ body.length+4;
			//System.out.println("计算长度"+length);
			IoBuffer buf = IoBuffer.allocate(length);
			buf.setAutoShrink(true);  
			buf.put(MsgProtocol.defaultFlag);//flag
			buf.putInt(length);//lengh
			buf.putInt(msgCode);
			buf.putInt(status);
			buf.put(body);
			buf.flip();
			buf.free();//性能优化(10-25)
			//System.out.println("buf实际长度---"+buf.capacity());
			return buf;
	}

	/**
	 * 释放资源(数据流、对象引用)
	 */
	public synchronized void release() {
		if (output != null) {
			output.close();
			output = null;
		}
		output = null;
	}
}