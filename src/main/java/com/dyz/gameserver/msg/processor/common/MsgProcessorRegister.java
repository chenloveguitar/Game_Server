package com.dyz.gameserver.msg.processor.common;

import com.context.ConnectAPI;
import com.dyz.gameserver.msg.processor.banker.GoOnZuoZhuangMsgProcssor;
import com.dyz.gameserver.msg.processor.banker.QiangZhuangMsgProcssor;
import com.dyz.gameserver.msg.processor.chat.ChatMsgProcessor;
import com.dyz.gameserver.msg.processor.chi.ChiMsgProcessor;
import com.dyz.gameserver.msg.processor.chupai.ChuPaiMsgProcessor;
import com.dyz.gameserver.msg.processor.contact.ContactMsgProcssor;
import com.dyz.gameserver.msg.processor.createroom.CreateRoomMsgProcssor;
import com.dyz.gameserver.msg.processor.dingerhong.DisassembleProcessor;
import com.dyz.gameserver.msg.processor.dingerhong.GameTypeProcessor;
import com.dyz.gameserver.msg.processor.dingerhong.XiaZhuProcessor;
import com.dyz.gameserver.msg.processor.draw.DrawProcessor;
import com.dyz.gameserver.msg.processor.gang.GangMsgProcessor;
import com.dyz.gameserver.msg.processor.heartbeat.HeadMsgProcessor;
import com.dyz.gameserver.msg.processor.host.ChongZhiProcessor;
import com.dyz.gameserver.msg.processor.host.HostNoitceProcessor;
import com.dyz.gameserver.msg.processor.host.IndexInfosProcessor;
import com.dyz.gameserver.msg.processor.hu.HuPaiMsgProcessor;
import com.dyz.gameserver.msg.processor.joinroom.GetRoomMsgProcessor;
import com.dyz.gameserver.msg.processor.joinroom.JoinRoomMsgProcessor;
import com.dyz.gameserver.msg.processor.location.LocationMsgProcessor;
import com.dyz.gameserver.msg.processor.login.LoginMsgProcessor;
import com.dyz.gameserver.msg.processor.login.LoginReturnInfoMsgProcessor;
import com.dyz.gameserver.msg.processor.login.LogoutMsgProcessor;
import com.dyz.gameserver.msg.processor.login.OpenAppMsgProcessor;
import com.dyz.gameserver.msg.processor.login.SignUpMsgProcessor;
import com.dyz.gameserver.msg.processor.messageBox.MessageBoxMsgProcessor;
import com.dyz.gameserver.msg.processor.methodCount.MethodCount;
import com.dyz.gameserver.msg.processor.outroom.ConstraintOutRoom;
import com.dyz.gameserver.msg.processor.outroom.DissolveRoomMsgProcessor;
import com.dyz.gameserver.msg.processor.outroom.OutRoomMsgProcessor;
import com.dyz.gameserver.msg.processor.pass.GaveUpMsgProcessor;
import com.dyz.gameserver.msg.processor.peng.PengMsgProcessor;
import com.dyz.gameserver.msg.processor.pickcard.PickCardMsgProcessor;
import com.dyz.gameserver.msg.processor.playrecord.PlayRecordMsgProcessor;
import com.dyz.gameserver.msg.processor.remotecontrol.RemoteControlProcessor;
import com.dyz.gameserver.msg.processor.standings.StandingsMsgProcessor;
import com.dyz.gameserver.msg.processor.standings.StandingsMsgProcessorSearch;
import com.dyz.gameserver.msg.processor.startgame.PrepareGameMSGProcessor;
import com.dyz.gameserver.msg.processor.ting.TingPaiMsgProcessor;


/**
 * 消息处理器注册类，所有的消息处理器，都在此注册实例化
 * @author dyz
 *
 */
public enum MsgProcessorRegister {
	/**用户打开app*/
	openApp(ConnectAPI.OPENAPP_REQUEST,new OpenAppMsgProcessor()),
	/**登陆处理器*//**断线重连**/
	login(ConnectAPI.LOGIN_REQUEST,new LoginMsgProcessor()),
	/**登陆处理器*//**断线重连**/
	loginReturnInfo(ConnectAPI.RETURN_ONLINE_REQUEST,new LoginReturnInfoMsgProcessor()),
	/**用户注册处理器*/
	signUp(ConnectAPI.SIGNUP_REQUEST,new SignUpMsgProcessor()),
	/**创建 房间*/
	createRoom(ConnectAPI.CREATEROOM_REQUEST,new CreateRoomMsgProcssor()),
	/**进入游戏房间*/
	joinRoom(ConnectAPI.JOIN_ROOM_REQUEST,new JoinRoomMsgProcessor()),
	/**摸牌*/
	pickPai(ConnectAPI.PICKCARD_REQUEST,new PickCardMsgProcessor()),
	/**出牌*/
	chuPai(ConnectAPI.CHUPAI_REQUEST,new ChuPaiMsgProcessor()),
	/**退出房间*/
	outRoom(ConnectAPI.OUT_ROOM_REQUEST,new OutRoomMsgProcessor()),
	/**申请解散房间*/
	dissolveRoom(ConnectAPI.DISSOLVE_ROOM_REQUEST,new DissolveRoomMsgProcessor()),
	/**吃牌*/
	chiPai(ConnectAPI.CHIPAI_REQUEST,new ChiMsgProcessor()),
	/**碰牌*/
	pengPai(ConnectAPI.PENGPAI_REQUEST,new PengMsgProcessor()),
	/**缸牌*/
	gangPai(ConnectAPI.GANGPAI_REQUEST,new GangMsgProcessor()),
	/**放弃操作*/
	gaveUp(ConnectAPI.GAVEUP_REQUEST,new GaveUpMsgProcessor()),
	/*胡牌**/
	hupai(ConnectAPI.HUPAI_REQUEST,new HuPaiMsgProcessor()),
	/**与前段握手*/
	successRerunMsg(ConnectAPI.SUCCESS_RETURN_MSG_RESPONSE,new SuccessReturnMsgProcessor()),
	/**游戏开始前准备*/
	prepareGame(ConnectAPI.PrepareGame_MSG_REQUEST,new PrepareGameMSGProcessor()),
	/**退出游戏*/
	loginOutGame(ConnectAPI.LOGINOUTGAME_MSG_REQUEST,new LogoutMsgProcessor()),
	
	messageBox(ConnectAPI.MessageBox_Request,new MessageBoxMsgProcessor()),
	/**心跳协议*/
	head(ConnectAPI.head,new HeadMsgProcessor()),
	/**抽奖*/
	draw(ConnectAPI.DRAw_REQUEST,new DrawProcessor()),
	/**后台发送消息*/
	hostSendMessage(ConnectAPI.HOST_SEND_REQUEST,new HostNoitceProcessor()),
	/**后台发送消息获取首页所有信息(当前在线任务，当前房间数量等等)*/
	indexInfosMessage(ConnectAPI.HOST_INDEXINFOS_REQUEST,new IndexInfosProcessor()),
	/**后台强制解散房间*/
	constraintOutRoom(ConnectAPI.HOST_INDOUTROOM_REQUEST,new ConstraintOutRoom()),
	/**前段请求发布的充卡联系人信息*/
	contactMessage(ConnectAPI.HOST_ADDROOMCARD_REQUEST,new ContactMsgProcssor()),
	/**远程请求*/
	romoteControlMessage(ConnectAPI.HOST_ROMOTECONTROL_REQUEST,new RemoteControlProcessor()),
	/**战绩请求*/
	standingsMessage(ConnectAPI.MSG_STANDINGS_REQUEST,new StandingsMsgProcessor()),
	/**room战绩请求*/
	standingsMessageRoom(ConnectAPI.MSG_STANDINGSSEAREH_REQUEST,new StandingsMsgProcessorSearch()),
	/**其他关闭请求*/
	methodCount(ConnectAPI.HOST_MethodCount,new MethodCount()),
	/**文字/表情*/
	chat(ConnectAPI.CHAT_REQUEST,new ChatMsgProcessor()),
	/**游戏回放*/
	PlayRecordMessage(ConnectAPI.PLAYRECORD_REQUEST,new PlayRecordMsgProcessor()),
	/**躺牌请求*/
	layCards_Request(ConnectAPI.LAYCARDS_REQUEST,new TingPaiMsgProcessor()),

	/**位置数据上传*/
	Location(ConnectAPI.SEND_REQUEST,new LocationMsgProcessor()),
	
	//吉吉牌九
		/**庄家继续坐庄*/
		GoOnZuoZhuang(ConnectAPI.PJ_ZUOZHUANG_REQUEST,new GoOnZuoZhuangMsgProcssor()),
		/**抢庄确认*/
		QiangZhuang(ConnectAPI.PJ_QIANGZHUANG_REQUEST,new QiangZhuangMsgProcssor()),
		/**获得场次信息*/
		GetRoom(ConnectAPI.PJ_GETROOM_REQUEST,new GetRoomMsgProcessor()),
		/**发送下注消息*/
		XiaZhu(ConnectAPI.PJ_XIAZHUSUCCEED_REQUEST,new XiaZhuProcessor()),
		/**充值检测*/
		ChongZhi(ConnectAPI.HOST_CHONGZHI_REQUEST,new ChongZhiProcessor()),
		/**传入前端拆分过后的牌组*/
		DisassembleMessage(ConnectAPI.DISASSEMBLE_REQUEST,new DisassembleProcessor()),

		/**
		 * 游戏规则
		 */
		GameType(ConnectAPI.PJ_GAMETYPE_REQUEST,new GameTypeProcessor());
	
	
	private int msgCode;
	private MsgProcessor processor;

	/**
	 * 不允许外部创建
	 * @param msgCode
	 * @param processor
     */
	private MsgProcessorRegister(int msgCode,MsgProcessor processor){
		this.msgCode = msgCode;
		this.processor = processor;
	}

	/**
	 * 获取协议号
	 * @return
     */
	public int getMsgCode(){
		return this.msgCode;
	}

	/**
	 * 获取对应的协议解晰类对象
	 * @return
     */
	public MsgProcessor getMsgProcessor(){
		return this.processor;
	}
}
