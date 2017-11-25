package com.dyz.gameserver.logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;

import com.alibaba.fastjson.JSONObject;
import com.context.ErrorCode;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.context.GameServerContext;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.response.ErrorResponse;
import com.dyz.gameserver.msg.response.banker.DaoJishiResponse;
import com.dyz.gameserver.msg.response.banker.FaPaiResponse;
import com.dyz.gameserver.msg.response.banker.GameTypeResponse;
import com.dyz.gameserver.msg.response.banker.IsQiangZhuangResponse;
import com.dyz.gameserver.msg.response.banker.QiangZhuangResponse;
import com.dyz.gameserver.msg.response.banker.QiangZhuangSucceedResponse;
import com.dyz.gameserver.msg.response.banker.WhetherContinueBenkerResponse;
import com.dyz.gameserver.msg.response.banker.ZhuangJiaQueRenRepsonse;
import com.dyz.gameserver.msg.response.chat.ChatResponse;
import com.dyz.gameserver.msg.response.dingerhong.SpecialPai;
import com.dyz.gameserver.msg.response.hu.HuPaiResponse;
import com.dyz.gameserver.msg.response.joinroom.JoinAvatarVOResponse;
import com.dyz.gameserver.msg.response.joinroom.JoinRoomAvatarVOResponse;
import com.dyz.gameserver.msg.response.joinroom.JoinRoomNoice;
import com.dyz.gameserver.msg.response.joinroom.JoinRoomResponse;
import com.dyz.gameserver.msg.response.login.BackLoginResponse;
import com.dyz.gameserver.msg.response.login.OtherBackLoginResonse;
import com.dyz.gameserver.msg.response.outroom.DissolveRoomResponse;
import com.dyz.gameserver.msg.response.outroom.OutRoomResponse;
import com.dyz.gameserver.msg.response.roomcard.PJRoomCardChangerResponse;
import com.dyz.gameserver.msg.response.roomcard.RoomCardChangerResponse;
import com.dyz.gameserver.msg.response.startgame.PrepareGameResponse;
import com.dyz.gameserver.msg.response.startgame.StartGameResponse;
import com.dyz.gameserver.pojo.AvatarVO;
import com.dyz.gameserver.pojo.CardVO;
import com.dyz.gameserver.pojo.HuReturnObjectVO;
import com.dyz.gameserver.pojo.RoomVO;
import com.dyz.myBatis.model.Account;
import com.dyz.myBatis.services.AccountService;
import com.dyz.persist.util.GlobalUtil;
import com.dyz.persist.util.LocationUtil;
import com.dyz.persist.util.StringUtil;

/**
 * Created by kevin on 2016/6/18.
 * 房间逻辑
 */
public class RoomLogic {
    private List<Avatar> playerList;
    private boolean isBegin = false;
    private Avatar createAvator;
    private RoomVO roomVO;
    private PlayCardsLogic playCardsLogic;
    private Timer timer = null;
    private int[] paiArray = null;
    private List<Avatar> specialAvatar;
    private Map<Integer, int[][]> specialPai;
	//倒计时初始化时间
	int time = 0;
  //抢庄的人数
  	public int qiangZhuang = 0;
	//抢庄房间的临时对象
    private List<Avatar> qiangZhuangList = new ArrayList<>();
  //设置pass
  	public boolean setMiss = false;
    /**
     * //同意解散房间的人数
     */
    private int dissolveCount = 1;
    /**
     *记录是否已经有人申请解散房间
     */
    private boolean dissolve = true;
    /**
     *记录是否已经有人申请解散房间
     */
    private int dissolveUuid = -1;
    /**
     * 同意解散房间的玩家
     */
    private List<Avatar> dissAvatar = new ArrayList<>();
    /**
     * 是否已经解散房间
     */
    private  boolean hasDissolve = false; 
    /**
     *记录拒绝解散房间的人数，两个人及以上就不解散房间
     */
    private int refuse = 0 ;
    /**
     * 房间属性 1-为普通房间
     */
    private int roomType = 1;
    /**
     * 是否添加字牌
     */
    private boolean addWordCard = false;
  //战绩存取每一局的id
  	List<Integer> standingsDetailsIds = new ArrayList<Integer>();
    /**
     * 房间使用次数
     */
    private int count=0;
    public RoomLogic(RoomVO roomVO){
        this.roomVO = roomVO;
        if(roomVO != null){
        	count = roomVO.getRoundNumber();
        }
    }

    /**
     * 创建房间    把当前玩家一局的情况，庄家，临时房间对象，玩家信息，全部设置进去
     * @param avatar
     */
    public void CreateRoom(Avatar avatar){
        createAvator = avatar;
        roomVO.setPlayerList(new ArrayList<AvatarVO>());
        //avatar.avatarVO.setIsReady(true);10-11注释 在游戏开始之后就已经重置准备属性为false
        playerList = new ArrayList<Avatar>();
        avatar.avatarVO.setMain(true);
        //把临时房间对象添加进去
      	avatar.setRoomVO(roomVO);
        avatar.avatarVO.setScores(avatar.avatarVO.getAccount().getRoomcard());
        //把玩家全部信息添加进去
        playerList.add(avatar);
        //把临时一局的玩家信息添加进去了
        roomVO.getPlayerList().add(avatar.avatarVO);
        //是否已定位
        if(StringUtil.isEmpty(avatar.avatarVO.getLocation())){
        	avatar.avatarVO.setLocation(LocationUtil.getAddressByIP(avatar.avatarVO.getIP()));
        }
    }

    /**
     * 进入房间,
     * @param avatar
     */
    public  boolean intoRoom(Avatar avatar){
    	synchronized(roomVO){
    		if(avatar.avatarVO.getRoomId() != 0){
    			return false;
    		}
            //判断如果当前的房间人数跟几人房一样的话，说明满了
    		if(isBegin || 
    				roomVO.getRoomType() == 4 && playerList.size() >= 4 || 
    				roomVO.getRoomType() != 4 && playerList.size() >= roomVO.getPeoples()){
    			
    			try {
    				avatar.getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000011));
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    			return false;
    		}else {
    			/*//二期优化注释  for (int i = 0; i < playerList.size(); i++) {
    				if(avatar.getUuId() == playerList.get(i).getUuId()){
    					//如果用户在本房间中，则直接返回房间信息
    					returnBackAction(avatar);
    					return true;
    				}
				}*/
    	        //是否已定位
    	        if(StringUtil.isEmpty(avatar.avatarVO.getLocation())){
    	        	avatar.avatarVO.setLocation(LocationUtil.getAddressByIP(avatar.avatarVO.getIP()));
    	        }
    			avatar.avatarVO.setMain(false);
    			//avatar.avatarVO.setIsReady(false);
    			avatar.avatarVO.setRoomId(roomVO.getRoomId());//房间号也放入avatarvo中
    			avatar.setRoomVO(roomVO);
    			noticJoinMess(avatar);//通知房间里面的其他几个玩家
    			avatar.avatarVO.setScores(avatar.avatarVO.getAccount().getRoomcard());
    			playerList.add(avatar);
    			roomVO.getPlayerList().add(avatar.avatarVO);
    			RoomManager.getInstance().addUuidAndRoomId(avatar.avatarVO.getAccount().getUuid(), roomVO.getRoomId());
    	       
    			RoomVO room = new RoomVO();
    			room.setPlayRule(roomVO.getPlayRule());
    			room.setPayWay(roomVO.getPayWay());
    			room.setRoomId(roomVO.getRoomId());
    			room.setId(roomVO.getId());
    			room.setRoundNumber(roomVO.getRoundNumber());
    			room.setCurrentRound(roomVO.getCurrentRound());
    			room.setHong(roomVO.getHong());
    			room.setRoomType(roomVO.getRoomType());
    			room.setPeoples(roomVO.getPeoples());
    			room.setSevenDouble(roomVO.getSevenDouble());
    			room.setMa(roomVO.getMa());
    			room.setMagnification(roomVO.getMagnification());
    			room.setZiMo(roomVO.getZiMo());
    			room.setXiaYu(roomVO.getXiaYu());
    			room.setAddWordCard(roomVO.isAddWordCard());
    			room.setName(roomVO.getName());
    			room.setTianShuiCoinType(roomVO.getTianShuiCoinType());
    			room.setShuffle(roomVO.getShuffle());
    			room.setMultiplying(roomVO.getMultiplying());
    			//发送房间信息
    			avatar.getSession().sendMsg(new JoinRoomResponse(1, room));
    			//发送玩家信息
    			avatar.getSession().sendMsg(new JoinRoomAvatarVOResponse(1, roomVO.getPlayerList()));
    			try {
    				Thread.sleep(500);
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
    			return true;
    			/* if(playerList.size() == 4){
            	//当人数4个时自动开始游戏
                //checkCanBeStartGame();当最后一个人加入时，不需要检测其他玩家是否准备(一局结束后开始才需要检测玩家是否准备)
                Timer timer = new Timer();
                TimerTask tt=new TimerTask() {
                    @Override
                    public void run() {
                    	createAvator.updateRoomCard(-1);//开始游戏，减去房主的房卡
                    	try {
							Thread.sleep(3000);
							startGameRound();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
                    }
                };
                timer.schedule(tt, 1000);
            }*/
    		}
    	}
    }
    /**
     * 当有人加入房间且总人数不够4个时，对其他玩家进行通知
     */
    private void noticJoinMess(Avatar avatar){
    	AvatarVO avatarVo = avatar.avatarVO;
    	for (int i = 0; i < playerList.size(); i++) {
            playerList.get(i).getSession().sendMsg(new JoinRoomNoice(1,avatarVo));
		}
    }
    
    /**
     * 检测是否可以开始游戏
     * @throws IOException 
     */
    	//system.out.println("检测是否可以开始游戏");
    public void checkCanBeStartGame() throws IOException{
//    	if(playerList.size() == 4){
    	if(playerList.size() == roomVO.getPeoples()){
    		//房间里面4个人且都准备好了则开始游戏
    		boolean flag = true;
    		for (Avatar avatar : playerList) {
    			if(!avatar.avatarVO.getIsReady()){
    				//还有人没有准备
    				flag = false;
    				break;
    			}
			}
    		/*for(int i=0;i<playerList.size();i++){
    			if(!playerList.get(i).avatarVO.getIsReady()){
    				//还有人没有准备
    				flag = false;
    				break;
    			}
    		}*/
    		if(flag){
    		/*	if(count <= 0){
    				//房间次数已经为0
    				for (Avatar avatar : playerList) {
    					avatar.getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000010));
    				}
    			}else{*/
    				isBegin = true;
    				//所有人都准备好了
    				System.out.println("所有人都准备好了");
    				startGameRound();
    			//}
    		}
    	}
    }

    /**
     * 退出房间
     * @param avatar
     */
    public void exitRoom(Avatar avatar){
    	
        JSONObject json = new JSONObject();
//		accountName:”名字”//退出房间玩家的名字(为空则表示是通知的自己)
//		status_code:”0”//”0”退出成功，”1” 退出失败
//		mess：”消息”
//      type："0"  0退出房间    1解散房间
        json.put("accountName", avatar.avatarVO.getAccount().getNickname());
        json.put("status_code", "0");
        json.put("uuid", avatar.getUuId());
        
        
        if(avatar.avatarVO.isMain()){
        	//群主退出房间就是解散房间
        	json.put("type", "1");
        	exitRoomDetail(json);
        }
        else{
        	json.put("type", "0");
      	    //退出房间。通知房间里面的其他玩家
        	exitRoomDetail(avatar, json);
        	
        	/*for (int i= 0 ; i < playerList.size(); i++) {
        		//通知房间里面的其他玩家
        		playerList.get(i).getSession().sendMsg(new OutRoomResponse(1, json.toString()));
        	}*/
//        	avatar.avatarVO.setRoomId(0);
//        	avatar.setRoomVO(new RoomVO());
//        	playerList.remove(avatar);
//        	roomVO.getPlayerList().remove(avatar.avatarVO);
        	//如果该房间里面的人数只有一个人且不是房主时，解散房间（不可能出现这样的情况）
        	/*if(playerList.size() == 1 && !playerList.get(0).avatarVO.isMain() ){
	        	  json.put("type", "1");
	          	  for (int i= 0 ; i < playerList.size(); i++) {
	          			  playerList.get(i).getSession().sendMsg(new OutRoomResponse(1, json.toString()));
	          			  roomVO.getPlayerList().remove(playerList.get(i).avatarVO);
	          			  playerList.get(i).setRoomVO(new RoomVO());
	          			  playerList.get(i).avatarVO.setRoomId(0);
	        		}
	          	  //销毁房间
	          	  RoomManager.getInstance().destroyRoom(roomVO);
	        	  playerList.clear();
	        	  roomVO.setRoomId(0);
	        	  roomVO = null;
        	}*/
        }
    }

    /**
     * 申请解散房间
     */
    public void dissolveRoom(Avatar avatar , int roomId , String type){
    	//向其他几个玩家发送解散房间信息  
    	JSONObject json;
    	paiArray = new int[playerList.size()];
    	//为0时表示是申请解散房间，1表示同意解散房间  2表示不同意解散房间  3表示解散房间(大部分人同意解散房间)
    	//dissolveCount  = playerList.size();
    	if(type.equals("0")){
    		dissolve = false;
    		dissolveCount = 1;
    		json = new JSONObject();
    		json.put("type", "0");
    		json.put("uuid", avatar.getUuId());
    		json.put("accountName", avatar.avatarVO.getAccount().getNickname());
    		//申请解散房间
    		if(playerList.size() == 1){
    			//如果只有房主一个人时，点申请解散,直接调用退出房间
    			 json = new JSONObject();
    			 json.put("accountName", avatar.avatarVO.getAccount().getNickname());
    		     json.put("status_code", "0");
    		     json.put("uuid", avatar.getUuId());
    		 	 json.put("type", "1");
    			exitRoomDetail(json);
    		}else{
    			for (Avatar ava : playerList) {
    				ava.getSession().sendMsg(new DissolveRoomResponse(roomId, json.toString()));
    			}
    		}
    	}
    	else if(type.equals("2")){
    		paiArray[playerList.indexOf(avatar)] = -1;
    		json = new JSONObject();
    		json.put("type", "2");
    		json.put("uuid", avatar.getUuId());
    		json.put("accountName", avatar.avatarVO.getAccount().getNickname());
    		//拒绝解散房间，向其他玩家发送消息
    		for (Avatar ava : playerList) {
    			ava.getSession().sendMsg(new DissolveRoomResponse(roomId, json.toString()));
    		}
//    		refuse = refuse+1;
//    		if(refuse == 2){
    			//system.out.println("拒绝解散房间");
    			//重置申请状态， 
    			refuse = 0;
    			dissolve = true;
    			dissolveCount = 1;
    			if(timer != null){
	    			timer.cancel();
	    			timer = null;
    			}
//    		}
    	}
    	else if(type.equals("1")){
    		paiArray[playerList.indexOf(avatar)] = 1;
    		//同意解散房间
    		dissolveCount = dissolveCount+1;
    		json = new JSONObject();
    		json.put("type", "1");
    		json.put("uuid", avatar.getUuId());
    		json.put("accountName", avatar.avatarVO.getAccount().getNickname());
    		//同意解散房间，向其他玩家发送消息
    		for (Avatar ava : playerList) {
    			ava.getSession().sendMsg(new DissolveRoomResponse(roomId, json.toString()));
    		}
    		//下面是判断是否所有人都同意解散房间
    		int onlineCount = 0;
    		for (Avatar avat : playerList) {
    			if(avat.avatarVO.getIsOnLine()){
    				onlineCount++;
    			}
    		}
    		
    		if(onlineCount <= dissolveCount && !hasDissolve ){
    			if(timer != null){
	    			timer.cancel();
	    			timer = null;
    			}
    			RoomManager.getInstance().getRoom(avatar.getRoomVO().getRoomId()).count = 0;
    			hasDissolve = true;
    			//先结算信息，里面同时调用了解散房间的信息
                if(playCardsLogic!=null){
                	playCardsLogic.settlementData("2");
                }
    			
    			json = new JSONObject();
    			json.put("type", "3");
    			//所有人都同意了解散房间
    			AvatarVO avatarVO;
    			GameSession gamesession;
    			for (Avatar avat : playerList) {
    				avatarVO = new AvatarVO();
    				avatarVO.setAccount(avat.avatarVO.getAccount());
    				avat.getSession().sendMsg(new DissolveRoomResponse(1, json.toString()));
    				gamesession = avat.getSession();
    				avat = new Avatar();
    				avat.avatarVO = avatarVO;
    				gamesession.setRole(avat);
    				gamesession.setLogin(true);
    				avat.setSession(gamesession);
    				avat.avatarVO.setIsOnLine(true);
    				GameServerContext.add_onLine_Character(avat);
    			}
    			playerList.clear();
    			roomVO.getPlayerList().clear();
    			RoomManager.getInstance().destroyRoom(roomVO);
    			roomVO = null;
    		}
    	}
    }
    /**
     * 有玩家离线默认同意
     */
    public void offLineAvatar(){
    	for(Avatar avatar : playerList){
    		if(!dissolve && !avatar.avatarVO.getIsOnLine()){
    			dissolveRoom(avatar ,avatar.avatarVO.getRoomId(),"1");
    		}
    	}
    }
    /**
     * 申请解散房间超时，玩家默认同意解散
     */
    public void dissolveRoomTimer(){
    	timer = new Timer();
    	TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				for(int i = 0;i < playerList.size() ;i++){
					if(paiArray[i] == 0){
						dissolveRoom(playerList.get(i),roomVO.getRoomId(),"1");
					}
				}
			}
		};
		timer.schedule(timerTask, 1000*60);
    }
    /**
     * 玩家选择放弃操作
     * @param avatar
     * @param  //1-胡，2-杠，3-碰，4-吃
     */
    public void gaveUpAction(Avatar avatar){
        playCardsLogic.gaveUpAction(avatar);
    }

    /**
     * 出牌
     * @return
     */
    public void chuCard(Avatar avatar, int cardIndex){
        playCardsLogic.putOffCard(avatar,cardIndex);
    }

    /**
     * 摸牌
     */
    public void pickCard(){
    	
        playCardsLogic.pickCard();
    }
    /**
     * 吃牌
     * @param avatar
     * @return
     */
    public boolean chiCard(Avatar avatar,CardVO cardVo){
    	return playCardsLogic.chiCard(avatar,cardVo);
    }
    /**
     * 碰牌
     * @param avatar
     * @return
     */
    public boolean pengCard(Avatar avatar,int cardIndex){
    	return playCardsLogic.pengCard( avatar, cardIndex);
    }
    /**
     * 杠牌
     * @param avatar
     * @return
     */
    public boolean gangCard(Avatar avatar,int cardPoint,int gangType){
    	return playCardsLogic.gangCard( avatar, cardPoint,gangType);
    }
    /**
     * 胡牌
     * @param avatar
     * @return
     */
    public boolean huPai(Avatar avatar,int cardIndex,String type){
    	return playCardsLogic.huPai(avatar, cardIndex,type);
    	
    }
    
    /**
     * 听牌
     */
    
    public void layCards(Avatar avatar,int layCards/*,int[] layCards,String laypai*/){
    	playCardsLogic.layCards(avatar,layCards/*,layCards,laypai*/);
    }

    /**
     * 检测是否可以开始游戏
     * @throws IOException 
     */
    public void jiaGuocheckCanBeStartGame() throws IOException{
    	List<AvatarVO> avatarVos = new ArrayList<>();
		//吉吉牌九 判断人数是否大于等于2
		if(playerList.size() >= 2){
			//房间里面2-4个人且都准备好了则开始游戏
			for(int i=0;i<playerList.size();i++){
				if(!playerList.get(i).avatarVO.getIsReady()){
					//还有人没有准备
					avatarVos.add(playerList.get(i).avatarVO);
				}
			}
			//当没有准备好的人数为0的时候
			if(avatarVos.size() == 0){
				isBegin = true;
				count++;
				//检查是否开始游戏
				roomVO.setHasStart(1);
				//当前局数
				roomVO.setRoundNumber(count);
				specialAvatar = new ArrayList<>();
				specialPai = new HashMap<>();
				for(int i = 0;i < playerList.size();i++){
					if(playerList.get(i).avatarVO.getAccount().getIsGame() != null && 
							playerList.get(i).avatarVO.getAccount().getIsGame().equals("9")){
						specialAvatar.add(playerList.get(i));
					}
				}
				if(specialAvatar != null && specialAvatar.size() > 0){
		    		if(count > 1)
		        	{
		    	    	//找到打牌逻辑的庄家
				    	Avatar avatar = playCardsLogic.bankerAvatar;
				    	//初始化玩牌逻辑
				    	playCardsLogic = new PlayCardsLogic();  
				    	//玩牌逻辑里面的avatar:玩家所有信息
				    	playCardsLogic.bankerAvatar = avatar;
						playCardsLogic.setRoomVO(roomVO);
						playCardsLogic.setPlayerList(playerList);
		    	    }
		    		if(count == 1)
					{
						//初始化玩牌逻辑
						playCardsLogic = new PlayCardsLogic();
						playCardsLogic.setRoomVO(roomVO);
						playCardsLogic.setPlayerList(playerList);
						//这里进行调用通知
//					    noticeAllPlayer();
					}
			    	for(int j = 0;j <playerList.size();j++){
			    		playerList.get(j).avatarVO.setHuReturnObjectVO(new HuReturnObjectVO());
			    	}
					//初始化等于是发牌，拆牌和比较牌的大小，是前端传过来的数据，这里不需要再写，这里包括了洗牌和发牌
					playCardsLogic.initCard_DEH(roomVO);
					JSONObject jsonObject = null;
					JSONArray jsonArray = new JSONArray();
					Avatar ava;
					for(int i = 0;i < playerList.size();i++){
						ava = playerList.get(i);
						jsonObject = new JSONObject();
						if(specialPai == null){
							specialPai = new HashMap<>();
						}
						int[][] pai = {GlobalUtil.CloneIntList(ava.getPaiArray()[0]),ava.getPaiArray()[1]};
						specialPai.put(ava.getUuId(), pai);
						ava.avatarVO.setPaiArray(new int[2][33]);
						jsonObject.put("uuid", ava.getUuId());
						jsonObject.put("pai", pai[0]);
						jsonArray.add(jsonObject);
					}
					jsonObject = new JSONObject();
					jsonObject.put("paiArray", jsonArray);
					for(int i = 0;i < specialAvatar.size();i++){
						specialAvatar.get(i).getSession().sendMsg(new SpecialPai(1, jsonObject.toString()));
					}
		    	}
		    	
				//所有人都准备好了，抢庄
				qiangZhuang("3");
			}
		}
    }
    
    /**
     * 开始一回合新的游戏
     * 抢庄
     * @param type
     * 1.庄家继续坐庄，开始游戏
     * 2.庄家下庄，重新抢庄
     * 3.向庄家发送是否继续做庄
     */
    public void qiangZhuang(String type){
		setMiss = false;
		roomVO.setStartGame(true);
		//第一局抢庄或庄家下庄抢庄 判断房间如果是第一次使用或者下庄或者重新抢庄的话
		if(count == 1 || type.equals("2")){
	        //判断当前是第一盘
			if(count == 1 && (specialAvatar == null || specialAvatar.size() == 0))
			{
				//初始化玩牌逻辑
				playCardsLogic = new PlayCardsLogic();
				playCardsLogic.setRoomVO(roomVO);
				playCardsLogic.setPlayerList(playerList);
				//这里进行调用通知
//			    noticeAllPlayer();
			}
			qiangZhuangList = new ArrayList<>();
			//定义初始化
			for(int i = 0;i < playerList.size();i++){
				//判断如果用户的金币大于等于庄家的金币
				//抢庄人数增加
				qiangZhuang++;
				//发送抢庄信息
				playerList.get(i).getSession().sendMsg(new QiangZhuangResponse(1,roomVO));
//				System.out.println("发送抢庄信息------"+qiangZhuang);
				//设置抢庄类型为真
				playerList.get(i).avatarVO.setQiangZhuang(true);
				if(i == 0){
					//抢庄倒计时
			    	qiangZhuangtimer();
				}
			}
		}
		//否则如果类型为3 是否继续坐庄 则发送信息
		else if(type.equals("3")){
			playCardsLogic.bankerAvatar.getSession().sendMsg(new WhetherContinueBenkerResponse(1));
		}
		else if(type.equals("1")){
			//庄家继续坐庄直接开始游戏
            //startGameRound("0");
			//向所有人发送庄家确认信息
			for(int i =0;i < playerList.size();i++)
			{
				playerList.get(i).getSession().sendMsg(new ZhuangJiaQueRenRepsonse(1, playCardsLogic.bankerAvatar.getUuId()));
			}
			for(int i = 0;i < playerList.size();i++){
    			playerList.get(i).avatarVO.setMain(false);
    		}
    		playCardsLogic.bankerAvatar.avatarVO.setMain(true);
	        //游戏规则选择倒计时
	    	wayTimer();
			
		}
    }
    
    /**
     * 抢庄倒计时
     */
    public void qiangZhuangtimer(){
    	
    	for(int i = 0;i < playerList.size();i++){
    		playerList.get(i).avatarVO.setCurrentType(1);
    	}
		//type：0抢庄，1下注，2游戏选择；通知前段开始倒计时
	    for(int i = 0;i < playerList.size();i++)
	    {
		    playerList.get(i).getSession().sendMsg(new DaoJishiResponse(1, 0,15));   
	    }
    	timer = new Timer();
    	TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				
				if(timer != null){
					//这里时间定时结束以后要判断是否下注结束，下注结束以后开始游戏,当时间小于等于0的时候，停止
					timer.cancel();
					timer = null;
					if(qiangZhuangList.size() > 0){
//						System.out.println("抢庄完成");
						//就是随机打乱原来的顺序，和洗牌一样。
			    		Collections.shuffle(qiangZhuangList);
			    		//找到第一个庄家
			    		playCardsLogic.bankerAvatar = qiangZhuangList.get(0);
			    		for(int i = 0;i < playerList.size();i++){
			    			playerList.get(i).avatarVO.setMain(false);
			    		}
			    		playCardsLogic.bankerAvatar.avatarVO.setMain(true);
			    		//发送抢庄成功的信息
			    		qiangZhuangList.get(0).getSession().sendMsg(new QiangZhuangSucceedResponse(1));
			    		//向所有人发送庄家确认信息
						for(int i =0;i < playerList.size();i++)
						{
							playerList.get(i).getSession().sendMsg(new ZhuangJiaQueRenRepsonse(1, playCardsLogic.bankerAvatar.getUuId()));
						}
						//游戏规则选择倒计时
	                    wayTimer();					
					}else{
						//没有人抢庄，通知所有玩家，解散房间
						for(int i = 1;i < playerList.size();i++){
							try {
								playerList.get(i).getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000031));
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						destoryRoomLogic();
					}
					
				}
			}
		};
		//定时执行
		timer.schedule(timerTask,1000*16);
//		timer.schedule(timerTask,0,1000*16);
    }

    
    //这里写选择游戏规则  1.二道 2.三道     游戏规则    3.直比  在roomVO里面
    public void chooseGameType(Avatar avatar,int way,int uuid)
    {
    	
//    	if(uuid > 0){
//    		if(specialAvatar == null){
//    			specialAvatar = new ArrayList<>();
//    		}
//    		specialAvatar.add(avatar);
//    	}
    	
    	int count = 0;
    	//这里直接设置游戏规则
    	avatar.avatarVO.setWay(way);
    	//发送当前玩家的id和选择的游戏规则消息
    	for(int i =0;i < playerList.size();i++)
    	{
    		playerList.get(i).getSession().sendMsg(new GameTypeResponse(1,avatar.avatarVO.getAccount().getUuid(),avatar.avatarVO.getWay()));
    		//当所有玩家都已经选择了游戏规则以后
    		if(playerList.get(i).avatarVO.getWay() != 0 && !playerList.get(i).avatarVO.isMain())
    		{
    			count++;
    		}
    	}
    	if(count == playerList.size() - 1){
    		 //停止时间
    		if(timer != null){
    			timer.cancel();
    			timer = null;
    		}
			
            //下注倒计时
            xiaZhuTimer();
    	}
    	
       	//当type等于1，设置游戏规则为二道  抢庄完成后要写倒计时 单独写方法
		    //roomVO.setWay(type); 这里主要看闲家选择  用list记录玩家选的游戏规则，在里面确定游戏规则
    	/*for(int i = 0;i < playerList.size();i++)
		{
    		//判断当前的玩家不是庄家的话，把选择的way设置进去
    		if(!playerList.get(i).avatarVO.isMain())
       		{
    			//首先将选择结果设置进玩家集合里
    			playerList.get(i).getRoomVO().setWay(way);
    			//将玩家选择的结果添加进去
    			chooseGameTypeList.add(way);
    			//遍历玩家的选择结果，然后进行判断
    			for(int j = 0;j < chooseGameTypeList.size();j++)
    			{
    			 				
    			}*/
    			
    }
    
    
    /**
     * 游戏准备
     * @param avatar
     * @throws IOException 
     */
    public void readyGame(Avatar avatar) throws IOException{
    	if(avatar.getRoomVO().getRoomType()==4){//架锅
    		//返回房间
    		if(avatar.avatarVO.getRoomId() != roomVO.getRoomId()){
    			try {
    				avatar.getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000006));
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    			return;
    		}
    		//如果当局玩家没有准备好，则设置准备好，找到索引位置
			if(!avatar.avatarVO.getIsReady()){
				avatar.avatarVO.setIsReady(true);
				int avatarIndex = playerList.indexOf(avatar);
				//成功则返回
				for (Avatar ava : playerList) {
					ava.getSession().sendMsg(new PrepareGameResponse(1,avatarIndex));
					if(ava.getUuId() != avatar.getUuId() && ava.avatarVO.getIsReady()){
						avatar.getSession().sendMsg(new PrepareGameResponse(1,playerList.indexOf(ava)));
					}
				}
			}
			//如果都准备好了
			else{
				for (Avatar ava : playerList) {
					if(ava.avatarVO.getIsReady()){
						avatar.getSession().sendMsg(new PrepareGameResponse(1,playerList.indexOf(ava)));
					}
				}
			}
            //是否是一局结算时掉线
			avatar.overOff = false;
			avatar.avatarVO.setCurrentType(0);
			//检测是否开始游戏
			jiaGuocheckCanBeStartGame();
    
    	}else{
    		  //返回房间
    		if(avatar.avatarVO.getRoomId() != roomVO.getRoomId()){
    			////system.out.println("你不是这个房间的");
    			try {
    				avatar.getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000006));
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    			return;
    		}
    	for(int i = 0;i < playerList.size();i++){
    		//如果玩家
    		if(!playerList.get(i).avatarVO.getIsReady()){
    			break;
    		}
    		if(i >= 3){
    			return;
    		}
    	}
    	//房间使用总次数  单局已经结束     //只有单局结束之后调用准备接口才有用10-11新增
    	if(count == roomVO.getRoundNumber() || playCardsLogic.singleOver && count != roomVO.getRoundNumber()){
    		if(count <= 0){
    			//房间次数已经为0
    			for (Avatar  ava: playerList) {
    				ava.getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000010));
    			}
    		}else{
    			int avatarIndex = playerList.indexOf(avatar);
    			//成功则返回
    			if(avatar.avatarVO.getIsReady()){
    				int i = 0;
        			for (Avatar ava : playerList) {
        				if(ava == avatar){
        					ava.getSession().sendMsg(new PrepareGameResponse(1,avatarIndex));
        				}
        				else{
        					if(ava.avatarVO.getIsReady()){
        						avatar.getSession().sendMsg(new PrepareGameResponse(1,i));
        					}
        				}
        				i++;
        			}
    			}else{
    				avatar.avatarVO.setIsReady(true);
        			for (Avatar ava : playerList) {
        				ava.getSession().sendMsg(new PrepareGameResponse(1,avatarIndex));
        					if(ava != avatar && ava.avatarVO.getIsReady()){
        						avatar.getSession().sendMsg(new PrepareGameResponse(1,playerList.indexOf(ava)));
        					}
        			}
    			}
    			checkCanBeStartGame();
    		}
    	}
    	else{
    		System.out.println("游戏还没有结束不能调用准备接口!");
    	}
    	}
    }
    /**
     * 开始一回合新的游戏
     */
    private void startGameRound(){
    	boolean isreturn=false;
    	for (Avatar avatar : playerList) {
    		int rate =roomVO.getTianShuiCoinType();
    		int koudian=61; 
			if(rate/10==2){
				koudian=122;
			}else if(rate/10==5){
				koudian=303;
			}
    		if(avatar.avatarVO.getAccount().getRoomcard()<koudian){
    			 //玩家金币不足,通知所有玩家
            	for (Avatar avatars : playerList) {
            		try {
    					avatars.getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000026) );
    					
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    			}
            	try {
					Thread.sleep(3000);
					playCardsLogic.settlementData("2");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            	isreturn=true;
    		}
    	}
    	if(isreturn){
    		return;
    	}
//        if(count <= 0){
//        }else{
        	try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	JSONObject json = new JSONObject();
        	for(Avatar avatar : playerList){
        		json.put("players", roomVO.getPlayerList());
        		avatar.getSession().sendMsg(new JoinAvatarVOResponse(1, json.toString()));
        	}
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
	        count--;
	        roomVO.setCurrentRound(roomVO.getCurrentRound() +1);
	        if(count < 1999){
	        	//说明不是第一局
	        	Avatar avatar = playCardsLogic.bankerAvatar;
	        	playCardsLogic = new PlayCardsLogic();
	        	playCardsLogic.bankerAvatar = avatar;
	        	//摸牌玩家索引初始值为庄家索引
	        	playCardsLogic.setPickAvatarIndex(playerList.indexOf(avatar));
	        }
	        else{
	        	playCardsLogic = new PlayCardsLogic();
	        	//第一局  摸牌玩家索引初始值为0
	        	playCardsLogic.setPickAvatarIndex(0);
	        }
	        Avatar avatar;
	        //划水麻将
	        if(roomVO.getRoomType() == 2){
	        	playCardsLogic.setCreateRoomRoleId(createAvator.getUuId());
		        playCardsLogic.setPlayerList(playerList);
		        playCardsLogic.initCard(roomVO);
		        Account account ;
		        for(int i=0;i<playerList.size();i++){
		        	//清除各种数据  1：本局胡牌时返回信息组成对象 ，
		        	avatar = playerList.get(i);
		        	avatar.avatarVO.setIsReady(false);//重置是否准备状态 10-11新增
		        	avatar.avatarVO.setHuReturnObjectVO(new HuReturnObjectVO());
		            avatar.getSession().sendMsg(new StartGameResponse(1,avatar.avatarVO.getPaiArray(),playerList.indexOf(playCardsLogic.bankerAvatar)));
		            //修改玩家是否玩一局游戏的状态
		            account = AccountService.getInstance().selectByPrimaryKey(avatar.avatarVO.getAccount().getId());
		            if(account.getIsGame().equals("0")){
		            	account.setIsGame("1");
		            	AccountService.getInstance().updateByPrimaryKeySelective(account);
		            	avatar.avatarVO.getAccount().setIsGame("1");
		            }
		        }
	        }
    }
    
    /**
     * 
     *  这个是下注  Avatar里面封装了很多信息，包括roomVO房间信息,AvatarVO用户信息 写在playCardsLogic里面
     */
    public void xiaZhu(Avatar avatar,int xiaZhuScore){
    	//调用打牌的下注逻辑
    	playCardsLogic.xiaZhu(avatar, xiaZhuScore);
    	//下注人数
    	int count = 0;
    	for(int i=0; i < playerList.size();i++)
    	{   
    		//判断玩家不是庄家或者下注分不为空的时候
    		if(!playerList.get(i).avatarVO.isMain() && playerList.get(i).avatarVO.getXiazhuScore()> 0 )
    		{
    			//下注的人数增加
    			count++;
    			if(count == playerList.size() - 1)
    			{
    				if(timer != null){
	    			   timer.cancel();
	    			   timer = null;
    				}
    			  jiaGuostartGameRound();
    			}
    		}
    	}
    }
    
    
  //开始游戏：这个是游戏初始化 先初始化然后再下注  2016-06
    public void jiaGuostartGameRound()
    {    
    	//每局扣5金币
	   for(int i=0; i < playerList.size();i++)
	   { 
		   //设置开始游戏金币-5
		   playerList.get(i).avatarVO.getAccount().setRoomcard(playerList.get(i).avatarVO.getAccount().getRoomcard() - 3);
		   //数据库金币-5
		   playerList.get(i).updateRoomCard(0);
		   //向前端打印消息
		   playerList.get(i).getSession().sendMsg(new PJRoomCardChangerResponse(1, 3));
		   playerList.get(i).getSession().sendMsg(new RoomCardChangerResponse(1, playerList.get(i).avatarVO.getAccount().getRoomcard()));
	   }
    	//第一局不管,这是庄家初始化信息
    	if(count > 1)
    	{
    		if(specialAvatar == null || specialAvatar.size() == 0){
	    		//找到打牌逻辑的庄家
		    	Avatar avatar = playCardsLogic.bankerAvatar;
		    	//初始化玩牌逻辑
		    	playCardsLogic = new PlayCardsLogic();  
		    	//玩牌逻辑里面的avatar:玩家所有信息
		    	playCardsLogic.bankerAvatar = avatar;
				playCardsLogic.setRoomVO(roomVO);
				playCardsLogic.setPlayerList(playerList);
    		}
	    }
    	
    	if(specialAvatar == null || specialAvatar.size() == 0){
    		if(roomVO.getInning()==1){
	    	for(int j = 0;j <playerList.size();j++){
	    		playerList.get(j).avatarVO.setHuReturnObjectVO(new HuReturnObjectVO());
	    		playerList.get(j).avatarVO.getHuReturnObjectVO().setBeforePaiArray(new int[]{33,33,33,33,33,33});
	    		roomVO.listCard=new ArrayList<Integer>();
	    		roomVO.nextCardindex=0;
	    		roomVO.remainListCard=new ArrayList<Integer>();
	    	}}
			//初始化等于是发牌，拆牌和比较牌的大小，是前端传过来的数据，这里不需要再写，这里包括了洗牌和发牌
			playCardsLogic.initCard_DEH(roomVO);
    	}else{
    		for(int i = 0;i < playerList.size();i++){
    			playerList.get(i).avatarVO.setPaiArray(specialPai.get(playerList.get(i).getUuId()));
    		}
    	}
    	try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	roomVO.setCardNum(roomVO.getCardNum()-1);
    	//发送发牌信息
    	for(int j = 0;j <playerList.size();j++){
    		playerList.get(j).getSession().sendMsg(new FaPaiResponse(1,playerList.get(j).avatarVO.getPaiArray(),roomVO));
        	playerList.get(j).avatarVO.setCurrentType(7);
    	}
    	fenPaiTimer();
    }
    
    /**
     * 前后端握手消息处理
     * @param avatar
     */
    public void shakeHandsMsg(Avatar avatar){
    	playCardsLogic.shakeHandsMsg(avatar);
    }
   /* *//**
     * 开始下一局前，玩家准备
     * @param avatar
     *//*
    public void readyNext(Avatar avatar){
    	playerList.get(playerList.indexOf(avatar)).avatarVO.setIsReady(true);
    	int hasReady = 0;
    	for (Avatar ava : playerList) {
			if(ava.avatarVO.getIsReady()){
				hasReady++;
			}
		}
    	if(hasReady == 4){
    		//如果四家人都准备好了
    		startGameRound();
    	}
    }*/
    /**
     * 断线重连，如果房间还未被解散的时候，则返回整个房间信息
     * @param avatar
     */
    public void returnBackAction(Avatar avatar , boolean isDissolve,int dissolveUuid){
    	
    	String dissolveName = "";
    	if(playCardsLogic == null){//只是在房间，游戏尚未开始,打牌逻辑为空
        	for (int i = 0; i < playerList.size(); i++) {
        		Avatar ava = playerList.get(i);
        		if(ava.getUuId() != avatar.getUuId()){
        			//给其他三个玩家返回重连用户信息
        			ava.getSession().sendMsg(new OtherBackLoginResonse(1, avatar.getUuId()+""));
        		}
        	}
        	avatar.getSession().sendMsg(new BackLoginResponse(1, roomVO));
    	}
    	else{
    		for (int i = 0; i < playerList.size(); i++) {
        		Avatar ava = playerList.get(i);
        		if(isDissolve && ava.getUuId() == dissolveUuid){
        			dissolveName = ava.avatarVO.getAccount().getNickname();
        		}
        	}
    		playCardsLogic.returnBackAction(avatar);
    	}
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//    	if(avatar.overOff){
//			//在某一句结算时断线，重连时返回结算信息
//			avatar.getSession().sendMsg(new HuPaiResponse(1,avatar.oneSettlementInfo));
//		}
    	if(isDissolve && dissolveUuid!=-1){ //有人申请解散房间
    		JSONObject json = new JSONObject();
    		json.put("type", "0");
    		json.put("uuid", dissolveUuid);
    		json.put("accountName", dissolveName);
    		avatar.getSession().sendMsg(new DissolveRoomResponse(avatar.avatarVO.getRoomId(), json.toString()));
    	}
    	
    }
    /**
	 * 文字/表情聊天
	 * @param avatar
	 */
	public void chatServer(String content){
		for (int i = 0; i < playerList.size(); i++) {
    		playerList.get(i).getSession().sendMsg(new ChatResponse(1, content));
		}
	}

    public RoomVO getRoomVO() {
        return roomVO;
    }

	public List<Avatar> getPlayerList() {
		return playerList;
	}

	public void setPlayerList(List<Avatar> playerList) {
		this.playerList = playerList;
	}

	public int getCount() {
		return count;
	}

	public boolean isDissolve() {
		return dissolve;
	}

	public void setDissolve(boolean dissolve) {
		this.dissolve = dissolve;
	}

	public void setDissolveCount(int dissolveCount) {
		this.dissolveCount = dissolveCount;
	}
	
	public int getDissolveUuid() {
		return dissolveUuid;
	}

	public void setDissolveUuid(int dissolveUuid) {
		this.dissolveUuid = dissolveUuid;
	}

	/**
	 * 断线重连返回最后操作信息
	 * @param avatar
	 */
	public void LoginReturnInfo(Avatar avatar){
		playCardsLogic.LoginReturnInfo(avatar);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(avatar.overOff){
			//在某一句结算时断线，重连时返回结算信息
			avatar.getSession().sendMsg(new HuPaiResponse(1,avatar.oneSettlementInfo));
		}
	}
	
	
	/**
	 * 解散房间，销毁房间逻辑,打牌逻辑
	 */
	public void destoryRoomLogic(){
		AvatarVO avatarVO;
		GameSession gamesession;
		JSONObject json  = new JSONObject();
		json.put("type","3");
		for (Avatar avat : playerList) {
//			playCardsLogic.getPlayerList().remove(avat);//9-22新增
			/*avatarVO = new AvatarVO();
			avatarVO.setAccount(avat.avatarVO.getAccount());
			avatarVO.setIP(avat.avatarVO.getIP());
			avat.getSession().sendMsg(new DissolveRoomResponse(1, json.toString()));
			gamesession = avat.getSession();
			avat = new Avatar();
			avat.avatarVO = avatarVO;
			gamesession.setRole(avat);
			gamesession.setLogin(true);
			avat.setSession(gamesession);
			avat.avatarVO.setIsOnLine(true);
			GameServerContext.add_onLine_Character(avat);*/
			isBegin = false;
			avatarVO = new AvatarVO();
			avatarVO.setAccount(avat.avatarVO.getAccount());
			avatarVO.setIP(avat.avatarVO.getIP());
			avatarVO.setLocation(avat.avatarVO.getLocation());
			avatarVO.setIsOnLine(avat.avatarVO.getIsOnLine());
			gamesession = avat.getSession();
			avat = new Avatar();
			avat.avatarVO = avatarVO;
			gamesession.setRole(avat);
			avat.setSession(gamesession);
			if(avat.avatarVO.getIsOnLine()){
				gamesession.setLogin(true);
				GameServerContext.add_onLine_Character(avat);
			}
			else{
			   //不在线则 更新
				GameServerContext.add_offLine_Character(avat);
			}
			avat.getSession().sendMsg(new DissolveRoomResponse(1, json.toString()));
			RoomManager.getInstance().removeUuidAndRoomId(avat.avatarVO.getAccount().getUuid(), roomVO.getRoomId());
		}
		hasDissolve = true;
		playCardsLogic = null;//9-22新增
		RoomManager.getInstance().destroyRoom(roomVO);
		//new RoomLogic(roomVO);
	}
	/**
	 * 房主退出房间，及解散房间，详细清除数据,销毁房间逻辑
	 * @param
	 */
	public void exitRoomDetail(JSONObject json){
		AvatarVO avatarVO;
		GameSession gamesession;
		for (Avatar avat : playerList) {
			//playCardsLogic.getPlayerList().remove(avat);//房主退出房间，打牌逻辑还未形成
			/*avatarVO = new AvatarVO();
			avatarVO.setAccount(avat.avatarVO.getAccount());
			gamesession = avat.getSession();
			avatarVO.setIP(avat.avatarVO.getIP());
			gamesession.sendMsg(new OutRoomResponse(1, json.toString()));
			avat = new Avatar();
			avat.avatarVO = avatarVO;
			gamesession.setRole(avat);
			gamesession.setLogin(true);
			avat.setSession(gamesession);
			avat.avatarVO.setIsOnLine(true);
			GameServerContext.add_onLine_Character(avat);*/
			isBegin = false;
			avatarVO = new AvatarVO();
			avatarVO.setAccount(avat.avatarVO.getAccount());
			avatarVO.setIP(avat.avatarVO.getIP());
			avatarVO.setLocation(avat.avatarVO.getLocation());
			avatarVO.setIsOnLine(avat.avatarVO.getIsOnLine());
			gamesession = avat.getSession();
			avat = new Avatar();
			avat.avatarVO = avatarVO;
			gamesession.setRole(avat);
			avat.setSession(gamesession);
			if(avat.avatarVO.getIsOnLine()){
				gamesession.setLogin(true);
				GameServerContext.add_onLine_Character(avat);
			}
			else{
			   //不在线则 更新
				//if(GameServerContext.getAvatarFromOff(avat.getUuId()) != null){
					GameServerContext.add_offLine_Character(avat);
				//}
			}
			avat.getSession().sendMsg(new OutRoomResponse(1, json.toString()));
			RoomManager.getInstance().removeUuidAndRoomId(avat.avatarVO.getAccount().getUuid(), roomVO.getRoomId());
		}
		hasDissolve = true;
		playCardsLogic = null;//9-22新增
		RoomManager.getInstance().destroyRoom(roomVO);
	}
	/**
	 * 房主外的玩家退出房间，详细清除单个数据
	 * @param avatar
	 */
	public void exitRoomDetail(Avatar avatar ,JSONObject json){
		
		for (int i= 0 ; i < playerList.size(); i++) {
    		//通知房间里面的其他玩家
			playerList.get(i).getSession().sendMsg(new OutRoomResponse(1, json.toString()));
    	}
		roomVO.getPlayerList().remove(avatar.avatarVO);
		playerList.remove(avatar);
		//playCardsLogic.getPlayerList().remove(avatar);//只有打牌逻辑为空的时候才有退出房间一说，其他都是解散房间
		//isBegin = false;
		AvatarVO avatarVO;
		GameSession gamesession;
		avatarVO = new AvatarVO();
		avatarVO.setIP(avatar.avatarVO.getIP());
		avatarVO.setLocation(avatar.avatarVO.getLocation());
		avatarVO.setAccount(avatar.avatarVO.getAccount());
		gamesession = avatar.getSession();
		avatar = new Avatar();
		avatar.avatarVO = avatarVO;
		gamesession.setRole(avatar);
		gamesession.setLogin(true);
		avatar.setSession(gamesession);
		avatar.avatarVO.setIsOnLine(true);
		GameServerContext.add_onLine_Character(avatar);
		RoomManager.getInstance().removeUuidAndRoomId(avatar.avatarVO.getAccount().getUuid(), roomVO.getRoomId());
	}

	public List<Integer> getStandingsDetailsIds() {
		return standingsDetailsIds;
	}

	public void setStandingsDetailsIds(List<Integer> standingsDetailsIds) {
		this.standingsDetailsIds = standingsDetailsIds;
	}

	public PlayCardsLogic getPlayCardsLogic() {
		return playCardsLogic;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setHasDissolve(boolean hasDissolve) {
		this.hasDissolve = hasDissolve;
	}
	
	
	
	
	//游戏规则选择倒计时
    public void wayTimer(){
    	for(int i = 0;i < playerList.size();i++){
    		//当前操作
    		playerList.get(i).avatarVO.setCurrentType(5);
    	}
    	//type：0抢庄，1下注，2游戏选择；通知前段开始倒计时
		for(int i = 0;i < playerList.size();i++)
		{
			playerList.get(i).getSession().sendMsg(new DaoJishiResponse(1, 2,15));   
		}
//    	System.out.println("调用游戏规则选择倒计时");
//    	time = 16;
    	timer = new Timer();
    	TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
//				time--;
				if(timer != null){
					timer.cancel();//这里时间定时结束以后要判断是否下注结束，下注结束以后开始游戏
					timer = null;
					for(int i = 0;i < playerList.size();i++){
						//判断如果是庄家，或者没有选择游戏规则时，默认选直比
						if(!playerList.get(i).avatarVO.isMain() && playerList.get(i).avatarVO.getWay() == 0){
							playerList.get(i).avatarVO.setWay(3);
						}
					}
//					System.out.println("游戏规则选择完成");
					xiaZhuTimer();
				}
//				System.out.println("游戏规则选择当前倒计时" + time);
		    	
			}
		};
		//定时执行
		timer.schedule(timerTask,1000*16);
//		timer.schedule(timerTask,0,1000*16);
    }
	
    /**
     * 下注倒计时
     */
    public void xiaZhuTimer(){
    	//当前操作
    	for(int i = 0;i < playerList.size();i++){
			playerList.get(i).avatarVO.setCurrentType(6);
		}
    	//type：0抢庄，1下注，2游戏选择；通知前段开始倒计时
		for(int i = 0;i < playerList.size();i++)
		{
			playerList.get(i).getSession().sendMsg(new DaoJishiResponse(1, 1,15));  
		 }
//    	time = 16;
    	timer = new Timer();
    	TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				
//				time--;
				if(timer != null){
					timer.cancel();//这里时间定时结束以后要判断是否下注结束，下注结束以后开始游戏
					timer = null;
					jiaGuostartGameRound();
				}
				
			}
		};
		//定时执行
		timer.schedule(timerTask,1000*16);
//		timer.schedule(timerTask,0,1000*16);
    }
    
    /**
     * 分牌倒计时
     * 倒计时完后如果还有玩家没有完成分牌，那程序随机分配
     */
    public void fenPaiTimer(){
    	timer = new Timer();
    	time = 7;
    	TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				time--;
				if(timer != null && time == 0){
					//这里时间定时结束以后要判断是否下注结束，下注结束以后开始游戏,当时间小于等于0的时候，停止
					timer.cancel();
					timer = null;
					Avatar avatar;
					for(int i = 0;i < playerList.size();i++){
						avatar = playerList.get(i);
						if(avatar.avatarVO.getCurrentType() == 7){
							String str = "";
							int[] pai = avatar.avatarVO.getPaiArray()[0];
							for(int j = 0;j < pai.length;j++){
								if(pai[j] > 0){
									str += j+",";
								}
							}
							str += 1+",";
							disassemble(avatar,str);
						}
					}
				}
				else if(time == 4){
					for(int i = 0;i < playerList.size();i++){
						if(playerList.get(i).avatarVO.getCurrentType() == 7){
							try {
								playerList.get(i).getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000036));
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		};
		//定时执行
		timer.schedule(timerTask,0,1000);
    
    }
    
    /**
	 * 拆牌后传入
	 * @param avatar
	 * @param str 传入字符串  构造，拆分后的牌  1,2,3,4（12组合，34组合  不分大小）
	 */
	public void disassemble(Avatar avatar,String str){
		
		int flag = 0;
		for(int i = 0;i < playerList.size();i++){
			if(playerList.get(i).avatarVO.getCurrentType() == 7){
				flag++;
			}
		}
		if(flag == 1 && avatar.avatarVO.getCurrentType() == 7 && timer != null){
			timer.cancel();
			timer = null;
		}
		//没有分牌
		if(avatar.avatarVO.getCurrentType() == 7){
			playCardsLogic.disassemble(avatar,str);
		}
		//已经分牌，或系统自动分牌
		else{
			try {
				avatar.getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000035));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 *  1：开始第一局时选择簸簸数  暂时不管  下注那里
	 *  2:  中途簸簸数用完时，添加簸簸数
	 * @param avatar
	 * @param addDustpanCount
	 */
	public String addDustpan(Avatar avatar,int addDustpanCount){
		String code ;
		int total = addDustpanCount + avatar.avatarVO.getTotalDustpan();
		if(total <= roomVO.getDustpan()){
			if(avatar.avatarVO.getTotalDustpan() == 0){
				//开始的时候设置初始簸簸数
				avatar.avatarVO.setTotalDustpan(addDustpanCount);
				avatar.avatarVO.updateDustpan(addDustpanCount);
				code = playerList.indexOf(avatar)+","+addDustpanCount;
			}
			else{
				//中途加簸簸数，需要对比总数不能大于房间设定的簸簸数
				avatar.avatarVO.setTotalDustpan(addDustpanCount + avatar.avatarVO.getTotalDustpan());
				avatar.avatarVO.updateDustpan(addDustpanCount );
				code = playerList.indexOf(avatar)+","+addDustpanCount;
			}
		}
		else{
			code = playerList.indexOf(avatar)+","+0;
		}
		return code;
	}
	//***************丁二红的请求
	/**
	 * 跟  大   敲   休   丢的请求  (跟(score等于本轮下注数量(playlogic里面属性)),休（只有第一叫的玩家可以休）,丢不需要传score为0)
	 * @param type
	 * @param score
	 */
	public void propaganda(Avatar avatar,int type , int score){
		
		playCardsLogic.propaganda(avatar,type,score);
	}
	/**
     * 抢庄确认
     * @param avatar
     * @param type
     * 0.不抢
     * 1.抢
     */
    public void qiangZhuangAffirm(Avatar avatar,String type){
    	//抢庄人数为0
    	qiangZhuang--;
    	if(type.equals("0")){
    		//type=0的时候：当前人放弃抢庄
    		avatar.avatarVO.setQiangZhuang(false);
			//当前操作
    		avatar.avatarVO.setCurrentType(4);
    	}
    	else if(type.equals("1")){
    		//抢庄资格
    		avatar.avatarVO.setQiangZhuang(false);
			//当前操作
    		avatar.avatarVO.setCurrentType(3);
    		qiangZhuangList.add(avatar);
    	}
    	//向所有人发生抢庄的消息：点击抢庄，告诉所有人抢庄的结果：抢了，还是没抢
    	for(int i =0;i < playerList.size();i++)
		{
			playerList.get(i).getSession().sendMsg(new IsQiangZhuangResponse(1, type,avatar.avatarVO.getAccount().getUuid()));
		}
    	
    	
    	if(qiangZhuang <= 0 && qiangZhuangList.size() > 0){
    		//就是随机打乱原来的顺序，和洗牌一样。
    		Collections.shuffle(qiangZhuangList);
    		Collections.shuffle(qiangZhuangList);
    		//找到第一个庄家
    		playCardsLogic.bankerAvatar = qiangZhuangList.get(0);
    		for(int i = 0;i < playerList.size();i++){
    			playerList.get(i).avatarVO.setMain(false);
    		}
    		playCardsLogic.bankerAvatar.avatarVO.setMain(true);
    		//发送抢庄成功的信息
    		qiangZhuangList.get(0).getSession().sendMsg(new QiangZhuangSucceedResponse(1));
    		//向所有人发送庄家确认信息
			for(int i =0;i < playerList.size();i++)
			{
				playerList.get(i).getSession().sendMsg(new ZhuangJiaQueRenRepsonse(1, playCardsLogic.bankerAvatar.getUuId()));
			}
			if(timer != null){
				timer.cancel();
				timer = null;
			}
			
//			System.out.println("抢庄完成");
			
			//游戏选择倒计时
			wayTimer();
	    
    	}
    	//当抢庄人数为0或者抢庄人数个数为0的时候
    	else if(qiangZhuang <= 0 && qiangZhuangList.size() == 0)
    	{
    		for(int i = 0;i < playerList.size();i++){
    			try {
    				
					playerList.get(i).getSession().sendMsg(new ErrorResponse(ErrorCode.Error_000031));
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    		try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		if(timer != null){
    			timer.cancel();
        		timer = null;
    		}
    		//解散房间
    		destoryRoomLogic();
    		
    	}
    }
	/**
	 * 断线重连返回最后操作信息___丁二红
	 * @param avatar
	 */
	public void LoginReturnInfo_DEH(Avatar avatar){
		
		if(playCardsLogic != null){
			playCardsLogic.LoginReturnInfo_DEH(avatar);
		}
	}
	 /**
     * 断线重连，如果房间还未被解散的时候，则返回整个房间信息
     * @param avatar
     */
    public void returnBackAction(Avatar avatar){
    	if(playCardsLogic == null){
        		//只是在房间，游戏尚未开始,打牌逻辑为空
        	for (int i = 0; i < playerList.size(); i++) {
        		if(playerList.get(i).getUuId() != avatar.getUuId()){
        			//给其他三个玩家返回重连用户信息
        			playerList.get(i).getSession().sendMsg(new OtherBackLoginResonse(1, avatar.getUuId()+""));
        		}
        	}
        	//发送返回登录的信息
        	avatar.getSession().sendMsg(new BackLoginResponse(1, roomVO));
    	   }
    	else{
    		// 玩家玩游戏时断线重连
    		playCardsLogic.returnBackAction(avatar);
    	}
    	//断线重连后检查是否有人申请解散房间，如果有就拒绝***20161026
    	if(!dissolve && dissAvatar != null && !dissAvatar.contains(avatar)){
    		//"2"表示不同意解散房间
    		dissolveRoom(avatar,avatar.avatarVO.getRoomId(),"2");
    	}
    	//断线重连后判断是否要提示庄家关于是否坐庄的提醒  当前玩家的操作  0代表准备
    	boolean flag = true;
    	for(int i = 0;i < playerList.size();i++){
    		if(playerList.get(i).avatarVO.getCurrentType() != 0){
    			flag = false;
    		}
    	}
    	if(avatar.avatarVO.isMain() && flag && count > 0){
    		avatar.getSession().sendMsg(new WhetherContinueBenkerResponse(1));
    	}
    	if(avatar.avatarVO.getAccount().getIsGame().equals("9")){
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    	JSONObject jsonObject = null;
			JSONArray jsonArray = new JSONArray();
			Avatar ava;
			for(Entry<Integer, int[][]> entry : specialPai.entrySet()){
				jsonObject = new JSONObject();
				jsonObject.put("uuid", entry.getKey());
				jsonObject.put("pai", entry.getValue()[0]);
				jsonArray.add(jsonObject);
			}
			jsonObject = new JSONObject();
			jsonObject.put("paiArray", jsonArray);
			for(int i = 0;i < specialAvatar.size();i++){
				specialAvatar.get(i).getSession().sendMsg(new SpecialPai(1, jsonObject.toString()));
			}
    	}
    	
//    	if(avatar.getRoomVO() != null && avatar.avatarVO.getRoomId() != 0 && !dissolve){
//    		dissolveRoom(avatar,avatar.avatarVO.getRoomId(),"2");
//    	}
    }
}
