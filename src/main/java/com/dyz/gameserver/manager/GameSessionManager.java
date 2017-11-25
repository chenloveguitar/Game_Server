package com.dyz.gameserver.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.commons.session.GameSession;
import com.dyz.gameserver.context.GameServerContext;

/**
 * Created by kevin on 2016/6/20.
 */
public class GameSessionManager {

    public Map<String,GameSession> sessionMap = new HashMap<String,GameSession>();
    
    public static int topOnlineAccountCount = 0;
    
    private static GameSessionManager gameSessionManager;

    public GameSessionManager(){

    }

    /**
     *
     * @return
     */
    public static GameSessionManager getInstance(){
        if(gameSessionManager == null){
            gameSessionManager = new GameSessionManager();
        }
        return gameSessionManager;
    }

    /**
     * 更新最高在线人数
     * @param gameSession
     * @return
     */
    public void updateTopOnlineAccountCount(){
        //Avatar avatar = gameSession.getRole(Avatar.class);
       /* boolean result = checkSessionIsHava(useId);
        //System.out.println(" result ==> "+result);
        if(result){
           System.out.println("这个用户已登录了,更新session"); 
            try {
				sessionMap.get("uuid_"+useId).sendMsg(new ErrorResponse(ErrorCode.Error_000022));
				sessionMap.get("uuid_"+useId).sendMsg(new BreakLineResponse(1));
				Thread.sleep(1000);
                //sessionMap.get("uuid_"+useId).close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

            Avatar avatar = gameSession.getRole(Avatar.class);
            GameServerContext.add_onLine_Character(avatar);
            GameServerContext.remove_offLine_Character(avatar);
            TimeUitl.stopAndDestroyTimer(avatar);
        	sessionMap.put("uuid_"+useId,gameSession);
        	//如果玩家在房间中 则需要给其他玩家发送在线消息
        	if(avatar.getRoomVO() != null){
        		RoomLogic roomLogic = RoomManager.getInstance().getRoom(avatar.getRoomVO().getRoomId());
        		if(roomLogic != null){
        			List<Avatar> playerList = RoomManager.getInstance().getRoom(avatar.getRoomVO().getRoomId()).getPlayerList();
        			for (int i = 0; i < playerList.size(); i++) {
        				if(playerList.get(i).getUuId() != avatar.getUuId()){
        					//给其他三个玩家返回重连用户信息
        					playerList.get(i).getSession().sendMsg(new OtherBackLoginResonse(1, avatar.getUuId()+""));
        				}
        			}
        			if(sessionMap.size() > topOnlineAccountCount){
        				topOnlineAccountCount = sessionMap.size();
        			}
        		}
        	}
        }else{
        	//System.out.println("denglu");
            sessionMap.put("uuid_"+useId,gameSession);*/
            if(sessionMap.size() > topOnlineAccountCount){
            	topOnlineAccountCount = sessionMap.size();
            }
        //}
       // return false;
    }

    public int getVauleSize(){
        return sessionMap.size();
    }

    /**
     * 通过用户得到session
     * @param avatar
     * @return
     */
    public GameSession getGameSessionFromHashMap(Avatar avatar){
        return sessionMap.get("uuid_"+avatar.getUuId());
    }
    /**
     *
     * @param
     * @return
     */
    public GameSession getAvatarByUuid(String uuid){
        return sessionMap.get(uuid);
    }
    /**
     *
     * @param avatar
     */
    public void removeGameSession(Avatar avatar){
        //System.out.println("removeForMap");
        GameSession gameSession =  sessionMap.get("uuid_"+avatar.getUuId());
        sessionMap.remove("uuid_"+avatar.getUuId());
        if(gameSession != null){
        	gameSession = null;
        }
        GameServerContext.remove_offLine_Character(avatar);
    	GameServerContext.remove_onLine_Character(avatar);
    	avatar.setLogOut(true);
    	//avatar = null;
    }

    public List<GameSession> getAllSession(){
        List<GameSession> result = null;
        if(getVauleSize() >0) {
            result = new ArrayList<GameSession>();
            Collection<GameSession> connection = sessionMap.values();
            Iterator<GameSession> iterator = connection.iterator();
            while (iterator.hasNext()) {
                result.add(iterator.next());
            }
        }
        return result;
    }

    /**
     * 检测用户session是否存在
     * @param uuid
     * @return
     */
    public boolean checkSessionIsHava(int uuid){
    	//可以用来判断是否在线****等功能
        GameSession gameSession = sessionMap.get("uuid_"+uuid);
        if(gameSession != null){
            return true;
        }
        return false;
    }

}
