package com.dyz.gameserver.test.listener;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyIoFutureListener implements IoFutureListener<IoFuture>{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void operationComplete(IoFuture future) {
		logger.info("operationComplete...");
	}
}
