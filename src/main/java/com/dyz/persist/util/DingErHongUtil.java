package com.dyz.persist.util;

import java.util.ArrayList;

import com.dyz.gameserver.Avatar;
import com.dyz.gameserver.msg.response.dingerhong.DisassembleResponse;
import com.dyz.gameserver.pojo.AvatarVO;

/**
 * 丁二红工具类
 * @author luck
 *
 */
public class DingErHongUtil {

	//单个牌是否一样(原)
	//private static int [] isSame = {0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,11,11,12,12,13,13,14,14,15,15,16,17};
	//单个牌是否一样
	private static int [] isSame = {0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9,10,10,11,11,12,12,13,13,14,14,15,15,16,17};
	//比较大小，确定叫牌玩家
	private static int [] isSameCall = {0,1,1,2,2,3,3,4,4,5,5,5,5,5,5,6,6,6,6,6,6,6,6,7,7,7,7,7,7,8,8,9,9};
	//单个牌的大小(成双成对大小比较通用)
	private static int [] sampleSingle = {0,1,1,2,2,3,3,4,4,5,5,5,5,5,5,6,6,6,6,6,6,6,6,7,7,7,7,7,7,7,7,9,10};
	//单个牌代表的点数
	private static int [] sampleList = {0,2,2,2,2,8,8,4,4,0,0,6,6,4,4,1,1,0,0,6,6,7,7,9,9,8,8,7,7,5,5,3,6};
	
	/**
	 * 比较传入的两张牌的大小，返回大的，一样大则返回-1
	 * 先介绍下单张牌的大小：
	 * 天>  地>  仁>   和>（梅子 =   长仨 =    长二）>( 斧头=  肆陆=   珠珠 =   幺陆)>(   杂玖=   杂捌 =   杂柒=   杂伍)>（丁丁=二红）
	 * 	12   34	 56    78   9,10     11,12	   13,14  15,16   17,18  19,20    21,22    23,24   25,26  27,28  29,30    31   32
	 * @param oldParam
	 * @param newparam
	 * @return
	 */
	public static int compareSingle(int oldParam,int newParam){
	
		if(isSameCall[oldParam] == isSameCall[newParam]){
			return -1;
		}
		else if(isSameCall[oldParam] < isSameCall[newParam]){
			return oldParam;
		}
		else{
			return newParam;
		}
	}
	
	public static void disass(String str,Avatar avatar){
		int one;
		String [] strs = str.split(",");
		if(true){
			//每次拆牌重置牌组
			avatar.avatarVO.setPaizu(new ArrayList<Integer>());
			//拆牌请求
			int str_one = Integer.parseInt(strs[0]);
			int str_two = Integer.parseInt(strs[1]);
			int ranking_one;//第一组牌在所有大小中占第几位   牌型大小的索引
			int nameindex;
			int bigOneParam;
			//判断如果第一张牌和第二张牌相等的话
			if(isSame[str_one] == isSame[str_two]){
				//赋值第一张牌的值
				bigOneParam = str_one;
				//对对
				one = sampleSingle[str_one];
				//对子处理的结果
				ranking_one = rankingSame(str_one);
				nameindex = rankingSame(str_one);
			}
			else{
				bigOneParam = str_one > str_two?str_two:str_one;
				one = sampleList[str_one] + sampleList[str_two];
				if(one >=10){
					one = one - 10;
				}
				ranking_one = rankingDifferent(str_one > str_two?str_two:str_one,str_one > str_two?str_one:str_two,one);
				nameindex=rankingDifferent1(str_one > str_two?str_two:str_one,str_one > str_two?str_one:str_two,one);
			}
			/*********
			 * 修改后规则
			 */
			
//			//头牌大小占的位置越大数字越大
			avatar.avatarVO.getPaizu().add(str_one);
			avatar.avatarVO.getPaizu().add(str_two);
			avatar.avatarVO.getPaizu().add(ranking_one);
			avatar.avatarVO.setHeadIndex(ranking_one);
			avatar.avatarVO.setHeadName(getName(nameindex,bigOneParam,one));
			avatar.getSession().sendMsg(new DisassembleResponse(1, avatar.avatarVO.getHeadName(),""));
			//将牌组设置到对象里
			avatar.avatarVO.getHuReturnObjectVO().setPaiArray(new int[]{str_one,str_two});
			//将牌型返回到牌组信息对象里面
			avatar.avatarVO.getHuReturnObjectVO().setPaiXing(new String[]{avatar.avatarVO.getHeadName(),""});
		}
	
	}

	/**
	 *1：       1丁二红>
	 *对子:   2对天>3对地>4对仁>5对和>6（对梅子=对长仨=对长二）>7(对斧头=对肆陆=对珠珠=对幺陆)>8(对杂玖=对杂捌=对杂柒=对杂伍)>
	 *9点：   9天玖王>10地玖王>11天罡>12地罡>13天牌玖>14地牌玖>15仁牌玖>16和牌玖>17(梅子玖=长仨玖=长二玖)>18(斧头玖=肆陆玖=珠珠玖)>
	 *8点：  19天牌捌>20地牌捌>21仁牌捌>22和牌捌>23梅子捌>24（斧头捌=肆陆捌）>25丁伍捌>
	 *7点：   26天牌柒>27地牌柒>28仁牌柒>29和牌柒>30(梅子柒=长仨柒=长二柒)>31(斧头柒=肆陆柒)>32杂牌柒>
	 *6点：   33天牌陆>34地牌陆>35仁牌陆 >36梅子陆>37(斧头陆=珠珠陆=幺陆陆)>38杂牌陆>
	 *5点：   39天牌伍>40地牌伍>41仁牌伍>42和牌伍>43（梅子伍=长仨伍=长二伍）>44(肆陆伍=珠珠伍=幺陆伍)>45杂牌伍>
	 *4点：   46天牌肆(霸王肆)>47仁牌肆>48和牌肆>49(梅子肆=长仨肆=长二肆)>50(斧头肆=珠珠肆=幺陆肆)>51杂牌肆>
	 *3点：   52天牌三>53地牌三>54仁牌三>55和牌三>56(梅子三=长仨三=长二三)>57(肆陆三=珠珠三)>58杂牌三>
	 *2点：   59天牌二>60地牌二>61仁牌二>62和牌二>63(长仨二=长二二)>64幺陆二>65杂二>
	 *1点：   66仁牌一>67和牌一>68(梅子一=长仨一=长二一)>69(斧头一=珠珠一)>70杂一>
	 *0点：   71瘪拾
	 * @param firstParam
	 * @param secondParam
	 * @return 在所有牌组中的大小位置
	 * 
	 * 对子处理
	 */
	private static int rankingSame(int paiIndex){
		
		return sampleSingle[paiIndex]+1;
	}
	/**
	 * @param firstParam
	 * @param secondParam
	 * @param paiIndex 两张牌加起来的点数
	 * @return  在所有牌组中的大小位置
	 * 
	 * 不是对子处理
	 *1：     1丁二红>
	 *对子:   2对天>3对地>4对仁>5对和>6（对梅子=对长仨=对长二）>7(对斧头=对肆陆=对珠珠=对幺陆)>8(对杂玖=对杂捌=对杂柒=对杂伍)>
	 *9点：   9天玖王>10地玖王>11天罡>12地罡>13天牌玖>14地牌玖>15仁牌玖>16和牌玖>17(梅子玖=长仨玖=长二玖)>18(斧头玖=肆陆玖=珠珠玖)>
	 *8点：  19天牌捌>20地牌捌>21仁牌捌>22和牌捌>23梅子捌>24（斧头捌=肆陆捌）>25丁伍捌>
	 *7点：   26天牌柒>27地牌柒>28仁牌柒>29和牌柒>30(梅子柒=长仨柒=长二柒)>31(斧头柒=肆陆柒)>32杂牌柒>
	 *6点：   33天牌陆>34地牌陆>35仁牌陆 >36梅子陆>37(斧头陆=珠珠陆=幺陆陆)>38杂牌陆>
	 *5点：   39天牌伍>40地牌伍>41仁牌伍>42和牌伍>43（梅子伍=长仨伍=长二伍）>44(肆陆伍=珠珠伍=幺陆伍)>45杂牌伍>
	 *4点：   46天牌肆(霸王肆)>47仁牌肆>48和牌肆>49(梅子肆=长仨肆=长二肆)>50(斧头肆=珠珠肆=幺陆肆)>51杂牌肆>
	 *3点：   52天牌三>53地牌三>54仁牌三>55和牌三>56(梅子三=长仨三=长二三)>57(肆陆三=珠珠三)>58杂牌三>
	 *2点：   59天牌二>60地牌二>61仁牌二>62和牌二>63(长仨二=长二二)>64幺陆二>65杂二>
	 *1点：   66仁牌一>67和牌一>68(梅子一=长仨一=长二一)>69(斧头一=珠珠一)>70杂一>
	 *0点：   71瘪拾
	 *
	 */
	private static int rankingDifferent1(int firstParam,int secondParam,int paiIndex){
		int position = 0;
		//System.out.println(firstParam+"--"+secondParam+"--"+paiIndex);
		if(firstParam == 31 && secondParam == 32){
			position = 1;//丁二皇
		}
		else if(sampleSingle[firstParam] == 1 &&  (secondParam == 23 || secondParam == 24)){
			position = 9;//天九王
		}
//		else if(sampleSingle[firstParam] == 2 &&  (secondParam == 23 || secondParam == 24)){
//			position = 10;//地九王
//		}
		else if(sampleSingle[firstParam] == 1 &&  (sampleSingle[secondParam]== 3 || secondParam == 25 || secondParam == 26)){
			position = 11;//天罡
		}
		else if(sampleSingle[firstParam] == 2 &&  (sampleSingle[secondParam] == 3 || secondParam == 25 || secondParam == 26)){
			position = 12;//地罡
		}
		else{
			switch (paiIndex) {
			case 0:
				switch (--firstParam) {
				case 6:
				case 7:
				case 10:
				case 11:
				case 12:
				case 13:
					position = 100;
					break;
				case 14:
				case 15:
					position = 101;
					break;
				case 20:
				case 21:
				case 26:
				case 27:
					position = 102;
					break;
					
				default:
					position = 71;
					break;
				}
				break;
			case 1:
				//1点：   66仁牌一>67和牌一>68(梅子一=长仨一=长二一)>69(斧头一=珠珠一)>70杂一>
				switch (sampleSingle[firstParam]) {
				case 3:
					position = 66;
					break;
				case 4:
					position = 67;				
					break;
				case 5:
					position = 68;	
					break;
				case 6:
					position = 69;	
					break;
				default:
					position = 70;	
					break;
				}
				
				//新牌型
				switch (--firstParam) {
				case 14:
				case 15:
					position = 104;
					break;
				case 4:
				case 5:
				case 24:
				case 25:
					position = 103;
					break;
				case 2:
				case 3:
					position = 133;
					break;
				}

				break;
			case 2:
				 //2点：   59天牌二>60地牌二>61仁牌二>62和牌二>63(长仨二=长二二)>64幺陆二>65杂二>
				switch (sampleSingle[firstParam]) {
				case 1:
					position = 59;
					break;
				case 2:
					position = 60;
					break;
				case 3:
					position = 61;
					break;
				case 4:
					position = 62;				
					break;
				case 5:
					position = 63;	
					break;
				case 6:
					position = 64;	
					break;
				default:
					position = 65;	
					break;
				}
				//新牌型
				switch (--firstParam) {
				case 0:
				case 1:
					position = 106;
					break;
				case 2:
				case 3:
					position = 105;
					break;
				case 20:
				case 21:
				case 26:
				case 27:
					position = 107;
					break;
				case 10:
				case 11:
					position = 108;
					break;
				}
				break;
			case 3:
				//3点：   52天牌三>53地牌三>54仁牌三>55和牌三>56(梅子三=长仨三=长二三)>57(肆陆三=珠珠三)>58杂牌三>
				switch (sampleSingle[firstParam]) {
				case 1:
					position = 52;
					break;
				case 2:
					position = 53;
					break;
				case 3:
					position = 54;
					break;
				case 4:
					position = 55;				
					break;
				case 5:
					position = 56;	
					break;
				case 6:
					position = 57;	
					break;
				default:
					position = 58;	
					break;
				}
				
				//新牌型
				switch (--firstParam) {
				case 10:
				case 11:
				case 18:
				case 19:
					position = 109;
					break;
				}
				break;
			case 4:
			   // 4点：   46天牌肆(霸王肆)>47仁牌肆>48和牌肆>49(梅子肆=长仨肆=长二肆)>50(斧头肆=珠珠肆=幺陆肆)>51杂牌肆>
				
				switch (sampleSingle[firstParam]) {
				case 1:
					position = 46;
					break;
				case 3:
					position = 47;
					break;
				case 4:
					position = 48;				
					break;
				case 5:
					position = 49;	
					break;
				case 6:
					position = 50;	
					break;
				default:
					position = 51;	
					break;
				}
				
				//新牌型
				switch (--firstParam) {
				case 0:
				case 1:
					position = 110;
					break;
				}
				
				
				break;
			case 5:
				// *5点：   39天牌伍>40地牌伍>41仁牌伍>42和牌伍>43（梅子伍=长仨伍=长二伍）>44(肆陆伍=珠珠伍=幺陆伍)>45杂牌伍>
				switch (sampleSingle[firstParam]) {
				case 1:
					position = 39;
					break;
				case 2:
					position = 40;
					break;
				case 3:
					position = 41;
					break;
				case 4:
					position = 42;				
					break;
				case 5:
					position = 43;	
					break;
				case 6:
					position = 44;	
					break;
				default:
					position = 45;	
					break;
				}
				
				//新牌型
				switch (--firstParam) {
				case 2:
				case 3:
					position = 111;
					break;
				case 0:
				case 1:
					position = 112;
					break;
				case 8:
				case 9:
					position = 113;
					break;
				case 6:
				case 7:
				case 12:
				case 13:
					position = 114;
					break;
				case 16:
				case 17:
					position = 115;
					break;
				}
				break;
			case 6:
				// *6点：   33天牌陆>34地牌陆>35仁牌陆 >36梅子陆>37(斧头陆=珠珠陆=幺陆陆)>38杂牌陆>
				switch (sampleSingle[firstParam]) {
				case 1:
					position = 33;
					break;
				case 2:
					position = 34;
					break;
				case 3:
					position = 35;
					break;
				case 5:
					position = 36;				
					break;
				case 6:
					position = 37;	
					break;
				case 7:
					position = 38;	
					break;
				default:
					break;
				}
				
				//新牌型
				switch (--firstParam) {
				case 2:
				case 3:
					position = 116;
					break;
				case 0:
				case 1:
					position = 117;
					break;
				case 8:
				case 9:
					position = 118;
					break;
				case 14:
				case 15:
					position = 119;
					break;
				case 16:
				case 17:
				case 10:
				case 11:
					position = 120;
					break;
				}
				
				break;
			case 7:
				 //*7点：   26天牌柒>27地牌柒>28仁牌柒>29和牌柒>30(梅子柒=长仨柒=长二柒)>31(斧头柒=肆陆柒)>32杂牌柒>
				switch (sampleSingle[firstParam]) {
				case 1:
					position = 26;
					break;
				case 2:
					position = 27;
					break;
				case 3:
					position = 28;
					break;
				case 4:
					position = 29;				
					break;
				case 5:
					position = 30;	
					break;
				case 6:
					position = 31;	
					break;
				default:
					position = 32;	
					break;
				}
				
				//新牌型
				switch (--firstParam) {
				case 2:
				case 3:
					position = 121;
					break;
				case 0:
				case 1:
					position = 122;
					break;
				case 8:
				case 9:
					position = 123;
					break;
				case 10:
				case 11:	
				case 14:
				case 15:
					position = 124;
					break;
				case 16:
				case 17:
					position = 125;
					break;
				}
				
				break;
			case 8:
				// *8点：  19天牌捌>20地牌捌>21仁牌捌>22和牌捌>23梅子捌>24（斧头捌=肆陆捌）>25丁伍捌>
				switch (sampleSingle[firstParam]) {
				case 1:
					position = 19;
					break;
				case 2:
					position = 20;
					break;
				case 3:
					position = 21;
					break;
				case 4:
					position = 22;				
					break;
				case 5:
					position = 23;	
					break;
				case 6:
					position = 24;	
					break;
				case 7:
					position = 25;	
					break;
				default:
					System.out.println("8点是出错："+firstParam+":"+secondParam);		
					break;
				}
				
				//新牌型
				switch (--firstParam) {
				case 2:
				case 3:
					position = 126;
					break;
				case 0:
				case 1:
					position = 127;
					break;
				case 8:
				case 9:
				case 4:
				case 5:
					position = 128;
					break;
				case 14:
				case 15:
					position = 129;
					break;
				}
				 if((firstParam==4||firstParam==5)&&(secondParam==16||secondParam==17)){
	                	position = 130;	//板八
	                }
				 if((firstParam==16||firstParam==17)&&(secondParam==24||secondParam==25)){
	                	position = 130;	//板八
	                }
				 
				break;
			case 9:
				//9点：  13天牌玖>14地牌玖>15仁牌玖>16和牌玖>17(梅子玖=长仨玖=长二玖)>18(斧头玖=肆陆玖=珠珠玖)>
				switch (sampleSingle[firstParam]) {
				case 1:
					position = 13;
					break;
				case 2:
					position = 14;
					break;
				case 3:
					position = 15;
					break;
				case 4:
					position = 16;				
					break;
				case 5:
					position = 17;	
					break;
				case 6:
					position = 18;	
					break;
				default:
					System.out.println("9点是出错："+firstParam+":"+secondParam);		
					break;
				}
				int a=firstParam-1;
				int b=secondParam-1;
				if((a==10||a==11)&&b==30){
					position = 131;	//假猴王
				}
                if((a==4||a==5)&&(b==14||b==15)){
                	position = 132;	//东北人
                }
				
				break;
			default:
				break;
			}
			
		}
		if(position == 0){
			System.out.println("DingErHONGUtil  rankingDifferent出问题! ");
		}
		return position;
	}
	
	/**
	 * @param firstParam
	 * @param secondParam
	 * @param paiIndex 两张牌加起来的点数
	 * @return  在所有牌组中的大小位置
	 * 
	 * 不是对子处理
	 *1：     1丁二红>
	 *对子:   2对天>3对地>4对仁>5对和>6（对梅子=对长仨=对长二）>7(对斧头=对肆陆=对珠珠=对幺陆)>8(对杂玖=对杂捌=对杂柒=对杂伍)>
	 *9点：   9天玖王>10地玖王>11天罡>12地罡>13天牌玖>14地牌玖>15仁牌玖>16和牌玖>17(梅子玖=长仨玖=长二玖)>18(斧头玖=肆陆玖=珠珠玖)>
	 *8点：  19天牌捌>20地牌捌>21仁牌捌>22和牌捌>23梅子捌>24（斧头捌=肆陆捌）>25丁伍捌>
	 *7点：   26天牌柒>27地牌柒>28仁牌柒>29和牌柒>30(梅子柒=长仨柒=长二柒)>31(斧头柒=肆陆柒)>32杂牌柒>
	 *6点：   33天牌陆>34地牌陆>35仁牌陆 >36梅子陆>37(斧头陆=珠珠陆=幺陆陆)>38杂牌陆>
	 *5点：   39天牌伍>40地牌伍>41仁牌伍>42和牌伍>43（梅子伍=长仨伍=长二伍）>44(肆陆伍=珠珠伍=幺陆伍)>45杂牌伍>
	 *4点：   46天牌肆(霸王肆)>47仁牌肆>48和牌肆>49(梅子肆=长仨肆=长二肆)>50(斧头肆=珠珠肆=幺陆肆)>51杂牌肆>
	 *3点：   52天牌三>53地牌三>54仁牌三>55和牌三>56(梅子三=长仨三=长二三)>57(肆陆三=珠珠三)>58杂牌三>
	 *2点：   59天牌二>60地牌二>61仁牌二>62和牌二>63(长仨二=长二二)>64幺陆二>65杂二>
	 *1点：   66仁牌一>67和牌一>68(梅子一=长仨一=长二一)>69(斧头一=珠珠一)>70杂一>
	 *0点：   71瘪拾
	 *
	 */
	private static int rankingDifferent(int firstParam,int secondParam,int paiIndex){
		int position = 0;
		//System.out.println(firstParam+"--"+secondParam+"--"+paiIndex);
		if(firstParam == 31 && secondParam == 32){
			position = 1;//丁二皇
		}
		else if(sampleSingle[firstParam] == 1 &&  (secondParam == 23 || secondParam == 24)){
			position = 9;//天九王
		}
//		else if(sampleSingle[firstParam] == 2 &&  (secondParam == 23 || secondParam == 24)){
//			position = 10;//地九王
//		}
		else if(sampleSingle[firstParam] == 1 &&  (sampleSingle[secondParam]== 3 || secondParam == 25 || secondParam == 26)){
			position = 11;//天罡
		}
		else if(sampleSingle[firstParam] == 2 &&  (sampleSingle[secondParam] == 3 || secondParam == 25 || secondParam == 26)){
			position = 12;//地罡
		}
		else{
			switch (paiIndex) {
			case 0:
				position = 72;
				break;
			case 1:
				//1点：   66仁牌一>67和牌一>68(梅子一=长仨一=长二一)>69(斧头一=珠珠一)>70杂一>
				switch (sampleSingle[firstParam]) {
				case 2:
					position = 66;
					break;
				case 3:
					position = 67;
					break;
				case 4:
					position = 68;				
					break;
				case 5:
					position = 69;	
					break;
				case 6:
					position = 70;	
					break;
				default:
					position = 71;	
					break;
				}
				


				break;
			case 2:
				 //2点：   59天牌二>60地牌二>61仁牌二>62和牌二>63(长仨二=长二二)>64幺陆二>65杂二>
				switch (sampleSingle[firstParam]) {
				case 1:
					position = 59;
					break;
				case 2:
					position = 60;
					break;
				case 3:
					position = 61;
					break;
				case 4:
					position = 62;				
					break;
				case 5:
					position = 63;	
					break;
				case 6:
					position = 64;	
					break;
				default:
					position = 65;	
					break;
				}

				break;
			case 3:
				//3点：   52天牌三>53地牌三>54仁牌三>55和牌三>56(梅子三=长仨三=长二三)>57(肆陆三=珠珠三)>58杂牌三>
				switch (sampleSingle[firstParam]) {
				case 1:
					position = 52;
					break;
				case 2:
					position = 53;
					break;
				case 3:
					position = 54;
					break;
				case 4:
					position = 55;				
					break;
				case 5:
					position = 56;	
					break;
				case 6:
					position = 57;	
					break;
				default:
					position = 58;	
					break;
				}
				

				break;
			case 4:
			   // 4点：   46天牌肆(霸王肆)>47仁牌肆>48和牌肆>49(梅子肆=长仨肆=长二肆)>50(斧头肆=珠珠肆=幺陆肆)>51杂牌肆>
				
				switch (sampleSingle[firstParam]) {
				case 1:
					position = 46;
					break;
				case 3:
					position = 47;
					break;
				case 4:
					position = 48;				
					break;
				case 5:
					position = 49;	
					break;
				case 6:
					position = 50;	
					break;
				default:
					position = 51;	
					break;
				}
				

				
				
				break;
			case 5:
				// *5点：   39天牌伍>40地牌伍>41仁牌伍>42和牌伍>43（梅子伍=长仨伍=长二伍）>44(肆陆伍=珠珠伍=幺陆伍)>45杂牌伍>
				switch (sampleSingle[firstParam]) {
				case 1:
					position = 39;
					break;
				case 2:
					position = 40;
					break;
				case 3:
					position = 41;
					break;
				case 4:
					position = 42;				
					break;
				case 5:
					position = 43;	
					break;
				case 6:
					position = 44;	
					break;
				default:
					position = 45;	
					break;
				}
				

				break;
			case 6:
				// *6点：   33天牌陆>34地牌陆>35仁牌陆 >36梅子陆>37(斧头陆=珠珠陆=幺陆陆)>38杂牌陆>
				switch (sampleSingle[firstParam]) {
				case 1:
					position = 33;
					break;
				case 2:
					position = 34;
					break;
				case 3:
					position = 35;
					break;
				case 5:
					position = 36;				
					break;
				case 6:
					position = 37;	
					break;
				case 7:
					position = 38;	
					break;
				default:
					break;
				}
				
				
				break;
			case 7:
				 //*7点：   26天牌柒>27地牌柒>28仁牌柒>29和牌柒>30(梅子柒=长仨柒=长二柒)>31(斧头柒=肆陆柒)>32杂牌柒>
				switch (sampleSingle[firstParam]) {
				case 1:
					position = 26;
					break;
				case 2:
					position = 27;
					break;
				case 3:
					position = 28;
					break;
				case 4:
					position = 29;				
					break;
				case 5:
					position = 30;	
					break;
				case 6:
					position = 31;	
					break;
				default:
					position = 32;	
					break;
				}
				
				
				break;
			case 8:
				// *8点：  19天牌捌>20地牌捌>21仁牌捌>22和牌捌>23梅子捌>24（斧头捌=肆陆捌）>25丁伍捌>
				switch (sampleSingle[firstParam]) {
				case 1:
					position = 19;
					break;
				case 2:
					position = 20;
					break;
				case 3:
					position = 21;
					break;
				case 4:
					position = 22;				
					break;
				case 5:
					position = 23;	
					break;
				case 6:
					position = 24;	
					break;
				case 7:
					position = 25;	
					break;
				default:
					System.out.println("8点是出错："+firstParam+":"+secondParam);		
					break;
				}

				 
				break;
			case 9:
				//9点：  13天牌玖>14地牌玖>15仁牌玖>16和牌玖>17(梅子玖=长仨玖=长二玖)>18(斧头玖=肆陆玖=珠珠玖)>
				switch (sampleSingle[firstParam]) {
				case 1:
					position = 13;
					break;
				case 2:
					position = 14;
					break;
				case 3:
					position = 15;
					break;
				case 4:
					position = 16;				
					break;
				case 5:
					position = 17;	
					break;
				case 6:
					position = 18;	
					break;
				default:
					System.out.println("9点是出错："+firstParam+":"+secondParam);		
					break;
				}
				
				break;
			default:
				break;
			}
			
		}
		if(position == 0){
			System.out.println("DingErHONGUtil  rankingDifferent出问题! ");
		}
		return position;
	}
	/**
	 * @param firstParam
	 * @param secondParam
	 * @param paiIndex 两张牌加起来的点数
	 * @return  在所有牌组中的大小位置
	 * 
	 * 不是对子处理
	 *1：     1丁二红>
	 *对子:   2对天>3对地>4对仁>5对和>6（对梅子=对长仨=对长二）>7(对斧头=对肆陆=对珠珠=对幺陆)>8(对杂玖=对杂捌=对杂柒=对杂伍)>
	 *9点：   9天玖王>10地玖王>11天罡>12地罡>13天牌玖>14地牌玖>15仁牌玖>16和牌玖>17(梅子玖=长仨玖=长二玖)>18(斧头玖=肆陆玖=珠珠玖)>
	 *8点：  19天牌捌>20地牌捌>21仁牌捌>22和牌捌>23梅子捌>24（斧头捌=肆陆捌）>25丁伍捌>
	 *7点：   26天牌柒>27地牌柒>28仁牌柒>29和牌柒>30(梅子柒=长仨柒=长二柒)>31(斧头柒=肆陆柒)>32杂牌柒>
	 *6点：   33天牌陆>34地牌陆>35仁牌陆 >36梅子陆>37(斧头陆=珠珠陆=幺陆陆)>38杂牌陆>
	 *5点：   39天牌伍>40地牌伍>41仁牌伍>42和牌伍>43（梅子伍=长仨伍=长二伍）>44(肆陆伍=珠珠伍=幺陆伍)>45杂牌伍>
	 *4点：   46天牌肆(霸王肆)>47仁牌肆>48和牌肆>49(梅子肆=长仨肆=长二肆)>50(斧头肆=珠珠肆=幺陆肆)>51杂牌肆>
	 *3点：   52天牌三>53地牌三>54仁牌三>55和牌三>56(梅子三=长仨三=长二三)>57(肆陆三=珠珠三)>58杂牌三>
	 *2点：   59天牌二>60地牌二>61仁牌二>62和牌二>63(长仨二=长二二)>64幺陆二>65杂二>
	 *1点：   66仁牌一>67和牌一>68(梅子一=长仨一=长二一)>69(斧头一=珠珠一)>70杂一>
	 *0点：   71瘪拾
	 */
	private static String getName(int paizuIndex,int bigParam,int position){
		String name = position+"";
		//System.out.println("getName方法"+paizuIndex+"--"+bigParam+"--"+position);
		switch (paizuIndex) {
		case 1:
			//name = "丁二皇";
			name = "39";
			break;
		case 2:
			//name = "对天";
			name = "38"; 
			break;
		case 3:
			//name = "对地"; 
			name = "37"; 
			break;
		case 4:
			//name = "对仁"; 
			name = "36"; 
			break;
		case 5:
			//name = "对和";		
			name = "35"; 
			break;
		case 6:
			if (bigParam == 9 || bigParam == 10 ) {
				//name = "对梅子";
				name = "34"; 
			}
			else if(bigParam == 11 || bigParam == 12){
				//name = "对长三";
				name = "33"; 
			}
			else if(bigParam == 13 || bigParam == 14){
				//name = "对长二";
				name = "32"; 
			}
			break;
		case 7:
			if (bigParam == 15 || bigParam == 16 ) {
				//name = "对斧头";
				name = "31"; 
			}
			else if(bigParam == 17 || bigParam == 18){
				//name = "对肆陆";
				name = "30"; 
			}
			else if(bigParam == 19 || bigParam == 20){
				//name = "对珠珠";
				name = "29"; 
			}
			else if(bigParam == 21 || bigParam == 22){
				//name = "对幺陆";
				name = "28"; 
			}
			break;
		case 8:
			if (bigParam == 23 || bigParam == 24 ) {
				//name = "对杂玖";
				name = "27"; 
			}
			else if(bigParam == 25 || bigParam == 26){
				//name = "对杂捌";
				name = "26"; 
			}
			else if(bigParam == 27 || bigParam == 28){
				//name = "对杂柒";
				name = "25"; 
			}
			else if(bigParam == 29 || bigParam == 30){
				//name = "对杂伍";
				name = "24"; 
			}
			break;
		case 9:
			//name = "天九王";
			name = "23"; 
			break;
		case 10:
			//name = "地九王";
			name = "22"; 
			break;
		case 11:
			//name = "天罡";
			name = "21"; 
			break;
		case 12:
			//name = "地罡";
			name = "20"; 
			break;
		case 13:
			//name = "天牌九";
			name = "19"; 
			break;
		case 14:
			//name = "地牌九";
			name = "18"; 
			break;
		case 15:
			//name = "仁牌九";
			name = "17"; 
			break;
		case 16:
			//name = "和牌九";			
			name = "16"; 
			break;
		case 17:
			if (bigParam == 9 || bigParam == 10 ) {
				//name = "梅子九";
				name = "15"; 
			}
			else if(bigParam == 11 || bigParam == 12){
				//name = "长三九";
				name = "14"; 
			}
			else if(bigParam == 13 || bigParam == 14){
				//name = "长二九";
				name = "13";
			}
			break;
		case 18:
			if (bigParam == 15 || bigParam == 16 ) {
				//name = "斧头九";
				name = "12";
			}
			else if(bigParam == 17 || bigParam == 18){
				//name = "肆陆九";
				name = "11";
			}
			else if(bigParam == 19 || bigParam == 20){
				//name = "珠珠九";
				name = "10";
			}
			else if(bigParam == 21 || bigParam == 22){
				//name = "幺陆九";
				name = "9";
			}
			break;
		default:
			if(paizuIndex>99){
				name=paizuIndex+"";
			}
			
			break;
		}
		
		
		//System.err.println(name);
		return name;
	}
	
	public static void main(String[] args) {
		Avatar avatar = new Avatar();
		avatar.avatarVO = new AvatarVO();
		disass("12,17,8,7,0",avatar);
	}
}