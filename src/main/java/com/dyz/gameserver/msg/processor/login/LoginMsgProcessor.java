package com.dyz.gameserver.msg.processor.login;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.mina.core.session.IoSession;

import com.context.ErrorCode;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.initial.Params;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.context.GameServerContext;
import com.dyz.gameserver.logic.RoomLogic;
import com.dyz.gameserver.manager.GameSessionManager;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;
import com.dyz.gameserver.msg.response.ErrorResponse;
import com.dyz.gameserver.msg.response.banker.DanJuResponse;
import com.dyz.gameserver.msg.response.host.HostNoitceResponse;
import com.dyz.gameserver.msg.response.login.LoginResponse;
import com.dyz.gameserver.pojo.AvatarVO;
import com.dyz.gameserver.pojo.LoginVO;
import com.dyz.myBatis.model.Account;
import com.dyz.myBatis.model.NoticeTable;
import com.dyz.myBatis.services.AccountService;
import com.dyz.myBatis.services.NoticeTableService;
import com.dyz.persist.util.JsonUtilTool;
import com.dyz.persist.util.TimeUitl;

/**
 * 登录
 * 
 * @author Administrator
 *
 */
public class LoginMsgProcessor extends MsgProcessor implements INotAuthProcessor {

	@Override
	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		String message = request.getString();
		LoginVO loginVO = JsonUtilTool.fromJson(message, LoginVO.class);
		Account account = AccountService.getInstance().selectAccount(loginVO.getOpenId());
		if (account != null && account.getStatus().equals("1")) {
			gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000024));
			return;
		} 
		/**
		 * TODO 判断用户是否在其他终端中已经登录了游戏,如果登录了,让其他终端的玩家下线,或是不允许重复登录
		 * 实现思路:(不知道项目中会不会有这样子的需求)
		 * 大概是将已登录的用户存放在一个map中,key为用户对象,value为ioSession,
		 * 如果map中已经存在该用户,则通过ioSession对象关闭连接,为当前的用户建立连接
		 */
		
		if (account == null) {
			// 创建新用户并登录
			account = new Account();
			account.setOpenid(loginVO.getOpenId());
			account.setUuid(AccountService.getInstance().selectMaxId() + 100000);
			account.setRoomcard(Params.initialRoomCard);
			//account.setRoomcard(99999); 测试用房卡
			account.setHeadicon(loginVO.getHeadIcon());
			account.setNickname(loginVO.getNickName());
			account.setCity(loginVO.getCity());
			account.setProvince(loginVO.getProvince());
			account.setSex(loginVO.getSex());
			account.setUnionid(loginVO.getUnionid());
			account.setPrizecount(Params.initialPrizeCount);
			account.setCreatetime(new Date());
			account.setActualcard(Params.initialRoomCard);
			account.setTotalcard(Params.initialRoomCard);
			account.setStatus("0");
			account.setIsGame("0");

			if (AccountService.getInstance().createAccount(account) == 0) {
				account = AccountService.getInstance().selectAccount(loginVO.getOpenId());
				if (account == null) {
					gameSession.sendMsg(new LoginResponse(0, null));
					TimeUitl.delayDestroy(gameSession, 1000);
				} else {
					System.out.println("登录出现异常!");
				}
			} else {
				Avatar tempAva = new Avatar();	
				AvatarVO tempAvaVo = new AvatarVO();
				tempAvaVo.setAccount(account);
				tempAvaVo.setIP(loginVO.getIP());
				tempAvaVo.setIsOnLine(true);
				tempAva.avatarVO = tempAvaVo;

				loginAction(gameSession, tempAva);
				// 把session放入到GameSessionManager
				// GameSessionManager.getInstance().putGameSessionInHashMap(gameSession,tempAva.getUuId());
				GameSessionManager.getInstance().sessionMap.put("uuid_" + account.getUuid(), gameSession);
				// 公告发送给玩家
				Thread.sleep(3000);
				NoticeTable notice = null;
				try {
					notice = NoticeTableService.getInstance().selectRecentlyObject();
				} catch (Exception e) {
					e.printStackTrace();
				}
				String content = notice.getContent();
				gameSession.sendMsg(new HostNoitceResponse(1, content));
			}
		} else {
			// 如果玩家是掉线的，则直接从缓存(GameServerContext)中取掉线玩家的信息
			// 判断用户是否已经进行断线处理(如果前端断线时间过短，后台则可能还未来得及把用户信息放入到离线map里面，就已经登录了，所以取出来就会是空)
//			Thread.sleep(1000);
			Avatar avatar = GameServerContext.getAvatarFromOn(account.getUuid());
			if (avatar == null) {
				avatar = GameServerContext.getAvatarFromOff(account.getUuid());
			}
			if (avatar == null) {
				GameSession gamesession = GameSessionManager.getInstance().getAvatarByUuid("uuid_" + account.getUuid());
				if (gamesession != null) {
					avatar = gamesession.getRole(Avatar.class);
				}
			}
			if (avatar == null) {
				// 判断微信昵称是否修改过，若修改过昵称，则更新数据库信息
				if (!loginVO.getNickName().equals(account.getNickname())) {
					account.setNickname(loginVO.getNickName());
					AccountService.getInstance().updateByPrimaryKeySelective(account);
				}
				// 断线超过时间后，自动退出
				avatar = new Avatar();
				AvatarVO avatarVO = new AvatarVO();
				avatarVO.setAccount(account);
				avatarVO.setIP(loginVO.getIP());
				avatarVO.setIsOnLine(true);
				avatar.avatarVO = avatarVO;
				// 把session放入到GameSessionManager
				// TimeUitl.stopAndDestroyTimer(avatar);
				loginAction(gameSession, avatar);
				// GameSessionManager.getInstance().putGameSessionInHashMap(gameSession,avatar.getUuId());
				GameSessionManager.getInstance().sessionMap.put("uuid_" + account.getUuid(), gameSession);
				GameSessionManager.getInstance().updateTopOnlineAccountCount();
//				Thread.sleep(3000);
				// 公告发送给玩家
				NoticeTable notice = null;
				try {
					notice = NoticeTableService.getInstance().selectRecentlyObject();
				} catch (Exception e) {
					e.printStackTrace();
				}
				String content = notice.getContent();
				gameSession.sendMsg(new HostNoitceResponse(1, content));
			} else {
				// 断线重连
				if (avatar.avatarVO.getIsOnLine()) {
					System.out.println("断线2");
					Avatar avatars = new Avatar();
					avatars.avatarVO = new AvatarVO();
					avatars.avatarVO.setAccount(account);
					avatars.avatarVO.setIsOnLine(false);
					try {
						GameSessionManager.getInstance().sessionMap.put("uuid_" + account.getUuid(), gameSession)
								.setRole(avatars);
					} catch (Exception e) {
						System.out.println(
								"GameSessionManager.getInstance().sessionMap.put(uuid_+account.getUuid(),gameSession).setRole(avatars)------------为空");
					}
				}

				avatar.avatarVO.setIsOnLine(true);
				avatar.avatarVO.setAccount(account);
				avatar.avatarVO.setIP(loginVO.getIP());
				avatar.setLogOut(false);
				TimeUitl.stopAndDestroyTimer(avatar);
				avatar.setSession(gameSession);
				// system.out.println("用户回来了，断线重连，中止计时器");
				// 返回用户断线前的房间信息******
				gameSession.setLogin(true);
				gameSession.setRole(avatar);
				GameServerContext.add_onLine_Character(avatar);
				GameServerContext.remove_offLine_Character(avatar);
				returnBackAction(gameSession, avatar);
				// 把session放入到GameSessionManager,并且移除以前的session
				// GameSessionManager.getInstance().putGameSessionInHashMap(gameSession,avatar.getUuId());
				GameSessionManager.getInstance().sessionMap.put("uuid_" + account.getUuid(), gameSession);
				GameSessionManager.getInstance().updateTopOnlineAccountCount();
				// 公告发送给玩家
				Thread.sleep(3000);
				NoticeTable notice = null;
				try {
					notice = NoticeTableService.getInstance().selectRecentlyObject();
				} catch (Exception e) {
					e.printStackTrace();
				}
				String content = notice.getContent();
				gameSession.sendMsg(new HostNoitceResponse(1, content));

			}
		}
		System.out.println(account.getUuid() + "  :登录游戏");
	}

	/**
	 * 登录操作
	 * 
	 * @param gameSession
	 * @param avatar
	 */
	public void loginAction(GameSession gameSession, Avatar avatar) {
		gameSession.setRole(avatar);
		gameSession.setLogin(true);
		avatar.setSession(gameSession);
		GameServerContext.add_onLine_Character(avatar);
		GameServerContext.remove_offLine_Character(avatar);
		gameSession.sendMsg(new LoginResponse(1, avatar.avatarVO));
	}

	public void paijiureturn(GameSession gameSession, Avatar avatar) {

		if (avatar.avatarVO.getRoomId() != 0) {
			RoomLogic roomLogic = RoomManager.getInstance().getRoom(avatar.avatarVO.getRoomId());
			//&&roomLogic.getRoomVO().getRoomType() == 4
			if (roomLogic != null) {
					// 如果用户是在玩游戏/在房间的时候断线，且返回时房间还未被解散，则需要返回游戏房间其他用户信息，牌组信息
					roomLogic.returnBackAction(avatar);
					try {
						Thread.sleep(2500);
						if (avatar.overOff) {
							avatar.getSession().sendMsg(new DanJuResponse(1, avatar.oneSettlementInfo));
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}else{
				GameSession gamesession = avatar.getSession();
				GameServerContext.add_onLine_Character(avatar);
				gamesession.sendMsg(new LoginResponse(1, avatar.avatarVO));
			}
		} else {
			// 如果不是在游戏时断线，则直接返回个人用户信息avatar
			avatar.getSession().sendMsg(new LoginResponse(1, avatar.avatarVO));
		}

	}

	/**
	 * 玩家断线重连操作
	 * 
	 * @param
	 * @param avatar
	 */
	public void returnBackAction(GameSession gameSession, Avatar avatar) {
		paijiureturn(gameSession, avatar);

		if (avatar.avatarVO.getRoomId() == 0) {
			avatar.avatarVO.setRoomId(RoomManager.getInstance().passIdGetRoom(avatar.getUuId()));
			if (avatar.avatarVO.getRoomId() != 0 && avatar.getRoomVO() == null) {
				System.out.println("session改变，重新设置session");
				RoomLogic roomLogic = RoomManager.getInstance().getRoom(avatar.avatarVO.getRoomId());
				if (roomLogic != null) {
					List<Avatar> playList = roomLogic.getPlayerList();
					avatar.setRoomVO(roomLogic.getRoomVO());
					for (int i = 0; i < playList.size(); i++) {
						if (avatar.getUuId() == playList.get(i).getUuId()) {
							AvatarVO avatarVO = playList.get(i).avatarVO;
							avatarVO.setAccount(avatar.avatarVO.getAccount());
							avatarVO.setIP(avatar.avatarVO.getIP());
							avatarVO.setIsOnLine(avatar.avatarVO.getIsOnLine());
							avatar.pengQuest = playList.get(i).pengQuest;
							avatar.gangQuest = playList.get(i).gangQuest;
							avatar.huQuest = playList.get(i).huQuest;
							avatar.qiangHu = playList.get(i).qiangHu;
							avatar.overOff = playList.get(i).overOff;
							avatar.oneSettlementInfo = playList.get(i).oneSettlementInfo;
							avatar.gangIndex = playList.get(i).gangIndex;
							avatar.huAvatarDetailInfo = playList.get(i).huAvatarDetailInfo;
							avatar.setRoomVO(playList.get(i).getRoomVO());
							avatar.setResultRelation(playList.get(i).getResultRelation());
							// avatar = playList.get(i);
							// gameSession.setRole(avatar);
							avatar.gangIndex = playList.get(i).gangIndex;
							avatar.avatarVO = avatarVO;
							avatar.setSession(gameSession);
							playList.set(i, avatar);
							setAvatarLIst(roomLogic, avatar, i);
						}
					}
				}
			}
		}
		if (avatar.avatarVO.getRoomId() != 0) {
			RoomLogic roomLogic = RoomManager.getInstance().getRoom(avatar.avatarVO.getRoomId());
			if (roomLogic != null) {
				// setAvatarLIst(roomLogic,avatar,roomLogic.getPlayerList().indexOf(avatar));
				// 如果用户是在玩游戏/在房间的时候断线，且返回时房间还未被解散，则需要返回游戏房间其他用户信息，牌组信息
				roomLogic.returnBackAction(avatar, !roomLogic.isDissolve(), roomLogic.getDissolveUuid());
				try {
					Thread.sleep(1000);
					/*
					 * if(!roomLogic.isDissolve()){ //有人申请解散房间 JSONObject json =
					 * new JSONObject(); json.put("type", "0"); json.put("uuid",
					 * avatar.getUuId()); json.put("accountName",
					 * avatar.avatarVO.getAccount().getNickname());
					 * gameSession.sendMsg(new
					 * DissolveRoomResponse(roomLogic.getRoomVO().getRoomId(),
					 * json.toString())); }
					 */
					/*
					 * if(avatar.overOff){ //在某一句结算时断线，重连时返回结算信息
					 * avatar.getSession().sendMsg(new
					 * HuPaiResponse(1,avatar.oneSettlementInfo)); }
					 */
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				// 如果是在游戏时断线,但是返回的时候，游戏房间已经被解散，则移除该用户的房间信息
				AvatarVO avatarVO = new AvatarVO();
				avatarVO.setAccount(avatar.avatarVO.getAccount());
				avatarVO.setIP(avatar.avatarVO.getIP());
				avatarVO.setIsOnLine(true);
				GameSession gamesession = avatar.getSession();
				avatar = new Avatar();
				avatar.avatarVO = avatarVO;
				avatar.setSession(gamesession);
				gamesession.setRole(avatar);
				// gamesession.setLogin(true);
				// GameServerContext.add_onLine_Character(avatar);
				gamesession.sendMsg(new LoginResponse(1, avatar.avatarVO));
			}
		} else {
			// 如果不是在游戏时断线，则直接返回个人用户信息avatar
			avatar.getSession().sendMsg(new LoginResponse(1, avatar.avatarVO));
		}

	}

	public void setAvatarLIst(RoomLogic roomLogic, Avatar avatar, int i) {
		if (roomLogic.getPlayCardsLogic() != null) {
			List<Avatar> list = new ArrayList<>();
			roomLogic.getPlayCardsLogic().getPlayerList().set(i, avatar);
			if (roomLogic.getPlayCardsLogic().getHuAvatar() != null
					&& roomLogic.getPlayCardsLogic().getHuAvatar().size() >= 1) {
				list = roomLogic.getPlayCardsLogic().getHuAvatar();
				for (int j = 0; j < list.size(); j++) {
					if (list.get(j).getUuId() == avatar.getUuId()) {
						roomLogic.getPlayCardsLogic().getHuAvatar().set(j, avatar);
						break;
					}
				}
			}
			if (roomLogic.getPlayCardsLogic().getPenAvatar() != null
					&& roomLogic.getPlayCardsLogic().getPenAvatar().size() >= 1) {
				list = roomLogic.getPlayCardsLogic().getPenAvatar();
				for (int j = 0; j < list.size(); j++) {
					if (list.get(j).getUuId() == avatar.getUuId()) {
						roomLogic.getPlayCardsLogic().getPenAvatar().set(j, avatar);
						break;
					}
				}
			}
			if (roomLogic.getPlayCardsLogic().getGangAvatar() != null
					&& roomLogic.getPlayCardsLogic().getGangAvatar().size() >= 1) {
				list = roomLogic.getPlayCardsLogic().getGangAvatar();
				for (int j = 0; j < list.size(); j++) {
					if (list.get(j).getUuId() == avatar.getUuId()) {
						roomLogic.getPlayCardsLogic().getGangAvatar().set(j, avatar);
						break;
					}
				}
			}
			if (roomLogic.getPlayCardsLogic().getQishouHuAvatar() != null
					&& roomLogic.getPlayCardsLogic().getQishouHuAvatar().size() >= 1) {
				list = roomLogic.getPlayCardsLogic().getQishouHuAvatar();
				for (int j = 0; j < list.size(); j++) {
					if (list.get(j).getUuId() == avatar.getUuId()) {
						roomLogic.getPlayCardsLogic().getQishouHuAvatar().set(j, avatar);
						break;
					}
				}
			}
		}
	}
}
