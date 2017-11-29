package com.dyz.gameserver.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kevin on 2016/6/22.
 */
public class RoomVO {
	
    ////////////////////////新加字段/////////////////
	private int shuffle;//几局一洗 ，3。三局一洗，4.四局一洗
	
	private int inning=1;//当前第几局
	
	private int cardNum=4;//剩余牌数  32/8=4
	private boolean StartGame=false;
	private int multiplying;//100锅 300锅  500锅
	
	

	public boolean isStartGame() {
		return StartGame;
	}

	public void setStartGame(boolean startGame) {
		StartGame = startGame;
	}

	public int getCardNum() {
		return cardNum;
	}

	public void setCardNum(int cardNum) {
		this.cardNum = cardNum;
	}
	/**
	 * 开始时桌面的牌
	 */
	public List<Integer> listCard = new ArrayList<Integer>();
	
	/**
	 * 发牌后剩下的牌
	 */
	public List<Integer> remainListCard =new ArrayList<Integer>();
	
	/**
	 * 下张牌的索引
	 */
	public int nextCardindex = 0;
	
	
	
   
    public int getInning() {
		return inning;
	}

	public void setInning(int inning) {
		this.inning = inning;
	}
	/**
     * 检查是否开始游戏  0未开始 1已开始
     * @return
     */
    private int hasStart;
    /**
     * 1.二道
     * 2.三道    游戏规则
     * 3.直比
     */
    private int way;
    
    


	public int getWay() {
		return way;
	}

	public void setWay(int way) {
		this.way = way;
	}

	public int getShuffle() {
		return shuffle;
	}

	public void setShuffle(int shuffle) {
		this.shuffle = shuffle;
	}

	public int getHasStart() {
		return hasStart;
	}

	public void setHasStart(int hasStart) {
		this.hasStart = hasStart;
	}
	///////////////////////////////////////////////
	/**
     * 簸箕数上限
     */
    private int dustpan;
    
    public int getDustpan() {
		return dustpan;
	}

	public void setDustpan(int dustpan) {
		this.dustpan = dustpan;
	}
	/**
     * 房间底分
     */
    private int leastScore = 1;
    
    public int getLeastScore() {
		return leastScore;
	}

	public void setLeastScore(int leastScore) {
		this.leastScore = leastScore;
	}
	
	/**
	 * 新加字段 金币倍数场  10倍 20倍50倍
	 */
	 private int tianShuiCoinType;
	 
	 
	
	public int getTianShuiCoinType() {
		return tianShuiCoinType;
	}

	public void setTianShuiCoinType(int tianShuiCoinType) {
		this.tianShuiCoinType = tianShuiCoinType;
	}
	/**
	 * 新加的字段 游戏规则  0：麦积区（不胡青七对），1：秦州区（胡青七对）
	 */
	private int playRule;
	
	/**
	 * 支付方式  0:大赢家,1庄家
	 */
	private int payWay;
	
    /**
     * 房间ID
     */
    private int roomId;
    /**
     * 数据库表ID
     */
    private int id;
    /**
     * 房间的使用总次数
     */
    private int roundNumber;
    /**
     * 房间当前轮数
     */
    private int currentRound = 0;
    /**
     *是否红中当赖子
     */
    private boolean hong;
    /**
     * 房间模式，1-转转麻将。2-划水麻将。3-长沙麻将
     * 4.架锅
     */
    private int roomType;
    

    
    /**
     * 几人房
     */
    private int peoples;
    /**
     *七小对
     */
    private boolean sevenDouble;
    /**
     *抓码的个数
     */
    private int ma;
    /**
     *长沙麻将传入的倍率
     */
    private int magnification ;
    /**
     *是否自摸胡，还是可以抢杠胡
     *0 可抢杠胡(默认)   1自摸胡
     */
    private int ziMo;
    /**
     * 下鱼(漂)(0--10)
     */
    private int xiaYu;
    /**
     * 是否要字牌
     */
    private boolean addWordCard;

    /**
     * 房间名
     */
    public String name;
    /**
     * 整个房间对应的所有人的牌组
     */
    private List<AvatarVO> playerList;
    /**
     * 开一个房间几局游戏完后，统计所有玩家的杠，胡次数
     * 第一个key：用户uuid
     * 
     * 转转麻将
     * 第二个key：1:自摸(zimo) value次数，2:接炮(jiepao) value次数,3:点炮(dianpao)value次数,
     * 4:明杠(minggang)value次数，5:暗杠(angang) value次数 , 6: 总成绩(scores)  value分数
     *  
     *  
     *  划水麻将
     *  第二个key 不同  value 为番数
     *  跟庄("跟庄")	1番（庄家出3番，其他每人得1番）
		过路杠("glgang")	1番
		暗杠("angang")	2番
		放杠("fanggang")	3番（谁放牌谁出番）
		自摸("zimo")	4番（其他三家每家出4番）
		普通点炮(pudian)	5番（谁放炮谁出番）
		七对点炮(qidian)	5*3番（谁放炮谁出番）
		七对自摸(qizimo)	4*3番（其他三家每家出12番）
		杠开(gangkaihu)	4*3番（其他三家每家出12番）      （杠上花）
		抢杠(qiangganghu)	5*3番（谁要杠牌谁出番）         （抢杠胡）
     */
    private Map<String , Map<String,Integer>> endStatistics = new HashMap<String, Map<String,Integer>>();
    
	public Map<String, Map<String, Integer>> updateEndStatistics(String uuid , String type ,int roundScore) {
    		if(endStatistics.get(uuid) == null){
    			Map<String,Integer > map = new HashMap<String , Integer>();
        		map.put(type,roundScore);
        		endStatistics.put(uuid, map);
    		}
    		else{
    			if(endStatistics.get(uuid).get(type) != null){
    				endStatistics.get(uuid).put(type, endStatistics.get(uuid).get(type)+roundScore);
    			}
    			else{
    				endStatistics.get(uuid).put(type, roundScore);
    			}
    		}
    	
		return endStatistics;
	}

	  
    public Map<String, Map<String, Integer>> getEndStatistics() {
		return endStatistics;
	}
    
    
    
    
	

	public int getRoomId() {
        return roomId;
    }

	public void setEndStatistics(Map<String, Map<String, Integer>> endStatistics) {
		this.endStatistics = endStatistics;
	}

	public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }
    
    public int getCurrentRound() {
		return currentRound;
	}

	public void setCurrentRound(int currentRound) {
		this.currentRound = currentRound;
	}


	public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public int getMa() {
        return ma;
    }

    public void setMa(int ma) {
        this.ma = ma;
    }

    public int getZiMo() {
        return ziMo;
    }

    public void setZiMo(int ziMo) {
        this.ziMo = ziMo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getHong() {
        return hong;
    }

    public void setHong(boolean hong) {
        this.hong = hong;
    }

    public boolean getSevenDouble() {
        return sevenDouble;
    }

    public void setSevenDouble(boolean sevenDouble) {
        this.sevenDouble = sevenDouble;
    }

    public int getXiaYu() {
        return xiaYu;
    }

    public void setXiaYu(int xiaYu) {
        this.xiaYu = xiaYu;
    }

    public List<AvatarVO> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<AvatarVO> playerList) {
        this.playerList = playerList;
    }

    public boolean isAddWordCard() {
        return addWordCard;
    }

    public void setAddWordCard(boolean addWordCard) {
        this.addWordCard = addWordCard;
    }
    
    public int getPeoples() {
		return peoples;
	}


	public void setPeoples(int peoples) {
		this.peoples = peoples;
	}


	public RoomVO clone(){
    	RoomVO result = new RoomVO();
    	result.roomId = roomId;
        result.roundNumber = roundNumber;
        result.currentRound = currentRound;
        result.hong = hong;
        result.roomType = roomType;
        result.sevenDouble = sevenDouble;
        result.ma = ma;
        result.ziMo = ziMo;
        result.xiaYu = xiaYu;
        result.addWordCard = addWordCard;
        result.name = name;
        result.playerList = playerList;
        result.endStatistics = endStatistics;
        result.id = id;
        result.magnification = magnification;
        result.peoples = peoples;
        result.shuffle=shuffle;
        result.inning=inning;
        result.multiplying = multiplying;
        return result;
    }


	public int getMagnification() {
		return magnification;
	}


	public void setMagnification(int magnification) {
		this.magnification = magnification;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public int getPlayRule() {
		return playRule;
	}


	public void setPlayRule(int playRule) {
		this.playRule = playRule;
	}


	public int getPayWay() {
		return payWay;
	}


	public void setPayWay(int payWay) {
		this.payWay = payWay;
	}

	public int getMultiplying() {
		return multiplying;
	}

	public void setMultiplying(int multiplying) {
		this.multiplying = multiplying;
	}

	@Override
	public String toString() {
		return "RoomVO [shuffle=" + shuffle + ", inning=" + inning + ", cardNum=" + cardNum + ", StartGame=" + StartGame
				+ ", multiplying=" + multiplying + ", listCard=" + listCard + ", remainListCard=" + remainListCard
				+ ", nextCardindex=" + nextCardindex + ", hasStart=" + hasStart + ", way=" + way + ", dustpan="
				+ dustpan + ", leastScore=" + leastScore + ", tianShuiCoinType=" + tianShuiCoinType + ", playRule="
				+ playRule + ", payWay=" + payWay + ", roomId=" + roomId + ", id=" + id + ", roundNumber=" + roundNumber
				+ ", currentRound=" + currentRound + ", hong=" + hong + ", roomType=" + roomType + ", peoples="
				+ peoples + ", sevenDouble=" + sevenDouble + ", ma=" + ma + ", magnification=" + magnification
				+ ", ziMo=" + ziMo + ", xiaYu=" + xiaYu + ", addWordCard=" + addWordCard + ", name=" + name
				+ ", playerList=" + playerList + ", endStatistics=" + endStatistics + "]";
	}
	
	

    
}
