package com.dyz.gameserver.test;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.context.ErrorCode;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.msg.response.ErrorResponse;
import com.dyz.myBatis.services.AccountService;

public class Test {
	static int time;
	public static void main(String[] args) {
		switch (13) {
		case 12:
			System.out.println(71);
		case 16:
			System.out.println(71);
			break;
		default:
			System.out.println(11111);
			break;
		}
	}

}
