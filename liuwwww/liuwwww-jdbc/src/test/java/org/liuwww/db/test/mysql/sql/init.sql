drop table if exists `test_user`;
CREATE TABLE `test_user` (
  `USER_ID` bigint(20) NOT NULL,
  `NAME` varchar(255) DEFAULT NULL,
  `USER_CODE` varchar(255) DEFAULT NULL,
  `PASSWORD` varchar(255) DEFAULT NULL,
  `CREATE_DATE` datetime DEFAULT NULL,
  `STS_DATE` datetime DEFAULT NULL ,
  `STS` char(1) DEFAULT NULL,
  `FIELD1` varchar(255) DEFAULT NULL,
  `FIELD2` varchar(255) DEFAULT NULL,
  `FIELD3` varchar(255) DEFAULT NULL,
  `FIELD4` varchar(255) DEFAULT NULL,
  `FIELD5` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

drop table if exists `test_user2`;
CREATE TABLE `test_user2` (
  `USER_ID` bigint(20) NOT NULL,
  `NAME` varchar(255) DEFAULT NULL,
  `USER_CODE` varchar(255) DEFAULT NULL,
  `PASSWORD` varchar(255) DEFAULT NULL,
  `CREATE_DATE` datetime DEFAULT NULL ,
  `STS_DATE` datetime DEFAULT NULL,
  `STS` char(1) DEFAULT NULL,
  `FIELD1` varchar(255) DEFAULT NULL,
  `FIELD2` varchar(255) DEFAULT NULL,
  `FIELD3` varchar(255) DEFAULT NULL,
  `FIELD4` varchar(255) DEFAULT NULL,
  `FIELD5` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;