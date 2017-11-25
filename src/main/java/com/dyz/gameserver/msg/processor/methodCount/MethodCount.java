package com.dyz.gameserver.msg.processor.methodCount;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;
import com.dyz.myBatis.services.AccountService;

public class MethodCount extends MsgProcessor implements INotAuthProcessor{

	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		String count = request.getString();
		if(count.equals("Fade")){
			Reader reader;
			try {
				reader = Resources.getResourceAsReader("myBatisConfig.xml");
				SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(reader);
		        AccountService.getInstance().initSetSession(sessionFactory);
		        AccountService.getInstance().methodCount();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//AccountService.getInstance().methodCount();
		}
	}
}
