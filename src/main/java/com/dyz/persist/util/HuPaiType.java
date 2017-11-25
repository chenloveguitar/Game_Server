package com.dyz.persist.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.context.Rule;
import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.msg.response.layCards.LayCardsResponse;

/**
 * 判断胡牌类型
 * @author luck
 * 
 */
public class HuPaiType {
     
	/**
	 * 有效码 key：原始码   value：处理之后的码
	 */
	private  Map<Integer , Integer> map = new HashMap<Integer , Integer>();
	/**
	 * 有效码
	 */
	private   List<Integer> validMa;
	
	/**
	 * 包含所有的有效码(处理成0-3之间数的码)
	 */
	private static StringBuffer sb;
	
	
	private static HuPaiType huPaiType ;
	
	
	private HuPaiType() {
		
	}
	public  static HuPaiType getInstance(){
		if(huPaiType == null){
			huPaiType = new HuPaiType();
		}
		return huPaiType;
	}
	/**
	 * 
	 * 	 //区分转转麻将，划水麻将，长沙麻将
	 * 
	 * 返回String的规格
     * 存储本局 杠，胡关系
     * list里面字符串规则 
     * 杠：uuid(出牌家),介绍(明杠，暗杠)  （123，明杠）
     * 自己摸来杠：介绍(明杠，暗杠)
     * 点炮：uuid(出牌家),介绍(胡的类型) （123，qishouhu）
     * 自摸：介绍(胡的类型)
     * Map：key-->1：表示信息    2:表示次数
     * count 为1表示单胡  2表示多响
     * type:传入抢胡和普通胡
     */
	public  List<Integer> getHuType(Avatar avatarShu , Avatar avatar , int roomType ,int cardIndex,
			List<Avatar> playerList,List<Integer> mas,int count,String type,boolean hongzhong){
		 //区分转转麻将，划水麻将，长沙麻将
		 if(roomType == 1){  
			 //转转麻将没有大小胡之分
			 return zhuanZhuan(avatarShu , avatar , cardIndex,playerList,mas,count,type,hongzhong);
		 }
		 else if(roomType == 2){
			 //划水麻将
			  huaShui(avatarShu , avatar, cardIndex,playerList,count);
			  return new ArrayList<>();     
		 } 
		 else{  
			 //长沙麻将
			 changSha(avatarShu,avatar,cardIndex,playerList,count,type);
			 return new ArrayList<>();
		 }
	}  
	/**
	 * 划水麻将
	 * @param avatarShu  输家
	 * @param avatar  自己
	 * @param cardIndex：当前摸得牌的索引
	 * @param playerList
	 * @param huCount 是否是一炮多响
	 * @param
	 */
	private static void huaShui(Avatar avatarShu , Avatar avatar,  int cardIndex , 
			List<Avatar> playerList , int huCount){
		int rate=avatarShu.getRoomVO().getTianShuiCoinType();

		String str;
		int score = 0;  
//		int xiayu = avatar.getRoomVO().getXiaYu();
		if(avatarShu.getUuId() == avatar.getUuId()){ //自摸
			for (int i = 0; i < playerList.size(); i++) {
				//判断当前玩家是赢家的话
				if(playerList.get(i).getUuId() == avatar.getUuId()){
					str ="0:"+cardIndex+":"+Rule.Hu_zi_common;  //自摸普通胡
					//判断如果遍历的第N个玩家是闲家自摸的话
					if(!playerList.get(i).avatarVO.isMain()){
						avatar.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
						avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("1", (2*(playerList.size()-2) + 4)*rate);
					}
					//否则是庄家自摸的话
					else
					{
						str =avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_zi_common;  
						avatar.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
						avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("1", (4*(playerList.size()-1))*rate);
					}
					
				}
				//别人自摸胡
				else{
					//判断是当前玩家是闲家自摸的话，扣2分
					if(!avatar.avatarVO.isMain())
					{
						//判断如果遍历的第N个玩家是闲家的话
						if(!playerList.get(i).avatarVO.isMain()){
						str =avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_other_common;  
						playerList.get(i).avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
						playerList.get(i).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("1", -1*2*rate );
						}
						else
						{
							str =avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_other_common;  
							playerList.get(i).avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
							playerList.get(i).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("1", -1*4*rate );
						}
					}
					//否则如果别人是庄家自摸的话，其他闲家每个人扣4分
					else
					{
						str =avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_other_common;  
						playerList.get(i).avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
						playerList.get(i).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("1", -1*4*rate);
					}
					
				}
			}
		}
		//点炮
		else{
			//未听牌 炮胡
			if(huCount == 1 && !lay(avatarShu) || avatarShu.getLayPaiType() == 0){
				str =avatar.getUuId()+":"+cardIndex+":"+Rule.pao_zi_common;  
				String str1 =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other;  
				//点炮的是闲家
				if(!avatarShu.avatarVO.isMain()){
					//赢家是庄家
					if(avatar.avatarVO.isMain()){
						avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("2",1*6*rate);
						//修改点炮庄家的分数
						avatarShu.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("3",-1*6*rate);
						
					}else{
						
						avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("2",1*4*rate);
						//修改点炮庄家的分数
						avatarShu.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("3",-1*4*rate);
					}
					
				}else {//点炮的是庄家
					
					avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("2",1*4*rate);
					//修改点炮庄家的分数
					avatarShu.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("3",-1*4*rate);
				}
			
				//点炮信息放入放炮玩家信息中
				avatar.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
				avatarShu.avatarVO.getHuReturnObjectVO().updateTotalInfo("", str1);
				
				
		}//否则是点炮，并且听牌  新加的
		else if(lay(avatarShu) && avatarShu.getLayPaiType() == 1)
		{ 
				
				//判断当前玩家如果是庄家点炮,并且听牌的话,结果：类似闲家自摸，庄家给2分，其他闲家给1分
				if(avatarShu.avatarVO.isMain()){
					str =avatar.getUuId()+":"+cardIndex+":"+Rule.ping_zi_common;  
					
					//别人点自己胡
					str =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_self;  
						
				    //接炮信息放入接炮玩家信息中
					avatar.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
					
					//赢家闲家得分数：庄家2分+其他闲家1分
					avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("2",(1*2 +(playerList.size()-2)) * rate);
					
					for(int i=0; i < playerList.size();i++){
					//判断如果是庄家的话，就修改庄家输的分数:2分 否则是闲家输的分数：1分
						if(playerList.get(i).avatarVO.isMain()){
							playerList.get(i).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("3",-1*2*rate);
						}
						else if(playerList.get(i).getUuId() != avatar.getUuId()){
							playerList.get(i).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("3",-1*1*rate);
						 }
					}
					//存储hu的关系信息 胡玩家uuid：胡牌id：胡牌类型
					str = avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other; 
					//点炮信息放入放炮玩家信息中
					avatarShu.avatarVO.getHuReturnObjectVO().updateTotalInfo("", str);
				
				
				}
					
					//点炮的是闲家,同时接炮的是庄家的话，规则：类似庄家自摸，所有人给2分
					else if(!avatarShu.avatarVO.isMain() && avatar.avatarVO.isMain())
					{    
						//别人点自己胡
						str =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_self;  
						//接炮信息放入接炮玩家信息中
						avatar.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
						//赢家庄家得分
						avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("2",(playerList.size() -1) * 2*rate);
						str = avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other;
						//修改点炮闲家的分数
					//	avatarShu.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("3",-1*2);
						//存储hu的关系信息 胡玩家uuid：胡牌id：胡牌类型
						 
						//点炮信息放入放炮玩家信息中
						//avatarShu.avatarVO.getHuReturnObjectVO().updateTotalInfo("", str);
						
						for(int i=0; i < playerList.size();i++){
						//判断如果是庄家的话，就修改庄家输的分数:2分 否则是闲家输的分数：1分
							if(!playerList.get(i).avatarVO.isMain()){
								playerList.get(i).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("3",-1*2*rate);
							}
							
						}
						
					}
					//闲家给闲家放炮 规则：庄家给2分，其他闲家给1分
					else{
						//别人点自己胡
						str =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_self;  
						//接炮信息放入接炮玩家信息中
						avatar.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
						//点炮信息放入放炮玩家信息中
						avatarShu.avatarVO.getHuReturnObjectVO().updateTotalInfo("", str);
						//赢家得分：庄家2分+其他闲家一分
						avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("2",(2 + (playerList.size() -2)) * rate);
						//修改点炮庄家的分数,判断如果点炮的是庄家的话，
						
						for(int i=0; i < playerList.size();i++){
							//判断如果是庄家的话，就修改庄家输的分数:2分 否则是闲家输的分数：1分
								if(playerList.get(i).avatarVO.isMain()){
									playerList.get(i).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("3",-1*2*rate);
								}else if(playerList.get(i).getUuId() != avatar.getUuId()){
									playerList.get(i).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("3",-1*1*rate);
									
								}
								
							}
						
						//存储hu的关系信息 胡玩家uuid：胡牌id：胡牌类型
						str = avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other; 
					}
				
				}
			
		}
		 
	}  
		
	
	 
	 /**
	    * 检测是否躺牌
	    * @param avatar
	    * @return
	    */
	   	public static boolean lay(Avatar avatar) {
	   		boolean str = false;
	   		//获取躺牌   *********
	   		if(avatar.avatarVO.getLay() != null){
	 	  		int[] lay = avatar.avatarVO.getLay();
	 	  		for(int i = 0;i < lay.length;i++){
	 	  			//检测有躺牌
	 	  			if(lay != null && lay.length > 0){
	 	  				str = true;
	 	  				return str;
	 	  			}
	 	  		}
	   		}
	 	  	return str;
	   	}
		
     /**
	 * 原来的划水麻将
	 * @param avatarShu  输家
	 * @param avatar  自己
	 * @param cardIndex
	 * @param playerList
	 * @param huCount 是否是一炮多响
	 */
	
		private static void huaShui2(Avatar avatarShu , Avatar avatar,  int cardIndex , 
				List<Avatar> playerList , int huCount){
			String str;
			int score = 0;
			int xiayu = avatar.getRoomVO().getXiaYu();
			if(avatarShu.getUuId() == avatar.getUuId()){
				//自摸类型     HuType等于1是小胡
				if(avatar.avatarVO.getHuType() == 1){
					//小胡 2分
					score = 2;
				}
				//HuType等于2是大胡
				else if(avatar.avatarVO.getHuType() == 2){
					//大胡  6 分
					score = 6;
				}
				//下鱼就是 票 0-10分的范围
				if(xiayu >= 0){
				//	score = score + 2*xiayu;
					score = score + xiayu;
				}
				for (int i = 0; i < playerList.size(); i++) {
					if(playerList.get(i).getUuId() == avatar.getUuId()){
						str ="0:"+cardIndex+":"+Rule.Hu_zi_common;  //自摸普通胡
						//更新胡牌，杠牌信息，比如谁杠了黑的牌，是明杠还是暗杠，就是统计详细信息
						avatar.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
						//更新类型，游戏自摸1，接炮2，点炮3，暗杠4，明杠5 ，胡6记录(type),加码7   （点杠 8），第二个参数是分数
						avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("1", score*(playerList.size()-1));
					}
					else{
						//别人自摸普通胡，自己减分
						str =avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_other_common;  
						playerList.get(i).avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
						playerList.get(i).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("1", -1*score);
					}
				}
			}
			else{
				//点炮   单响
				if(avatar.avatarVO.getHuType() == 1){
					//点炮小胡 3分
					score = 3;
				}
				else if(avatar.avatarVO.getHuType() == 2){
					//点炮大胡  3*3 = 9 分
					score = 9;
				}
				if(xiayu >= 0){
//					score = score + 2*xiayu;
					score = score + xiayu;
				}
				//一炮单响 等于1的时候，被人点自己胡
				if(huCount == 1){
					str =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_self;  
					//修改胡家自己的番数
					avatar.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
					
					avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("2",1*score);
					//修改点炮玩家的番
					avatarShu.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("3",-1*score);
					//存储hu的关系信息 胡玩家uuid：胡牌id：胡牌类型
					str = avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other; 
					//点炮信息放入放炮玩家信息中
					avatarShu.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
				}
				else{
					//点炮  多响  
					str =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_self;  
					//修改胡家自己的番数
					avatar.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
					avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("2",1*score);
					//修改点炮玩家的番数
					avatarShu.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("3",-1*score);
					
					//存储hu的关系信息 胡玩家uuid：胡牌id：胡牌类型
					str = avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other; 
					//点炮信息放入放炮玩家信息中
					avatarShu.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
				}
			}
	}
	/**
	 *  转转麻将 算分
	 * @param avatarShu 输家 自摸时也表示赢家
	 * @param avatar 赢家   
	 * @param cardIndex
	 * @param playerList
	 * @param mas
	 * @param count
	 *  type  qiangganghu
	 */
	private  List<Integer> zhuanZhuan(Avatar  avatarShu , Avatar avatar , int cardIndex, List<Avatar> playerList,
			List<Integer> mas , int count,String type,boolean hongzhong){
		map = new HashMap<Integer,Integer>();
		sb = new StringBuffer();
		int score = 0;
		String str; 
		int selfCount = 0;
		List<Integer> maPoint = new ArrayList<Integer>();
		//有效的码   sb = "1,2,3"样式
		sb.append("0,");
		if(mas != null){
			int ma;
			for (Integer cardPoint : mas) {
				ma = returnMa(cardPoint);
				maPoint.add(ma);
				//system.out.println("处理过的码----"+cardPoint);
				map.put(cardPoint, ma);
			}
		}
		//抓的码里面有多少个指向对应的各个玩家
		selfCount  = Collections.frequency(maPoint, 0);//自己 
		int downCount  = Collections.frequency(maPoint, 1);//下家
		int towardCount  = Collections.frequency(maPoint, 2);//对家
		int upCount  = Collections.frequency(maPoint, 3);//上家
		
		
		//int selfIndex = 0;//胡家在数组中的位置 （0-3）//2016-8-3
		int selfIndex = playerList.indexOf(avatar);
		/*for (int i = 0; i < playerList.size(); i++) {
				if(playerList.get(i).getUuId() == avatar.getUuId()){
					selfIndex = i;
				}
			}*///2016-8-3
		//其他三家在playerList中的下标，同上面的selfCount，downCount对应
		int downIndex = otherIndex(selfIndex,1);
		int towardIndex = otherIndex(selfIndex,2);
		int upIndex = otherIndex(selfIndex,3);
		if(avatarShu.getUuId() == avatar.getUuId() ){
			//自摸
			score = 2;
			for (int i = 0; i < playerList.size(); i++) {
				str ="0:"+cardIndex+":"+Rule.Hu_zi_common;  
				if(playerList.get(i).getUuId() == avatar.getUuId()){
					// avatar.avatarVO.updateScoreRecord(1, 2*3);//记录分数
					//:游戏自摸1，接炮2，点炮3，暗杠4，明杠5 ，胡6记录(key), 码7
					//修改自己的分数
					//avatar.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
					playerList.get(i).avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
					avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("1", score*(playerList.size()-1));
					for (int j = 0; j < selfCount; j++) {
						//抓码 抓到自己，再加分
						avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("7", score*(playerList.size()-1));
					}
				}
				else{
					//修改其他三家分数
					//ava.avatarVO.updateScoreRecord(1, -1*2);//记录分数（负分表示别人自摸扣的分）
					str =avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_other_common;
					playerList.get(i).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("1", -1*score);
					for (int j = 0; j < selfCount; j++) {
						playerList.get(i).avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
						//胡家抓码抓到自己，所有这里还要再减分
						playerList.get(i).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("7", -1*score);
					}
				}
			}
			if(hongzhong){
				//抢胡抓的码 只有 1 5 9 或红中有效
			}
			else{
				//自摸没选红中癞子的情况下所有码都有效
				sb.append("1,2,3");
				//抓码加减分
				for (int j = 0; j < downCount; j++) {
					//抓码 抓到下家，胡家加分，下家减分
					playerList.get(selfIndex).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("7", score);
					playerList.get(downIndex).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("7", -1*score);
				}
				for (int j = 0; j < towardCount; j++) {
					//抓码 抓到对家，胡家加分，对家减分
					playerList.get(selfIndex).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("7", score);
					playerList.get(towardIndex).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("7", -1*score);
				}
				for (int j = 0; j < upCount; j++) {
					//抓码 抓到上家，胡家加分，上家减分
					playerList.get(selfIndex).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("7", score);
					playerList.get(upIndex).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("7",-1*score);
				}
			}
		}
		else{
			//点炮   单响  
			score = 1;
			//抢杠胡加上红中癞子时 抢杠胡 6分
			if(StringUtil.isNotEmpty(type) && type.equals("qianghu") && hongzhong){
				score = 6;
			}
			if(count == 1){
				int dPaoIndex = playerList.indexOf(avatarShu);//点炮人在数组中的下标 2016-8-1
				/* for (Avatar ava : playerList) {
					if(ava.getUuId() == avatarShu.getUuId()){
						 //得到点炮玩家的下标
						 dPaoIndex = playerList.indexOf(ava);
					 }
				 }*///2016-8-1
				//点炮玩家被抓到码的次数
				int dPaoCount = 0;
				if(dPaoIndex == downIndex){
					dPaoCount = downCount;
					if(!hongzhong){
						sb.append("1,");
					}
				}
				else if(dPaoIndex == towardIndex){
					dPaoCount = towardCount;
					if(!hongzhong){
						sb.append("2,");
					}
				}
				else if(dPaoIndex == upIndex){
					dPaoCount = upCount;
					if(!hongzhong){
						sb.append("3");
					}
				}
				
				str =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_self;  
				//修改胡家自己的分数
				avatar.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
				avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("2",score);
				//修改点炮玩家的分数
				avatarShu.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("3",-1*score);
				
				for (int j = 0; j < selfCount; j++) {
					//抓码 抓到胡家，胡家加分
					avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("7", score);
					//抓码 抓到胡家，点家再减分
					avatarShu.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("7", -1*score);
				}
				if(StringUtil.isNotEmpty(type) && type.equals("qianghu") && hongzhong){
					//有红中癞子的时候抢胡抓的码 只有 1 5 9 或红中有效
				}
				else{
					for (int j = 0; j < dPaoCount; j++) {
						//抓码 抓到点炮玩家，胡家加分
						avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("7", score);
						//抓码 抓到输家，点家再减分
						avatarShu.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("7", -1*score);
					}
				}
				
				//存储hu的关系信息 胡玩家uuid：胡牌id：胡牌类型
				str = avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other; 
				//点炮信息放入放炮玩家信息中
				avatarShu.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
			}
			else{
				//点炮  多响   (抓码人为点炮玩家)
				//点炮玩家被抓到码的次数   selfCount
				//胡牌玩家被抓到码的次数
				selfIndex = playerList.indexOf(avatarShu);//点家的索引，及摸码玩家的索引
				
				downIndex = otherIndex(selfIndex,1);
				towardIndex = otherIndex(selfIndex,2);
				upIndex = otherIndex(selfIndex,3);
				
				int huCount = playerList.indexOf(avatar);
				if(huCount == downIndex){
					huCount = downCount;
					sb.append("1,");
				}
				else if(huCount == towardIndex){
					huCount = towardCount;
					sb.append("2,");
				}
				else if(huCount == upIndex){
					huCount = upCount;
					sb.append("3");
				}
				
				str =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_self;  
				//修改胡家自己的分数
				avatar.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
				avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("2",score);
				//修改点炮玩家的分数
				avatarShu.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("3",-1*score);
				for (int j = 0; j < selfCount; j++) {
					//抓码 抓到自己，赢家加分
					avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("7", score);
					//抓码 抓到胡家，输家再减分
					avatarShu.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("7", -1*score);
				}
				if(StringUtil.isNotEmpty(type) && type.equals("qianghu")){
					//抢胡抓的码 只有 1 5 9 或红中有效
				}
				else{
					for (int j = 0; j < huCount; j++) {
						//抓码 抓到点炮玩家，赢家加分
						avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("7", score);
						//抓码 抓到自己，输家再减分
						avatarShu.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("7", -1*score);
					}
				}
				//存储hu的关系信息 胡玩家uuid：胡牌id：胡牌类型
				str = avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other; 
				//点炮信息放入放炮玩家信息中
				avatarShu.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
				
			}
		}
		validMa = new ArrayList<Integer>();
		Set<Entry<Integer, Integer>>  set= map.entrySet();
		for (Entry<Integer, Integer> entry : set) {
			if(sb.toString().contains(entry.getValue()+"")){
				validMa.add(entry.getKey());
			}
		}
		System.out.println("有效码："+validMa);
		return validMa;
	}
	/**
	 * 处理抓到的码点数，成0-3之间的数
	 * @param cardPoint
	 * @return
	 */
	public static int returnMa(int cardPoint){
			if(cardPoint  <= 8){ 
				return cardPoint%4;
			}
			else {
				cardPoint = cardPoint-9;
				return returnMa(cardPoint);
			}
	}
	
	
	/**
	 * 长沙麻将 算分
	 * @param uuid
	 * @param avatar
	 * @param str
	 * @return
	 */
	private  void changSha(Avatar  avatarShu , Avatar avatar , int cardIndex,
		List<Avatar> playerList , int huCount,String type){
			String str;
			//int score = (int) Math.pow(2,avatar.avatarVO.getHuType());
			avatar.avatarVO.setHuType(avatar.avatarVO.getHuType()+1);//平湖底分还是从2分开始
			int score = (int) Math.pow(2,avatar.avatarVO.getHuType())*avatar.getRoomVO().getMagnification();
			if(StringUtil.isNotEmpty(type) && type.equals("qianghu")){
				score = score*(playerList.size()-1);
			}
			if(avatarShu.getUuId() == avatar.getUuId() ){
				//自摸类型
				for (int i = 0; i < playerList.size(); i++) {
					if(playerList.get(i).getUuId() == avatar.getUuId()){
						str ="0:"+cardIndex+":"+Rule.Hu_zi_common;  
						avatar.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
						avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("1", score*(playerList.size()-1));
					}
					else{
						str =avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_other_common;  
						playerList.get(i).avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
						playerList.get(i).avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("1", -1*score);
					}
				}
			}
			else{
				if(huCount == 1){
					str =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_self;  
					//修改胡家自己的番数
					avatar.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
					
					avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("2",1*score);
					//修改点炮玩家的番
					avatarShu.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("3",-1*score);
					//存储hu的关系信息 胡玩家uuid：胡牌id：胡牌类型
					str = avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other; 
					//点炮信息放入放炮玩家信息中
					avatarShu.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
				}
				else{
					//点炮  多响  
					str =avatarShu.getUuId()+":"+cardIndex+":"+Rule.Hu_d_self;  
					//修改胡家自己的番数
					avatar.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
					avatar.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("2",1*score);
					//修改点炮玩家的番数
					avatarShu.avatarVO.getHuReturnObjectVO().updateGangAndHuInfos("3",-1*score);
					
					//存储hu的关系信息 胡玩家uuid：胡牌id：胡牌类型
					str = avatar.getUuId()+":"+cardIndex+":"+Rule.Hu_d_other; 
					//点炮信息放入放炮玩家信息中
					avatarShu.avatarVO.getHuReturnObjectVO().updateTotalInfo("hu", str);
				}
			}
		/*String str = null;
		int uuid  = avatarShu.getUuId();
		int [] paiList = avatar.getSinglePaiArray();
		 //长沙麻将
		 if(avatarShu.getUuId() == avatar.getUuId() ){
				//自摸类型
				if(checkQingyise(paiList)){
					//清一色
					str = "0:"+Rule.Hu_zi_qingyise;
				}
				if(avatar.getRoomVO().getSevenDouble() && checkQiDui(paiList)){
					if(str != null){
						//七小队对
						str = str +"-"+0+Rule.Hu_self_qixiaodui;
					}
					else{
						str = Rule.Hu_self_qixiaodui;
					}
				}
				if(str == null){
					//str = "0:"+Rule.Hu_zi_common;
				}
			}
			else{
				//点炮类型
				if(checkQingyise(paiList)){
					//清一色
					str = uuid+":"+Rule.Hu_d_qingyise;
				}
				if(avatar.getRoomVO().getSevenDouble() && checkQiDui(paiList)){
					if(str != null){
						//七小队对
						str = str +"-"+uuid+":"+Rule.Hu_other_qixiaodui;
					}
					else{
						str = uuid+":"+Rule.Hu_other_qixiaodui;
					}
				}
				if(str == null){
					//str =uuid+":"+Rule.Hu_zi_common;
				}
			}*/
		 
	}
	
	
	
	/**
	 * 判断是否是清一色
	 * @param paiList
	 * @return
	 */
	public  boolean checkQingyise(int [] paiList){
		boolean wan = false;
		boolean tiao = false;
		boolean tong = false;
		boolean total = true;
		//是否是清一色
		for (int i = 0; i < paiList.length; i++) {
			if(i <= 8){
				if(paiList[i]>=1){
					wan = true;
					if(tiao || tong){
						return false;
					}
				}
			}
			else if( i >= 9 && i<= 17){
				if(paiList[i]>=1){
					tiao = true;
					if(wan || tong){
						return false;
					}
				}
			}
			else{
				if(paiList[i]>=1){
					tong = true;
					if(tiao || wan){
						return false;
					}
				}
			}
		}
		
		return total;
	}
	/**
     * 判断是否碰碰胡
     * @param paiList
     * @return
     */
    public boolean checkPengPengHu(int[] paiList){
        int num = 0;
        for(int i=0;i<paiList.length;i++){
            if(paiList[i] != 0){
                if(paiList[i] < 2){
                    return false;
                }else{
                    if(paiList[i] == 2){
                        num++;
                        if(num >1){
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
	
	private static int otherIndex(int selfindex,int count){
		int thisIndex = selfindex + count;
		if(thisIndex>= 4){
			thisIndex = thisIndex -4;
		}
		return thisIndex;
	}
	public List<Integer> getValidMa() {
		return validMa;
	}
	public void setValidMa(List<Integer> validMa) {
		this.validMa = validMa;
	}
	public static void main(String[] args) {
		int[] pai = new int[]{0,0,0,0,0,0,0, 4,0,2,3,3,3,3,3,3,3,3, 0,0,0,0,0,0,0,0,0};
		int[][] pais = {{0,0,0,0,0,0,0,4, 0, 2,3,3,3,3,3,3,3,3, 0,0,0,0,0,0,0,0,0},
						{0,0,0,0,0,0,0,2, 0, 0,1,0,1,0,1,0,0,0, 0,0,0,0,0,0,0,0,0}};
    	//int [] pai = new int[]{0,0,0,0,0,0,1,1,1,     0,0,2,0,3,1,1,1,0,     0,0,1,1,1,0,0,0,0,   0,0,0,0,0,0,0};
    	System.out.println(new HuPaiType().checkPengPengHu(pai));
	}
}
