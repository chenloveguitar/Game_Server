package com.dyz.gameserver.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.dyz.gameserver.bootstrap.GameServer;

public class GamserServerListener implements ServletContextListener{

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		
		GameServer gameServer = (GameServer) event.getServletContext().getAttribute("gameserver");
		gameServer.stop();
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		GameServer gameServer = GameServer.getInstance();
		event.getServletContext().setAttribute("gameserver", gameServer);
		gameServer.startUp();
	}

}
