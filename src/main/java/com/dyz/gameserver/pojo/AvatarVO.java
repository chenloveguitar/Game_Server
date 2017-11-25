package com.dyz.gameserver.pojo;

import java.util.ArrayList;
import java.util.List;

import com.dyz.myBatis.model.Account;
import com.dyz.persist.util.StringUtil;

/**
 * Created by kevin on 2016/6/23.
 * 临时所有玩家数据
 */
public class AvatarVO {
	
	/**
	 * 新添加的
	 */
	/**
     * 定缺的类型 -0-未定缺，1-万，2-条，3-筒
     */
    private int missType;
    /**
     * 定位
     */
    private String location = "";
    /**
     * 是否需要自摸 - 口子牌
     */
    private boolean kouZi = false;
    /**
     * 当局结果 0:输 1：平 2：赢
     * @return
     */
    private int typeResult = -1;
    
	public int getTypeResult() {
		return typeResult;
	}
	public void setTypeResult(int typeResult) {
		this.typeResult = typeResult;
	}
    
    /**
     * 闲家确定大小
     * true 已确定
     * false 未确定
     */
    private boolean player = false;
    
    
    public boolean isPlayer() {
		return player;
	}
	public void setPlayer(boolean player) {
		this.player = player;
	}
	/**
     * 下注金额
     * @return
     */
    private int xiazhuScore;
    
    public int getXiazhuScore() {
		return xiazhuScore;
	}
	public void setXiazhuScore(int xiazhuScore) {
		this.xiazhuScore = xiazhuScore;
	}
	/**
     * 游戏规则
     * 1.二道
     * 2.三道    
     * 3.直比
     */
    private int way;
    
    
	public int getWay() {
		return way;
	}
	public void setWay(int way) {
		this.way = way;
	}
	/**
	 * 房间类型
	 */
	private int roomType;
	
	public int getRoomType() {
		return roomType;
	}
	public void setRoomType(int roomType) {
		this.roomType = roomType;
	}

	/**
	 * 抢庄资格
	 */
	private boolean qiangZhuang = false;
	
   
    public boolean isQiangZhuang() {
		return qiangZhuang;
	}
	public void setQiangZhuang(boolean qiangZhuang) {
		this.qiangZhuang = qiangZhuang;
	}
	/**
     * 躺牌的胡牌
     */
    private List<Integer> layHu = new ArrayList<Integer>();
    
    /**
     * 躺牌时可以出的牌
     */
    private List<Integer> layChu = new ArrayList<>();
    
    /**
     * 躺牌的牌
     */
    private int[] lay;
	
    /**
     * 用户基本信息
     */
    private Account account;
    /**
     * 房间号
     */
    private int roomId;
    /**
     * 是否准备
     */
    private boolean isReady = false;
    /**
     * 是否是庄家
     */
    private boolean isMain = false;
    /**
     * 是否在线
     */
    private boolean isOnLine = false;
    /**
     * 当前分数，起始分1000
     */
    private int scores = 0;
    public void setScores(int scores) {
		this.scores = scores;
	}
	/**
     * 打了的牌的字符串  1,2,3,4,5,6,1,3,5 格式
     */
    private List<Integer>  chupais = new ArrayList<Integer>();
    /**
     * 碰牌过
     */
    private List<Integer> pengGuo = new ArrayList<Integer>();
    /**
     * 普通牌张数
     */
    private int commonCards;
    /**
     * 摸牌出牌状态 
     * 摸了牌/碰/杠 true  出牌了false
     * 为true 表示该出牌了    为false表示不该出牌
     */
    private boolean hasMopaiChupai = false;
    /**
     * 划水麻将 ： 胡牌的类型(1:普通小胡(点炮/自摸)    2:大胡(点炮/自摸))
     * 长沙麻将 ： 胡的番数（0：普通胡   其他胡1 2 3 ） 得分为2的huType次方(大胡番数叠加)
     * 放弃操作，摸牌，出牌，都需要重置
     * 
     */
    private int huType = 0;
    /**
     * 牌数组
     * /碰 1  杠2  胡3  吃4
     */
    private int[][] paiArray=new int[1][1];
    /**
     * 存储整局牌的 杠，胡以及得分情况的对象，游戏结束时直接返回对象
     */
    private HuReturnObjectVO  huReturnObjectVO=new HuReturnObjectVO();
    
    private String IP;
    
    
    

	/**
	 *胡牌后的番数
	 */
	private int multiple;
	
	
	/**
	 * 胡牌的顺序
	 */
	private int huNumber = 0;
	/**
	 * 胡牌的点数
	 */
	private int huCount = 0;
	/**
	 *三张要换的牌
	 */
	private List<Integer> changeCardList = new ArrayList<>();
	/**
	 * 报胡	0、未开始 1、有叫确认报胡 2、无叫确认不报胡 3、正在选择
	 */
	private int baohu;
	/**
	 * 大邑麻将-请胡
	 */
	private boolean qingHu = false;
	/**
	 * 大邑麻将-控制出牌
	 */
	private boolean chuPai = false;

    //丁二红
    /**
     * 个人的簸箕总分(当中途某人想增加簸簸数时需要增加，整个房间结束时，用整个参数算最终输赢分数)
     */
    private int totalDustpan;
    /**
     * 个人的簸箕的实时数
     */
    private int dustpan;
    /**
     * 本轮下注分
     */
    private int currentRoundBottomPour;
    /**
     * 中间分池子中包含自己的总的下注分
     */
    private int currentRoundBottomPours;
    /**
     * 丁二红摸的牌的数组
     */
    private int [] pickCards = new int [2];
    /**
     * 玩家当前状态     跟0   大1    敲2    休3    丢4 丁二黄
     * 玩家当前操作	
     * 0.准备	
     * 1.开始抢庄 - 可以抢庄
     * 2.开始抢庄 - 不可以抢庄
     * 3.抢庄
     * 4.不抢庄
     * 5.房间选项
     * 6.下注
     * 7.未分牌
     * 8.已分牌
     * 9.结束
     * 
     * 初始值 -1
     */
    private int currentType = -1;
    /**
     * 牌组,已经牌好序 1,2,3,4,5(第一组牌点数(1,2组成)),6(第二组牌点数(3,4组成))
     */
    private List<Integer> paizu = new ArrayList<>();
    /**
     * 头牌大小占的位置越大数字越大
     */
    private int headIndex;
    private String headName;
    /**
     * 尾牌大小占的位置越大数字越大
     */
    private int tailIndex;
    private String tailName;
//    /**
//     * 一局结束之后自己的总分(初始为-1，用来判断用户是否进行了分数处理)，新的一局需要重置
//     */
//    private int endScores = -1;
    /**
     * 分数是否全部处理完成
     */
    private boolean hasOver =false;
    /**
     * 一局结束之后输(负)赢(正)的分数
     */
    private int winScores = 0;
    /**
     * 整个房间结束时分数
     */
    private int totalWinScores = 0;
    
    
    
    
    
	public String getHeadName() {
		return headName;
	}
	public void setHeadName(String headName) {
		this.headName = headName;
	}
	public String getTailName() {
		return tailName;
	}
	public void setTailName(String tailName) {
		this.tailName = tailName;
	}
	public int getWinScores() {
		return winScores;
	}
	public void setWinScores(int winScores) {
		this.winScores = winScores;
	}

	public void updateWinScores(int winScores) {
		this.winScores = this.winScores +winScores;
	}
	public boolean isHasOver() {
		return hasOver;
	}
	public void setHasOver(boolean hasOver) {
		this.hasOver = hasOver;
	}
//	public int getEndScores() {
//		return endScores;
//	}
//	public void setEndScores(int endScores) {
//		this.endScores = endScores;
//	}
	
	public int getHeadIndex() {
		return headIndex;
	}
	public int getTotalWinScores() {
		return totalWinScores;
	}
	public void setTotalWinScores(int totalWinScores) {
		this.totalWinScores = totalWinScores;
	}
	public void setHeadIndex(int headIndex) {
		this.headIndex = headIndex;
	}
	public int getTailIndex() {
		return tailIndex;
	}
	public void setTailIndex(int tailIndex) {
		this.tailIndex = tailIndex;
	}
	public List<Integer> getPaizu() {
		return paizu;
	}
	public void setPaizu(List<Integer> paizu) {
		this.paizu = paizu;
	}
	
	public int getTotalDustpan() {
		return totalDustpan;
	}
	public void setTotalDustpan(int totalDustpan) {
		this.totalDustpan = totalDustpan;
	}
	public int getDustpan() {
		return dustpan;
	}
	public void setDustpan(int dustpan) {
		this.dustpan = dustpan;
	}
	public void updateDustpan(int dustpan) {
		this.dustpan = this.dustpan + dustpan;
	}
	
	public int getCurrentRoundBottomPour() {
		return currentRoundBottomPour;
	}
	public void setCurrentRoundBottomPour(int currentRoundBottomPour) {
		this.currentRoundBottomPour = currentRoundBottomPour;
	}
	public int getCurrentRoundBottomPours() {
		return currentRoundBottomPours;
	}
	public void setCurrentRoundBottomPours(int currentRoundBottomPours) {
		this.currentRoundBottomPours = currentRoundBottomPours;
	}
	public int[] getPickCards() {
		return pickCards;
	}
	public void setPickCards(int[] pickCards) {
		this.pickCards = pickCards;
	}
	public int getCurrentType() {
		return currentType;
	}
	public void setCurrentType(int currentType) {
		this.currentType = currentType;
	}
    
    public boolean isChuPai() {
		return chuPai;
	}
	public void setChuPai(boolean chuPai) {
		this.chuPai = chuPai;
	}
	public boolean isQingHu() {
		return qingHu;
	}
	public void setQingHu(boolean qingHu) {
		this.qingHu = qingHu;
	}
	public int getBaohu() {
		return baohu;
	}
	public void setBaohu(int baohu) {
		this.baohu = baohu;
	}
	public int getHuCount() {
		return huCount;
	}
	public void setHuCount(int huCount) {
		this.huCount = huCount;
	}

	public List<Integer> getChangeCardList() {
		return changeCardList;
	}
	public void setChangeCardList(Integer change) {
		changeCardList.add(change);
	}
	public void setChangeCardList(List<Integer> changeCardList) {
		this.changeCardList = changeCardList;
	}





	public int getHuNumber() {
		return huNumber;
	}

	public void setHuNumber(int huNumber) {
		this.huNumber = huNumber;
	}

	
	
    public int getMissType() {
        return missType;
    }

    public void setMissType(int missType) {
        this.missType = missType;
    }

    public int getMultiple() {
        return multiple;
    }

    public void setMultiple(int multiple) {
        this.multiple = multiple;
    }

	public List<Integer> getLayHu() {
		return layHu;
	}

	public void setLayHu(List<Integer> layHu) {
		this.layHu = layHu;
	}
	
	public void setLayHu(int layHu) {
		this.layHu.add(layHu);
	}

	public List<Integer> getLayChu() {
		return layChu;
	}

	public void setLayChu(List<Integer> layChu) {
		this.layChu = layChu;
	}

	public int[] getLay() {
		return lay;
	}

	public void setLay(int[] lay) {
		this.lay = lay;
	}

public HuReturnObjectVO getHuReturnObjectVO() {
		return huReturnObjectVO;
	}

	public void setHuReturnObjectVO(HuReturnObjectVO huReturnObjectVO) {
		this.huReturnObjectVO = huReturnObjectVO;
	}
	public Account getAccount() {
        return account;
    }

	public void setAccount(Account account) {
        this.account = account;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public boolean getIsReady() {
        return isReady;
    }

    public void setIsReady(boolean ready) {
        isReady = ready;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }

    public boolean getIsOnLine() {
        return isOnLine;
    }

    public void setIsOnLine(boolean onLine) {
        isOnLine = onLine;
    }

    public int[][] getPaiArray() {
        return paiArray;
    }

    public void setPaiArray(int[][] paiArray) {
        this.paiArray = paiArray;
    }

	public List<Integer> getChupais() {
		return chupais;
	}
	/**
	 * 出了的牌添加到数组中
	 * @param chupai
	 */
	public void updateChupais(Integer chupai) {
		chupais.add(chupai);
 	}
	/**
	 * 移除最后一张牌
	 * @param chupai
	 */
	public void removeLastChupais() {
		int inde = chupais.size();
		chupais.remove(inde-1);
 	}
	
	public int getCommonCards() {
		return commonCards;
	}

	public void setCommonCards(int commonCards) {
		this.commonCards = commonCards;
	}

	public int getScores() {
		return scores;
	}
	/**
	 * 修改分数  正加  负减
	 * @param score
	 */
	public void supdateScores(int score) {
		this.scores = this.scores +score;
	}

	public boolean isHasMopaiChupai() {
		return hasMopaiChupai;
	}

	public void setHasMopaiChupai(boolean hasMopaiChupai) {
		this.hasMopaiChupai = hasMopaiChupai;
	}

	public int getHuType() {
		return huType;
	}

	public void setHuType(int huType) {
		this.huType = huType;
	}

	public String getIP() {
		return IP;
	}

	public void setIP(String IP) {
		this.IP = IP;
	}

	public List<Integer> getPengGuo() {
		return pengGuo;
	}

	public void setPengGuo(List<Integer> pengGuo) {
		this.pengGuo = pengGuo;
	}
	public void addPengGuo(int pengGuo) {
		this.pengGuo.add(pengGuo);
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public boolean isKouZi() {
		return kouZi;
	}
	public void setKouZi(boolean kouZi) {
		this.kouZi = kouZi;
	}
	
}
