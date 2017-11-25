package com.dyz.gameserver.logic;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.context.ErrorCode;
import com.context.Rule;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.initial.AppCf;
import com.dyz.gameserver.commons.message.ResponseMsg;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.response.ErrorResponse;
import com.dyz.gameserver.msg.response.banker.DanJuResponse;
import com.dyz.gameserver.msg.response.banker.XiaZhuResponse;
import com.dyz.gameserver.msg.response.chupai.ChuPaiResponse;
import com.dyz.gameserver.msg.response.common.ReturnInfoResponse;
import com.dyz.gameserver.msg.response.common.ReturnOnLineResponse;
import com.dyz.gameserver.msg.response.dingerhong.BeginDisassemble;
import com.dyz.gameserver.msg.response.dingerhong.OperateResponse;
import com.dyz.gameserver.msg.response.dingerhong.PickCard_DEH_Response;
import com.dyz.gameserver.msg.response.followBanker.FollowBankerResponse;
import com.dyz.gameserver.msg.response.gang.GangResponse;
import com.dyz.gameserver.msg.response.gang.OtherGangResponse;
import com.dyz.gameserver.msg.response.hu.HuPaiAllResponse;
import com.dyz.gameserver.msg.response.hu.HuPaiResponse;
import com.dyz.gameserver.msg.response.layCards.LayCardsResponse;
import com.dyz.gameserver.msg.response.layCards.LayFailedResponse;
import com.dyz.gameserver.msg.response.login.BackLoginResponse;
import com.dyz.gameserver.msg.response.login.OtherBackLoginResonse;
import com.dyz.gameserver.msg.response.peng.PengResponse;
import com.dyz.gameserver.msg.response.pickcard.OtherPickCardResponse;
import com.dyz.gameserver.msg.response.pickcard.PickCardResponse;
import com.dyz.gameserver.msg.response.roomcard.RoomCardChangerResponse;
import com.dyz.gameserver.pojo.AvatarVO;
import com.dyz.gameserver.pojo.CardVO;
import com.dyz.gameserver.pojo.FinalGameEndItemVo;
import com.dyz.gameserver.pojo.HuReturnObjectVO;
import com.dyz.gameserver.pojo.PlayBehaviedVO;
import com.dyz.gameserver.pojo.PlayRecordGameVO;
import com.dyz.gameserver.pojo.PlayRecordItemVO;
import com.dyz.gameserver.pojo.RoomVO;
import com.dyz.myBatis.model.Account;
import com.dyz.myBatis.model.PlayRecord;
import com.dyz.myBatis.model.Standings;
import com.dyz.myBatis.model.StandingsAccountRelation;
import com.dyz.myBatis.model.StandingsDetail;
import com.dyz.myBatis.model.StandingsRelation;
import com.dyz.myBatis.services.AccountService;
import com.dyz.myBatis.services.PlayRecordService;
import com.dyz.myBatis.services.StandingsAccountRelationService;
import com.dyz.myBatis.services.StandingsDetailService;
import com.dyz.myBatis.services.StandingsRelationService;
import com.dyz.myBatis.services.StandingsService;
import com.dyz.persist.util.DateUtil;
import com.dyz.persist.util.DingErHongUtil;
import com.dyz.persist.util.GlobalUtil;
import com.dyz.persist.util.HuPaiType;
import com.dyz.persist.util.Naizi;
import com.dyz.persist.util.NormalHuPai;
import com.dyz.persist.util.StringUtil;

/**
 * Created by kevin on 2016/6/18. 玩牌逻辑
 */
public class PlayCardsLogic {

	/**
	 * 当前操作玩家索引
	 */
	private int currentOperateAvatarIndex = -1;
	
	int jiaguomoney=23;
	
	/**
	 * 本局第几次发牌
	 */
	private int pickCardRound = 0;
	/**
	 * 记录当前玩家能够进行的操作 断线重连时需要
	 */
	private String currentOperations="";
	/**
	 * 判断是否结束叫牌，是否该发牌 每次用户大的时候就重置，并且大的玩家放在第一位 key ==1
	 */
	private Map<Integer, Integer> isFinal = new HashMap<>();

	public RoomVO getRoomVO() {
		return roomVO;
	}

	public void setRoomVO(RoomVO roomVO) {
		this.roomVO = roomVO;
	}

	public int getCurrentOperateAvatarIndex() {
		return currentOperateAvatarIndex;
	}

	public void setCurrentOperateAvatarIndex(int currentOperateAvatarIndex) {
		this.currentOperateAvatarIndex = currentOperateAvatarIndex;
	}

	public int getPickCardRound() {
		return pickCardRound;
	}

	public void setPickCardRound(int pickCardRound) {
		this.pickCardRound = pickCardRound;
	}

	public String getCurrentOperations() {
		return currentOperations;
	}

	public void setCurrentOperations(String currentOperations) {
		this.currentOperations = currentOperations;
	}

	public Map<Integer, Integer> getIsFinal() {
		return isFinal;
	}

	public void setIsFinal(Map<Integer, Integer> isFinal) {
		this.isFinal = isFinal;
	}

	/**
	 * 上一个玩家总下注分数(当前所有玩家中下注最多玩家的分数)
	 */
	private int currentPool;

	public int getCurrentPool() {
		return currentPool;
	}

	public void setCurrentPool(int currentPool) {
		this.currentPool = currentPool;
	}

	/**
	 * 中间的分数池
	 */
	private int scorePool;

	public int getScorePool() {
		return scorePool;
	}

	public void setScorePool(int scorePool) {
		this.scorePool = scorePool;
	}

	/**
	 * 牌的总数
	 */
	private int paiCount;
	/**
	 * 当前出牌人的索引
	 */
	private int curAvatarIndex;
	/**
	 * 当前摸牌人的索引(初始值为庄家索引)
	 * 
	 */
	private int pickAvatarIndex;
	/**
	 * 整张桌子上所有牌的数组
	 */
	private List<Integer> listCard = null;
	/**
	 * 有人要胡的數組
	 */
	private List<Avatar> huAvatar = new ArrayList<>();
	/**
	 * 有人要碰的數組
	 */
	private List<Avatar> penAvatar = new ArrayList<>();
	/**
	 * 有人要杠的數組
	 */
	private List<Avatar> gangAvatar = new ArrayList<>();
	/**
	 * 有人要咋吃的數組
	 */
	private List<Avatar> chiAvatar = new ArrayList<>();
	/**
	 * 起手胡
	 */
	private List<Avatar> qishouHuAvatar = new ArrayList<>();
	/**
	 * 存抓的码
	 */
	List<Integer> mas = new ArrayList<Integer>();
	/**
	 * 存抓的有效码
	 */
	List<Integer> validMa = new ArrayList<Integer>();
	/**
	 * 下张牌的索引
	 */
	private int nextCardindex = 0;
	/**
	 * 上一家出的牌的点数
	 */
	private int putOffCardPoint;
	/**
	 * 判断是否是抢胡
	 */
	private boolean qianghu = false;
	/**
	 * 当前玩家摸的牌的点数
	 */
	private int currentCardPoint = -2;
	/**
	 * 4家玩家信息集合
	 */
	private List<Avatar> playerList;
	/**
	 * 判断是否可以同时几个人胡牌
	 */
	private int huCount = 0;
	/**
	 * 庄家
	 */
	public Avatar bankerAvatar = null;
	/**
	 * 房间信息
	 */
	private RoomVO roomVO;
	/**
	 * 记录本次游戏是否已经胡了，控制摸牌
	 */
	private boolean hasHu;
	/**
	 * 记录某个玩家断线时最后一条消息
	 */
	// private ResponseMsg responseMsg;
	/**
	 * 记录某个玩家断线时发送最后一条消息的玩家
	 */
	// private Avatar lastAvtar;
	/**
	 * 判断是否胡牌
	 */

	private NormalHuPai normalHuPai;
	/**
	 * String有胡家uuid:码牌1:码牌2 组成
	 */
	private String allMas;
	/**
	 * 控制胡牌返回次数
	 */
	int numb = 1;
	// 跟庄牌
	int followPoint = -1;
	// 是否跟庄
	boolean followBanke = true;
	// 跟庄的次数
	int followNumber = 0;
	// 是否被跟庄，最后结算的时候用
	boolean isFollow = false;
	// 记录抢杠胡 多响情况
	boolean hasPull = true;
	// 单局是否结束，判断能否调用准备接口 10-11新增
	boolean singleOver = true;
	/**
	 * 打牌逻辑当前状态 跟0 大1 敲2 休3 丢4
	 */
	private int currentType = -1;

	public int getCurrentType() {
		return currentType;
	}

	public void setCurrentType(int currentType) {
		this.currentType = currentType;
	}

	/**
	 * 逆时针排序集合
	 */
	private List<Avatar> playerAvatar;

	// 游戏回放，
	PlayRecordGameVO playRecordGame;
	/**
	 * 和前段握手，判断是否丢包的情况，丢包则继续发送信息 Integer为用户uuid
	 */
	// private List<Integer> shakeHandsInfo = new ArrayList<Integer>();
	private Map<Integer, ResponseMsg> shakeHandsInfo = new HashMap<Integer, ResponseMsg>();
	/**
	 * 房主ID
	 */
	private int theOwner;
	/**
	 * 出牌对应的胡牌
	 */
	private Map<Integer,List<Integer>> huPai;

	public void setPickAvatarIndex(int pickAvatarIndex) {
		this.pickAvatarIndex = pickAvatarIndex;
	}

	public Map<Integer, ResponseMsg> getShakeHandsInf() {
		return shakeHandsInfo;
	}

	public void updateShakeHandsInfo(Integer uuid, ResponseMsg msg) {
		shakeHandsInfo.put(uuid, msg);
	}

	public String getAllMas() {
		return allMas;
	}

	public List<Avatar> getPlayerList() {
		return playerList;
	}

	public void setCreateRoomRoleId(int value) {
		theOwner = value;
	}

	public void setPlayerList(List<Avatar> playerList) {
		this.playerList = playerList;
	}

	public PlayCardsLogic() {
		normalHuPai = new NormalHuPai();
	}

	/**
	 * 初始化牌
	 */
	public void initCard(RoomVO value) {
		roomVO = value;
		if (roomVO.getRoomType() == 1) {
			// 转转麻将
			paiCount = 27;
			if (roomVO.getHong()) {
				paiCount = 34;
			}
		} else if (roomVO.getRoomType() == 2) {
			// 划水麻将
			if (roomVO.isAddWordCard()) {
				paiCount = 34;
			} else {
				paiCount = 27;
			}
		} else if (roomVO.getRoomType() == 3) {
			// 长沙麻将
			paiCount = 27;
		}
		paiCount = 34;
		listCard = new ArrayList<Integer>();
		for (int i = 0; i < paiCount; i++) {
			for (int k = 0; k < 4; k++) {
				/*
				 * if(roomVO.getHong() && i == 27) { listCard.add(31); }else
				 * if(roomVO.getHong() && i >= 28){ break; }else{
				 * listCard.add(i); }
				 */

				listCard.add(i);
			}
		}

		for (int i = 0; i < playerList.size(); i++) {
			playerList.get(i).avatarVO.setPaiArray(new int[2][paiCount]);
		}
		// 洗牌
		shuffleTheCards(); //原本随机洗牌
		//stuffle();//洗好牌
		// 发牌
		dealingTheCards();
	}

	/**
	 * 随机洗牌
	 */
	public void shuffleTheCards() {
		Collections.shuffle(listCard);
		Collections.shuffle(listCard);
	}
	
    /**
     * 行为 ： 洗牌
     */
    public void stuffle(){
    	Integer temp;
        for (int n = 0; n <70; n++) {
            int i = (int) (Math.random() * 135);
            int j = (int) (Math.random() * 135);
            temp = listCard.get(i);
            listCard.set(i, listCard.get(j));
            listCard.set(j, temp);
        }
    }
	/**
	 * 架锅随机洗牌
	 */
	public void jiaGuoshuffleTheCards() {
		Collections.shuffle(roomVO.listCard);
		Collections.shuffle(roomVO.listCard);
	}

	/**
	 * 检测玩家是否胡牌了
	 * 
	 * @param avatar
	 * @param cardIndex
	 * @param type
	 *            当type为""
	 */
	public boolean checkAvatarIsHuPai(Avatar avatar, int cardIndex, String type) {
		if (cardIndex != 100) {
			// 传入的参数牌索引为100时表示天胡/或是摸牌，不需要再在添加到牌组中
			// System.out.println("检测胡牌的时候------添加别人打的牌："+cardIndex);
			avatar.putCardInList(cardIndex);
		}
		if ((!type.equals("chu") || !avatar.avatarVO.isKouZi()) && checkHu(avatar, cardIndex)) {
			// System.out.println("确实胡牌了");
			// System.out.println(avatar.printPaiString() +" avatar =
			// "+avatar.avatarVO.getAccount().getNickname());
			if (type.equals("chu")) {
				// System.out.println("检测胡牌成功的时候------移除别人打的牌："+cardIndex);
				avatar.pullCardFormList(cardIndex);
			} else if (type.equals("ganghu")) {
				if (roomVO.getRoomType() == 3) {
					avatar.avatarVO.setHuType(avatar.avatarVO.getHuType() + 1);
					// ling
					avatar.avatarVO.getHuReturnObjectVO().setHuType("杠上花");
				} else {
					// 划水麻将杠上花 ，大胡
					avatar.avatarVO.setHuType(2);
				}
			}
			return true;
		} else {
			// System.out.println("没有胡牌");
			if (type.equals("chu")) {
				// System.out.println("检测胡牌失败的时候------移除别人打的牌："+cardIndex);
				avatar.pullCardFormList(cardIndex);
			}
			return false;
		}
	}
	/**
	 * 摸牌
	 *
	 *
	 */
	public void pickCard() {
		clearAvatar();
		// pickAvatarIndex = nextIndex;
		// 本次摸得牌点数，下一张牌的点数，及本次摸的牌点数
		int tempPoint = getNextCardPoint();
		// System.out.println("摸牌："+tempPoint+"----上一家出牌"+putOffCardPoint+"--摸牌人索引:"+pickAvatarIndex);
		if (tempPoint != -1) {

			// 摸牌
			pickAvatarIndex = getNextAvatarIndex();
			// 回放记录
			PlayRecordOperation(pickAvatarIndex, tempPoint, 2, -1, null, null);

			// 当前摸牌的点数
			currentCardPoint = tempPoint;
			Avatar avatar = playerList.get(pickAvatarIndex);
			avatar.avatarVO.setHasMopaiChupai(true);// 修改出牌 摸牌状态，表示摸了牌了
			avatar.qiangHu = true;
			// avatar.canHu = true;
			avatar.avatarVO.getPengGuo().clear();
			// avatar.avatarVO.setHuType(0);//重置划水麻将胡牌格式 10-20移动到下面for循环里面
			// 记录摸牌信息
			// avatar. = true;
			avatar.getSession().sendMsg(new PickCardResponse(1, tempPoint));

			if (avatar.getLayPaiType() == 0 && lay(avatar)) {
				avatar.setLayPaiType(1);
			}

			for (int i = 0; i < playerList.size(); i++) {
				playerList.get(i).avatarVO.setHuType(0);// 重置所有玩家划水麻将/长沙麻将胡牌格式(番数)
				if (i != pickAvatarIndex) {
					playerList.get(i).getSession().sendMsg(new OtherPickCardResponse(1, pickAvatarIndex));
				} else {
					playerList.get(i).gangIndex.clear();// 每次摸牌就先清除缓存里面的可以杠的牌下标
				}
			}
			// 判断自己摸上来的牌自己是否可以胡
			StringBuffer sb = new StringBuffer();
			// 摸起来也要判断是否可以杠，胡，现在是可以巴杠了，躺了的牌里面摸起来一张还是可以杠
			/// *&& !avatar.avatarVO.getLay().equals(currentCardPoint)*/

			avatar.putCardInList(tempPoint);

			if (listCard.size() > nextCardindex + 1) {

				if (!lay(avatar) && avatar.checkSelfGang(currentCardPoint)) {
					gangAvatar.add(avatar);
					sb.append("gang");
					for (int i : avatar.gangIndex) {
						sb.append(":" + i);
					}
					sb.append(",");

				}
				else if ((lay(avatar) && avatar.checkSelfGang(currentCardPoint))) {
					// int[][] pai = avatar.getPaiArray().clone();
					int[][] pai = { avatar.getSinglePaiArray(), GlobalUtil.CloneIntList(avatar.getPaiArray()[1]) };

					pai[0][avatar.gangIndex.get(0)] = 0; // 将手上的杠牌出去
					if (layGang(pai)) {
						gangAvatar.add(avatar);
						sb.append("gang");
						for (int i : avatar.gangIndex) {
							sb.append(":" + i);
						}
						sb.append(",");
					}

				}

			}

			// 判断如果听牌了，是否可以胡牌 目前是没有听过牌才可以胡牌
			if (checkAvatarIsHuPai(avatar, 100, "mo") && lay(avatar) && avatar.getRoomVO().getRoomType() != 3) {
				huAvatar.add(avatar);
				sb.append("normalhu:" + currentCardPoint + ",");

			}
			if (lay(avatar)) {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				avatar.getSession().sendMsg(new ReturnInfoResponse(1, sb.toString()));
			} else {
				if (sb.length() > 2) {
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					avatar.getSession().sendMsg(new ReturnInfoResponse(1, sb.toString()));

				}
			}

		}
		// 否则是流局
		else {
			// System.out.println("流局");
			PlayRecordOperation(pickAvatarIndex, -1, 9, -1, null, null);
			/*
			 * for (Avatar itemAva : playerList) {//流局 最后一家摸牌玩家为 下一把的庄家
			 * if(playerList.get(pickAvatarIndex).getUuId() == itemAva.getUuId()
			 * ){ // itemAva.avatarVO.setMain(true); bankerAvatar = itemAva;
			 * itemAva.avatarVO.setMain(true); } else{
			 * itemAva.avatarVO.setMain(false); } }
			 */
			// 流局处理，直接算分
			settlementData("1");
		}
	}
	/**
	 * 杠了别人(type)/自己摸杠了自后摸牌起来 然后再检测是否可以胡 可以杠等情况
	 * @param avatar
	 */
	public void pickCardAfterGang(Avatar avatar) {

		// 本次摸得牌点数，下一张牌的点数，及本次摸的牌点数
		int tempPoint = getNextCardPoint();
		// System.out.println("摸牌!--"+tempPoint);
		if (tempPoint != -1) {
			currentCardPoint = tempPoint;
			// int avatarIndex = playerList.indexOf(avatar); // 2016-8-2注释
			pickAvatarIndex = playerList.indexOf(avatar);
			// Avatar avatar = playerList.get(pickAvatarIndex);
			// 记录摸牌信息
			for (int i = 0; i < playerList.size(); i++) {
				if (i != pickAvatarIndex) {
					playerList.get(i).getSession().sendMsg(new OtherPickCardResponse(1, pickAvatarIndex));
				} else {
					playerList.get(i).gangIndex.clear();// 每次出牌就先清除缓存里面的可以杠的牌下标
					playerList.get(i).getSession().sendMsg(new PickCardResponse(1, tempPoint));
					// 摸牌之后就重置可否胡别人牌的标签
					// playerList.get(i).canHu = true;
					// System.out.println("摸牌玩家------index"+pickAvatarIndex+"名字"+playerList.get(i).avatarVO.getAccount().getNickname());
				}
			}
			// 记录摸牌信息
			PlayRecordOperation(pickAvatarIndex, currentCardPoint, 2, -1, null, null);

			// 判断自己摸上来的牌自己是否可以胡
			StringBuffer sb = new StringBuffer();
			// 摸起来也要判断是否可以杠，胡
			avatar.putCardInList(tempPoint);

			if (listCard.size() > nextCardindex + 1) {

				if (!lay(avatar) && avatar.checkSelfGang(currentCardPoint)) {
					gangAvatar.add(avatar);
					sb.append("gang");
					for (int i : avatar.gangIndex) {
						sb.append(":" + i);
					}
					sb.append(",");

				} else if ((lay(avatar) && avatar.checkSelfGang(currentCardPoint))) {
					int[][] pai = { avatar.getSinglePaiArray(), GlobalUtil.CloneIntList(avatar.getPaiArray()[1]) };
					pai[0][avatar.gangIndex.get(0)] = 0; // 将手上的杠牌出去
					if (layGang(pai)) {
						gangAvatar.add(avatar);
						sb.append("gang");
						for (int i : avatar.gangIndex) {
							sb.append(":" + i);
						}
						sb.append(",");
					}

				}

			}

			/*
			 * if (avatar.checkSelfGang(currentCardPoint) && listCard.size() >
			 * nextCardindex + 1) { gangAvatar.add(avatar); sb.append("gang");
			 * for (int i : avatar.gangIndex) { sb.append(":"+i); }
			 * sb.append(","); //avatar.gangIndex.clear(); }
			 */
			if (checkAvatarIsHuPai(avatar, 100, "ganghu") && lay(avatar)) {
				// 检测完之后不需要移除
				huAvatar.add(avatar);
				sb.append("normalhu:" + currentCardPoint + ",");
			}

			// 暗缸判断是否听牌。如果是就发送空消息
			if (lay(avatar)) {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				avatar.getSession().sendMsg(new ReturnInfoResponse(1, sb.toString()));
			} else {
				if (sb.length() > 2) {
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					avatar.getSession().sendMsg(new ReturnInfoResponse(1, sb.toString()));

				}
			}

			// if (sb.length() > 2) {
			// // System.out.println(sb);
			// avatar.getSession().sendMsg(new ReturnInfoResponse(1,
			// sb.toString()));
			// }

		} else {
			// 流局
			// system.out.println("流局");
			// 记录摸牌信息
			PlayRecordOperation(pickAvatarIndex, -1, 9, -1, null, null);
			/*
			 * for (Avatar itemAva : playerList) {//流局 最后一家摸牌玩家为 下一把的庄家
			 * if(playerList.get(pickAvatarIndex).getUuId() == itemAva.getUuId()
			 * ){ // itemAva.avatarVO.setMain(true); bankerAvatar = itemAva;
			 * itemAva.avatarVO.setMain(true); } else{
			 * itemAva.avatarVO.setMain(false); } }
			 */
			// 结算当局信息
			settlementData("1");
		}
	}

	/**
	 * 获取下一位摸牌人的索引
	 * 
	 * @return
	 */
	public int getNextAvatarIndex() {
		int nextIndex = curAvatarIndex + 1;
		// if(nextIndex >= 4){
		if (nextIndex >= roomVO.getPeoples()) {
			nextIndex = 0;
		}
		return nextIndex;
	}

	/**
	 * 玩家选择放弃操作
	 * 
	 * @param avatar
	 * @param
	 *
	 */
	public void gaveUpAction(Avatar avatar) {
		// 放弃的时候，至少一个数组不为空才行
		if (validateStatus()) {
			// 检查到有人胡牌时存储胡牌的详细信息
			avatar.huAvatarDetailInfo.clear();
			// System.out.println(JsonUtilTool.toJson(avatar.getRoomVO()));
			avatar.avatarVO.setHuType(0);// 重置划水麻将/长沙麻将胡牌格式
			avatar.avatarVO.getHuReturnObjectVO().setHuType("");// 重置胡牌类型

			// 记录摸牌信息
			PlayRecordOperation(playerList.indexOf(avatar), -1, 10, -1, null, null);
			// 判断当前摸牌人的索引
			if (pickAvatarIndex == playerList.indexOf(avatar)) {
				// 如果是自己摸的过，则 canHu = true；
				// avatar.canHu = true;
				// system.out.println("自己摸的过");
				/*
				 * if(huAvatar.contains(avatar)){ huAvatar.remove(avatar); }
				 * if(gangAvatar.contains(avatar)){ gangAvatar.remove(avatar); }
				 * if(penAvatar.contains(avatar)){ penAvatar.remove(avatar); }
				 * if(chiAvatar.contains(avatar)){ chiAvatar.remove(avatar); }
				 */
				if (huAvatar.contains(avatar)) {
					avatar.avatarVO.setKouZi(true);
				}
				// 清空所有数组
				clearAvatar();
			} else {
				// system.out.println("别人打的过");
				// 如果别人打的牌过，
				// 放弃胡，则检测有没人杠
				if (huAvatar.contains(avatar)) {
					huAvatar.remove(avatar);
					avatar.canHu = false;
					avatar.qiangHu = false;
					avatar.avatarVO.setKouZi(true);
				}
				if (gangAvatar.contains(avatar)) {
					gangAvatar.remove(avatar);
					avatar.gangIndex.clear();
				}
				if (penAvatar.contains(avatar)) {
					penAvatar.remove(avatar);
					// avatar.peng = false;
					avatar.avatarVO.addPengGuo(putOffCardPoint);
				}
				if (chiAvatar.contains(avatar)) {
					chiAvatar.remove(avatar);
				}
				if (huAvatar.size() == 0) {
					for (Avatar item : gangAvatar) {
						if (item.gangQuest) {
							avatar.qiangHu = false;
							// 进行这个玩家的杠操作，并且把后面的碰，吃数组置为0;
							gangCard(item, putOffCardPoint, 1);
							clearArrayAndSetQuest();
							// system.out.println("********过了但是还有人gang");
							return;
						}
					}
					for (Avatar item : penAvatar) {
						if (item.pengQuest) {
							// 进行这个玩家的碰操作，并且把后面的吃数组置为0;
							pengCard(item, putOffCardPoint);
							clearArrayAndSetQuest();
							// system.out.println("********过了但是还有人pen");
							return;
						}
					}
					for (Avatar item : chiAvatar) {
						if (item.chiQuest) {
							// 进行这个玩家的吃操作
							CardVO cardVo = new CardVO();
							cardVo.setCardPoint(putOffCardPoint);
							chiCard(item, cardVo);
							clearArrayAndSetQuest();
							// system.out.println("********过了但是还有人吃");
							return;
						}
					}
				} else {
					Avatar item;
					for (int i = 0; i < huAvatar.size(); i++) {
						item = huAvatar.get(i);
						// 判断当前可以胡牌，并且检测当前玩家是一炮多响是否可以胡牌，才胡牌
						if (item.huQuest && isHu(playerList.indexOf(item))) {
							huPai(item, putOffCardPoint, "");
							return;
						}
					}
				}
				// 如果都没有人胡，没有人杠，没有人碰，没有人吃的情况下。则下一玩家摸牌
				chuPaiCallBack();
			}
		}
	}

	/**
	 * 清理胡杠碰吃数组，并把玩家的请求状态全部设置为false;
	 */
	public void clearArrayAndSetQuest() {
		while (gangAvatar.size() > 0) {
			gangAvatar.remove(0).setQuestToFalse();
		}
		while (penAvatar.size() > 0) {
			penAvatar.remove(0).setQuestToFalse();
		}
		while (chiAvatar.size() > 0) {
			chiAvatar.remove(0).setQuestToFalse();
		}
	}

	/**
	 * 出牌
	 * 
	 * @param avatar
	 * @param cardPoint
	 */
	public void putOffCard(Avatar avatar, int cardPoint) {
		if(huPai != null && huPai.size() > 0 && huPai.get(cardPoint) != null){
			//获得出牌
			List<Integer> chupais = avatar.avatarVO.getChupais();
			List<Integer> huList = huPai.get(cardPoint);
			
			for(int i = 0;i < huList.size();i++){
				//检查出牌是否有可以胡牌
				if(chupais.contains(huList.get(i))){
					//如果有就只能自摸
					avatar.avatarVO.setKouZi(true);
					break;
				}
			}
			huPai = null;
		}
		// 出牌信息放入到缓存中，掉线重连的时候，返回房间信息需要
		avatar.avatarVO.updateChupais(cardPoint);
		avatar.avatarVO.setHasMopaiChupai(true);// 修改出牌 摸牌状态
		// 已经出牌就清除所有的吃，碰，杠，胡的数组
		clearAvatar();
		avatar.avatarVO.setLayChu(new ArrayList<Integer>()); // 躺牌可以出的牌清空
		putOffCardPoint = cardPoint;
		// system.out.println("出牌点数"+putOffCardPoint+"---出牌人索引:"+playerList.indexOf(avatar));
		curAvatarIndex = playerList.indexOf(avatar);
		// 游戏回放
		PlayRecordOperation(curAvatarIndex, cardPoint, 1, -1, null, null);
		avatar.pullCardFormList(putOffCardPoint);
		for (int i = 0; i < playerList.size(); i++) {
			// 不能返回给自己
			playerList.get(i).avatarVO.setHuType(0);// 重置所有玩家划水麻将/长沙麻将胡牌格式(番数)
			if (i != curAvatarIndex) {
				playerList.get(i).getSession().sendMsg(new ChuPaiResponse(1, putOffCardPoint, curAvatarIndex));
				// //system.out.println("发送打牌消息----"+playerList.get(i).avatarVO.getAccount().getNickname());
			} else {
				playerList.get(i).gangIndex.clear();// 每次出牌就先清除缓存里面的可以杠的牌下标
			}
		}
		// 房间为可抢杠胡
		if (avatar.getRoomVO().getZiMo() == 0 && !avatar.getRoomVO().getHong()) {
			// 出牌时，房间为可抢杠胡并且没有癞子时才检测其他玩家有没胡的情况
			Avatar ava;
			StringBuffer sb;
			for (int i = 0; i < playerList.size(); i++) {
				ava = playerList.get(i);
				if (ava.getUuId() != avatar.getUuId()) {
					sb = new StringBuffer();
					// 判断吃，碰， 胡 杠的时候需要把以前吃，碰，杠胡的牌踢出再计算(暂定长沙麻将只可以自摸)
					if (lay(ava) && ava.canHu && ava.getRoomVO().getRoomType() != 3
							&& checkAvatarIsHuPai(ava, putOffCardPoint, "chu") && chuPaiNoHu(ava, cardPoint)) {
						
						// 胡牌状态为可胡的状态时才行
						huAvatar.add(ava);
						sb.append("normalhu:" + putOffCardPoint + ",");
					}
					if (!lay(ava) && ava.checkGang(putOffCardPoint) && listCard.size() > nextCardindex + 1) {
						gangAvatar.add(ava);
						// 同时传会杠的牌的点数
						sb.append("gang:" + putOffCardPoint + ",");
					}
					// 檢測是否可以碰 大于等于2张且对应的下标不为1都可以碰

					if (!lay(ava) && ava.checkPeng(putOffCardPoint)) {
						penAvatar.add(ava);
						sb.append("peng:" + curAvatarIndex + ":" + putOffCardPoint + ",");
					}

					if (sb.length() > 1) {
						try {
							Thread.sleep(300);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// system.out.println(sb);
						ava.getSession().sendMsg(new ReturnInfoResponse(1, sb.toString()));
						// responseMsg = new ReturnInfoResponse(1,
						// sb.toString());
						// lastAvtar = ava;
					}
				}
			}
		} else {
			Avatar ava;
			StringBuffer sb;
			for (int i = 0; i < playerList.size(); i++) {
				ava = playerList.get(i);
				if (ava.getUuId() != avatar.getUuId()) {
					sb = new StringBuffer();
					if (ava.checkGang(putOffCardPoint) && listCard.size() > nextCardindex + 1) {
						gangAvatar.add(ava);
						// 同时传会杠的牌的点数
						sb.append("gang:" + putOffCardPoint + ",");
					}
					if (ava.checkPeng(putOffCardPoint)) {
						penAvatar.add(ava);
						sb.append("peng:" + curAvatarIndex + ":" + putOffCardPoint + ",");
					}
					// if ( roomVO.getRoomType() == 3 &&
					// ava.checkChi(putOffCardPoint) && getNextAvatarIndex() ==
					// i){
					// //(长沙麻将)只有下一家才能吃
					// chiAvatar.add(ava);
					// sb.append("chi");
					// }
					if (sb.length() > 1) {
						// system.out.println(sb);
						try {
							Thread.sleep(300);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						ava.getSession().sendMsg(new ReturnInfoResponse(1, sb.toString()));
						// responseMsg = new ReturnInfoResponse(1,
						// sb.toString());
						// lastAvtar = ava;
					}
				}
			}
		}
		// 如果没有吃，碰，杠，胡的情况，则下家自动摸牌
		chuPaiCallBack();
	}

	/**
	 * 吃牌
	 * 
	 * @param avatar
	 * @param
	 * @return
	 */
	public boolean chiCard(Avatar avatar, CardVO cardVo) {
		// 碰，杠都比吃优先
		boolean flag = false;
		// int avatarIndex = playerList.indexOf(avatar);
		if (roomVO.getRoomType() == 3) {
			if (huAvatar.size() == 0 && penAvatar.size() == 0 && gangAvatar.size() == 0 && chiAvatar.size() > 0) {
				if (chiAvatar.contains(avatar)) {
					// 回放记录
					// PlayRecordOperation(playerList.indexOf(avatar),cardVo.getCardPoint(),3,-1);
					// 更新牌组
					avatar.putCardInList(cardVo.getCardPoint());
					avatar.setCardListStatus(cardVo.getCardPoint(), 4);
					clearArrayAndSetQuest();
					flag = true;
					for (int i = 0; i < playerList.size(); i++) {
						if (avatar.getUuId() == playerList.get(i).getUuId()) {
							// *****吃牌后面弄，需要修改传入的参数 CardVO
							// String str = "";.getClass()
							// playerList.get(i).avatarVO.getHuReturnObjectVO().updateTotalInfo("chi",
							// str);
							// 标记吃了的牌的下标//碰 1 杠2 胡3 吃4
							// playerList.get(i).avatarVO.getPaiArray()[1][cardVo.getCardPoint()]
							// = 1 ;
							// playerList.get(i).avatarVO.getPaiArray()[1][cardVo.getOnePoint()]
							// = 1;
							// playerList.get(i).avatarVO.getPaiArray()[1][cardVo.getOnePoint()]
							// = 1;
						}
					}
					// curAvatarIndex = avatarIndex;// 2016-8-1注释掉
					// 更新用户的正常牌组(不算上碰，杠，胡，吃)吃牌这里还需要修改****
					// playerList.get(avatarIndex).avatarVO.updateCurrentCardList(cardVo.getCardPoint());
				}
			} else {
				if (chiAvatar.size() > 0) {
					for (Avatar ava : chiAvatar) {
						ava.chiQuest = true;
						ava.cardVO = cardVo;// 存储前段发送过来的吃对象
					}
				}
			}
		} else {
			// system.out.println("只有长沙麻将可以吃!");
		}
		return flag;
	}

	/**
	 * 碰牌
	 * 
	 * @param avatar
	 * @return
	 */
	public boolean pengCard(Avatar avatar, int cardIndex) {

		if (avatar.getLayPaiType() == 0 && lay(avatar)) {
			avatar.setLayPaiType(1);
		}

		boolean flag = false;
		// 这里可能是自己能胡能碰能杠 但是选择碰 上一家出的牌的点数
		if (cardIndex != putOffCardPoint) {
			System.out.println("传入错误的牌:传入的牌" + cardIndex + "---上一把出牌：" + putOffCardPoint);
		}
		if (roomVO.getRoomType() == 2) {
			// 是否跟庄
			if (followBanke) {
				followBanke = false;
			}
		}
		if (cardIndex < 0) {
			try {
				avatar.getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000019));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		// if((huAvatar.size() == 0 || huAvatar.contains(avatar)) &&
		// penAvatar.size() >= 1)) {
		// 判断碰的个数>=1 并且胡的个数为0
		if ((penAvatar.size() >= 1 && huAvatar.size() == 0)
				|| (huAvatar.contains(avatar) && huAvatar.size() == 1 && penAvatar.size() == 1)) {
			avatar.avatarVO.setHasMopaiChupai(true);// 修改出牌 摸牌状态
			if (huAvatar.contains(avatar)) {
				huAvatar.remove(avatar);
			}
			if (gangAvatar.contains(avatar)) {
				gangAvatar.remove(avatar);
			}
			if (penAvatar.contains(avatar)) {
				// 回放记录
				PlayRecordOperation(playerList.indexOf(avatar), cardIndex, 4, -1, null, null);
				// 把出的牌从出牌玩家的chupais中移除掉
				playerList.get(curAvatarIndex).avatarVO.removeLastChupais();
				penAvatar.remove(avatar);
				// 更新牌组
				flag = avatar.putCardInList(cardIndex);
				avatar.setCardListStatus(cardIndex, 1);

				// 把各个玩家碰的牌记录到缓存中去,牌的index
				avatar.avatarVO.getHuReturnObjectVO().updateTotalInfo("peng", cardIndex + "");
				// avatar.getResultRelation().put(key, value);
				// 清理碰，杠，胡
				clearArrayAndSetQuest();
				for (int i = 0; i < playerList.size(); i++) {
					if (playerList.get(i).getUuId() == avatar.getUuId()) {
						// 碰了的牌放入到avatar的resultRelation Map中
						playerList.get(i).putResultRelation(1, cardIndex + "");
						playerList.get(i).avatarVO.getPaiArray()[1][cardIndex] = 1;
						avatar.getPaiArray()[1][cardIndex] = 1;
					}
					playerList.get(i).getSession().sendMsg(new PengResponse(1, cardIndex, playerList.indexOf(avatar)));
				}
				// responseMsg = new
				// PengResponse(1,cardIndex,playerList.indexOf(avatar));
				// lastAvtar = avatar;
				// 更新摸牌人信息 2016-8-3
				pickAvatarIndex = playerList.indexOf(avatar);
				curAvatarIndex = playerList.indexOf(avatar);
				currentCardPoint = -2;// 断线重连判断该自己出牌
				// }
			}
		} else {
			if (penAvatar.size() > 0) {
				for (Avatar ava : penAvatar) {
					ava.pengQuest = true;
				}
			}

		}
		return flag;
	}

	/**
	 * 杠牌
	 * 
	 * @param avatar
	 * @return
	 */
	public boolean gangCard(Avatar avatar, int cardPoint, int gangType) {

		if (avatar.getLayPaiType() == 0 && lay(avatar)) {
			avatar.setLayPaiType(1);
		}

		boolean flag = false;
//		if (roomVO.getRoomType() == 2) {
//			// 是否跟庄
//			if (followBanke) {
//				followBanke = false;
//			}
//		}
		int avatarIndex = playerList.indexOf(avatar);
		// if(gangAvatar.size() > 0 && huAvatar.size() == 0) {//2016-8-1
		// if(gangAvatar.size() > 0 && huAvatar.size() == 0 || (huAvatar.size()
		// == 1 && huAvatar.contains(avatar) )) {//2016-8-1
		if (gangAvatar.size() > 0) {
			if ((huAvatar.contains(avatar) && huAvatar.size() == 1) || huAvatar.size() == 0) {
				avatar.avatarVO.setHasMopaiChupai(true);// 修改出牌 摸牌状态
				if (huAvatar.contains(avatar)) {
					huAvatar.remove(avatar);
				}
				if (penAvatar.contains(avatar)) {
					penAvatar.remove(avatar);
				}
				if (chiAvatar.contains(avatar)) {
					chiAvatar.remove(avatar);
				}
				if (gangAvatar.contains(avatar)) {
					gangAvatar.remove(avatar);
					// 判断杠的类型，自杠，还是点杠
					String str;
					int type;
					int score; // 杠牌分数(转转麻将) 杠牌番数(划水麻将)
					String recordType;// 暗杠 4 ， 明杠 5(用于统计不同type下的次数和得分)
					String endStatisticstype;
					int playRecordType;// 游戏回放 记录杠的类型
					// if(avatar.getUuId() ==
					// playerList.get(curAvatarIndex).getUuId()){pickAvatarIndex
					if (avatar.getUuId() == playerList.get(pickAvatarIndex).getUuId()) {

						// system.out.println("自杠**********自己摸牌*************自杠");
						// 自己摸牌，所以更新摸牌人
						// pickAvatarIndex =
						// playerList.indexOf(avatar);//2016-8-1
						// 自杠(明杠或暗杠)，，这里的明杠时需要判断本房间是否是抢杠胡的情况，
						// 如果是抢杠胡，则其他玩家有胡牌的情况下，可以胡
						String strs = avatar.getResultRelation().get(1);
						// if(strs != null && strs.contains(cardPoint+"")){
						if (strs != null && avatar.avatarVO.getPaiArray()[1][cardPoint] == 1) {
							playRecordType = 3;
							// 明杠（划水麻将里面的过路杠）
							 if(checkQiangHu(avatar,cardPoint)){
								 //如果是抢杠胡，则判断其他玩家有胡牌的情况，有则给予提示 //判断其他三家是否能抢杠胡。
								 //如果抢胡了，则更新上家出牌的点数为 杠的牌
								 putOffCardPoint = cardPoint;
								 gangAvatar.add(avatar);
								 avatar.gangQuest = true;
								 //回放记录
								 PlayRecordOperation(avatarIndex,cardPoint,5,4,null,null);
								 return false;
							} else {
								// 存储杠牌的信息，
								avatar.putResultRelation(2, cardPoint + "");
								avatar.avatarVO.getPaiArray()[1][cardPoint] = 2;
								avatar.getPaiArray()[1][cardPoint] = 2;

								avatar.setCardListStatus(cardPoint, 2);// 杠牌标记2
								if (roomVO.getRoomType() == 1) {
									// 转转麻将
									str = "0:" + cardPoint + ":" + Rule.Gang_ming;
									type = 0;
									score = 1;
									recordType = "5";
									endStatisticstype = "minggang";
									// system.out.println("自杠*************************明杠");
								} else if (roomVO.getRoomType() == 2) {
									// 划水麻将
									// str =
									// "0:"+cardPoint+":"+Rule.Gang_ming_guolu;
									str = "0:" + cardPoint + ":" + Rule.Gang_ming;
									type = 0;
									score = 1;
									recordType = "5";
									endStatisticstype = "minggang";
								} else {
									// 长沙麻将
									str = "0:" + cardPoint + ":" + Rule.Gang_ming;
									type = 0;
									score = 1;
									int magnification = avatar.getRoomVO().getMagnification();
									if (magnification > 1) {
										score = score * magnification / 2;
									}
									recordType = "5";
									endStatisticstype = "minggang";
									// system.out.println("自杠*************************明杠");
								}
							}
						} else {
							playRecordType = 2;
							// 存储杠牌的信息，
							avatar.putResultRelation(2, cardPoint + "");
							avatar.avatarVO.getPaiArray()[1][cardPoint] = 2;
							avatar.getPaiArray()[1][cardPoint] = 2;

							avatar.setCardListStatus(cardPoint, 2);// 杠牌标记2
							// 暗杠
							if (roomVO.getRoomType() == 1) {
								// 转转麻将
								str = "0:" + cardPoint + ":" + Rule.Gang_an;
								type = 1;
								score = 2;
								recordType = "4";
								endStatisticstype = "angang";
								// system.out.println("自杠*************************暗杠");
							} else if (roomVO.getRoomType() == 2) {
								// 划水麻将
								str = "0:" + cardPoint + ":" + Rule.Gang_an;
								type = 1;
								score = 2;
								recordType = "4";
								endStatisticstype = "angang";
							} else {
								// 长沙麻将
								str = "0:" + cardPoint + ":" + Rule.Gang_an;
								type = 1;
								score = 2;
								int magnification = avatar.getRoomVO().getMagnification();
								if (magnification > 1) {
									score = score * magnification / 2;
								} else {
									score = 1;
								}
								recordType = "4";
								endStatisticstype = "angang";
							}
						}
						score = 0;

						// 杠分乘倍率
						for (Avatar ava : playerList) {
							if (ava.getUuId() == avatar.getUuId()) {
								// 修改玩家整个游戏总分和杠的总分
								avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos(recordType,
										score * (playerList.size() - 1));
								// 整个房间统计每一局游戏 杠，胡的总次数
								roomVO.updateEndStatistics(ava.getUuId() + "", endStatisticstype, 1);
							} else {
								// 修改其他三家的分数
								ava.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos(recordType, -1 * score);
							}
						}
						flag = true;
					} else {
						// 存储杠牌的信息，
						playRecordType = 1;

						avatar.putResultRelation(2, cardPoint + "");
						avatar.avatarVO.getPaiArray()[1][cardPoint] = 2;
						avatar.getPaiArray()[1][cardPoint] = 2;

						avatar.setCardListStatus(cardPoint, 2);// 杠牌标记2
						// 点杠(分在明杠里面)（划水麻将里面的放杠）
						// 把出的牌从出牌玩家的chupais中移除掉
						playerList.get(curAvatarIndex).avatarVO.removeLastChupais();

						// 更新牌组(点杠时才需要更新) 自摸时不需要更新
						flag = avatar.putCardInList(cardPoint);
						if (roomVO.getRoomType() == 1) {
							// 转转麻将
							score = 3;
							// recordType = "5";
							recordType = "8";
							str = playerList.get(curAvatarIndex).getUuId() + ":" + cardPoint + ":" + Rule.Gang_dian;
							type = 0;
							endStatisticstype = "minggang";
							// system.out.println("点杠*************************明杠");
						} else if (roomVO.getRoomType() == 2) {
							// 划水麻将 这里改为0分
							// score = 3;
							score = 0;
							// recordType = "5";
							recordType = "8";
							str = playerList.get(curAvatarIndex).getUuId() + ":" + cardPoint + ":" + Rule.Gang_fang;
							type = 0;
							endStatisticstype = "fanggang";
						} else {
							// 长沙麻将，杠分乘倍率
							// score = 3;
							score = 0;
							int magnification = avatar.getRoomVO().getMagnification();
							if (magnification > 1) {
								score = score * magnification / 2;
							} else {
								score = 2;
							}
							// recordType = "5";
							recordType = "8";
							str = playerList.get(curAvatarIndex).getUuId() + ":" + cardPoint + ":" + Rule.Gang_dian;
							type = 0;
							endStatisticstype = "minggang";
						}
						score = 0;

						// 减点杠玩家的分数
						playerList.get(curAvatarIndex).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos(recordType,
								-1 * score);
						// 增加杠家的分数
						avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos(recordType, score);
						// 整个房间统计每一局游戏 杠，胡的总次数
						roomVO.updateEndStatistics(avatar.getUuId() + "", endStatisticstype, 1);
					}

					avatar.avatarVO.getHuReturnObjectVO().updateTotalInfo("gang", str);
					// 回放记录
					PlayRecordOperation(avatarIndex, cardPoint, 5, playRecordType, null, null);

					clearArrayAndSetQuest();
					if (gangType == 0 && !hasHu) {
						// 可以换牌的情况只补一张牌
						// 摸牌并判断自己摸上来的牌自己是否可以胡/可以杠****
						for (int i = 0; i < playerList.size(); i++) {
							if (avatar.getUuId() != playerList.get(i).getUuId()) {
								// 杠牌返回给其他人只返回杠的类型和杠牌的玩家位置
								playerList.get(i).getSession()
										.sendMsg(new OtherGangResponse(1, cardPoint, avatarIndex, type));
								// responseMsg = new
								// OtherGangResponse(1,cardPoint,avatarIndex,type);
								// lastAvtar = playerList.get(i);
							} else {
								// 杠牌返回给其他人只返回杠的类型和杠牌的玩家位置
								playerList.get(i).getSession().sendMsg(new GangResponse(1, 1, 1, type));
								// responseMsg = new GangResponse(1, 1, 1,type);
								// lastAvtar = playerList.get(i);
							}
						}
						pickCardAfterGang(avatar);// 2016-8-1

						// }
					} else if (gangType == 1 && !hasHu) {
						// 摸两张 **** 这里需要单独处理摸的两张牌 是否可以胡，可以杠
						// 摸牌并判断自己摸上来的牌自己是否可以胡/可以杠****
						for (int i = 0; i < playerList.size(); i++) {
							if (avatar.getUuId() != playerList.get(i).getUuId()) {
								// 杠牌返回给其他人只返回杠的类型和杠牌的玩家位置
								playerList.get(i).getSession()
										.sendMsg(new OtherGangResponse(1, cardPoint, avatarIndex, type));
								// responseMsg = new
								// OtherGangResponse(1,cardPoint,avatarIndex,type);
								// lastAvtar = playerList.get(i);
							} else {
								// 杠牌返回给其他人只返回杠的类型和杠牌的玩家位置
								playerList.get(i).getSession().sendMsg(new GangResponse(1, 1, 1, type));
								// responseMsg = new GangResponse(1, 1, 1,type);
								// lastAvtar = playerList.get(i);
							}
						}
						pickCardAfterGang(avatar);// 2016-8-1
					}
				}
			} else {
				if (gangAvatar.size() > 0) {
					for (Avatar ava : gangAvatar) {
						ava.gangQuest = true;
					}
				}
			}
		} else {
			if (gangAvatar.size() > 0) {
				for (Avatar ava : gangAvatar) {
					ava.gangQuest = true;
				}
			}
			try {
				playerList.get(avatarIndex).getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000016));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return flag;
	}

	/**
	 * 胡牌
	 * 
	 * @param avatar
	 * @return
	 */
	public boolean huPai(Avatar avatar, int cardIndex, String type) {
		// 庄家的索引
		int bankerIndex = 0;
		// 自己准备做的推倒胡，首先判断当前胡牌的人数集合个数大于等于1的时候,说明有1个人以上的胡牌资格
		if (huAvatar.size() > 1) {
			// 如果胡牌玩家就是庄家，直接胡
			// if(avatar.getUuId() == bankerAvatar.getUuId()){
			//
			// }
			// 是否该当前玩家胡
			if (!isHu(playerList.indexOf(avatar))) {
				avatar.huQuest = true;
				try {
					avatar.getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000025));
				} catch (IOException e) {
					e.printStackTrace();
				}
				/*
				 * System.out.println(
				 * "当前不是离庄家最近的玩家，要等待前面玩家是否胡牌或者放弃操作的结果，才能进行胡牌等操作");
				 */
				return false;
			}
			// for(int i = 0; i < playerList.size();i++)
			// {
			// //庄家的索引
			// bankerIndex = playerList.indexOf(bankerAvatar);
			// avatar = playerList.get(i);
			// //胡牌玩家的索引是逆时针，在庄家索引基础上-1,这里判断如果离庄家最近的玩家胡牌了的话，结束整场牌局
			// if(bankerIndex - playerList.indexOf(avatar) != -1)
			// {
			// System.out.println("当前不是离庄家最近的玩家，要等待前面玩家是否胡牌或者放弃操作的结果，才能进行胡牌等操作");
			// return false;
			// }
			// }
		}
		// 后面执行胡牌的代码操作

		boolean flag = false;
		;
		// 胡牌就清除掉存的其他人所有的可以碰 杠 吃的信息
		if (huCount == 0) {
			huCount = 1;
			// huCount = huAvatar.size();
		}
		StringBuffer sb = new StringBuffer();
		// 得到牌组的一维数组。用来判断是否胡牌和听牌用
		avatar.getPaiArray()[1][cardIndex] = 3;
		// 当胡家手上没有红中，则多抓一个码
		int playRecordType = 6;// 胡牌的分类
		int listCount = avatar.getRoomVO().getMa(); // 抓码的个数
		if (avatar.getRoomVO().getMa() >= 1 && huCount == 1) {
			sb.append(avatar.getUuId());
			// 单响 胡家抓码 //是否红中当癞子
			int ma;
			if (roomVO.getHong() && avatar.getPaiArray()[0][31] == 0) {
				listCount++;
			}
			for (int i = 0; i < listCount; i++) {
				// ma = (int) Math.round(Math.random()*26);
				ma = getNextCardPoint();
				if (ma != -1) {
					mas.add(ma);
					sb.append(":" + ma);
				} else {
					i = 100;
				}
			}
			allMas = sb.toString();
		} else if (avatar.getRoomVO().getMa() >= 1 && huCount > 1) {
			// 多响 点炮玩家抓码 出牌人的索引 curAvatarIndex
			if (StringUtil.isEmpty(allMas)) {
				sb.append(playerList.get(pickAvatarIndex).getUuId());
				int ma;
				for (int i = 0; i < listCount; i++) {
					// ma = (int) Math.round(Math.random()*26);
					ma = getNextCardPoint();
					if (ma != -1) {
						mas.add(ma);
						sb.append(":" + ma);
					} else {
						i = 100;
					}
				}
				allMas = sb.toString();
			}
		}
		if (huAvatar.size() > 0) {
			if (huAvatar.contains(avatar)) {
				// if(playerList.get(pickAvatarIndex).getUuId() !=
				// avatar.getUuId()){
				if (StringUtil.isNotEmpty(type) && type.equals("qianghu")) {
					// 是抢胡，则各更新 出牌人等于摸牌人 , 然后冲被抢玩家的牌里移除此牌
					playRecordType = 7;
					if (hasPull) {// 两个人都抢胡，只能移除一次牌
						curAvatarIndex = pickAvatarIndex;
						playerList.get(curAvatarIndex).pullCardFormList(cardIndex);
						hasPull = false;
					}
				}
				if (pickAvatarIndex == curAvatarIndex) {
					// 把胡了的牌索引放入到对应赢家的牌组中
					avatar.putCardInList(cardIndex);
					// system.out.println("点炮");
					// 当摸牌人的索引等于出牌人的索引时，表示点炮了
					// 点炮 别人点炮的时候查看是否可以胡
					if (avatar.canHu) {
						// 胡牌数组中移除掉胡了的人
						// huAvatar.remove(avatar);
						huAvatar.clear();
						gangAvatar.clear();
						penAvatar.clear();
						chiAvatar.clear();
						// 两个人之间建立关联，游戏结束算账用
						if (!validMa.isEmpty()) {
							List<Integer> newValidMa = HuPaiType.getInstance().getHuType(playerList.get(curAvatarIndex),
									avatar, roomVO.getRoomType(), cardIndex, playerList, mas, huCount, type,
									roomVO.getHong());
							for (Integer j : newValidMa) {
								validMa.add(j);
							}
						} else {
							validMa = HuPaiType.getInstance().getHuType(playerList.get(curAvatarIndex), avatar,
									roomVO.getRoomType(), cardIndex, playerList, mas, huCount, type, roomVO.getHong());
						}
						// 整个房间统计每一局游戏 杠，胡的总次数
						roomVO.updateEndStatistics(avatar.getUuId() + "", "jiepao", 1);
						roomVO.updateEndStatistics(playerList.get(curAvatarIndex).getUuId() + "", "dianpao", 1);
						flag = true;
					} else {
						// System.out.println("放过一个玩家就要等自己摸牌之后才能胡");
						huAvatar.remove(avatar);

					}
				} else {
					// 自摸,
					// system.out.println("自摸");
					// 胡牌数组中移除掉胡了的人
					// huAvatar.remove(avatar);
					huAvatar.clear();
					gangAvatar.clear();
					penAvatar.clear();
					chiAvatar.clear();
					// 两个人之间建立关联，游戏结束算账用 自摸不会出现抢胡的情况
					if (!validMa.isEmpty()) {
						List<Integer> newValidMa = HuPaiType.getInstance().getHuType(playerList.get(curAvatarIndex),
								avatar, roomVO.getRoomType(), cardIndex, playerList, mas, huCount, type,
								roomVO.getHong());
						for (Integer j : newValidMa) {
							validMa.add(j);
						}
					} else {
						validMa = HuPaiType.getInstance().getHuType(avatar, avatar, roomVO.getRoomType(), cardIndex,
								playerList, mas, huCount, "", roomVO.getHong());
					}
					roomVO.updateEndStatistics(avatar.getUuId() + "", "zimo", 1);
					flag = true;
				}
				// 本次游戏已经胡，不进行摸牌
				hasHu = true;
				// avatar.canHu = true;
				// 游戏回放
				PlayRecordOperation(playerList.indexOf(avatar), cardIndex, playRecordType, -1, null, null);
			}
		}
		if (huAvatar.size() == 0 && numb == 1) {
			numb++;
			// 所有人胡完
			if (huCount >= 2) {
				// 重新分配庄家，下一局点炮的玩家坐庄
				for (Avatar itemAva : playerList) {
					if (playerList.get(pickAvatarIndex).getUuId() == itemAva.getUuId()) {
						// itemAva.avatarVO.setMain(true);
						bankerAvatar = itemAva;
						itemAva.avatarVO.setMain(true);
					} else {
						itemAva.avatarVO.setMain(false);
					}
				}
			} else {
				if (!avatar.avatarVO.isMain()) {

					int nextIndex = 0;
					// 庄家的索引
					int zhuangJiaIndex = playerList.indexOf(bankerAvatar);
					if (zhuangJiaIndex + 1 < playerList.size()) {
						nextIndex = zhuangJiaIndex + 1;
					} else {
						nextIndex = 0;
					}

					// 重新分配庄家，下一局胡家坐庄
					for (Avatar itemAva : playerList) {
						itemAva.avatarVO.setMain(false);

					}
					bankerAvatar = playerList.get(nextIndex);
					playerList.get(nextIndex).avatarVO.setMain(true);
				}

			}
			// 更新roomlogic的PlayerList信息
			RoomManager.getInstance().getRoom(playerList.get(0).getRoomVO().getRoomId()).setPlayerList(playerList);
			// 一局牌胡了，返回这一局的所有数据吃，碰， 杠，胡等信息
			settlementData("0");
		}
		return flag;
	}

	/**
	 * 
	 * 胡牌/流局/解散房间后返回结算数据信息 不能多次调用，多次调用，总分会多增加出最近一局的分数 第一局结束扣房卡
	 */
	public void settlementData(String type) {
		int rate = roomVO.getTianShuiCoinType();
		int koudian = -1; // 每把结束玩家扣金币数
		if (rate / 10 == 2) {
			koudian = -2;
		} else if (rate / 10 == 5) {
			koudian = -5;
		}
		int totalCount = roomVO.getRoundNumber();
		int useCount = RoomManager.getInstance().getRoom(roomVO.getRoomId()).getCount();
		if (!type.equals("2") && !AppCf.freePlay) {
			for (Avatar avatar : playerList) {
				avatar.updateRoomCard(koudian);// 每把扣金币
				avatar.avatarVO.supdateScores(koudian);
			}
		}
		JSONArray array = new JSONArray();
		JSONObject json = new JSONObject();
		if (!type.equals("0")) {
			allMas = null;
		}
		StandingsDetail standingsDetail = new StandingsDetail();
		StringBuffer content = new StringBuffer();
		StringBuffer score = new StringBuffer();
		
		for (Avatar avatar : playerList) {
			HuReturnObjectVO huReturnObjectVO = avatar.avatarVO.getHuReturnObjectVO();
			if(huReturnObjectVO!=null){
				// 生成战绩内容
				content.append(avatar.avatarVO.getAccount().getNickname() + ":" + huReturnObjectVO.getTotalScore() + ",");

				// 统计本局分数
				huReturnObjectVO.setNickname(avatar.avatarVO.getAccount().getNickname());
				huReturnObjectVO.setPaiArray(avatar.avatarVO.getPaiArray()[0]);
				huReturnObjectVO.setUuid(avatar.getUuId());
				array.add(huReturnObjectVO);
				// 在整个房间信息中修改总分数(房间次数用完之后的总分数)
				roomVO.updateEndStatistics(avatar.getUuId() + "", "scores", huReturnObjectVO.getTotalScore());

				// 当前输赢分数计算房卡（金币）
				if(roomVO.getRoomType()!=4){
				avatar.updateRoomCard(avatar.avatarVO.getHuReturnObjectVO().getTotalScore());
				}
				avatar.getSession().sendMsg(new RoomCardChangerResponse(1, avatar.avatarVO.getAccount().getRoomcard()));

				score.append(avatar.getUuId() + ":" + avatar.avatarVO.getAccount().getRoomcard() + ",");
				// 修改存储的分数(断线重连需要)
				avatar.avatarVO.supdateScores(huReturnObjectVO.getTotalScore());
			}
			
			if(roomVO.getRoomType()!=4){//不是架锅
				// 游戏回放 中码消息
				if (avatar.avatarVO.isMain()) {
					if (!type.equals("0")) {
						PlayRecordOperation(playerList.indexOf(avatar), -1, 8, -1, null, null);
					} else {
						PlayRecordOperation(playerList.indexOf(avatar), -1, 8, -1, allMas,
								HuPaiType.getInstance().getValidMa());
					}
				}
			}
			
		}
		json.put("avatarList", array);
		json.put("allMas", allMas);
		json.put("type", type);
		if (!type.equals("0")) {
			json.put("validMas", new ArrayList<>());
		} else {
			json.put("validMas", validMa);
		}
		json.put("currentScore", score.toString());
		// 生成战绩content
		standingsDetail.setContent(content.toString());
		standingsDetail.setRoomType(roomVO.getRoomType());
		if(roomVO.getRoomType()!=4){
		try {
			standingsDetail.setCreatetime(DateUtil.toChangeDate(new Date(), DateUtil.maskC));
			int id = StandingsDetailService.getInstance().saveSelective(standingsDetail);
			if (id > 0) {
				RoomLogic roomLogic = RoomManager.getInstance().getRoom(roomVO.getRoomId());
				roomLogic.getStandingsDetailsIds().add(standingsDetail.getId());
				// 更新游戏回放中的玩家分数
				
					PlayRecordInitUpdateScore(standingsDetail.getId());
				
				
			} else {
				System.out.println("分局战绩录入失败：" + new Date());
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		}
		int count = 10;
		for (Avatar avatar : playerList) {

			// 发送消息
			avatar.getSession().sendMsg(new HuPaiResponse(1, json.toString()));

			avatar.overOff = true;
			avatar.oneSettlementInfo = json.toString();

			// 清除一些存储数据
			avatar.getResultRelation().clear();
			// avatar.avatarVO.setIsReady(false);10-11注释 在游戏开始之后就已经重置准备属性为false
			avatar.avatarVO.getChupais().clear();
			avatar.avatarVO.setCommonCards(0);
			avatar.avatarVO.setHasMopaiChupai(false);
			avatar.avatarVO.getPengGuo().clear();
			// 清除 hu ReturnObjectVO 信息
			avatar.avatarVO.setHuReturnObjectVO(new HuReturnObjectVO());
			avatar.avatarVO.setLay(null);
			avatar.avatarVO.setLayChu(new ArrayList<Integer>());
			avatar.avatarVO.setLayHu(new ArrayList<Integer>());
			avatar.LayPaiType = 0;
			avatar.avatarVO.setKouZi(false);

			count = RoomManager.getInstance().getRoom(avatar.getRoomVO().getRoomId()).getCount();
			// 没有经过算分 不能开始下一局游戏
			singleOver = true;// 10-11新增
			avatar.canHu = true;
			avatar.setQuestToFalse();
		}
		// 房间局数用完，返回本局胡牌信息的同时返回整个房间这几局的胡，杠等统计信息
		if (type.equals("2")) {
			//// int nowScore = 0;
			//// Avatar avatar2 = null;
			////
			////
			//// if(roomVO.getPayWay() == 0)
			//// {
			//// for(int i = 0; i < playerList.size();i++)
			//// {
			//// if(i == 0){
			//// //当前玩家的分数
			//// nowScore = playerList.get(i).avatarVO.getScores();
			//// avatar2 = playerList.get(i);
			//// }
			//// else{
			//// //判断当前玩家赢得最多的，按照大赢家的房卡扣赢得最多的，否则扣建房间的
			//// if(nowScore < playerList.get(i).avatarVO.getScores()){
			//// //这个就是赢得最多的玩家
			//// avatar2 = playerList.get(i);
			//// //赢得最多玩家的分数
			//// nowScore = playerList.get(i).avatarVO.getScores();
			////
			////
			//// }
			////
			//// }
			//// }
			//// deductRoomCard0(avatar2);
			////
			//// }
			//
			// //总房间战绩
			Standings standings = new Standings();
			StringBuffer sb = new StringBuffer();
			standings.setContent(content.toString());
			Map<String, Map<String, Integer>> endStatistics = roomVO.getEndStatistics();
			Map<String, Integer> map = new HashMap<String, Integer>();
			Set<Entry<String, Map<String, Integer>>> set = endStatistics.entrySet();
			JSONObject js = new JSONObject();
			List<FinalGameEndItemVo> list = new ArrayList<FinalGameEndItemVo>();
			FinalGameEndItemVo obj;
			for (Entry<String, Map<String, Integer>> param : set) {
				obj = new FinalGameEndItemVo();
				obj.setUuid(Integer.parseInt(param.getKey()));
				sb.append(AccountService.getInstance().selectByUUid(Integer.parseInt(param.getKey())).getNickname());
				map = param.getValue();
				for (Entry<String, Integer> entry : map.entrySet()) {
					switch (entry.getKey()) {
					case "zimo":
						obj.setZimo(entry.getValue());
						break;
					case "jiepao":
						obj.setJiepao(entry.getValue());
						break;
					case "dianpao":
						obj.setDianpao(entry.getValue());
						break;
					case "minggang":
						obj.setMinggang(entry.getValue());
						break;
					case "angang":
						obj.setAngang(entry.getValue());
						break;
					case "scores":
						obj.setScores(entry.getValue());
						sb.append(":" + entry.getValue() + ",");
						break;
					default:
						break;
					}
				}
				list.add(obj);
			}
			js.put("totalInfo", list);
			js.put("theowner", theOwner);
			// system.out.println("这个房间次数用完：返回数据=="+js.toJSONString());
			// //战绩记录存储
			standings.setContent(sb.toString());
			try {
				standings.setCreatetime(DateUtil.toChangeDate(new Date(), DateUtil.maskC));
				standings.setRoomid(roomVO.getId());
				int i = StandingsService.getInstance().saveSelective(standings);
				if (i > 0) {
					// 存储 房间战绩和每局战绩关联信息
					StandingsRelation standingsRelation;
					List<Integer> standingsDetailsIds = RoomManager.getInstance().getRoom(roomVO.getRoomId())
							.getStandingsDetailsIds();
					for (Integer standingsDetailsId : standingsDetailsIds) {
						standingsRelation = new StandingsRelation();
						standingsRelation.setStandingsId(standings.getId());
						standingsRelation.setStandingsdetailId(standingsDetailsId);
						StandingsRelationService.getInstance().saveSelective(standingsRelation);
					}
					// 存储 房间战绩和每个玩家关联信息
					StandingsAccountRelation standingsAccountRelation;
					for (Avatar avatar : playerList) {
						standingsAccountRelation = new StandingsAccountRelation();
						standingsAccountRelation.setStandingsId(standings.getId());
						standingsAccountRelation.setAccountId(avatar.avatarVO.getAccount().getId());
						StandingsAccountRelationService.getInstance().saveSelective(standingsAccountRelation);
					}
				}
				// System.out.println("整个房间战绩:"+i);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			// //发送消息
			for (Avatar avatar : playerList) {
				avatar.getSession().sendMsg(new HuPaiAllResponse(1, js.toString()));
			}
			// 4局完成之后解散房间//销毁
			RoomLogic roomLogic = RoomManager.getInstance().getRoom(roomVO.getRoomId());
			roomLogic.destoryRoomLogic();
			roomLogic = null;
		}
	}

	/**
	 * 出牌返回出牌点数和下一家玩家信息
	 * 
	 * @param
	 *
	 */
	private void chuPaiCallBack() {
		// 把出牌点数和下面该谁出牌发送会前端 下一家都还没有摸牌就要出牌了??
		if (!hasHu && checkMsgAndSend()) {
			// 如果没有吃，碰，杠，胡的情况，则下家自动摸牌
			pickCard();
		}
	}

	/**
	 * 發送吃，碰，杠，胡牌信息
	 * 
	 * @return
	 */
	private boolean checkMsgAndSend() {
		if (huAvatar.size() > 0) {
			return false;
		}
		if (gangAvatar.size() > 0) {
			return false;
		}
		if (penAvatar.size() > 0) {
			return false;
		}
		if (chiAvatar.size() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * 发牌
	 */
	private void dealingTheCards() {
		nextCardindex = 0;
		bankerAvatar = null;
		for (int i = 0; i < 13; i++) {
			for (int k = 0; k < playerList.size(); k++) {
				if (bankerAvatar == null) {
					if (playerList.get(k).avatarVO.isMain()) {
						bankerAvatar = playerList.get(k);
					}
				}

				playerList.get(k).putCardInList(listCard.get(nextCardindex));
				playerList.get(k).oneSettlementInfo = "";
				playerList.get(k).overOff = false;
				nextCardindex++;
			}
		}

		int cardpoint = listCard.get(nextCardindex);// 10-26新增listCard.get(nextCardindex)
		// int cardpoint = 9;
		bankerAvatar.putCardInList(cardpoint);
		// nextCardindex++;
		singleOver = false;
		// 检测一下庄家有没有天胡
		if (checkHu(bankerAvatar, cardpoint) && lay(bankerAvatar)) {
			// 检查有没有天胡/有则把相关联的信息放入缓存中
			huAvatar.add(bankerAvatar);
			//// 二期优化注释 pickAvatarIndex =
			//// playerList.indexOf(bankerAvatar);//第一个摸牌人就是庄家
			// 发送消息
			bankerAvatar.getSession().sendMsg(new ReturnInfoResponse(1, "normalhu:" + cardpoint + ","));
			bankerAvatar.huAvatarDetailInfo.add(cardpoint + ":" + 0);
		}
		// 检测庄家起手有没的杠 长沙麻将叫做大四喜
		if (bankerAvatar.checkSelfGang(cardpoint)) {
			gangAvatar.add(bankerAvatar);
			// 发送消息
			StringBuffer sb = new StringBuffer();
			sb.append("gang");
			for (int i : bankerAvatar.gangIndex) {
				sb.append(":" + i);
			}
			sb.append(",");

			bankerAvatar.getSession().sendMsg(new ReturnInfoResponse(1, sb.toString()));
			// bankerAvatar.huAvatarDetailInfo.add(bankerAvatar.gangIndex.get(0)+":"+2);
			// bankerAvatar.gangIndex.clear();
		}
		// 游戏回放
		PlayRecordInit();
	}

	/**
	 * 战绩细节初始化 游戏回放，记录 房间信息和初始牌组，玩家信息
	 */
	public void PlayRecordInit() {
		playRecordGame = new PlayRecordGameVO();
		RoomVO roomVo = roomVO.clone();
		roomVo.setEndStatistics(new HashMap<String, Map<String, Integer>>());
		roomVo.setPlayerList(new ArrayList<>());
		playRecordGame.roomvo = roomVo;
		PlayRecordItemVO playRecordItemVO;
		Account account;
		StringBuffer sb;
		for (int i = 0; i < playerList.size(); i++) {
			playRecordItemVO = new PlayRecordItemVO();
			account = playerList.get(i).avatarVO.getAccount();
			playRecordItemVO.setAccountIndex(i);
			playRecordItemVO.setAccountName(account.getNickname());
			sb = new StringBuffer();
			int[] str = playerList.get(i).getPaiArray()[0];
			for (int j = 0; j < str.length; j++) {
				sb.append(str[j] + ",");
			}
			playRecordItemVO.setCardList(sb.substring(0, sb.length() - 1));
			playRecordItemVO.setHeadIcon(account.getHeadicon());
			playRecordItemVO.setSex(account.getSex());
			playRecordItemVO.setGameRound(roomVO.getCurrentRound());
			playRecordItemVO.setUuid(account.getUuid());
			playRecordGame.playerItems.add(playRecordItemVO);
		}
	}

	/**
	 * 游戏回放，记录打牌操作信息
	 * 
	 * @param curAvatarIndex
	 *            操作玩家索引
	 * @param cardIndex
	 *            操作相关牌索引
	 * @param type
	 *            操作相关步骤 1出牌，2摸牌，3吃，4碰，5杠，6胡(自摸/点炮),7抢胡,8抓码,9:流局,10:过.....11，躺牌
	 * @param gangType
	 *            type不为杠时 传入 -1
	 * @param ma
	 *            不是抓码操作时 为null
	 */
	public void PlayRecordOperation(Integer curAvatarIndex, Integer cardIndex, Integer type, Integer gangType,
			String ma, List<Integer> valideMa) {

		// System.out.println("记录操作"+type);
		PlayBehaviedVO behaviedvo = new PlayBehaviedVO();
		behaviedvo.setAccountindex_id(curAvatarIndex);
		behaviedvo.setCardIndex(cardIndex + "");
		behaviedvo.setRecordindex(playRecordGame.behavieList.size());
		behaviedvo.setType(type);
		behaviedvo.setGangType(gangType);
		if (StringUtil.isNotEmpty(ma)) {
			behaviedvo.setMa(ma);
			behaviedvo.setValideMa(valideMa);
		}
		playRecordGame.behavieList.add(behaviedvo);

	}

	/**
	 * 游戏回放，记录 房间信息和初始牌组，玩家信息 中添加分数
	 * 
	 * @param standingsDetailId
	 *            本局游戏的id
	 */
	public void PlayRecordInitUpdateScore(int standingsDetailId) {

		if (!playRecordGame.playerItems.isEmpty()) {
			// 没有发牌就解散房间
			for (int i = 0; i < playerList.size(); i++) {
				playRecordGame.playerItems.get(i).setSocre(playerList.get(i).avatarVO.getScores());
			}
			// playRecordGame.standingsDetailId = standingsDetailId;
			// 信息录入数据库表中
			// String playRecordContent = JsonUtilTool.toJson(playRecordGame);
			String playRecordContent = JSONObject.toJSONString(playRecordGame);
			// System.out.println(playRecordContent);
			PlayRecord playRecord = new PlayRecord();
			playRecord.setPlayrecord(playRecordContent);
			playRecord.setStandingsdetailId(standingsDetailId);
			PlayRecordService.getInstance().saveSelective(playRecord);
			// 录入表之后重置 记录
			playRecordGame = new PlayRecordGameVO();
		}

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
//			 return 25; 
		}
		return -1;
		// return TestCardPoint();//测试发牌方法
	}

	/**
	 * 测试发牌方法 1 1 1
	 */
	int aaa[] = new int[] { 0, 0, 0, 3, 0, 0, 0, 0, 0 };
	int a = -1;

	private int TestCardPoint() {
		a++;
		return aaa[a];
	}

	private void checkQiShouFu() {
		for (int i = 0; i < playerList.size(); i++) {
			// 判断是否有起手胡，有则加入到集合里面
			if (qiShouFu(playerList.get(i))) {
				qishouHuAvatar.add(playerList.get(i));
			}
		}
	}

	/**
	 * 是否是起手胡
	 * 
	 * @return
	 */
	public boolean qiShouFu(Avatar avatar) {
		/**
		 * 起手胡： 1 、大四喜：起完牌后，玩家手上已有四张一样的牌，即可胡牌。（四喜计分等同小胡自摸）pai[i] == 4 2
		 * 、板板胡：起完牌后，玩家手上没有一张 2 、 5 、 8 （将牌），即可胡牌。（等同小胡自摸） 3
		 * 、缺一色：起完牌后，玩家手上筒、索、万任缺一门，即可胡牌。（等同小胡自摸） 4 、六六顺：起完牌后，玩家手上已有 2
		 * 个刻子（刻子：三个一样的牌），即可胡牌。（等同小胡自摸）
		 */
		// 1:大四喜
		boolean flag = false;
		int[] pai = avatar.avatarVO.getPaiArray()[0];
		boolean flagWan = true;
		boolean flagTiao = true;
		boolean flagTong = true;
		int threeNum = 0;
		boolean dasixi = false;
		boolean banbanhu = false;
		boolean quyise = false;
		boolean liuliushun = false;
		for (int i = 0; i < pai.length; i++) {
			if (pai[i] == 4) {
				// 大四喜
				dasixi = true;
				// 胡牌信息放入缓存中****
			}
			if (pai[i] == 3) {
				// 六六顺
				threeNum++;
				if (threeNum == 2) {
					liuliushun = true;
				}
			}
			if (i >= 0 && i <= 8) {
				// 缺一色
				if (pai[i] > 0) {
					// 只要存在一条万子
					flagWan = false;
				}
			} else if (i > 9 && i <= 18) {
				// 缺一色
				if (pai[i] > 0) {
					// 只要存在一条条子
					flagTiao = false;
				}
			} else {
				// 缺一色
				if (pai[i] > 0) {
					// 只要存在一条筒子
					flagTong = false;
				}
			}
		}
		if (pai[1] == 0 && pai[4] == 0 && pai[7] == 0 && pai[10] == 0 && pai[13] == 0 && pai[16] == 0 && pai[19] == 0
				&& pai[22] == 0 && pai[25] == 0) {
			// 板板胡
			banbanhu = true;
		}
		if ((flagWan || flagTiao || flagTong)) {
			// 缺一色
			quyise = true;
		}
		return flag;
	}

	private List<AvatarVO> getAvatarVoList() {
		List<AvatarVO> result = new ArrayList<>();
		for (int m = 0; m < playerList.size(); m++) {
			result.add(playerList.get(m).avatarVO);
		}
		return result;
	}

	/**
	 * 清理玩家身上的牌数据
	 */
	private void cleanPlayListCardData() {
		for (int i = 0; i < playerList.size(); i++) {
			playerList.get(i).cleanPaiData();
		}
	}

	/**
	 * 检测胡牌算法，其中包含七小对，普通胡牌
	 * 
	 * @param avatar
	 * @return
	 */
	private boolean checkHu(Avatar avatar, Integer cardIndex) {
		// 根据不同的游戏类型进行不用的判断
		if (roomVO.getRoomType() == 1) {
			// 转转麻将
			return checkHuZZhuan(avatar);
		} else if (roomVO.getRoomType() == 2) {
			// 划水麻将
			return checkHuHShui(avatar, cardIndex);
		} else {
			// 长沙麻将
			return checkHuChangsha(avatar);
		}

		/*
		 * if(roomVO.getSevenDouble() && !roomVO.getHong()) {
		 * //有癞子时，直接进行癞子的胡牌判断，不需要进行单独的判断 int isSeven =
		 * checkSevenDouble(paiList); if(isSeven == 0){
		 * System.out.println("没有七小对"); if(isHuPai(paiList)){
		 * System.out.print("胡牌"); //cleanPlayListCardData(); }else{
		 * System.out.println("checkHu 没有胡牌"); } }else{
		 * 
		 * if(isSeven == 1){ System.out.println("七对"); }else{
		 * System.out.println("龙七对"); } //cleanPlayListCardData(); return true;
		 * } } if(roomVO.getRoomType() == 1 && roomVO.getHong()){ //转转麻将，可以选择红中
		 * //红中当癞子 return Naizi.testHuiPai(paiList); } else{ return
		 * isHuPai(paiList); }
		 */
	}

	/**
	 * 判断转转麻将是否胡牌
	 * 
	 * @param avatar
	 * @return 1:是否自摸胡 /1- 自摸胡，2- 可以抢杠胡 2:是否抢杠胡 /1- 自摸胡，2- 可以抢杠胡 3:是否红中赖子 4:是否抓码
	 *         5:是否可胡七小对
	 */
	public boolean checkHuZZhuan(Avatar avatar) {
		int[][] paiList = avatar.getPaiArray();
		// 不需要移除掉碰，杠了的牌组，在判断是否胡的时候就应判断了
		// paiList = cleanGangAndPeng(paiList,avatar);
		boolean flag = false;
		// if(roomVO.getZiMo() == 2 || roomVO.getZiMo() == 0){
		// 可以抢杠胡（只有可抢杠胡的时候才判断其他人有没有胡牌）
		if (roomVO.getSevenDouble() && !flag) {
			// 可七小队
			int isSeven = checkSevenDouble(paiList.clone());
			if (isSeven == 0) {
				// System.out.println("没有七小对");
			} else {
				if (isSeven == 1) {
					// System.out.println("七对");
				} else {
					// System.out.println("龙七对");
				}
				flag = true;
			}
		}
		if (!flag) {
			if (roomVO.getHong()) {
				// 有癞子
				flag = Naizi.testHuiPai(paiList.clone());
			} else {
				flag = normalHuPai.checkZZHu(paiList.clone());
			}
		}
		return flag;
	}

	/**
	 * 判断划水麻将是否胡牌
	 * 
	 * @param avatar
	 * @return
	 */
	public boolean checkHuHShui(Avatar avatar, Integer cardIndex) {
		int[][] paiList = avatar.getPaiArray();
		boolean flag = false;
		if (roomVO.getSevenDouble() && !flag) {
			// 可七小队
			int isSeven = checkSevenDouble(paiList.clone());
			if (isSeven == 0) {
				// system.out.println("没有七小对");
			} else {
				if (isSeven == 1) {
					// system.out.println("七对");
				} else {
					// system.out.println("龙七对");
				}
				if (pickAvatarIndex == playerList.indexOf(avatar)) {
					// 自摸七小队
					avatar.huAvatarDetailInfo.add(currentCardPoint + ":" + 7);
				} else {
					// 点炮七小队
					avatar.huAvatarDetailInfo.add(cardIndex + ":" + 6);
				}
				avatar.avatarVO.setHuType(2);// 划水麻将大胡
				flag = true;
			}
		}
		/*
		 * if(!flag){
		 *//**
			 * 1111 、11 、11 、11、 11 、11 （这种也算胡） 1111、1111、11、11、11（这种也算对胡） 1111
			 * 1111 1111 11(也算对胡）
			 *//*
			 * //特殊算法 if(!flag){ int twoCard = 0; int fourCard = 0; //1111 、11
			 * 、11 、11、 11 、11 for (int i = 0; i < paiList[0].length; i++) {
			 * if(paiList[0][i] ==2){ twoCard++; } else if(paiList[0][i] ==4){
			 * fourCard ++; } } if((twoCard == 5 && fourCard == 1) || (twoCard
			 * == 3 && fourCard == 2) || (twoCard == 1 && fourCard == 3)){
			 * avatar.avatarVO.setHuType(1);//划水麻将小胡
			 * System.out.println("特殊算法胡"); flag = true; } } }
			 */
		// 判断是否可以普通胡的时候，需要检测 风牌是否都是成对或成三
		if (!flag) {
			flag = normalHuPai.checkHSHu(paiList.clone(), roomVO.isAddWordCard());
			if (flag) {
				// system.out.println("普通胡");
				avatar.avatarVO.setHuType(1);// 划水麻将小胡
			}
		}
		return flag;

	}

	/**
	 * 判断长沙麻将是否胡牌
	 * 
	 * @param avatar
	 * @return
	 */
	public boolean checkHuChangsha(Avatar avatar) {
		/*
		 * if(roomVO.getRoomType() == 3) { //判读有没有起手胡 checkQiShouFu(); } return
		 * false;
		 */

		/**
		 * 长沙麻将第一版
		 */
		int[][] paiList = avatar.getPaiArray();
		// 不需要移除掉碰，杠了的牌组，在判断是否胡的时候就应判断了
		// paiList = cleanGangAndPeng(paiList,avatar);
		boolean flag = false;
		// if(roomVO.getZiMo() == 2 || roomVO.getZiMo() == 0){
		// 可以抢杠胡（只有可抢杠胡的时候才判断其他人有没有胡牌）
		// if(roomVO.getSevenDouble() && !flag){ //默认客户七小队
		// 可七小队
		int result = checkSevenDoubleCS(paiList.clone());
		if (result == 0) {
			// System.out.println("没有七小对");
		} else {
			// ling
			if (result == 1) {
				avatar.avatarVO.getHuReturnObjectVO().setHuType("七小对");
			} else if (result == 2) {
				avatar.avatarVO.getHuReturnObjectVO().setHuType("豪华七小队");
			} else if (result == 3) {
				avatar.avatarVO.getHuReturnObjectVO().setHuType("双豪华七小队");
			} else if (result == 4) {
				avatar.avatarVO.getHuReturnObjectVO().setHuType("三豪华七小队");
			}
			avatar.avatarVO.setHuType(avatar.avatarVO.getHuType() + result);
			flag = true;
		}
		// }
		if (!flag) {
			/*
			 * if(roomVO.getHong()){ //有癞子 flag =
			 * Naizi.testHuiPai(paiList.clone()); } else{
			 */
			flag = normalHuPai.checkChangS(paiList.clone());
			// }
		}

		if (flag) {// 判断是否是碰碰胡/清一色
			if (HuPaiType.getInstance().checkQingyise(paiList[0].clone())) {
				avatar.avatarVO.setHuType(avatar.avatarVO.getHuType() + 1);
				// ling
				avatar.avatarVO.getHuReturnObjectVO().setHuType("清一色");
			}
			if (HuPaiType.getInstance().checkPengPengHu(paiList[0].clone()) && result == 0) {
				avatar.avatarVO.setHuType(avatar.avatarVO.getHuType() + 1);
				// ling
				avatar.avatarVO.getHuReturnObjectVO().setHuType("碰碰胡");
			}
		}
		return flag;

	}

	/**
	 * 最后胡牌的检测胡牌的时候在牌组中提出条碰，杠的牌组再进行验证
	 * 
	 * @param paiList
	 * @return
	 */
	public int[] cleanGangAndPeng(int[] paiList, Avatar avatar) {

		String str;
		String strs[];
		int cardIndex;
		if ((str = avatar.getResultRelation().get(1)) != null) {
			// 踢出碰的牌组
			strs = str.split(",");
			for (String string : strs) {
				cardIndex = Integer.parseInt(string.split(":")[1]);
				if (paiList[cardIndex] >= 3) {
					paiList[cardIndex] = paiList[cardIndex] - 3;
				} else {
					// system.out.println("出现碰了的牌不在手牌中的错误情况!");
				}
			}
		}

		if (avatar.getResultRelation().get(2) != null) {
			// 踢出杠的牌组
			// 踢出碰的牌组
			strs = str.split(",");
			for (String string : strs) {
				cardIndex = Integer.parseInt(string.split(":")[1]);
				if (paiList[cardIndex] == 4) {
					paiList[cardIndex] = 0;
				} else {
					// system.out.println("出现碰了的牌不在手牌中的错误情况!");
				}
			}
		}
		return paiList;
	}

	/**
	 * 
	 * @param paiList
	 * @return
	 */
	String getString(int[] paiList) {
		String result = "int string = ";
		for (int i = 0; i < paiList.length; i++) {
			result += paiList[i];
		}
		return result;
	}

	/**
	 * 检查是否七小对胡牌
	 * 
	 * @param paiList
	 * @return 0-没有胡牌。1-普通七小对，2-龙七对
	 */
	public int checkSevenDouble(int[][] paiList) {
		int result = 1;
		if (roomVO.getHong()) {
			// 红中麻将另算
			int count = 0;// 单拍个数
			for (int i = 0; i < paiList[0].length; i++) {
				if (paiList[0][i] != 0 && i != 31) {
					if (paiList[0][i] != 2 && paiList[0][i] != 4) {
						if (paiList[1][i] == 0) {
							count++;
						} else {
							return 0;
						}
					} else {
						if (paiList[1][i] != 0) {
							return 0;
						} else {
							if (paiList[0][i] == 4) {
								result = 2;
							}
						}
					}
				}
			}
			if (count != 0 && count != paiList[0][31]) {
				return 0;
			}
		} else {
			for (int i = 0; i < paiList[0].length; i++) {
				if (paiList[0][i] != 0) {
					if (paiList[0][i] != 2 && paiList[0][i] != 4) {
						return 0;
					} else {
						if (paiList[1][i] != 0) {
							return 0;
						} else {
							if (paiList[0][i] == 4) {
								result = 2;
							}
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * 检查是否七小对胡牌(长沙麻将专用)
	 * 
	 * @param paiList
	 * @return 0-没有胡牌。1-普通七小对，2-龙七对(豪华)，3:-龙七对(双豪华)，4-龙七对(三豪华) result 代表番数(实际分数为
	 *         2的result次方)
	 */
	public int checkSevenDoubleCS(int[][] paiList) {
		int result = 1;
		for (int i = 0; i < paiList[0].length; i++) {
			if (paiList[0][i] != 0) {
				if (paiList[0][i] != 2 && paiList[0][i] != 4) {
					return 0;
				} else {
					if (paiList[1][i] != 0) {
						return 0;
					} else {
						if (paiList[0][i] == 4) {
							result++;
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * 前后端握手消息处理，前段接收到消息则会访问整个握手接口，说明接收到信息了 然后后台从list里面移除这个用户对应的uuid，
	 * 到最后list里面剩下的就表示前段还没有接收到消息， 则重新发送消息
	 * 
	 * @param avatar
	 */
	public void shakeHandsMsg(Avatar avatar) {
		shakeHandsInfo.remove(avatar.getUuId());

	}

	/**
	 * 在可以抢杠胡的情况下，判断其他人有没胡的情况
	 * 
	 * @return boolean
	 */
	public boolean checkQiangHu(Avatar avatar, int cardPoint) {
		boolean flag = false;

		for (Avatar ava : playerList) {
			if (ava.getUuId() != avatar.getUuId() && ava.qiangHu) {
				// 判断其他三家有没抢胡的情况
				ava.putCardInList(cardPoint);
				// 存抢胡信息（划水麻将才有，转转麻将当做普通点炮）(转转麻将被抢胡了 减6分)
				if (roomVO.getRoomType() == 2) {// 划水麻将
					if (lay(ava) && checkHuHShui(ava, cardPoint) && chuPaiNoHu(ava, cardPoint) && !ava.avatarVO.isKouZi()) {
						huAvatar.add(ava);
						// 向玩家发送消息
						ava.getSession().sendMsg(new ReturnInfoResponse(1, "qianghu:" + cardPoint));
						ava.avatarVO.setHuType(2);// 划水麻将抢杠胡为大胡
						flag = true;

					}
				} else if (roomVO.getRoomType() == 1) {// 转转麻将
					// 在后面的胡牌信息里面修改的分数
					if (checkHuZZhuan(ava)) {
						huAvatar.add(ava);
						// 向玩家发送消息
						ava.getSession().sendMsg(new ReturnInfoResponse(1, "qianghu:" + cardPoint));
						flag = true;
					}
				} else if (roomVO.getRoomType() == 3) {// 长沙麻将
					// 在后面的胡牌信息里面修改的分数
					if (checkHuChangsha(ava)) {
						huAvatar.add(ava);
						// 向玩家发送消息
						// ava.avatarVO.setHuType(ava.avatarVO.getHuType() +
						// 1);//长沙麻将抢杠胡加一番(重新算法)
						ava.getSession().sendMsg(new ReturnInfoResponse(1, "qianghu:" + cardPoint));
						flag = true;
					}
				}
				ava.pullCardFormList(cardPoint);
			}
		}
		if (flag) {
			qianghu = true;
			// 有人可以抢杠胡的时候，出牌玩家的索引为当前杠牌玩家
			// curAvatarIndex = playerList.indexOf(avatar);//2016-8-9 22:34修改
			// avatar.pullCardFormList(cardPoint);
		}
		return flag;
	}

	/**
	 * 玩家玩游戏时断线重连
	 * 
	 * @param avatar
	 */
	public void returnBackAction(Avatar avatar) {
		RoomVO room = roomVO.clone();
		List<AvatarVO> lists = new ArrayList<AvatarVO>();
		for (int i = 0; i < playerList.size(); i++) {
			if (playerList.get(i).getUuId() != avatar.getUuId()) {
				// 给其他三个玩家返回重连用户信息
				playerList.get(i).getSession().sendMsg(new OtherBackLoginResonse(1, avatar.getUuId() + ""));
			}
			lists.add(playerList.get(i).avatarVO);
		}
		// 给自己返回整个房间信息
		AvatarVO avatarVo = null;
		List<AvatarVO> playerLists = new ArrayList<AvatarVO>();
		for (int j = 0; j < lists.size(); j++) {
			int paiCount = 0;// 有多少张普通牌
			avatarVo = lists.get(j);
			if (avatarVo.getAccount().getUuid() != avatar.getUuId()) {
				// 其他三家的牌组需要处理，不能让重连的玩家知道详细的牌组
				for (int k = 0; k < avatarVo.getPaiArray()[0].length; k++) {
					if (avatarVo.getPaiArray()[0][k] != 0) {
						paiCount = paiCount + avatarVo.getPaiArray()[0][k];
						// avatarVo.getPaiArray()[0][k] = 0;
					}
				}
				//avatarVo.setPickCards(avatarVo.getHuReturnObjectVO().getPaiArray());
				avatarVo.setCommonCards(paiCount);
				playerLists.add(avatarVo);

			} else {
				// 不需要处理自己的牌组
				playerLists.add(avatarVo);
			}
		}
		if (playerList.size() == roomVO.getPeoples() - 1) {
			playerList.add(avatar);
		}
		if (playerLists.size() == roomVO.getPeoples() - 1) {
			playerLists.add(avatar.avatarVO);
		}
		/*
		 * else{ for (int i = 0; i < playerLists.size(); i++) {
		 * if(playerLists.get(i).getAccount().getUuid() == avatar.getUuId() ){
		 * playerLists.remove(i); playerLists.add(avatar.avatarVO);; } } }
		 */
		
		room.setPlayerList(playerLists);
		avatar.getSession().sendMsg(new BackLoginResponse(1, room));
		// lastAvtar.getSession().sendMsg(responseMsg);

	}

	/**
	 * 断线重连返回最后操作信息
	 * 
	 * @param avatar
	 */
	public void LoginReturnInfo(Avatar avatar) {
		// 断线重连之后，该进行的下一步操作，json存储下一步操作指引
		JSONObject json = new JSONObject();//
		StringBuffer sb = new StringBuffer();
		if (huAvatar.contains(avatar)) {
			// 这里需要判断是自摸胡，还是别人点炮胡
			if (pickAvatarIndex != curAvatarIndex) {
				// 自摸
				json.put("currentCardPoint", currentCardPoint);// 当前摸的牌点数
				json.put("curAvatarIndex", curAvatarIndex);// 当前出牌人的索引
				json.put("putOffCardPoint", putOffCardPoint);// 当前出的牌的点数
				if (!qianghu) {
					sb.append("normalhu:" + currentCardPoint + ",");
				}
			} else {
				// 点炮
				json.put("curAvatarIndex", curAvatarIndex);// 当前出牌人的索引
				json.put("putOffCardPoint", putOffCardPoint);// 当前出的牌的点数
				if (!qianghu) {
					sb.append("normalhu:" + putOffCardPoint + ",");
				}
			}
			if (qianghu) {
				sb.append("qianghu:" + putOffCardPoint + ",");
				// system.out.println("抢胡");
			}
			// else{
			// sb.append("hu,");
			// //system.out.println("胡");
			// }
		}

		if (penAvatar.contains(avatar)) {
			sb.append("peng:" + curAvatarIndex + ":" + putOffCardPoint + ",");
			json.put("curAvatarIndex", curAvatarIndex);// 当前出牌人的索引
			json.put("putOffCardPoint", putOffCardPoint);// 当前出的牌的点数
			System.out.println("碰");
		}
		if (gangAvatar.contains(avatar)) {
			// 这里需要判断是别人打牌杠，还是自己摸牌杠
			StringBuffer gangCardIndex = new StringBuffer();
			List<Integer> gangIndexs = avatar.gangIndex;
			for (int i = 0; i < gangIndexs.size(); i++) {
				gangCardIndex.append(":" + gangIndexs.get(i));
			}
			if (avatar.getUuId() == playerList.get(pickAvatarIndex).getUuId()) {
				// 自摸杠
				sb.append("gang" + gangCardIndex.toString() + ",");
				json.put("currentCardPoint", currentCardPoint);// 当前摸的牌点数
				json.put("pickAvatarIndex", pickAvatarIndex);// 当前摸牌人的索引
				json.put("putOffCardPoint", putOffCardPoint);// 当前出的牌的点数
				json.put("curAvatarIndex", curAvatarIndex);// 当前出牌人的索引
				// system.out.println("自杠");
			} else {
				json.put("curAvatarIndex", curAvatarIndex);// 当前出牌人的索引
				json.put("putOffCardPoint", putOffCardPoint);// 当前出的牌的点数
				sb.append("gang" + gangCardIndex.toString() + ",");
				// system.out.println("点杠");
			}
		}
		if (sb.length() > 1) {
			// 该自己杠/胡/碰
			// 游戏轮数
			int roundNum = RoomManager.getInstance().getRoom(avatar.getRoomVO().getRoomId()).getCount();
			json.put("gameRound", roundNum);// 游戏轮数
			// 桌面剩余牌数
			json.put("surplusCards", listCard.size() - nextCardindex - 1);

			// 刚加的：当前出的牌
			json.put("curAvatarIndex", curAvatarIndex);

			avatar.getSession().sendMsg(new ReturnOnLineResponse(1, json.toString()));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// System.out.println(sb);
			avatar.getSession().sendMsg(new ReturnInfoResponse(1, sb.toString()));
		} else {
			if (avatar.getUuId() == playerList.get(pickAvatarIndex).getUuId()) {
				// 该自己出牌
				// system.out.println("自己出牌");
				json.put("currentCardPoint", currentCardPoint);// 当前摸的牌点数，当currentCardPoint
																// = -2时
																// 表示是碰了之后出牌
				json.put("pickAvatarIndex", pickAvatarIndex);// 当前摸牌人的索引
				json.put("curAvatarIndex", curAvatarIndex);// 当前出牌人的索引
				json.put("putOffCardPoint", putOffCardPoint);// 当前出的牌的点数
			} else {
				json.put("curAvatarIndex", curAvatarIndex);// 当前出牌人的索引
				json.put("pickAvatarIndex", pickAvatarIndex);// 当前摸牌人的索引
				json.put("putOffCardPoint", putOffCardPoint);// 当前出的牌的点数
			}
			// 游戏局数
			int roundNum = RoomManager.getInstance().getRoom(avatar.getRoomVO().getRoomId()).getCount();
			json.put("gameRound", roundNum);
			// 桌面剩余牌数
			json.put("surplusCards", listCard.size() - nextCardindex - 1);
			// System.out.println(json.toString());
			avatar.getSession().sendMsg(new ReturnOnLineResponse(1, json.toString()));
		}

	}

	/**
	 * 清空所有数组
	 */
	public void clearAvatar() {
		huAvatar.clear();
		gangAvatar.clear();
		penAvatar.clear();
		chiAvatar.clear();
		qishouHuAvatar.clear();
	}

	/**
	 * 清空除胡之外的数组
	 */
	public void clearAvatarExceptHu() {
		penAvatar.clear();
		gangAvatar.clear();
		chiAvatar.clear();
		qishouHuAvatar.clear();
	}

	/**
	 * 检测当，缓存数组里全部为空时，放弃操作，则不起作用
	 */
	public boolean validateStatus() {
		if (huAvatar.size() > 0 || penAvatar.size() > 0 || gangAvatar.size() > 0 || chiAvatar.size() > 0
				|| qishouHuAvatar.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 第一局结束扣房卡
	 */
	public void deductRoomCard() {
		int currentCard = 0;
		Avatar avatar = playerList.get(0);
		if (roomVO.getRoundNumber() == 4 && avatar.avatarVO.getAccount().getRoomcard() >= 20) {
			currentCard = -20;
		} else if (roomVO.getRoundNumber() == 8 && avatar.avatarVO.getAccount().getRoomcard() >= 30) {
			currentCard = -30;
		} else if (roomVO.getRoundNumber() == 16 && avatar.avatarVO.getAccount().getRoomcard() >= 40) {
			currentCard = -40;
		}
		/*
		 * else{ currentCard = 0 - roomVO.getRoundNumber()/8; }
		 */
		avatar.updateRoomCard(currentCard);// 开始游戏，减去房主的房卡,同时更新缓存里面对象的房卡(已经在此方法中修改)
		int roomCard = avatar.avatarVO.getAccount().getRoomcard();
		avatar.getSession().sendMsg(new RoomCardChangerResponse(1, roomCard));
		System.out.println("扣房卡玩家：" + avatar.avatarVO.getAccount().getNickname());
	}

	// 大赢家
	public void deductRoomCard0(Avatar avatar) {

		int currentCard = 0;
		if (roomVO.getRoundNumber() == 4 && avatar.avatarVO.getAccount().getRoomcard() >= 20) {
			currentCard = -20;
		} else if (roomVO.getRoundNumber() == 8 && avatar.avatarVO.getAccount().getRoomcard() >= 30) {
			currentCard = -30;
		} else if (roomVO.getRoundNumber() == 16 && avatar.avatarVO.getAccount().getRoomcard() >= 40) {
			currentCard = -40;
		}
		/*
		 * else { currentCard = -1; }
		 */
		// Avatar zhuangAvatar = playerList.get(0);
		avatar.updateRoomCard(currentCard);// 开始游戏，减去房主的房卡,同时更新缓存里面对象的房卡(已经在此方法中修改)
		int roomCard = avatar.avatarVO.getAccount().getRoomcard();
		avatar.getSession().sendMsg(new RoomCardChangerResponse(1, roomCard));
		System.out.println("扣房卡玩家：" + avatar.avatarVO.getAccount().getNickname());
	}

	/**
	 * 过手碰不能碰
	 * 
	 * @param peng
	 * @param cardPoint
	 * @return
	 */
	// public boolean pengGuo(List<Integer> peng,int cardPoint){
	// if(!peng.isEmpty() && peng.size() >= 1){
	// for(Integer integer : peng){
	// if(integer == cardPoint){
	// return false;
	// }
	// }
	// }
	// return true;
	// }

	public List<Avatar> getHuAvatar() {
		return huAvatar;
	}

	public List<Avatar> getPenAvatar() {
		return penAvatar;
	}

	public List<Avatar> getGangAvatar() {
		return gangAvatar;
	}

	public List<Avatar> getQishouHuAvatar() {
		return qishouHuAvatar;
	}
	/**
	 * 躺牌请求
	 * 
	 * @param avatar
	 * @param layCards
	 * @return
	 */
	public boolean layCards(Avatar avatar,int layCards) {
		//出牌对应的胡牌
		huPai = new HashMap<Integer,List<Integer>>();
		// 如果是七对，就用这个来存放七对躺牌的胡牌
		int qiDui = 100;
		//是否通过七对判断
		boolean qiDuiHu = false;
		// 获取可以出的牌
		List<Integer> putOffCardList = new ArrayList<>();
		// 初始化牌组
		int[] paiArrays = new int[34];
		// 获取牌组
		int[][] paiArra = { avatar.getSinglePaiArray(), GlobalUtil.CloneIntList(avatar.getPaiArray()[1]) };
		// 二抓躺牌
		if (roomVO.getSevenDouble() && layQiDui(paiArra) == 6) {
			// 设置躺牌中的单牌初始值
			int one = 0;
			// 躺牌中单牌只能有一张，而这一张也是胡牌
			for (int i = 0; i < paiArrays.length; i++) {
				if (paiArrays[i] != 0 && paiArrays[i] == 1) {
					one++;
					qiDui = i;
				}
			}
			// 躺牌中单牌不只一张，躺牌失败
			if (one != 1) {
				avatar.getSession().sendMsg(new LayFailedResponse(1, "1"));
				// System.out.println("躺牌失败");
				qiDuiHu = false;
			}
			one = 0;
			// 去掉躺牌的手牌中，单牌只能有一张，而这一张也是要出的牌
			for (int i = 0; i < paiArra[0].length; i++) {
				if (paiArra[0][i] != 0 && paiArra[0][i] == 1) {
					one++;
					putOffCardList.add(i);
					qiDuiHu = true;
				}
			}
			// 去掉躺牌的手牌中，单牌不止一张，躺牌失败
			if (one != 1) {
				avatar.getSession().sendMsg(new LayFailedResponse(1, "1"));
				// System.out.println("躺牌失败");
				qiDuiHu = false;
			}
		}
		// 前面是判断七小队的，下面是正常牌型
		if(!qiDuiHu){
			boolean hu = true;
			// 判断当前摸上来的牌可否胡牌,并且没有听牌的话,就让任意出一张牌
			if (normalHuPai.checkHSHu(paiArra, hu) && !lay(avatar)) {
				for (int k = 0; k < paiArra[0].length; k++) {
					putOffCardList.add(k);
				}
				avatar.avatarVO.setKouZi(true);
			}
			else {
				// 判断传过来的没有值的话。是碰听
				if (layCards == -1) {
					for (int i = 0; i < paiArra[0].length; i++) {
						// 判断当前有牌的话，减一张
						if (paiArra[0][i] != 0) {
							paiArra[0][i]--;
							// 这里是加一张牌一张牌进去看能不能胡
							for (int j = 0; j < paiArra[0].length; j++) {
								paiArra[0][j]++;
								// 判断如果可以胡牌的话
								if (paiCount(paiArra) && !putOffCardList.contains(i)) {
									putOffCardList.add(i);
									//保存胡的牌
									if(huPai.get(i) == null){
										List<Integer> huList = new ArrayList<Integer>();
										huList.add(j);
										huPai.put(i, huList);
									}
									else{
										List<Integer> huList = huPai.get(i);
										huList.add(j);
										huPai.put(i, huList);
									}
								}
								paiArra[0][j]--;
							}
							paiArra[0][i]++;
						}
					}
				}
				else {
					// 主要是检测去掉躺牌以后，出一张牌能不能胡，第一层循环牌 摸听
					for (int i = 0; i < paiArra[0].length; i++) {
						// 判断当前有牌的话，减一张 当前前端传过来的牌不等于当前索引，就不是莫的牌
						if (paiArra[0][i] != 0 && (i != layCards || i == layCards && paiArra[0][i] > 1)) {
							paiArra[0][i]--;
							// 这里是加一张牌进去看能不能胡
							for (int j = 0; j < paiArra[0].length; j++) {
								paiArra[0][j]++;
								// 判断如果可以胡牌的话
								if (paiCount(paiArra) && paiArra[1][i] != 1 && paiArra[1][i] != 2) {
									if(!putOffCardList.contains(i)){
										putOffCardList.add(i);
									}
									//保存胡的牌
									if(huPai.get(i) == null){
										List<Integer> huList = new ArrayList<Integer>();
										huList.add(j);
										huPai.put(i, huList);
									}
									else{
										List<Integer> huList = huPai.get(i);
										huList.add(j);
										huPai.put(i, huList);
									}
								}
								paiArra[0][j]--;
							}
							paiArra[0][i]++;
						}
					}
				}
			}
		}
		// 没有可以出的牌，躺牌失败 并且摸的牌不等于出的牌，躺牌失败
		if (putOffCardList.size() == 0 && !putOffCardList.equals(currentCardPoint)) {
			avatar.getSession().sendMsg(new LayFailedResponse(1, "1"));
			// System.out.println("躺牌失败");
			return false;
		}
		// 记录可以出的牌
		avatar.avatarVO.setLayChu(putOffCardList);
		// 当前出的牌的点数
		int[] pickList = { putOffCardPoint };
		// 当前摸得牌的点数
		int[] curList = { currentCardPoint };
		// 给其他玩家发送躺牌消息和躺牌
		for (int i = 0; i < playerList.size(); i++) {
			if (nextCardindex == 13 * playerList.size()) {
				int cardpoint = listCard.get(nextCardindex);
				pickList = new int[1];
				pickList[0] = cardpoint;
				avatar.avatarVO.setLay(pickList);
				playerList.get(i).getSession().sendMsg(
						new LayCardsResponse(1, avatar.getUuId(), avatar.avatarVO.getLay(), putOffCardList, true));
			}
			// 这里意思是碰牌，就把上一家的牌设置进去
			else if (layCards == -1) {
				avatar.avatarVO.setLay(pickList);
				playerList.get(i).getSession().sendMsg(
						new LayCardsResponse(1, avatar.getUuId(), avatar.avatarVO.getLay(), putOffCardList, true));
			}
			// 否则是摸牌，就把当前摸得牌放置进去
			else {
				avatar.avatarVO.setLay(curList);
				playerList.get(i).getSession().sendMsg(
						new LayCardsResponse(1, avatar.getUuId(), avatar.avatarVO.getLay(), putOffCardList, false));
			}
		}
		// 判断如果是碰听的话，就将上一家出的牌传进去，否则直接传档期安排
		if (layCards == -1) {
			PlayRecordOperation(playerList.indexOf(avatar), putOffCardPoint, 11, -1, null, null);
		} else {
			PlayRecordOperation(playerList.indexOf(avatar), layCards, 11, -1, null, null);
		}
		
		return true;
	}
	/**
	 * 检测除去躺牌后手牌是否能胡
	 * 
	 * @param paiList
	 * @return
	 */
	public boolean paiCount(int paiList[][]) {
		// 不需要移除掉碰，杠了的牌组，在判断是否胡的时候就应判断了
		boolean feng = true;
		boolean flag = false;
		// 可七小队
		int isSeven = checkSevenDouble(paiList.clone());
		if (isSeven == 7) {
			flag = true;
		}
		if (!flag) {
			flag = normalHuPai.checkHSHu(paiList, feng);
		}
		return flag;
	}

	// 判断二抓的

	public int layQiDui(int[][] paiList) {
		int result = 0;
		for (int i = 0; i < paiList[0].length; i++) {
			if (paiList[0][i] != 0) {
				if (paiList[1][i] != 0) {
					return 0;
				}
				if (paiList[0][i] == 2) {
					result++;
				} else if (paiList[0][i] == 3) {
					result++;
					// qingHu = i;
				} else if (paiList[0][i] == 4) {
					result = result + 2;
				}
				// else{
				// qingHu = i;
				// }
			}
		}
		return result;
	}

	/**
	 * 判断躺牌是否胡牌
	 * 
	 * @param paiList
	 * @return
	 */
	public boolean layHu(int[][] paiList) {
		// 不需要移除掉碰，杠了的牌组，在判断是否胡的时候就应判断了
		boolean flag = false;
		// 可七小队
		int isSeven = checkSevenDouble(paiList.clone());
		if (isSeven == 7) {
			flag = true;
		}
		if (!flag) {
			flag = normalHuPai.checkZZHu(paiList);
		}
		return flag;
	}

	/**
	 * 检测躺牌是否能胡，能胡就将牌点数加入躺牌胡牌点数组
	 * 
	 * @param layPai
	 * @param avatar
	 * @param i
	 * @return
	 */
	public boolean layount(int[][] layPai, Avatar avatar, int i) {
		boolean lay = false;
		layPai[0][i]++;
		// 检测躺牌组是否能胡
		if (layHu(layPai)) {
			// 能胡，将胡牌点数放入胡牌数组
			avatar.avatarVO.setLayHu(i);
			lay = true;
		}
		layPai[0][i]--;
		return lay;
	}

	/**
	 * 躺牌时检测胡的牌点数
	 * 
	 * @param layCards
	 * @param hu
	 * @return
	 */
	public boolean huCards(Avatar avatar, int hu) {
		List<Integer> layHu = avatar.avatarVO.getLayHu();
		if (layHu != null) {
			for (int i = 0; i < layHu.size(); i++) {
				if (layHu.get(i) == hu) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 躺牌后杠的请求
	 * 
	 * @param avatar
	 * @param cardPoint
	 *            杠牌
	 * @return
	 */
	public boolean layCardsBar(Avatar avatar, int cardPoint) {
		boolean str = false;
		/**
		 * cardPoint 杠牌
		 * 
		 */

		// boolean strs = false;
		/*
		 * int[] layHu = avatar.avatarVO.getLayHu(); for(int i = 0;i <
		 * layHu.length;i++){ strs = true; //如果cardPoint 是要胡的牌 不会到这里 return
		 * false; } }
		 */
		/*
		 * //麻胡不能杠所有牌 int[] lays = avatar.avatarVO.getLay(); for (int i = 0; i <
		 * lays.length; i++) { if (cardPoint == lays[i]) { return false; } }
		 */
		// 检测是否躺牌
		if (!lay(avatar)) {
			return true;
		}
		// 获取躺牌组
		int[] lay = GlobalUtil.CloneIntList(avatar.avatarVO.getLay());
		// 获取牌组
		int[][] pai = { avatar.getSinglePaiArray(), GlobalUtil.CloneIntList(avatar.getPaiArray()[1]) };

		// 判断把杠牌去掉以后没有叫的话，返回false 2017-06-19
		pai[0][cardPoint] = 0; // 将手上的杠牌出去
		if (!normalHuPai.checkHSHu(pai, true)) // 判断手牌是否胡牌 如果不能胡牌，返回false
		{
			return false;
		}

		// 检测杠牌是否为躺牌 如果是的话，返回false
		for (int i = 0; i < lay.length; i++) {
			if (lay[i] == cardPoint)
				return false;
		}

		// 判断如果躺牌了，并且巴杠的话，返回fasle，因为这里不能巴杠
		if (lay(avatar) && checkBaGang(cardPoint, avatar)) {
			return false;
		}

		// 检测手牌如果可以碰牌的话，返回false 新加的
		/*
		 * for(int i = 0; i < pai.length;i++) { //获得手牌 //int shouPai = pai[0][i]
		 * - lay[i]; if(pai[1][i] == 1) { return false; }
		 * 
		 * 
		 * }
		 */

		// 将躺牌从手牌中剔除
		/*
		 * for(int i = 0;i < lay.length;i++){ pai[0][lay[i]]--; }
		 */

		// 获得躺牌的胡牌
		List<Integer> layHu = avatar.avatarVO.getLayHu();
		if (cardPoint != 100 && cardPoint != -1 && layHu != null) {
			if (pai[0][cardPoint] < 3) {
				return false;
			}
			pai[0][cardPoint] = 0;// 将杠牌除去
			// 检测手牌是否能胡
			for (int i = 0; i < layHu.size(); i++) {
				// if(layHu.get(i) > 0){
				// 在躺牌的基础上加一张牌
				pai[0][layHu.get(i)]++;
				if (normalHuPai.checkHSHu(pai, true)) {
					str = true;
					pai[0][layHu.get(i)]--;
				} else {
					return false;
				}
				// }
			}
		}
		// else {
		// //自己摸牌的情况下
		// pai[0][avatar.gangIndex.get(0)] = 0;
		//
		// for (int i = 0; i < layHu.length; i++) {
		// if(layHu[i] > 0){
		// pai[0][i]++;
		// if(normalHuPai.checkZZHu(pai)){
		// str = true;
		// pai[0][i]--;
		// }
		// else{
		// return false;
		// }
		// }
		// }
		//
		// }
		return str;
	}

	/**
	 * 检测是否躺牌
	 * 
	 * @param avatar
	 * @return
	 */
	public static boolean lay(Avatar avatar) {
		boolean str = false;
		// 获取躺牌 *********
		if (avatar.avatarVO.getLay() != null) {
			int[] lay = avatar.avatarVO.getLay();
			for (int i = 0; i < lay.length; i++) {
				// 检测有躺牌
				if (lay != null && lay.length > 0) {
					str = true;
					return str;
				}
			}
		}
		return str;
	}

	/**
	 * 碰了之后再摸一个一样的起来不能杠
	 */
	public boolean checkBaGang(int tempPoint, Avatar avatar) {

		// 找到摸牌人的索引
		// Avatar avatar = playerList.get(pickAvatarIndex);
		// 当前摸牌的点数
		// 判断当前摸牌的点数等于摸牌人碰的牌，并且当前牌组是碰牌的话
		if (avatar.avatarVO.getPaiArray()[0][tempPoint] == 4 && avatar.getPaiArray()[1][tempPoint] == 1) {
			// 不能杠,设置请求杠为false
			return false;
		}

		return true;
	}

	public boolean layGang(int[][] paiArra) {
		// 这里是加一张牌一张牌进去看能不能胡
		for (int j = 0; j < paiArra[0].length; j++) {
			paiArra[0][j]++;

			// 判断如果可以胡牌的话
			if (paiCount(paiArra)) {
				return true;
			}
			paiArra[0][j]--;

		}

		return false;
	}

	/**
	 * 一炮多响是检测当前胡牌玩家是否能胡
	 * 
	 * @param index
	 * @return
	 */
	public boolean isHu(int index) {
		// 出牌玩家索引
		int curAvatarIndexNext = curAvatarIndex;
		;
		// for(int j = 0;j < huAvatar.size();j++){
		// if(huAvatar.get(j).getUuId() == bankerAvatar.getUuId()){
		// if(index == bankerIndexNext){
		// return true;
		// }
		// else{
		// return false;
		// }
		// }
		// }
		for (int i = 0; i < playerList.size(); i++) {
			// 出牌玩家下家或下下家索引
			curAvatarIndexNext += 1;
			if (curAvatarIndexNext >= playerList.size()) {
				curAvatarIndexNext = 0;
				;
			}
			for (int j = 0; j < huAvatar.size(); j++) {
				// 胡牌数组里面有一个玩家索引符合条件
				if (playerList.indexOf(huAvatar.get(j)) == curAvatarIndexNext) {
					// 符合条件索引是当前胡牌玩家索引
					if (index == curAvatarIndexNext) {
						return true;
					}
					// 符合条件索引不是当前胡牌玩家索引
					else {
						return false;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 2016-07-26 当前玩家出的牌不能胡
	 */
	public boolean chuPaiNoHu(Avatar avatar, int cardPoint) {

		// 判断如果当前玩家出的牌
		if (avatar.avatarVO.getChupais().contains(cardPoint)) {
			return false;
		}

		return true;

	}

	/**
	 * 获取下一位摸牌人的索引 丁二红专用
	 * 
	 * @return
	 */
	public int getNextAvatarIndex_DEH(int count) {
		int nextIndex = curAvatarIndex + 1;
		int avatarCount = playerList.size();
		if (nextIndex >= avatarCount) {
			nextIndex = 0;
		}
		// 丁二红下家跳过已经丢了和簸簸数为空的玩家
		if (playerList.get(nextIndex).avatarVO.getCurrentType() == 4
				|| playerList.get(nextIndex).avatarVO.getDustpan() == 0) {

			// isFinal.put(isFinal.size()+1, 4);
			curAvatarIndex = nextIndex;
			if (count >= 1) {
				nextIndex = getNextAvatarIndex_DEH(count - 1);
			} else {
				return -1;
			}
		}
		return nextIndex;
	}
	public boolean isShuffle(){
		if(roomVO.getShuffle()==roomVO.getInning()){
			return true;
		}else{
			return false;
		}
		
	}

	/**
	 * 架锅发牌
	 */
	private void dealingTheCards_DEH() {
//		int a=0;//正常发牌
//		
//		if(a==1){//测试发牌
//			 playerList.get(0).putCardInList(3);
//			 playerList.get(0).putCardInList(23);
//
//			 playerList.get(1).putCardInList(11);
//			 playerList.get(1).putCardInList(19);
//			
//		}else{
		bankerAvatar = null;
		for (int i = 0; i < 2; i++) {
			for (int k = 0; k < playerList.size(); k++) {
				if (bankerAvatar == null) {
					if (playerList.get(k).avatarVO.isMain()) {
						bankerAvatar = playerList.get(k);
					}
				}
				// System.out.println(listCard.get(nextCardindex));
				 List<Integer> list=roomVO.listCard;
					playerList.get(k).putCardInList(roomVO.listCard.get(roomVO.nextCardindex));
					playerList.get(k).oneSettlementInfo = "";
					playerList.get(k).overOff = false;
					roomVO.nextCardindex++;
			}
		}
	//	}
	}

	/**
	 * p判断谁离庄家近
	 * 
	 * @param tankIndex
	 *            庄家的id
	 * @return
	 */
	public int getNearestAvatarIndex(int tankIndex) {
		int nextIndex = tankIndex + 1;
		int avatarCount = playerList.size();
		if (nextIndex >= avatarCount) {
			nextIndex = nextIndex - avatarCount;
		}
		return nextIndex;
	}

	// ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓以下为丁二红所有方法↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
	/**
	 * 初始化牌（丁二红）
	 */
	public void initCard_DEH(RoomVO value) {
		// roomVO = value;
		paiCount = 33;
		if(roomVO.getInning()==1){
			
			// 定义整张桌子所有牌的集合
			for (int i = 1; i < paiCount; i++) {
				roomVO.listCard.add(i);
			}
		}
		for (int i = 0; i < playerList.size(); i++) {
			// 设置玩家拿的牌数组,一次性发4张牌
			playerList.get(i).avatarVO.setPaiArray(new int[1][paiCount]);
		}
		if(roomVO.getInning()==1){
			// 洗牌
			jiaGuoshuffleTheCards();
		}
		// 发牌
		dealingTheCards_DEH();

	}

	/**
	 * 2017-06 这个是下注 Avatar里面封装了很多信息，包括roomVO房间信息,AvatarVO用户信息
	 */
	public void xiaZhu(Avatar avatar, int Score) {

		// 总下注分 初始化， 下注分是之前下注的
		int totalXiaZhuFen = 0;
		totalXiaZhuFen = avatar.avatarVO.getXiazhuScore() + Score;
		if (avatar.avatarVO.isMain()) {
			try {
				avatar.getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000034));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		// 判断房间下注上限 小于等于 下注总分数。则set进去分数
		if (totalXiaZhuFen <= avatar.avatarVO.getAccount().getRoomcard() - 3) {
			// 则把分数set进去
			avatar.avatarVO.setXiazhuScore(totalXiaZhuFen);
			// 发送下注信息
			for (int i = 0; i < playerList.size(); i++) {
				playerList.get(i).getSession().sendMsg(new XiaZhuResponse(1, avatar.avatarVO.getXiazhuScore(),
						avatar.avatarVO.getAccount().getUuid()));
			}
		} else if (avatar.avatarVO.getAccount().getRoomcard() - 3 > 0) {
			// 则把分数set进去
			avatar.avatarVO.setXiazhuScore(avatar.avatarVO.getAccount().getRoomcard() - 3);
			// 发送下注信息
			for (int i = 0; i < playerList.size(); i++) {
				playerList.get(i).getSession().sendMsg(new XiaZhuResponse(1, avatar.avatarVO.getXiazhuScore(),
						avatar.avatarVO.getAccount().getUuid()));
			}
		}
		// 分数不够下注
		else {
			try {
				avatar.getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000033));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// xiaZhuFen.put(avatar.avatarVO.getAccount().getUuid().toString(),
		// String.valueOf(avatar.avatarVO.getXiazhuScore()));
		// String
		// a=xiaZhuFen.get(avatar.avatarVO.getAccount().getUuid().toString());
		// System.out.println("uuid__"+avatar.avatarVO.getAccount().getUuid().toString()+"下注分："+a);

	}
	
	/**
	 * 断线重连返回最后操作信息__丁二红
	 * 
	 * @param avatar
	 */
	public void LoginReturnInfo_DEH(Avatar avatar) {
		// 断线重连之后，该进行的下一步操作，json存储下一步操作指引
		JSONObject json;
		// 游戏局数
		int roundNum = RoomManager.getInstance()
				.getRoom(avatar.getRoomVO().getRoomId()).getCount();
		json = new JSONObject();
		json.put("gameRound", roundNum);
		json.put("currentOperateAvatarIndex", currentOperateAvatarIndex);// 当前操作玩家的索引
		json.put("preOperateAvatarIndex", curAvatarIndex);// 上一把操作玩家的索引
		// 上一把玩家出的分数
		json.put("currentRoundBottomPour", currentPool);
		// 打牌的当前状态
		json.put("opreateType", currentType);
//		if(currentType==9){
//			json.put("oneSettlementInfo", avatar.oneSettlementInfo);
//		}
		// 记录当前玩家能够进行的操作
		json.put("operations", currentOperations);
		// 判断未有操作，没有拆分牌组，并且没有抢庄
		if (currentOperations.equals("-1") && !avatar.isDetached() && avatar.avatarVO.getCurrentType() != 4) {
			json.put("detached", "1");
		} else {
			json.put("detached", "0");
		}
		avatar.getSession().sendMsg(new ReturnOnLineResponse(1, json.toString()));
	}

	/**
	 * 1:拆牌之后传入 2:当所有人都传入完成之后，开始比较大小
	 * 
	 * @param avatar
	 * @param str
	 */
	public void disassemble(Avatar avatar, String str) {
		avatar.avatarVO.setCurrentType(8);
		String[] strs = str.split(",");
		// 处理拆分之后传入的牌点。放入AvatarVO的paizu属性中，从大到小排列 拆分之后看是否牌组，并且有所需的牌型，以及点数，记录牌的大小
		DingErHongUtil.disass(str, avatar);

		// 所有人拆完牌传入后台之后，后台进行比较，算分等操作 isDetached:是否已经拆分，equls值为1
		// if (strs[4].equals("1") && !avatar.isDetached()) {
		if (!avatar.isDetached()) {
			// 设置拆分牌组为真
			avatar.setDetached(true);
			int count = playerList.size();
			for (int i = 0; i < playerList.size(); i++) {
				// 判断如果拆分了的话
				if (playerList.get(i).isDetached()) {
					// 玩家总数减直到拆分结束完成
					count--;
				}
			}
			if (count == 0) {
				// 所有人拆完牌传入后台之后，后台进行比较，算分等操作
				processorScores();
			}
		}
	}

	/**
	 * 根据庄家逆时针排序
	 */
	public void counterClockWiseOrdering() {
		playerAvatar = new ArrayList<>();

		int zhuangjia = 0;
		for (int i = 0; i < playerList.size(); i++) {
			if (playerList.get(i).avatarVO.isMain()) {// 庄家
				zhuangjia = i;
			}
		}
		for (int i = zhuangjia+1; i < playerList.size(); i++) {
			playerAvatar.add(playerList.get(i));
		}
		for (int i = 0; i <3; i++) {
			if(i==zhuangjia){
				break;
			}
			playerAvatar.add(playerList.get(i));
		}
	}

	/**
	 * 牌九比牌
	 * 
	 * @param type
	 *            1.闲家与闲家比牌，确定与庄家比牌顺序 2.闲家依牌大小顺序依次与庄家比牌
	 */
	public void processorScores() {
		Avatar avatar = null;
		counterClockWiseOrdering();
		/**
		 * 2:让尾牌最大的玩家和其他玩家一个一个比较头牌，如果头牌也大，则赢此玩家分数（以下，下注统一指个人下注总和）
		 * (如果自己下的注大于此玩家下的注，则全部赢，如果小于，则按照自己下注的分数赢)
		 */
		if (playerAvatar.size() > 0) {
			avatar = bankerAvatar;
			for (int k = 0; k < 2; k++) {
				for (int i = 0; i < playerAvatar.size(); i++) {
					Avatar compAvatar = playerAvatar.get(i);
					// 庄家的金币
					int avatarScore = avatar.avatarVO.getAccount().getRoomcard() + avatar.avatarVO.getWinScores();
					// 闲家下注金币
					int compAvatarScore = compAvatar.avatarVO.getXiazhuScore();

					// 牌九 天王和地王单独写入
					// 尾牌
					// 0.和 1.第二家赢 2.第一家赢
//					int flagSmall = 0;
					//int weiOneSmall = 0;
//					try {
//						weiOneSmall = avatar.avatarVO.getPaizu().get(5);//庄家尾牌点数
//					} catch (Exception e) {
//						System.out.println("Index: 5, Size: 0");
//					}
//					int weiTweSmall = playerAvatar.get(i).avatarVO.getPaizu().get(5);//闲家尾牌点数

					// 第一个玩家
					int str_one = avatar.avatarVO.getPaizu().get(0);
					int str_two = avatar.avatarVO.getPaizu().get(1);
					//int str_three = avatar.avatarVO.getPaizu().get(2);
					//int str_four = avatar.avatarVO.getPaizu().get(3);
					// 第二个玩家
					int play2_str_one = playerAvatar.get(i).avatarVO.getPaizu().get(0);
					int play2_str_two = playerAvatar.get(i).avatarVO.getPaizu().get(1);
					//int play2_str_three = playerAvatar.get(i).avatarVO.getPaizu().get(2);
					//int play2_str_four = playerAvatar.get(i).avatarVO.getPaizu().get(3);
					// 头牌
					// 0.和 1.第二家赢 2.第一家赢
					int flagBig = 0;
					int weiOneBig = avatar.avatarVO.getPaizu().get(2);
					int weiTweBig = playerAvatar.get(i).avatarVO.getPaizu().get(2);

					// 第一家大
					if (weiOneBig == 9 && weiTweBig != 1) {
						flagBig = 1;
					}
					// 第一家大
					else if (weiOneBig == 10 && weiTweBig != 1 && weiTweBig != 2 && weiTweBig != 9) {
						flagBig = 1;
					}
					// 第二家大
					else if (weiTweBig == 9 && weiOneBig != 1) {
						flagBig = 2;
					}
					// 第二家大
					else if (weiTweBig == 10 && weiOneBig != 1 && weiOneBig != 2 && weiOneBig != 9) {
						flagBig = 2;
					}
					// 庄家先把输家吃掉
					if (k == 0) {
						// 输赢百分比
						double percentage = 1;
						// 赢家最小牌型
						//int paiXing = avatar.avatarVO.getPaizu().get(5);
						
						// 1.头牌庄家赢
						if (((flagBig == 1 || (avatar.avatarVO.getPaizu().get(2) 
								< compAvatar.avatarVO.getPaizu().get(2)&& flagBig != 2)))
								||
								// 2.头牌大和尾牌平，庄家赢
								(avatar.avatarVO.getPaizu().get(2) == compAvatar.avatarVO.getPaizu().get(2))
								||
								// 3.头牌平和尾牌大，庄家赢
								((flagBig == 1 || (avatar.avatarVO.getPaizu().get(2) < compAvatar.avatarVO
												.getPaizu().get(2) && flagBig != 2)))
								||
								// 4.头牌和尾牌都平，庄家赢
								(avatar.avatarVO.getPaizu().get(2) == compAvatar.avatarVO.getPaizu()
												.get(2))) {

							// 输赢金币数
							int gold = compAvatarScore;
//							// 闲家下注大于庄家金币
//							if (avatarScore < compAvatarScore) {
//								gold = avatarScore;
//							}
//							// 闲家下注小于庄家金币，则输赢金币数等于闲家的金币
//							else {
//								gold = compAvatarScore;
//							}
							// 等于输赢金币数*输赢系数 默认为1
							gold = (int) (gold * percentage);
							compAvatar.avatarVO.setXiazhuScore(gold);
							// 记录分数
							avatar.avatarVO.updateWinScores(gold);
							compAvatar.avatarVO.updateWinScores(0 - gold);
							// 记录输赢
							compAvatar.avatarVO.setTypeResult(0);
							// 庄家输赢
							avatar.avatarVO.setTypeResult(2);
						}
					}
					
					// 庄家输 闲家赢
					else if (k == 1 && avatarScore > 0
							&& ((flagBig == 2
									|| (avatar.avatarVO.getPaizu().get(2) > compAvatar.avatarVO.getPaizu().get(2)
											&& flagBig != 1)))) {

						// 输赢百分比
						double percentage = 1;
						// 输赢金币数
						int gold = 0;
						// 闲家下注大于庄家金币
						if (avatarScore < compAvatarScore * percentage) {
							gold = avatarScore;
						}
						// 闲家下注小于庄家金币
						else {
							gold = (int) (compAvatarScore * percentage);
						}
						// gold = (int) (gold*percentage);
						compAvatar.avatarVO.setXiazhuScore(gold);
						// 记录分数
						avatar.avatarVO.updateWinScores(0 - gold);
						// 庄家输赢
						avatar.avatarVO.setTypeResult(0);
						compAvatar.avatarVO.updateWinScores(gold);
						// 记录输赢
						compAvatar.avatarVO.setTypeResult(2);
					}
				}
			}
			// 如果还有玩家就全都是平
			for (Avatar ava : playerAvatar) {
				// 结果默认-1
				if (ava.avatarVO.getTypeResult() == -1) {
					// 记录输赢
					ava.avatarVO.setTypeResult(1);
				}
			}
			// 平
			if (bankerAvatar.avatarVO.getWinScores() == 0) {
				// 记录输赢
				bankerAvatar.avatarVO.setTypeResult(1);
			}
			// 赢
			else if (bankerAvatar.avatarVO.getWinScores() > 0) {
				// 记录输赢
				bankerAvatar.avatarVO.setTypeResult(2);
			}
			// 输
			else if (bankerAvatar.avatarVO.getWinScores() < 0) {
				// 记录输赢
				bankerAvatar.avatarVO.setTypeResult(0);
			}
			// 比牌完成调用单局结算
			endSendIfos(0);
		}

	}

	/**
	 * 一局结束之后发送相关信息给前端（最后一局结束，同时整个房间结束，返回总结算信息，结算房间） 1：返回本局的各个玩家的输赢，输赢分数，牌组等
	 * 2：若有玩家的簸簸数不够房间基础分，则提醒前端是否需要加簸簸数(在准备的时候判断并提醒) 3: 当这局是最后一局时。还需要返回总的结算信息
	 * private int currentRoundBottomPour; private int currentRoundBottomPours;
	 * private int [] pickCards = new int [2]; private int currentType = -1;
	 * private List<Integer> paizu = new ArrayList<>(); private int headIndex;
	 * private int tailIndex; private int endScores = -1; private boolean
	 * hasOver =false; paiArray  结算1
	 */
	public void endSendIfos(int type) {
		// 1:牌组
		JSONObject json = null;
		JSONArray array = new JSONArray();
		AvatarVO avatarVo;
		int count = 100;
		Avatar avatar;
		boolean enoughDustpan = true;
		StringBuffer content = new StringBuffer();

		if (type == 0) {
			for (int i = 0; i < playerList.size(); i++) {
				json = new JSONObject();
				avatarVo = playerList.get(i).avatarVO;
				// 整个房间结束时分数
				
				avatarVo.setTotalWinScores(avatarVo.getTotalWinScores() + avatarVo.getWinScores());
				avatarVo.getHuReturnObjectVO().setTotalScore(avatarVo.getHuReturnObjectVO().getTotalScore()+ avatarVo.getWinScores());
				//.setTotalScore(avatarVo.getHuReturnObjectVO().getTotalScore()+ avatarVo.getWinScores());
				// 设置输赢分
				content.append(avatarVo.getAccount().getNickname() + ":" + avatarVo.getWinScores() + ",");
				// 单局输/赢的分数
				json.put("winScores", avatarVo.getWinScores());
				// 下注分
				json.put("xiaZhuFen", avatarVo.getXiazhuScore());
				// 牌型
				json.put("paiXing", avatarVo.getHuReturnObjectVO().getPaiXing());
				// 牌数组
				json.put("paiZu", avatarVo.getHuReturnObjectVO().getPaiArray());

				// 洗牌前牌组
				json.put("beForePaiZu", avatarVo.getHuReturnObjectVO().getBeforePaiArray());
				
				// json.put("paiZu", avatarVo.getPaiArray());
				// 是否是庄家
				json.put("isMain", avatarVo.isMain());
				// 当前游戏规则 选道
				json.put("gameType", playerList.get(i).avatarVO.getWay());
				// 当前玩家名字
				json.put("nickName", avatarVo.getAccount().getNickname());
				// 下注结果（0：输 1：平 2：赢）
				json.put("typeResult", avatarVo.getTypeResult());
				// 玩家的uuid
				json.put("uuid", avatarVo.getAccount().getUuid());

				json.put("headIcon", avatarVo.getAccount().getHeadicon());
				json.put("socre", avatarVo.getAccount().getRoomcard());
				json.put("accountIndex", i);
				
				 if(roomVO.getInning()<=roomVO.getShuffle()){
						 if(roomVO.getInning()==1){
							 avatarVo.getHuReturnObjectVO().getBeforePaiArray()[0]=avatarVo.getHuReturnObjectVO().getPaiArray()[0];
			    			 avatarVo.getHuReturnObjectVO().getBeforePaiArray()[1]=avatarVo.getHuReturnObjectVO().getPaiArray()[1];
						 }else if(roomVO.getInning()==2){
							 avatarVo.getHuReturnObjectVO().getBeforePaiArray()[2]=avatarVo.getHuReturnObjectVO().getPaiArray()[0];
			    			 avatarVo.getHuReturnObjectVO().getBeforePaiArray()[3]=avatarVo.getHuReturnObjectVO().getPaiArray()[1];
						 }else if(roomVO.getInning()==3){
							 avatarVo.getHuReturnObjectVO().getBeforePaiArray()[4]=avatarVo.getHuReturnObjectVO().getPaiArray()[0];
			    			 avatarVo.getHuReturnObjectVO().getBeforePaiArray()[5]=avatarVo.getHuReturnObjectVO().getPaiArray()[1];
						 }
						 
					
	                }
				// 数组添加进去
				array.add(json);
			}
			// 返回单局结算信息
			json = new JSONObject();
			json.put("danJuList", array);
			json.put("roomId", roomVO.getRoomId());
			// 是否是鬼子玩法
			// json.put("playWay ", roomVO.getPalyWay());
			json.put("roomType ", roomVO.getRoomType());
			json.put("inning", roomVO.getInning());
            json.put("shuffle", roomVO.getShuffle());
			// 保存单局战绩操作和分数
			savestandins(json, content.toString());

			for (int i = 0; i < playerList.size(); i++) {
				avatar = playerList.get(i);
				// 存储某一句游戏断线时 结算信息
				avatar.oneSettlementInfo = json.toString();
				// avatar.getSession().sendMsg(new RoomPartOverResponse(1,
				// json.toString()));
				avatar.getSession().sendMsg(new DanJuResponse(1, json.toString()));
				// count =
				// RoomManager.getInstance().getRoom(avatar.getRoomVO().getRoomId(),avatar.avatarVO.getRoomType()).getCount();
			}
			
			array = new JSONArray();
			// 计算输赢分
			for (int i = 0; i < playerList.size(); i++) {
				json = new JSONObject();
				Avatar ava = playerList.get(i);
				ava.avatarVO.getAccount()
						.setRoomcard(ava.avatarVO.getAccount().getRoomcard() + ava.avatarVO.getWinScores());
				json.put("accountIndex", i);
				json.put("roomCard",  playerList.get(i).avatarVO.getAccount().getRoomcard());
				array.add(json);
				//ava.getSession()
				//		.sendMsg(new RoomCardChangerResponse(1, playerList.get(i).avatarVO.getAccount().getRoomcard()));
				// 更新数据库
				AccountService.getInstance().updateAccount(ava.avatarVO.getAccount());
			}
			json= new JSONObject();
			json.put("Avatar", array);
			for (int i = 0; i < playerList.size(); i++) {
				Avatar ava = playerList.get(i);
				ava.getSession().sendMsg(new RoomCardChangerResponse(1, json));
				ava.getSession().sendMsg(new RoomCardChangerResponse(1, ava.avatarVO.getAccount().getRoomcard()));
			}
		}
		// 单局结束之后清除一些参数，AvatarVO 和本类的
		for (Avatar ava : playerList) {
			// 当前操作
			ava.avatarVO.setCurrentType(9);

			// 是否一局结算
			ava.overOff = true;
			// 设置准备为false
			ava.avatarVO.setIsReady(false);
			// 设置用户信息
			ava.avatarVO.setAccount(ava.avatarVO.getAccount());
			//ava.avatarVO.setCurrentType(-1);
			// 下注分
			ava.avatarVO.setXiazhuScore(0);
			// 当前游戏规则设置
			ava.getRoomVO().setWay(0);
			ava.avatarVO.setWay(0);
			ava.avatarVO.setCurrentRoundBottomPour(0);
			ava.avatarVO.setCurrentRoundBottomPours(0);
			ava.avatarVO.setPickCards(new int[2]);
			ava.avatarVO.setPaizu(new ArrayList<Integer>());
			ava.avatarVO.getHuReturnObjectVO().setPaiArray(new int[1]);
			ava.avatarVO.getHuReturnObjectVO().setPaiXing(new String[1]);
			ava.avatarVO.setHeadIndex(0);
			ava.avatarVO.setTailIndex(0);
			ava.avatarVO.setHasOver(false);
			ava.avatarVO.setWinScores(0);
			ava.avatarVO.setHeadName("");
			ava.avatarVO.setTailName("");
			// ava.avatarVO.setMain(false);
			ava.setDetached(false);
			ava.avatarVO.setIsReady(false);
			ava.avatarVO.setPlayer(false);
			ava.avatarVO.setTypeResult(-1);
			ava.avatarVO.setKouZi(false);
		}
		
		
		// 检查金币是否足够
		for (int i = 0; i < playerList.size(); i++) {
			if (playerList.get(i).avatarVO.getAccount().getRoomcard() < 80) {
				RoomLogic roomLogic = RoomManager.getInstance().getRoom(roomVO.getRoomId());
				for (int j = 0; j < playerList.size(); j++) {
					try {
						playerList.get(j).getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000032));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(6000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				settlementData("2");
				//roomLogic.destoryRoomLogic();
				return;
			}
		}
		if(isShuffle()){
			roomVO.setInning(1);
			roomVO.setCardNum(4);
		}else{
		roomVO.setInning(roomVO.getInning()+1);
		}
		
		
	}

	/**
	 * 保存单局战绩操作和分数
	 */
	public void savestandins(JSONObject json, String content) {

		try {
			// 单局分数总结
			StandingsDetail standingsDetail = new StandingsDetail();
			standingsDetail.setRoomType(roomVO.getRoomType());
			standingsDetail.setContent(content);
			standingsDetail.setCreatetime(DateUtil.toChangeDate(new Date(), DateUtil.maskC));
			int temp = StandingsDetailService.getInstance().saveSelective(standingsDetail);
			if (temp > 0) {
				RoomLogic roomLogic = RoomManager.getInstance().getRoom(roomVO.getRoomId());
				roomLogic.getStandingsDetailsIds().add(standingsDetail.getId());
			}
			// 创建一个战绩细节对象
			PlayRecord playRecord = new PlayRecord();
			playRecord.setStandingsdetailId(standingsDetail.getId());
			// 将操作放入战绩
			playRecord.setPlayrecord(json.toString());
			// 保存数据库
			PlayRecordService.getInstance().saveSelective(playRecord);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 跟(不需要传分)，大(需要传分)，敲(不需要传分)，休(不需要传分)，丢(不需要传分)的请求
	 * (跟(score等于本轮下注数量(playlogic里面属性))) 跟:0 大:1 敲:2 休:3 丢:4
	 * 
	 * @param avatar
	 * @param type
	 *            改变整个房间的状态currentType
	 * @param score
	 */
	public void propaganda(Avatar avatar, int type, int score) {
		if (scorePool != 0) {
			avatar.avatarVO.setCurrentType(type);
			curAvatarIndex = playerList.indexOf(avatar);
			// System.out.println("当前操作玩家:
			// "+avatar.avatarVO.getAccount().getNickname()+"--索引:"+curAvatarIndex);
			switch (type) {
			case 0:
				isFinal.put(isFinal.size() + 1, type);
				follow(avatar, type);
				break;
			case 1:
				// 大的时候重置isFinal
				isFinal.clear();
				isFinal.put(1, type);
				huge(avatar, score, type);
				break;
			case 2:
				knock(avatar, type);
				break;
			case 3:
				isFinal.put(isFinal.size() + 1, type);
				rest(avatar, type);
				break;
			case 4:
				// 丢不改变整个房间的状态currentType
				// isFinal.put(isFinal.size()+1, type);
				drop(avatar, type);
				break;
			default:
				break;
			}
		} else {
			// System.out.println("本局已经结束，下局还未开始，不能进行跟大敲丢休等操作");
		}
	}

	/**
	 * 跟0 大1 敲2 休3 跟 1：在某一轮中，只要有人叫了大，之后的人才能跟 2：或者敲的人的簸簸数很小
	 * ，没有下面几家的多，后面的人就可以点跟,大,休,丢等操作
	 * 
	 */
	public void follow(Avatar avatar, int type) {
		// System.out.println("操作--跟:"+"本轮下注分："+currentPool
		// +"
		// --总下注分："+avatar.avatarVO.getCurrentRoundBottomPours()+"--实时簸簸数："+(avatar.avatarVO.getDustpan()-currentPool));
		if ((currentType == 0 || currentType == 1 || currentType == 2)
				&& avatar.avatarVO.getCurrentRoundBottomPours() < currentPool) {
			int followScore = currentPool - avatar.avatarVO.getCurrentRoundBottomPours();// 跟的分数等于
																							// currentPool减去自己总下注分数
			scorePool = scorePool + followScore;
			int curentAvatarIndex = curAvatarIndex;
			currentOperateAvatarIndex = getNextAvatarIndex_DEH(playerList.size());
			avatar.avatarVO.setCurrentRoundBottomPour(followScore);
			// avatar.avatarVO.setCurrentRoundBottomPours(avatar.avatarVO.getCurrentRoundBottomPours()+currentPool);
			avatar.avatarVO.setCurrentRoundBottomPours(currentPool);
			avatar.avatarVO.updateDustpan(0 - followScore);
			JSONObject json = new JSONObject();
			// 1：个人簸箕分数 2： 已下注分数 3：本次下注分数 4：本次操作类型(跟？大？丢？。。) 5：中间池中分数 6：本次操作玩家索引
			// 7：下次操作玩家索引
			json.put("dustpan", avatar.avatarVO.getDustpan());
			json.put("currentRoundBottomPours", avatar.avatarVO.getCurrentRoundBottomPours());
			json.put("currentRoundBottomPour", avatar.avatarVO.getCurrentRoundBottomPour());
			json.put("opreateType", type);
			json.put("scorePool", scorePool);
			json.put("avatarIndex", curentAvatarIndex);
			if (currentOperateAvatarIndex == playerList.indexOf(avatar)) {
				currentOperateAvatarIndex = -1;
			}
			json.put("nextAvatarIndex", currentOperateAvatarIndex);

			currentType = type;
			pickCardOrEnd(json);
		} else {
			// System.out.println("分数不够跟");
		}
	}

	/**
	 * 判断是否该发牌，或者结束游戏(除去丢了的玩家) 1：所有玩家状态 1个为大，其他都为跟时 2：所有玩家状态都为休(本局结束，大家不输不赢)
	 * 3：所有玩家都敲()具体应该是所有玩家都出了簸簸里面的所有分 4：所有玩家都丢 5：正常结束需要把牌组显示给所有人看 跟0 大1 敲2 休3 丢4
	 */
	public void pickCardOrEnd(JSONObject str) {
		// System.out.println("nextAvatarIndex:"+str.get("nextAvatarIndex"));
		boolean flag = judgeIsFinal();
		// 下面判断是否结束本轮
		if (!flag) { // int type_follow = 0;
			int type_rest = 0;
			int type_drop = 0;
			int type_knock = 0;
			int totalAccountCount = 0;
			for (int i = 0; i < playerList.size(); i++) {
				totalAccountCount++;
				switch (playerList.get(i).avatarVO.getCurrentType()) {
				case 0:
					// type_follow++;
					break;
				case 1:
					// type_huge++;
					break;
				case 2:
					type_knock++;
					break;
				case 3:
					type_rest++;
					break;
				case 4:
					totalAccountCount--;
					type_drop++;
					break;
				default:
					break;
				}
			}
			if (type_knock == totalAccountCount) {
				// 所有没有丢的人都敲
				// System.out.println("所有没有丢的人都敲");
				flag = true;
			} else if (type_rest == totalAccountCount || (type_rest + type_drop) == playerList.size()) {
				// 所有没丢的人都休。则结束游戏。大家不输不赢
				flag = true;
				// System.out.println("所有没丢的人都休。则结束游戏。大家不输不赢");
				processorScores_back();
				return;
			} else if (totalAccountCount == 0) {
				// 所有玩家都丢了
				// System.out.println("所有玩家都丢了");
				flag = true;
			} else if ((type_drop + 1) == playerList.size()) {
				// 丢来只剩最后一家，直接结算
				for (int i = 0; i < playerList.size(); i++) {
					playerList.get(i).getSession().sendMsg(new OperateResponse(1, str.toString()));
				}
				processorScores_onlyOne();
				return;
			} else if (str.get("nextAvatarIndex").equals(-1)) {
				flag = true;
			} else {
				// System.out.println("不满足下一轮条件！大:"+type_huge+"
				// 跟:"+type_follow+" 敲:"+type_knock+" 休:"+type_rest+"
				// 丢:"+type_drop);
			}
		}
		// System.out.println(str.toString());
		// 叫牌结束
		if (flag) {
			// System.out.println("叫牌结束");
			callOver(str);
		}
		// 叫牌未结束，则发送消息给其他玩家
		else {
			// 特殊情况处理1， 最后一家丢，其他家都敲(且只有一家簸簸数不为为0了)，应该直接发牌分牌了
			if (currentType == 4) {
				int knock_two = 0;
				int score_pool = 0;
				boolean isTure = true;
				AvatarVO avatarVO;
				for (int i = 0; i < playerList.size(); i++) {
					avatarVO = playerList.get(i).avatarVO;
					if (avatarVO.getCurrentType() != 4) {
						if (avatarVO.getDustpan() != 0) {
							knock_two++;
						}
						if (score_pool == 0) {
							score_pool = avatarVO.getCurrentRoundBottomPours();
						} else if (score_pool != avatarVO.getCurrentRoundBottomPours()) {
							isTure = false;
							break;
						}
					}
				}
				if (knock_two == 1 && isTure) {
					// 只有一个人簸簸数不为0
					// return true;
					// System.out.println("叫牌结束");
					str.put("nextAvatarIndex", -1);
					callOver(str);
					return;
				}
			} else if (currentType == 2) {
				// 特殊情况2： 最后一家为敲的时候，如果总下注仍然不够前面玩家的大，但是前面没丢的玩家的总下注又相等
				// 当没丢的玩家总下注又相等，且人数为1 则直接发完牌拆牌， 任务大于1 则发完牌之后还需要叫牌
				int account_count = 0;
				boolean isTure = true;
				AvatarVO avatarVO;
				for (int i = 0; i < playerList.size(); i++) {
					avatarVO = playerList.get(i).avatarVO;
					if (avatarVO.getCurrentType() != 4 && avatarVO.getDustpan() != 0) {
						if (avatarVO.getCurrentRoundBottomPours() != currentPool) {
							isTure = false;
							break;
						} else {
							account_count++;
						}
					}
				}
				if (isTure) {
					// System.out.println("叫牌结束");
					if (account_count <= 1) {
						str.put("nextAvatarIndex", -1);
					}
					callOver(str);
					return;
				}
			}

			// System.out.println("继续叫牌");
			callContinue(str);
		}

	}

	/**
	 * 跟0 大1 敲2 休3 大
	 */
	public void huge(Avatar avatar, int score, int type) {
		// System.out.println("操作--大:"+"本轮下注分："+score
		// +"
		// --总下注分："+avatar.avatarVO.getCurrentRoundBottomPours()+"--实时簸簸数："+(avatar.avatarVO.getDustpan()
		// - score));
		if (score <= avatar.avatarVO.getDustpan() && score >= 1) {
			int curentAvatarIndex = curAvatarIndex;
			currentOperateAvatarIndex = getNextAvatarIndex_DEH(playerList.size());
			// 有人大了之后就更新本轮最低下注分(里面包含了房间底分)
			currentPool = score + avatar.avatarVO.getCurrentRoundBottomPours();
			// 改变中间池中的总分
			scorePool = scorePool + score;
			// 修改自己的本轮下注分
			avatar.avatarVO.setCurrentRoundBottomPour(score);
			// 修改自己的总下注分
			avatar.avatarVO.setCurrentRoundBottomPours(currentPool);
			// 更新自己的簸箕数量
			// avatar.avatarVO.setDustpan(avatar.avatarVO.getDustpan() -
			// currentPool);这里不修改属性dustpan 一局结束之后才修改
			// 修改自己的状态
			avatar.avatarVO.setCurrentType(type);
			avatar.avatarVO.updateDustpan(0 - score);

			JSONObject json = new JSONObject();
			// 1：个人簸箕分数 2： 已下注分数 3：本次下注分数 4：本次操作类型(跟？大？丢？。。) 5：中间池中分数 6：操作玩家索引
			// 7：下次操作玩家索引
			json.put("dustpan", avatar.avatarVO.getDustpan());
			json.put("currentRoundBottomPours", avatar.avatarVO.getCurrentRoundBottomPours());
			json.put("currentRoundBottomPour", avatar.avatarVO.getCurrentRoundBottomPour());
			json.put("opreateType", type);
			json.put("scorePool", scorePool);
			json.put("avatarIndex", curentAvatarIndex);
			if (currentOperateAvatarIndex == playerList.indexOf(avatar)) {
				currentOperateAvatarIndex = -1;
			}
			json.put("nextAvatarIndex", currentOperateAvatarIndex);

			currentType = type;
			pickCardOrEnd(json);
		} else {
			// System.out.println("分数不够大:"+score);
		}
	}

	/**
	 * 跟0 大1 敲2 休3 敲
	 */
	public void knock(Avatar avatar, int type) {
		int score = avatar.avatarVO.getDustpan();
		if (currentPool < (avatar.avatarVO.getCurrentRoundBottomPours() + score)) {
			currentPool = avatar.avatarVO.getCurrentRoundBottomPours() + score;
			isFinal.clear();
			isFinal.put(1, type);
		} else {
			isFinal.put(isFinal.size() + 1, type);
		}
		scorePool = scorePool + score;
		int curentAvatarIndex = curAvatarIndex;
		currentOperateAvatarIndex = getNextAvatarIndex_DEH(playerList.size());
		avatar.avatarVO.setCurrentRoundBottomPour(score);
		avatar.avatarVO.setCurrentRoundBottomPours(avatar.avatarVO.getCurrentRoundBottomPours() + score);
		// System.out.println("操作--敲:"+"本轮下注分："+score
		// +"
		// --总下注分："+avatar.avatarVO.getCurrentRoundBottomPours()+"--实时簸簸数："+0);
		// 修改自己的状态
		avatar.avatarVO.setCurrentType(type);
		avatar.avatarVO.setDustpan(0);
		JSONObject json = new JSONObject();
		// 1：个人簸簸分数 2： 已下注分数 3：本次下注分数 4：本次操作类型(跟？大？丢？。。) 5：中间池中分数 6：操作玩家索引
		// 7：下次操作玩家索引
		json.put("dustpan", 0);
		json.put("currentRoundBottomPours", avatar.avatarVO.getCurrentRoundBottomPours());
		json.put("currentRoundBottomPour", score);
		json.put("opreateType", type);
		json.put("scorePool", scorePool);
		json.put("avatarIndex", curentAvatarIndex);
		if (currentOperateAvatarIndex == playerList.indexOf(avatar)) {
			currentOperateAvatarIndex = -1;
		}
		json.put("nextAvatarIndex", currentOperateAvatarIndex);
		currentType = type;
		pickCardOrEnd(json);
	}

	/**
	 * 跟0 大1 敲2 休3
	 * 某一轮喊话开始之后，第一个叫休了，第二个人也可以叫，顺延(当一轮所有人都休了，则分数全部返回给对应的每个人，大家都不输不赢) 休 1:告诉其他玩家
	 * 此玩家休即可 2:当所有玩家都叫了休，则游戏结束，所有人不输不赢
	 */
	public void rest(Avatar avatar, int type) {
		// System.out.println("操作--休:"+"本轮下注分："+0
		// +"
		// --总下注分："+avatar.avatarVO.getCurrentRoundBottomPours()+"--实时簸簸数："+0);
		if (currentType == 3 || currentType == -1) {
			JSONObject json = new JSONObject();
			int curentAvatarIndex = curAvatarIndex;
			currentOperateAvatarIndex = getNextAvatarIndex_DEH(playerList.size());
			// 修改自己的状态
			json.put("dustpan", avatar.avatarVO.getDustpan());
			json.put("currentRoundBottomPours", avatar.avatarVO.getCurrentRoundBottomPours());
			json.put("currentRoundBottomPour", 0);
			json.put("opreateType", type);
			json.put("scorePool", scorePool);
			json.put("avatarIndex", curentAvatarIndex);
			if (currentOperateAvatarIndex == playerList.indexOf(avatar)) {
				currentOperateAvatarIndex = -1;
			}
			json.put("nextAvatarIndex", currentOperateAvatarIndex);
			// currentType = type;
			pickCardOrEnd(json);
		}
	}

	/**
	 * 跟0 大1 敲2 休3 丢( 1:当剩下的所有人都叫了休，则丢牌玩家也会退回分数) 2:当丢到只剩下最后一家人时。最后一家直接得分 3：丢了的玩家
	 * Avatar的detached属性ture(等同拆完牌)
	 */
	public void drop(Avatar avatar, int type) {
		// System.out.println("操作--丢:"+"本轮下注分："+0
		// +"
		// --总下注分："+avatar.avatarVO.getCurrentRoundBottomPours()+"--实时簸簸数："+0);
		JSONObject json = new JSONObject();
		int curentAvatarIndex = curAvatarIndex;
		currentOperateAvatarIndex = getNextAvatarIndex_DEH(playerList.size());
		// 修改自己的状态
		json.put("dustpan", avatar.avatarVO.getDustpan());
		json.put("currentRoundBottomPours", avatar.avatarVO.getCurrentRoundBottomPours());
		json.put("currentRoundBottomPour", 0);
		json.put("opreateType", type);
		json.put("scorePool", scorePool);
		json.put("avatarIndex", curentAvatarIndex);
		if (currentOperateAvatarIndex == playerList.indexOf(avatar)) {
			currentOperateAvatarIndex = -1;
		}
		json.put("nextAvatarIndex", currentOperateAvatarIndex);
		avatar.setDetached(true);
		pickCardOrEnd(json);
	}

	/**
	 * 判断叫牌是否结束
	 * 
	 * @return
	 */
	public boolean judgeIsFinal() {
		boolean flag = false;
		// Set<Entry<Integer, Integer>> set = isFinal.entrySet();
		int score = 0;
		int otherScore;
		boolean flag_two = true;
		int noDrop_count = 0;// 没有丢的玩家数量
		int operater_count = 0;// 本轮操作玩家数量
		AvatarVO avatarVO;
		for (int i = 0; i < playerList.size(); i++) {
			avatarVO = playerList.get(i).avatarVO;
			if (avatarVO.getCurrentType() != 4) {
				noDrop_count++;
				// if(avatarVO.getDustpan() !=0 ||
				// (avatarVO.getCurrentRoundBottomPours() == currentPool &&
				// avatarVO.getDustpan() ==0 )){
				if (avatarVO.getDustpan() != 0
						|| (avatarVO.getCurrentRoundBottomPours() == currentPool && avatarVO.getDustpan() == 0)
						|| (currentType == 2 && avatarVO.getDustpan() == 0)) {
					operater_count++;
					otherScore = avatarVO.getCurrentRoundBottomPours();
					if (score == 0) {
						score = avatarVO.getCurrentRoundBottomPours();
					}
					if (otherScore != score) {
						flag_two = false;
						break;
					}
				}
			}
		}
		if (isFinal.size() == operater_count && (isFinal.get(1) == 1 || isFinal.get(1) == 2) && noDrop_count != 1
				&& flag_two) {
			// System.out.println("isFinal数量等于玩家数量"+isFinal.size()+":"+playerList.size());
			flag = true;
		} else {
			// System.err.println("isFinal.size()="+isFinal.size()+"
			// operater_count :"+operater_count+"
			// noDrop_count="+noDrop_count+":flag_two="+flag_two);
			flag = false;
		}
		return flag;
	}

	/**
	 * 当休的玩家数量加上丢的玩家数量等于总玩家数量的时候，游戏直接开始下一局，不输不赢
	 */
	public void processorScores_back() {
		// 1:先得到唯一一家未丢的玩家
		Avatar avatar = null;
		for (int i = 0; i < playerList.size(); i++) {
			avatar = playerList.get(i);
			avatar.avatarVO.updateDustpan(avatar.avatarVO.getCurrentRoundBottomPours());
		}
		// 结束之后进行的操作
		endSendIfos(0);
	}

	/**
	 * 叫牌结束，然后发牌或拆牌,或直接发送结算信息(丢来只剩最后一家)
	 */
	public void callOver(JSONObject str) {
		int type_drop = 0;
		for (int i = 0; i < playerList.size(); i++) {
			playerList.get(i).getSession().sendMsg(new OperateResponse(1, str.toString()));
			if (playerList.get(i).avatarVO.getCurrentType() == 4) {
				type_drop++;
			}
		}
		if ((type_drop + 1) == playerList.size()) {
			processorScores_onlyOne();
			return;
		}
		// 表明发完牌之后就可以拆牌
		if (str.get("nextAvatarIndex").equals(-1) || pickCardRound == 2) {
			currentOperations = "-1";
			// 如果第一轮时所有人都敲了，则发两张牌
			int count = 2 - pickCardRound;
			for (int j = 0; j < count; j++) {
				pickCard_DEH_total();
			}
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (int i = 0; i < playerList.size(); i++) {
				if (playerList.get(i).avatarVO.getCurrentType() != 4) {
					playerList.get(i).getSession().sendMsg(new BeginDisassemble(1, 1));
				}
			}
		} else {
			// 继续发牌，然后叫牌
			pickCard_DEH();
		}
	}

	/**
	 * 特殊算分，当丢来只剩一家时，直接算分
	 */
	public void processorScores_onlyOne() {
		// 1:先得到唯一一家未丢的玩家
		Avatar avatar = null;
		for (int i = 0; i < playerList.size(); i++) {
			if (!playerList.get(i).avatarVO.isHasOver()) {
				if (playerList.get(i).avatarVO.getCurrentType() != 4) {
					avatar = playerList.get(i);
				}
			}
		}
		/**
		 * 2:让尾牌最大的玩家和其他玩家一个一个比较头牌，如果头牌也大，则赢此玩家分数（以下，下注统一指个人下注总和）
		 * (如果自己下的注大于此玩家下的注，则全部赢，如果小于，则按照自己下注的分数赢)
		 */
		Avatar compAvatar;
		int avatarScore = avatar.avatarVO.getCurrentRoundBottomPours();
		if (scorePool >= avatarScore) {// 如果分数池中的分数大于自己总下注的分数，则返还自己所有下注的分数，然后分数池中相应减少
			scorePool -= avatarScore;
			avatar.avatarVO.updateDustpan(avatarScore);
		}
		int compAvatarScore;
		for (int i = 0; i < playerList.size(); i++) {
			if (!playerList.get(i).avatarVO.isHasOver()) {
				compAvatar = playerList.get(i);
				compAvatarScore = compAvatar.avatarVO.getCurrentRoundBottomPours();
				if (compAvatar.avatarVO.getCurrentType() == 4) {
					avatar.avatarVO.updateWinScores(compAvatarScore);
					avatar.avatarVO.updateDustpan(compAvatarScore);

					compAvatar.avatarVO.updateWinScores(0 - compAvatarScore);
					// compAvatar.avatarVO.updateDustpan(0-compAvatarScore);
					scorePool -= compAvatarScore;
					compAvatar.avatarVO.setHasOver(true);

				}
			}
		}
		avatar.avatarVO.setHasOver(true);
		// 结束之后进行的操作
		endSendIfos(0);
	}

	/**
	 * 继续叫牌
	 */
	public void callContinue(JSONObject str) {
		// 判断下一家可以进行的操作，通过前端对比上一家出的分数大小，当分数大于自己的实时簸簸数时，只能丢，敲
		// 对比下家的簸簸数和当前最低下注分数 跟0 大1 敲2 休3 丢4
		Avatar ava = playerList.get(currentOperateAvatarIndex);
		StringBuffer operations = new StringBuffer();
		operations.append("4,2");
		if (currentPool < (ava.avatarVO.getDustpan() + ava.avatarVO.getCurrentRoundBottomPours())) {
			// 跟 大
			operations.append(",1");
			if (currentPool > ava.avatarVO.getCurrentRoundBottomPours()) {
				operations.append(",0");
			}
		} else {
			operations.append(",2");
		}
		if (currentType == -1) {// 休
			operations.append(",3");
		}
		// 判断下一家可以进行的操作，通过前端对比上一家出的分数大小，当分数大于自己的实时簸簸数时，只能丢，敲
		str.put("operations", operations);
		currentOperations = operations.toString();
		for (int i = 0; i < playerList.size(); i++) {
			playerList.get(i).getSession().sendMsg(new OperateResponse(1, str.toString()));
		}
	}

	/**
	 * 第一轮除去丢了的玩家所有人都敲时，直接连续发两轮牌
	 */
	public void pickCard_DEH_total() {
		currentPool = roomVO.getLeastScore();
		currentType = -1;
		Avatar avatar;
		int cardIndex;
		int nextOperateAvatar = -1;
		JSONObject json;
		JSONArray array = new JSONArray(); // operateAvaterIndex 下一步该操作的玩家索引)
		for (int i = 0; i < playerList.size(); i++) {
			avatar = playerList.get(i);
			if (avatar.avatarVO.getCurrentType() != 4) {
				json = new JSONObject();
				cardIndex = getNextCardPoint();
				avatar.avatarVO.setCurrentType(-1);
				avatar.putCardInList(cardIndex);
				avatar.avatarVO.getPickCards()[pickCardRound] = cardIndex;
				avatar.getPaiArray()[0][cardIndex]++;
				// 根据牌大小对比出，发完牌之后该谁先喊话
				nextOperateAvatar = 8;
				// cardIndex 牌索引 ,,pickAvatarIndex 摸牌玩家索引
				json.put("cardIndex", cardIndex);
				json.put("pickAvatarIndex", i);
				array.add(json);
				// scorePool += roomVO.getLeastScore() ;第二轮发牌不扣分
			}
		}
		json = new JSONObject();
		json.put("data", array);
		json.put("nextOperaterIndex", nextOperateAvatar);
		pickCardRound++;
		// System.out.println(pickCardRound);
		// 比较所有玩家摸牌的大小，最大(且离庄家近)的喊话
		for (int i = 0; i < playerList.size(); i++) {
			playerList.get(i).getSession().sendMsg(new PickCard_DEH_Response(1, json.toString()));
		}

	}

	/**
	 * 一轮二轮发牌（每次都从庄家开始发牌） 1:发的牌放入对应用户的AvatarVO中的pickCards数组中，
	 * 2:发送给每个玩家，中途睡眠一点时间(不需要)， 3:发牌期间所有玩家不能进行任何操作， 4:发完牌之后，牌最大的玩家开始喊话，
	 * 5:更新玩家状态(丢了的玩家除外)， 6:重置一些属性(playCardsLogic和avatarVo， )
	 */
	public void pickCard_DEH() {
		currentPool = roomVO.getLeastScore();
		currentType = -1;
		Avatar avatar;
		int cardIndex;
		int nextOperateAvatar = -1;
		JSONObject json;
		JSONArray array = new JSONArray(); // operateAvaterIndex 下一步该操作的玩家索引)
		currentOperateAvatarIndex = -1;
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < playerList.size(); i++) {
			avatar = playerList.get(i);
			if (avatar.avatarVO.getCurrentType() != 4) {
				json = new JSONObject();
				cardIndex = getNextCardPoint();
				avatar.avatarVO.setCurrentType(-1);
				avatar.putCardInList(cardIndex);
				avatar.avatarVO.getPickCards()[pickCardRound] = cardIndex;
				avatar.getPaiArray()[0][cardIndex]++;
				// 根据牌大小对比出，发完牌之后该谁先喊话,簸簸数为0的玩家不再喊话
				if (avatar.avatarVO.getDustpan() != 0) {
					nextOperateAvatar = nextOperateAvatar(i, cardIndex);
				}
				// cardIndex 牌索引 ,,pickAvatarIndex 摸牌玩家索引
				json.put("cardIndex", cardIndex);
				json.put("pickAvatarIndex", i);
				array.add(json);
				// scorePool += roomVO.getLeastScore() ;第二轮发牌不扣分
			}
		}
		json = new JSONObject();
		json.put("data", array);
		json.put("nextOperaterIndex", nextOperateAvatar);
		pickCardRound++;
		// System.out.println(pickCardRound);
		// 比较所有玩家摸牌的大小，最大(且离庄家近)的喊话
		for (int i = 0; i < playerList.size(); i++) {
			playerList.get(i).getSession().sendMsg(new PickCard_DEH_Response(1, json.toString()));
		}

	}

	/**
	 * 获取下一轮喊话玩家
	 * 
	 * @param avatarIndex
	 * @param cardindex_new
	 * @return
	 */
	public int nextOperateAvatar(int avatarIndex, int cardindex_new) {
		if (currentOperateAvatarIndex == -1) {
			currentOperateAvatarIndex = avatarIndex;
		} else {
			int cardIndex_Old = playerList.get(currentOperateAvatarIndex).avatarVO.getPickCards()[pickCardRound];
			// 返回大的
			int returnIndex = DingErHongUtil.compareSingle(cardIndex_Old, cardindex_new);
			if (returnIndex == cardIndex_Old) {
				// System.out.println("当前操作玩家索引不变");
			} else if (returnIndex == cardindex_new) {
				currentOperateAvatarIndex = avatarIndex;
			} else {
				// 一样大,则需要判断逆时针那个玩家离庄家近(如果是和庄家一样，则庄家先叫)
				if (playerList.get(currentOperateAvatarIndex) == bankerAvatar) {
					// System.out.println("当前操作玩家索引不变");
				} else if (playerList.get(avatarIndex) == bankerAvatar) {
					currentOperateAvatarIndex = avatarIndex;
				} else {
					int bankerAvatarIndex = playerList.indexOf(bankerAvatar);
					int nearestIndex;
					for (int i = 0; i < playerList.size(); i++) {
						nearestIndex = getNearestAvatarIndex(bankerAvatarIndex + i);
						if (nearestIndex == currentOperateAvatarIndex) {
							// 不变
							break;
						} else if (nearestIndex == avatarIndex) {
							currentOperateAvatarIndex = avatarIndex;
							break;
						}
					}
				}
			}
		}
		return currentOperateAvatarIndex;
	}

	//////////////////////////////////////////////
}
