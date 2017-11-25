/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50553
Source Host           : localhost:3306
Source Database       : roledata

Target Server Type    : MYSQL
Target Server Version : 50553
File Encoding         : 65001

Date: 2017-11-25 16:14:09
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for account
-- ----------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Uuid` int(11) DEFAULT NULL,
  `openid` varchar(255) DEFAULT NULL,
  `nickName` varchar(255) DEFAULT NULL,
  `headIcon` varchar(255) DEFAULT NULL,
  `roomCard` int(11) DEFAULT NULL,
  `unionid` varchar(255) DEFAULT NULL,
  `province` varchar(255) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `sex` int(11) DEFAULT NULL,
  `prizecount` int(11) DEFAULT NULL,
  `manager_up_id` int(11) DEFAULT NULL,
  `actualCard` int(11) DEFAULT NULL,
  `totalCard` int(11) DEFAULT NULL,
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `lastLoginTime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `status` char(1) DEFAULT NULL,
  `isGame` char(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for contactway
-- ----------------------------
DROP TABLE IF EXISTS `contactway`;
CREATE TABLE `contactway` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) DEFAULT NULL,
  `jiaguo` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for game
-- ----------------------------
DROP TABLE IF EXISTS `game`;
CREATE TABLE `game` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `endtTime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `room_id` int(11) DEFAULT NULL,
  `status` char(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for gameaccountindex
-- ----------------------------
DROP TABLE IF EXISTS `gameaccountindex`;
CREATE TABLE `gameaccountindex` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `game_id` int(11) DEFAULT NULL,
  `account_id` int(11) DEFAULT NULL,
  `accountIndex` int(11) DEFAULT NULL,
  `cardList` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for gamerecord
-- ----------------------------
DROP TABLE IF EXISTS `gamerecord`;
CREATE TABLE `gamerecord` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `game_id` int(11) DEFAULT NULL,
  `type` char(1) DEFAULT NULL,
  `cardIndex` varchar(255) DEFAULT NULL,
  `acountindex_id` int(11) DEFAULT NULL,
  `curentTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `playerList_one` varchar(255) DEFAULT NULL,
  `playerList_two` varchar(255) DEFAULT NULL,
  `playerList_three` varchar(255) DEFAULT NULL,
  `playerList_four` varchar(255) DEFAULT NULL,
  `status` char(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for noticetable
-- ----------------------------
DROP TABLE IF EXISTS `noticetable`;
CREATE TABLE `noticetable` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for playrecord
-- ----------------------------
DROP TABLE IF EXISTS `playrecord`;
CREATE TABLE `playrecord` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `standingsDetail_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for prize
-- ----------------------------
DROP TABLE IF EXISTS `prize`;
CREATE TABLE `prize` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `index_id` int(11) DEFAULT NULL,
  `prize_name` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `probability` int(11) DEFAULT NULL,
  `status` char(1) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `prizecount` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for prizerule
-- ----------------------------
DROP TABLE IF EXISTS `prizerule`;
CREATE TABLE `prizerule` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) DEFAULT NULL,
  `precount` int(11) DEFAULT NULL,
  `status` char(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for roominfo
-- ----------------------------
DROP TABLE IF EXISTS `roominfo`;
CREATE TABLE `roominfo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `gameType` char(1) DEFAULT NULL,
  `isHong` char(1) DEFAULT NULL,
  `roomid` int(11) DEFAULT NULL,
  `sevenDouble` char(1) DEFAULT NULL,
  `ma` int(11) DEFAULT NULL,
  `zimo` char(1) DEFAULT NULL,
  `xiayu` int(11) DEFAULT NULL,
  `addWordCard` char(1) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `cardNumb` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for standings
-- ----------------------------
DROP TABLE IF EXISTS `standings`;
CREATE TABLE `standings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `roomid` int(11) DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for standingsaccountrelation
-- ----------------------------
DROP TABLE IF EXISTS `standingsaccountrelation`;
CREATE TABLE `standingsaccountrelation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `standings_id` int(11) DEFAULT NULL,
  `account_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for standingsdetail
-- ----------------------------
DROP TABLE IF EXISTS `standingsdetail`;
CREATE TABLE `standingsdetail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) DEFAULT NULL,
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `roomType` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for standingsrelation
-- ----------------------------
DROP TABLE IF EXISTS `standingsrelation`;
CREATE TABLE `standingsrelation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `standings_id` int(11) DEFAULT NULL,
  `standingsDetail_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for techargerecord
-- ----------------------------
DROP TABLE IF EXISTS `techargerecord`;
CREATE TABLE `techargerecord` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_id` int(11) DEFAULT NULL,
  `manager_id` int(11) DEFAULT NULL,
  `manager_up_id` int(11) DEFAULT NULL,
  `createtime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `techargeMoney` int(11) DEFAULT NULL,
  `mark` varchar(255) DEFAULT NULL,
  `status` char(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `f_id` int(11) NOT NULL AUTO_INCREMENT,
  `f_name` varchar(255) DEFAULT NULL,
  `f_phonenumber` varchar(255) DEFAULT NULL,
  `f_email` varchar(255) DEFAULT NULL,
  `f_passwd` varchar(255) DEFAULT NULL,
  `f_regdate` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`f_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for winnersinfo
-- ----------------------------
DROP TABLE IF EXISTS `winnersinfo`;
CREATE TABLE `winnersinfo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `prize_id` int(11) DEFAULT NULL,
  `account_id` int(11) DEFAULT NULL,
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `awardTime` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `status` char(1) DEFAULT NULL,
  `mark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
