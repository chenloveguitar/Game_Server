package com.dyz.gameserver.logic;

import java.util.Collections;
import java.util.List;

public class JaGuoLogic {
	
	/**
	 * 整张桌子上所有牌的数组
	 */
	private List<Integer> listCard = null;
	
	/**
	 * 下张牌的索引
	 */
	private int nextCardindex = 0;
	
	
	//1.创建房间（一洗三局）
	//2.加入房间
	//3.判断人数 开始游戏
	//4.抢庄
	//5.押注
	//6.比牌
	//7.赔分
	//8.断线重连
	//9.查看战绩
	//10.游戏回放
	
	/**
	 * 随机洗牌
	 */
	public void shuffleTheCards() {
		Collections.shuffle(listCard);
		Collections.shuffle(listCard);
	}
	
	/**
	 * 获取下一张牌的点数,如果返回为-1 ，则没有牌了
	 * 
	 * @return
	 */
	public int getNextCardPoint() {
		nextCardindex++;
		if (nextCardindex < listCard.size()) {
			return listCard.get(nextCardindex);
			// return 24;
		}
		return -1;
	}

}
