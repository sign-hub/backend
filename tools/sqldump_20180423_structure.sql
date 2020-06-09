-- MySQL dump 10.14  Distrib 5.5.52-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: cinidb
-- ------------------------------------------------------
-- Server version	5.5.52-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `cinidb`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `cinidb` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `cinidb`;

--
-- Table structure for table `EndUser`
--

DROP TABLE IF EXISTS `EndUser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EndUser` (
  `uuid` varchar(255) NOT NULL,
  `deleted` tinyint(1) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `esAccountId` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `registrationDate` datetime DEFAULT NULL,
  `surname` varchar(255) DEFAULT NULL,
  `verificationCode` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EsAccount`
--

DROP TABLE IF EXISTS `EsAccount`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EsAccount` (
  `uuid` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `userGroup` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EsAccount_EsRole`
--

DROP TABLE IF EXISTS `EsAccount_EsRole`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EsAccount_EsRole` (
  `users_uuid` varchar(255) NOT NULL,
  `roles_uuid` varchar(255) NOT NULL,
  PRIMARY KEY (`users_uuid`,`roles_uuid`),
  KEY `FK_7917kovyd7cq1km9n63u97sf0` (`roles_uuid`),
  KEY `FK_m75gnehjdyujyi59wmr6yt8wt` (`users_uuid`),
  CONSTRAINT `FK_7917kovyd7cq1km9n63u97sf0` FOREIGN KEY (`roles_uuid`) REFERENCES `EsRole` (`uuid`),
  CONSTRAINT `FK_m75gnehjdyujyi59wmr6yt8wt` FOREIGN KEY (`users_uuid`) REFERENCES `EsAccount` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EsAuth`
--

DROP TABLE IF EXISTS `EsAuth`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EsAuth` (
  `uuid` varchar(255) NOT NULL,
  `description` longtext,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EsAuth_EsRole`
--

DROP TABLE IF EXISTS `EsAuth_EsRole`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EsAuth_EsRole` (
  `auths_uuid` varchar(255) NOT NULL,
  `roles_uuid` varchar(255) NOT NULL,
  PRIMARY KEY (`auths_uuid`,`roles_uuid`),
  KEY `FK_i4kgwy1ei5bs5vs8u8e6fd1b5` (`roles_uuid`),
  KEY `FK_p17iy1pib19ayens8cjiay8r2` (`auths_uuid`),
  CONSTRAINT `FK_i4kgwy1ei5bs5vs8u8e6fd1b5` FOREIGN KEY (`roles_uuid`) REFERENCES `EsRole` (`uuid`),
  CONSTRAINT `FK_p17iy1pib19ayens8cjiay8r2` FOREIGN KEY (`auths_uuid`) REFERENCES `EsAuth` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EsRole`
--

DROP TABLE IF EXISTS `EsRole`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EsRole` (
  `uuid` varchar(255) NOT NULL,
  `description` longtext,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Grammar`
--

DROP TABLE IF EXISTS `Grammar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Grammar` (
  `uuid` varchar(255) NOT NULL,
  `creationDate` datetime DEFAULT NULL,
  `deleted` tinyint(1) DEFAULT NULL,
  `grammarName` varchar(255) DEFAULT NULL,
  `grammarStatus` varchar(255) DEFAULT NULL,
  `revisionDate` datetime DEFAULT NULL,
  `author_uuid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FK_oe32rw4bnqwfyugnvgylcrub5` (`author_uuid`),
  CONSTRAINT `FK_oe32rw4bnqwfyugnvgylcrub5` FOREIGN KEY (`author_uuid`) REFERENCES `EndUser` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `GrammarPart`
--

DROP TABLE IF EXISTS `GrammarPart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `GrammarPart` (
  `uuid` varchar(255) NOT NULL,
  `creationDate` datetime DEFAULT NULL,
  `deleted` tinyint(1) DEFAULT NULL,
  `elementNumber` varchar(255) DEFAULT NULL,
  `grammarPartName` varchar(255) DEFAULT NULL,
  `grammarPartOrder` int(11) DEFAULT NULL,
  `grammarPartStatus` varchar(255) DEFAULT NULL,
  `grammarPartTYpe` varchar(255) DEFAULT NULL,
  `html` longtext,
  `revisionDate` datetime DEFAULT NULL,
  `author_uuid` varchar(255) DEFAULT NULL,
  `parent_uuid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FK_aobrvj49wpoax93viskpvfwx` (`author_uuid`),
  KEY `FK_o4a86yyqr43fyetsrpb3sa9ut` (`parent_uuid`),
  CONSTRAINT `FK_aobrvj49wpoax93viskpvfwx` FOREIGN KEY (`author_uuid`) REFERENCES `EndUser` (`uuid`),
  CONSTRAINT `FK_o4a86yyqr43fyetsrpb3sa9ut` FOREIGN KEY (`parent_uuid`) REFERENCES `GrammarPart` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `GrammarPart_GrammarPart`
--

DROP TABLE IF EXISTS `GrammarPart_GrammarPart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `GrammarPart_GrammarPart` (
  `GrammarPart_uuid` varchar(255) NOT NULL,
  `parts_uuid` varchar(255) NOT NULL,
  UNIQUE KEY `UK_2mi7rowfqo1m7igp6auf5k29r` (`parts_uuid`),
  KEY `FK_2mi7rowfqo1m7igp6auf5k29r` (`parts_uuid`),
  KEY `FK_s2jgmqsch5ya7b7pvgatxy9i0` (`GrammarPart_uuid`),
  CONSTRAINT `FK_2mi7rowfqo1m7igp6auf5k29r` FOREIGN KEY (`parts_uuid`) REFERENCES `GrammarPart` (`uuid`),
  CONSTRAINT `FK_s2jgmqsch5ya7b7pvgatxy9i0` FOREIGN KEY (`GrammarPart_uuid`) REFERENCES `GrammarPart` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Grammar_GrammarPart`
--

DROP TABLE IF EXISTS `Grammar_GrammarPart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Grammar_GrammarPart` (
  `Grammar_uuid` varchar(255) NOT NULL,
  `parts_uuid` varchar(255) NOT NULL,
  UNIQUE KEY `UK_nedw9rackj1nrqe0sntwq91ob` (`parts_uuid`),
  KEY `FK_nedw9rackj1nrqe0sntwq91ob` (`parts_uuid`),
  KEY `FK_q01odd0dyk5vy9o9q1qaxcght` (`Grammar_uuid`),
  CONSTRAINT `FK_nedw9rackj1nrqe0sntwq91ob` FOREIGN KEY (`parts_uuid`) REFERENCES `GrammarPart` (`uuid`),
  CONSTRAINT `FK_q01odd0dyk5vy9o9q1qaxcght` FOREIGN KEY (`Grammar_uuid`) REFERENCES `Grammar` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Media`
--

DROP TABLE IF EXISTS `Media`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Media` (
  `uuid` varchar(255) NOT NULL,
  `createdAt` datetime DEFAULT NULL,
  `deleted` tinyint(1) DEFAULT NULL,
  `mediaName` varchar(255) DEFAULT NULL,
  `mediaPath` varchar(255) DEFAULT NULL,
  `mediaType` varchar(255) DEFAULT NULL,
  `mediaAuthor_uuid` varchar(255) DEFAULT NULL,
  `repositoryId` varchar(255) DEFAULT NULL,
  `thumbPath` varchar(255) DEFAULT NULL,
  `thumbRepositoryId` varchar(255) DEFAULT NULL,
  `testType` varchar(255) DEFAULT NULL,
  `isUser` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FK_2jokm9639webc71m663g2g6yk` (`mediaAuthor_uuid`),
  CONSTRAINT `FK_2jokm9639webc71m663g2g6yk` FOREIGN KEY (`mediaAuthor_uuid`) REFERENCES `EndUser` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Metadata`
--

DROP TABLE IF EXISTS `Metadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Metadata` (
  `uuid` varchar(255) NOT NULL,
  `keyfield` varchar(255) DEFAULT NULL,
  `valuefield` varchar(255) DEFAULT NULL,
  `report_uuid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FK_39ytmxe02nll8xhrxtk1mbea` (`report_uuid`),
  CONSTRAINT `FK_39ytmxe02nll8xhrxtk1mbea` FOREIGN KEY (`report_uuid`) REFERENCES `Report` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Option`
--

DROP TABLE IF EXISTS `Option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Option` (
  `uuid` varchar(255) NOT NULL,
  `keyfield` varchar(255) DEFAULT NULL,
  `valuefield` longtext,
  `question_uuid` varchar(255) DEFAULT NULL,
  `slide_uuid` varchar(255) DEFAULT NULL,
  `slideContentComponent_uuid` varchar(255) DEFAULT NULL,
  `test_uuid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FK_eawn5ebi5n0vb27xkf57gfsgm` (`question_uuid`),
  KEY `FK_755nuyolqk3krvm7b3qsnake0` (`slide_uuid`),
  KEY `FK_eoqjunnadgpsx7dr5ilb7bw2c` (`slideContentComponent_uuid`),
  KEY `FK_cw0qldqn191stsk7o4nbxnxql` (`test_uuid`),
  CONSTRAINT `FK_755nuyolqk3krvm7b3qsnake0` FOREIGN KEY (`slide_uuid`) REFERENCES `Slide` (`uuid`),
  CONSTRAINT `FK_cw0qldqn191stsk7o4nbxnxql` FOREIGN KEY (`test_uuid`) REFERENCES `Test` (`uuid`),
  CONSTRAINT `FK_eawn5ebi5n0vb27xkf57gfsgm` FOREIGN KEY (`question_uuid`) REFERENCES `Question` (`uuid`),
  CONSTRAINT `FK_eoqjunnadgpsx7dr5ilb7bw2c` FOREIGN KEY (`slideContentComponent_uuid`) REFERENCES `SlideContentComponent` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Question`
--

DROP TABLE IF EXISTS `Question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Question` (
  `uuid` varchar(255) NOT NULL,
  `deleted` tinyint(1) DEFAULT NULL,
  `transitionType` varchar(255) DEFAULT NULL,
  `test_uuid` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `orderNum` int(11) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FK_3u5be6nyduwxj4uk3g8xlmtfn` (`test_uuid`),
  CONSTRAINT `FK_3u5be6nyduwxj4uk3g8xlmtfn` FOREIGN KEY (`test_uuid`) REFERENCES `Test` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Question_Option`
--

DROP TABLE IF EXISTS `Question_Option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Question_Option` (
  `Question_uuid` varchar(255) NOT NULL,
  `options_uuid` varchar(255) NOT NULL,
  UNIQUE KEY `UK_rk30kg68hro883bswk1vow8ge` (`options_uuid`),
  KEY `FK_rk30kg68hro883bswk1vow8ge` (`options_uuid`),
  KEY `FK_mxnmcp6vfkswhgatc05vbp523` (`Question_uuid`),
  CONSTRAINT `FK_mxnmcp6vfkswhgatc05vbp523` FOREIGN KEY (`Question_uuid`) REFERENCES `Question` (`uuid`),
  CONSTRAINT `FK_rk30kg68hro883bswk1vow8ge` FOREIGN KEY (`options_uuid`) REFERENCES `Option` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Question_Slide`
--

DROP TABLE IF EXISTS `Question_Slide`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Question_Slide` (
  `Question_uuid` varchar(255) NOT NULL,
  `slides_uuid` varchar(255) NOT NULL,
  UNIQUE KEY `UK_lvc45uhe2u8pk2datsj6spxal` (`slides_uuid`),
  KEY `FK_lvc45uhe2u8pk2datsj6spxal` (`slides_uuid`),
  KEY `FK_nxyobrly5dv5391suwsy3the4` (`Question_uuid`),
  CONSTRAINT `FK_lvc45uhe2u8pk2datsj6spxal` FOREIGN KEY (`slides_uuid`) REFERENCES `Slide` (`uuid`),
  CONSTRAINT `FK_nxyobrly5dv5391suwsy3the4` FOREIGN KEY (`Question_uuid`) REFERENCES `Question` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Report`
--

DROP TABLE IF EXISTS `Report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Report` (
  `uuid` varchar(255) NOT NULL,
  `effectiveElapsedDate` datetime DEFAULT NULL,
  `endedAt` datetime DEFAULT NULL,
  `reportCsvPath` varchar(255) DEFAULT NULL,
  `reportDate` datetime DEFAULT NULL,
  `startedAt` datetime DEFAULT NULL,
  `author_uuid` varchar(255) DEFAULT NULL,
  `reportTest_uuid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FK_xmlvtskn093fv864dm8n7ntu` (`author_uuid`),
  KEY `FK_666qw50slxk47ble2wfkj44qs` (`reportTest_uuid`),
  CONSTRAINT `FK_666qw50slxk47ble2wfkj44qs` FOREIGN KEY (`reportTest_uuid`) REFERENCES `Test` (`uuid`),
  CONSTRAINT `FK_xmlvtskn093fv864dm8n7ntu` FOREIGN KEY (`author_uuid`) REFERENCES `EndUser` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReportQuestion`
--

DROP TABLE IF EXISTS `ReportQuestion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ReportQuestion` (
  `uuid` varchar(255) NOT NULL,
  `effectiveElapsedDate` datetime DEFAULT NULL,
  `endedAt` datetime DEFAULT NULL,
  `orderfield` int(11) DEFAULT NULL,
  `startedAt` datetime DEFAULT NULL,
  `question_uuid` varchar(255) DEFAULT NULL,
  `report_uuid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FK_p4uf10d9qxy7pb6br3yxsli8e` (`question_uuid`),
  KEY `FK_fqcuglih9edr6ydtwi5s203me` (`report_uuid`),
  CONSTRAINT `FK_fqcuglih9edr6ydtwi5s203me` FOREIGN KEY (`report_uuid`) REFERENCES `Report` (`uuid`),
  CONSTRAINT `FK_p4uf10d9qxy7pb6br3yxsli8e` FOREIGN KEY (`question_uuid`) REFERENCES `Question` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReportQuestion_ReportSlide`
--

DROP TABLE IF EXISTS `ReportQuestion_ReportSlide`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ReportQuestion_ReportSlide` (
  `ReportQuestion_uuid` varchar(255) NOT NULL,
  `slides_uuid` varchar(255) NOT NULL,
  UNIQUE KEY `UK_6vit8l31bf71pxkrqx55i45ho` (`slides_uuid`),
  KEY `FK_6vit8l31bf71pxkrqx55i45ho` (`slides_uuid`),
  KEY `FK_219elw4g8b9ud4nmmqeom7nyx` (`ReportQuestion_uuid`),
  CONSTRAINT `FK_219elw4g8b9ud4nmmqeom7nyx` FOREIGN KEY (`ReportQuestion_uuid`) REFERENCES `ReportQuestion` (`uuid`),
  CONSTRAINT `FK_6vit8l31bf71pxkrqx55i45ho` FOREIGN KEY (`slides_uuid`) REFERENCES `ReportSlide` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ReportSlide`
--

DROP TABLE IF EXISTS `ReportSlide`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ReportSlide` (
  `uuid` varchar(255) NOT NULL,
  `answers` varchar(255) DEFAULT NULL,
  `effectiveElapsedDate` datetime DEFAULT NULL,
  `endedAt` datetime DEFAULT NULL,
  `expectedAnswers` varchar(255) DEFAULT NULL,
  `mouseTracking` varchar(255) DEFAULT NULL,
  `presentationOrder` int(11) DEFAULT NULL,
  `startedAt` datetime DEFAULT NULL,
  `transitionPerformed` varchar(255) DEFAULT NULL,
  `slide_uuid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FK_mhlrf7obb2ygu0nule0h2c7on` (`slide_uuid`),
  CONSTRAINT `FK_mhlrf7obb2ygu0nule0h2c7on` FOREIGN KEY (`slide_uuid`) REFERENCES `Slide` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Report_Metadata`
--

DROP TABLE IF EXISTS `Report_Metadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Report_Metadata` (
  `Report_uuid` varchar(255) NOT NULL,
  `metadata_uuid` varchar(255) NOT NULL,
  UNIQUE KEY `UK_qwtfxvxm8wa01gyrw9pudgi43` (`metadata_uuid`),
  KEY `FK_qwtfxvxm8wa01gyrw9pudgi43` (`metadata_uuid`),
  KEY `FK_rhd69xdm779s03rsfrtn59ue3` (`Report_uuid`),
  CONSTRAINT `FK_qwtfxvxm8wa01gyrw9pudgi43` FOREIGN KEY (`metadata_uuid`) REFERENCES `Metadata` (`uuid`),
  CONSTRAINT `FK_rhd69xdm779s03rsfrtn59ue3` FOREIGN KEY (`Report_uuid`) REFERENCES `Report` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Report_ReportQuestion`
--

DROP TABLE IF EXISTS `Report_ReportQuestion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Report_ReportQuestion` (
  `Report_uuid` varchar(255) NOT NULL,
  `questions_uuid` varchar(255) NOT NULL,
  UNIQUE KEY `UK_64stjwlcx03qovakycbuvg89w` (`questions_uuid`),
  KEY `FK_64stjwlcx03qovakycbuvg89w` (`questions_uuid`),
  KEY `FK_pgey1y1rw1lawyekgomsoqxvc` (`Report_uuid`),
  CONSTRAINT `FK_64stjwlcx03qovakycbuvg89w` FOREIGN KEY (`questions_uuid`) REFERENCES `ReportQuestion` (`uuid`),
  CONSTRAINT `FK_pgey1y1rw1lawyekgomsoqxvc` FOREIGN KEY (`Report_uuid`) REFERENCES `Report` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Slide`
--

DROP TABLE IF EXISTS `Slide`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Slide` (
  `uuid` varchar(255) NOT NULL,
  `deleted` tinyint(1) DEFAULT NULL,
  `transitionType` varchar(255) DEFAULT NULL,
  `typefield` varchar(255) DEFAULT NULL,
  `question_uuid` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `orderNum` int(11) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FK_i8i7st3jgwctkfeo7qkq1n4br` (`question_uuid`),
  CONSTRAINT `FK_i8i7st3jgwctkfeo7qkq1n4br` FOREIGN KEY (`question_uuid`) REFERENCES `Question` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SlideContentComponent`
--

DROP TABLE IF EXISTS `SlideContentComponent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SlideContentComponent` (
  `uuid` varchar(255) NOT NULL,
  `componentType` varchar(255) DEFAULT NULL,
  `dim` varchar(255) DEFAULT NULL,
  `pos` varchar(255) DEFAULT NULL,
  `media_uuid` varchar(255) DEFAULT NULL,
  `slide_uuid` varchar(255) DEFAULT NULL,
  `test_uuid` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FK_jpdmortgujprf3vrjm1mvtse1` (`media_uuid`),
  KEY `FK_pf0xbd1wdjsjk8b6h53vh9gqc` (`slide_uuid`),
  KEY `FK_6asvxybvo7sq5t1503uq5y7yh` (`test_uuid`),
  CONSTRAINT `FK_6asvxybvo7sq5t1503uq5y7yh` FOREIGN KEY (`test_uuid`) REFERENCES `Test` (`uuid`),
  CONSTRAINT `FK_jpdmortgujprf3vrjm1mvtse1` FOREIGN KEY (`media_uuid`) REFERENCES `Media` (`uuid`),
  CONSTRAINT `FK_pf0xbd1wdjsjk8b6h53vh9gqc` FOREIGN KEY (`slide_uuid`) REFERENCES `Slide` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SlideContentComponent_Option`
--

DROP TABLE IF EXISTS `SlideContentComponent_Option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SlideContentComponent_Option` (
  `SlideContentComponent_uuid` varchar(255) NOT NULL,
  `options_uuid` varchar(255) NOT NULL,
  UNIQUE KEY `UK_kuigwq8i5lvkk075tr7n94irg` (`options_uuid`),
  KEY `FK_kuigwq8i5lvkk075tr7n94irg` (`options_uuid`),
  KEY `FK_drvjbilr4tp4t9ylcqlkjxaes` (`SlideContentComponent_uuid`),
  CONSTRAINT `FK_drvjbilr4tp4t9ylcqlkjxaes` FOREIGN KEY (`SlideContentComponent_uuid`) REFERENCES `SlideContentComponent` (`uuid`),
  CONSTRAINT `FK_kuigwq8i5lvkk075tr7n94irg` FOREIGN KEY (`options_uuid`) REFERENCES `Option` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Slide_Option`
--

DROP TABLE IF EXISTS `Slide_Option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Slide_Option` (
  `Slide_uuid` varchar(255) NOT NULL,
  `options_uuid` varchar(255) NOT NULL,
  UNIQUE KEY `UK_mt6k1fdgck43eg8mx7qp7tdtt` (`options_uuid`),
  KEY `FK_mt6k1fdgck43eg8mx7qp7tdtt` (`options_uuid`),
  KEY `FK_spvg3nau4pcoxas48tuvhue5u` (`Slide_uuid`),
  CONSTRAINT `FK_mt6k1fdgck43eg8mx7qp7tdtt` FOREIGN KEY (`options_uuid`) REFERENCES `Option` (`uuid`),
  CONSTRAINT `FK_spvg3nau4pcoxas48tuvhue5u` FOREIGN KEY (`Slide_uuid`) REFERENCES `Slide` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Slide_SlideContentComponent`
--

DROP TABLE IF EXISTS `Slide_SlideContentComponent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Slide_SlideContentComponent` (
  `Slide_uuid` varchar(255) NOT NULL,
  `slideContent_uuid` varchar(255) NOT NULL,
  UNIQUE KEY `UK_p5jn104pkt739b8b8t2q3bk74` (`slideContent_uuid`),
  KEY `FK_p5jn104pkt739b8b8t2q3bk74` (`slideContent_uuid`),
  KEY `FK_71p4mqadoa445bp7a5pw0swi` (`Slide_uuid`),
  CONSTRAINT `FK_71p4mqadoa445bp7a5pw0swi` FOREIGN KEY (`Slide_uuid`) REFERENCES `Slide` (`uuid`),
  CONSTRAINT `FK_p5jn104pkt739b8b8t2q3bk74` FOREIGN KEY (`slideContent_uuid`) REFERENCES `SlideContentComponent` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Test`
--

DROP TABLE IF EXISTS `Test`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Test` (
  `uuid` varchar(255) NOT NULL,
  `deleted` tinyint(1) DEFAULT NULL,
  `revisionDate` datetime DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `testName` varchar(255) DEFAULT NULL,
  `author_uuid` varchar(255) DEFAULT NULL,
  `testType` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  PRIMARY KEY (`uuid`),
  KEY `FK_o9m5ymaslwy7ax0w4v9mkv01b` (`author_uuid`),
  CONSTRAINT `FK_o9m5ymaslwy7ax0w4v9mkv01b` FOREIGN KEY (`author_uuid`) REFERENCES `EndUser` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Test_Option`
--

DROP TABLE IF EXISTS `Test_Option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Test_Option` (
  `Test_uuid` varchar(255) NOT NULL,
  `options_uuid` varchar(255) NOT NULL,
  UNIQUE KEY `UK_8eqmfddle57gop4qqpc0u6uaj` (`options_uuid`),
  KEY `FK_8eqmfddle57gop4qqpc0u6uaj` (`options_uuid`),
  KEY `FK_g9nkf30vyle6c9wcljck7x6gf` (`Test_uuid`),
  CONSTRAINT `FK_8eqmfddle57gop4qqpc0u6uaj` FOREIGN KEY (`options_uuid`) REFERENCES `Option` (`uuid`),
  CONSTRAINT `FK_g9nkf30vyle6c9wcljck7x6gf` FOREIGN KEY (`Test_uuid`) REFERENCES `Test` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Test_Question`
--

DROP TABLE IF EXISTS `Test_Question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Test_Question` (
  `Test_uuid` varchar(255) NOT NULL,
  `questions_uuid` varchar(255) NOT NULL,
  UNIQUE KEY `UK_19nhhwlnqmob3q4afd7lus6uo` (`questions_uuid`),
  KEY `FK_19nhhwlnqmob3q4afd7lus6uo` (`questions_uuid`),
  KEY `FK_p8coqxoo6sxi73p29i9j0w97s` (`Test_uuid`),
  CONSTRAINT `FK_19nhhwlnqmob3q4afd7lus6uo` FOREIGN KEY (`questions_uuid`) REFERENCES `Question` (`uuid`),
  CONSTRAINT `FK_p8coqxoo6sxi73p29i9j0w97s` FOREIGN KEY (`Test_uuid`) REFERENCES `Test` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Topic`
--

DROP TABLE IF EXISTS `Topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Topic` (
  `uuid` varchar(255) NOT NULL,
  `definition` longtext,
  `word` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `grammar_contentprovideruser`
--

DROP TABLE IF EXISTS `grammar_contentprovideruser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `grammar_contentprovideruser` (
  `Grammar_uuid` varchar(255) NOT NULL,
  `contentProviderList_uuid` varchar(255) NOT NULL,
  KEY `FK_sh3llllhlereu41on0hxlx1ym` (`contentProviderList_uuid`),
  KEY `FK_lsxmq0mppeo22cvf8svqqhhx9` (`Grammar_uuid`),
  CONSTRAINT `FK_lsxmq0mppeo22cvf8svqqhhx9` FOREIGN KEY (`Grammar_uuid`) REFERENCES `Grammar` (`uuid`),
  CONSTRAINT `FK_sh3llllhlereu41on0hxlx1ym` FOREIGN KEY (`contentProviderList_uuid`) REFERENCES `EndUser` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `grammar_editoruser`
--

DROP TABLE IF EXISTS `grammar_editoruser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `grammar_editoruser` (
  `Grammar_uuid` varchar(255) NOT NULL,
  `editorList_uuid` varchar(255) NOT NULL,
  KEY `FK_r3say00ks09am3miru9e05uh7` (`editorList_uuid`),
  KEY `FK_7y91fop0ql69dfji35sf1wdsd` (`Grammar_uuid`),
  CONSTRAINT `FK_7y91fop0ql69dfji35sf1wdsd` FOREIGN KEY (`Grammar_uuid`) REFERENCES `Grammar` (`uuid`),
  CONSTRAINT `FK_r3say00ks09am3miru9e05uh7` FOREIGN KEY (`editorList_uuid`) REFERENCES `EndUser` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `grammarpart_contentprovideruser`
--

DROP TABLE IF EXISTS `grammarpart_contentprovideruser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `grammarpart_contentprovideruser` (
  `GrammarPart_uuid` varchar(255) NOT NULL,
  `contentProviderList_uuid` varchar(255) NOT NULL,
  KEY `FK_4wjrdxg1f1twe361m57reketb` (`contentProviderList_uuid`),
  KEY `FK_q5oadxabxyqpgfv5xowst632g` (`GrammarPart_uuid`),
  CONSTRAINT `FK_4wjrdxg1f1twe361m57reketb` FOREIGN KEY (`contentProviderList_uuid`) REFERENCES `EndUser` (`uuid`),
  CONSTRAINT `FK_q5oadxabxyqpgfv5xowst632g` FOREIGN KEY (`GrammarPart_uuid`) REFERENCES `GrammarPart` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `grammarpart_editoruser`
--

DROP TABLE IF EXISTS `grammarpart_editoruser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `grammarpart_editoruser` (
  `GrammarPart_uuid` varchar(255) NOT NULL,
  `editorList_uuid` varchar(255) NOT NULL,
  KEY `FK_ju9lm5fqmv5lt9nb1gy9w9vfb` (`editorList_uuid`),
  KEY `FK_80u8tb1yiuol2mhwu2hojugia` (`GrammarPart_uuid`),
  CONSTRAINT `FK_80u8tb1yiuol2mhwu2hojugia` FOREIGN KEY (`GrammarPart_uuid`) REFERENCES `GrammarPart` (`uuid`),
  CONSTRAINT `FK_ju9lm5fqmv5lt9nb1gy9w9vfb` FOREIGN KEY (`editorList_uuid`) REFERENCES `EndUser` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `play_evolutions`
--

DROP TABLE IF EXISTS `play_evolutions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `play_evolutions` (
  `id` int(11) NOT NULL,
  `hash` varchar(255) NOT NULL,
  `applied_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `apply_script` text,
  `revert_script` text,
  `state` varchar(255) DEFAULT NULL,
  `last_problem` text,
  `module_key` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`,`module_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-04-23 19:56:59
