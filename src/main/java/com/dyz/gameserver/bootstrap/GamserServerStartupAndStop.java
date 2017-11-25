package com.dyz.gameserver.bootstrap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class GamserServerStartupAndStop implements ServletContextListener{

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
