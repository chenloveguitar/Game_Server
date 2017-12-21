package com.dyz.gameserver.msg.processor.createroom;

import com.context.ErrorCode;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.message.ClientRequest;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.manager.RoomManager;
import com.dyz.gameserver.msg.processor.common.INotAuthProcessor;
import com.dyz.gameserver.msg.processor.common.MsgProcessor;
import com.dyz.gameserver.msg.response.ErrorResponse;
import com.dyz.gameserver.msg.response.createroom.CreateRoomResponse;
import com.dyz.gameserver.pojo.AvatarVO;
import com.dyz.gameserver.pojo.RoomVO;
import com.dyz.myBatis.model.ContactWay;
import com.dyz.myBatis.services.ContactWayService;
import com.dyz.persist.util.JsonUtilTool;

/**
 * Created by kevin on 2016/6/21.
 */
public class CreateRoomMsgProcssor extends MsgProcessor implements INotAuthProcessor {

	public CreateRoomMsgProcssor() {

	}

	public void process(GameSession gameSession, ClientRequest request) throws Exception {
		String message = request.getString();
		RoomVO roomVO = (RoomVO) JsonUtilTool.fromJson(message, RoomVO.class);
		if (gameSession.isLogin()) {
			Avatar avatar = gameSession.getRole(Avatar.class);
			System.out.println("房卡" + avatar.avatarVO.getAccount().getRoomcard() + "局数：" + roomVO.getRoundNumber());
			if (roomVO.getRoomType() == 4) {//创建架锅房间
				int fen = 0;
				if(roomVO.getMultiplying() == 100){
					fen = 100;
				}
				else if(roomVO.getMultiplying() == 300){
					fen = 500;
				}
				else if(roomVO.getMultiplying() == 500){
					fen = 1000;
				}
				if(avatar.avatarVO.getAccount().getRoomcard() >= fen){
					createRoomForJaGuo(gameSession, avatar, roomVO);
				}
			} else {//创建麻将房间
				if(roomVO.getTianShuiCoinType()==10  ){
					if(avatar.avatarVO.getAccount().getRoomcard()<61){
						gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000043));
						System.out.println("创建10金币场玩家金币数不足");
						return;
						
					}
					createRoomForMaJiang(gameSession, avatar, roomVO);
					
				}
				else if(roomVO.getTianShuiCoinType()==20 ){
					if(avatar.avatarVO.getAccount().getRoomcard()<300){
						gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000043));
						System.out.println("创建20金币场玩家金币数不足");
						return;
						
					}
					createRoomForMaJiang(gameSession, avatar, roomVO);
					
				}
				else if(roomVO.getTianShuiCoinType()==50){
					if(avatar.avatarVO.getAccount().getRoomcard()<1000){
						gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000043));
						System.out.println("创建20金币场玩家金币数不足");
						return;
						
					}
					createRoomForMaJiang(gameSession, avatar, roomVO);
					
				}
				
			}
		}else {
			gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000002));
		}
	}
	
	
	/**
	 * 创建架锅房间
	 * @param gameSession
	 * @param avatar
	 * @param roomVO
	 * @throws Exception
	 */
	private void createRoomForJaGuo(GameSession gameSession,Avatar avatar,RoomVO roomVO)throws Exception{
		
		ContactWay way =ContactWayService.getInstance().selectByPrimaryKey(1);
		if(way.getJiaguo() == null || way.getJiaguo()==0){
			gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000089));
		}else{
			AvatarVO avatarVo = avatar.avatarVO;
			if (avatar.avatarVO.getAccount().getRoomcard() >= 300) {
				if (avatarVo.getRoomId() == 0) {
					RoomManager.getInstance().createRoom(avatar, roomVO);
					// system.out.println("房间创建成功--
					gameSession.sendMsg(new CreateRoomResponse(1, roomVO.getRoomId() + ":" + avatar.avatarVO.getLocation()));
				} else {
					// system.out.println("你已经在房间里了，不能再创建房间");
					gameSession.sendMsg(new CreateRoomResponse(1, avatarVo.getRoomId() + ""));
				}
			} else {
				// system.out.println("房间卡不足");
				gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000014));
			}
		}
	}
	
	/**
	 * 创建麻将房间
	 * @param gameSession
	 * @param avatar
	 * @param roomVO
	 * @throws Exception
	 */
	private void createRoomForMaJiang(GameSession gameSession,Avatar avatar,RoomVO roomVO)throws Exception{
		AvatarVO avatarVo = avatar.avatarVO;
		int rate = roomVO.getTianShuiCoinType();
		int koudian = 61;
		if (rate / 10 == 2) {
			koudian = 122;
		} else if (rate / 10 == 5) {
			koudian = 303;
		}
		if (avatar.avatarVO.getAccount().getRoomcard() >= koudian) {
			if (avatarVo.getRoomId() == 0) {
				if (roomVO.getRoomType() == 3) {
					roomVO.setHong(true);
					roomVO.setZiMo(0);
				}
				RoomManager.getInstance().createRoom(avatar, roomVO);
				// system.out.println("房间创建成功--
				// roomId:"+roomVO.getRoomId());
				gameSession.sendMsg(new CreateRoomResponse(1, roomVO.getRoomId() + ":" + avatar.avatarVO.getLocation()));
			} else {
				// system.out.println("你已经在房间里了，不能再创建房间");
				gameSession.sendMsg(new CreateRoomResponse(1, avatarVo.getRoomId() + ""));
			}
		} else {
			// system.out.println("房间卡不足");
			gameSession.sendMsg(new ErrorResponse(ErrorCode.Error_000014));
		}
	}
}
