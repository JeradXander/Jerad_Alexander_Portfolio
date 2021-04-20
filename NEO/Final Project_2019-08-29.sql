# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 127.0.0.1 (MySQL 5.7.23)
# Database: Final Project
# Generation Time: 2019-08-29 14:31:32 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table NEOS
# ------------------------------------------------------------

DROP TABLE IF EXISTS `NEOS`;

CREATE TABLE `NEOS` (
  `id` int(100) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL DEFAULT '',
  `speed` decimal(50,2) NOT NULL,
  `size` decimal(50,2) NOT NULL,
  `threat` tinyint(1) NOT NULL,
  `url` varchar(500) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `NEOS` WRITE;
/*!40000 ALTER TABLE `NEOS` DISABLE KEYS */;

INSERT INTO `NEOS` (`id`, `name`, `speed`, `size`, `threat`, `url`)
VALUES
	(456,'t',2.00,2.00,0,'ddsfasdf'),
	(500,'(2007 TC66)',41201.11,748.24,0,'http://ssd.jpl.nasa.gov/sbdb.cgi?sstr=3388353'),
	(501,'476093 (2007 TC66)',41201.13,748.24,0,'http://ssd.jpl.nasa.gov/sbdb.cgi?sstr=2476093'),
	(502,'(2005 EY169)',27212.92,215.79,0,'http://ssd.jpl.nasa.gov/sbdb.cgi?sstr=3274304'),
	(503,'351278 (2004 SB20)',54586.88,1032.86,0,'http://ssd.jpl.nasa.gov/sbdb.cgi?sstr=2351278'),
	(504,'415713 (1998 XX2)',31490.82,622.36,1,'http://ssd.jpl.nasa.gov/sbdb.cgi?sstr=2415713'),
	(505,'(2012 LF11)',41388.15,411.19,0,'http://ssd.jpl.nasa.gov/sbdb.cgi?sstr=3605760'),
	(506,'523661 (2012 LF11)',41388.17,411.19,0,'http://ssd.jpl.nasa.gov/sbdb.cgi?sstr=2523661'),
	(507,'(2014 DK10)',39635.28,17.14,0,'http://ssd.jpl.nasa.gov/sbdb.cgi?sstr=3662293'),
	(508,'(2007 RX8)',13413.78,74.82,0,'http://ssd.jpl.nasa.gov/sbdb.cgi?sstr=3384028'),
	(509,'(2019 EC2)',23962.47,329.79,0,'http://ssd.jpl.nasa.gov/sbdb.cgi?sstr=3838936'),
	(510,'162416 (2000 EH26)',48620.17,271.67,1,'http://ssd.jpl.nasa.gov/sbdb.cgi?sstr=2162416'),
	(511,'(2013 GX79)',52256.34,411.19,0,'http://ssd.jpl.nasa.gov/sbdb.cgi?sstr=3634786'),
	(512,'(2013 YC)',61503.01,311.92,1,'http://ssd.jpl.nasa.gov/sbdb.cgi?sstr=3655362'),
	(513,'(2016 EJ27)',51363.01,54.21,0,'http://ssd.jpl.nasa.gov/sbdb.cgi?sstr=3745111');

/*!40000 ALTER TABLE `NEOS` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table users
# ------------------------------------------------------------

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `userid` int(50) NOT NULL AUTO_INCREMENT,
  `firstname` varchar(100) DEFAULT NULL,
  `lastname` varchar(100) DEFAULT NULL,
  `username` varchar(20) DEFAULT NULL,
  `password` varchar(20) DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `md5` varchar(100) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`userid`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;

INSERT INTO `users` (`userid`, `firstname`, `lastname`, `username`, `password`, `dob`, `md5`, `email`)
VALUES
	(1,'jerad','alexander','jerad32','blue32','1990-03-09','21ae48b700dea38c286a95e9f2990093',NULL),
	(2,'bob','bobo','bob32','pass','1990-03-26','1a1dc91c907325c69271ddf0c944bc72','name'),
	(3,'bill','billy bob','bill32','pass','1997-03-14','1a1dc91c907325c69271ddf0c944bc72','sadsdfs@gmail.com'),
	(5,'tank','alexander','tank','mydog','2015-03-09','3c7eb8275e0869e49228573033e77c58','tank@gmail.com'),
	(6,'dfgdfg','sdsadf','sdag','bless','1990-03-19','ab3d1393a756e2c820eb376caf349f25','sdgfdsg'),
	(7,'bo','fett','bo','pwd','1955-07-14','9003d1df22eb4d3820015070385194c8','fett@gmail.com'),
	(8,'lando','corinthian','lando','star','1980-08-14','8ff953dd97c4405234a04291dee39e0b','starwars@gmail.com'),
	(9,'l','l','l','l','1990-03-09','2db95e8e1a9267b7a1188556b2013b33','l'),
	(10,'billybob','thortan','billy','passbob','1990-03-08','f7a0fb31391e2bde3d1771742531615e','thortan@gmail.com');

/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;



/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
