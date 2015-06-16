
drop database if exists SE;

create database SE;
 
use SE;
 
CREATE TABLE `autocomp` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `text` varchar(50) NOT NULL DEFAULT '',
  `count` int(11),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=gb2312;

INSERT INTO autocomp (text, count) VALUES ('清华校园', 2);
INSERT INTO autocomp (text, count) VALUES ('清华地图', 3);
#INSERT INTO autocomp (text, count) VALUES ('清华大学', 1);
INSERT INTO autocomp (text, count) VALUES ('测试', 1);

CREATE TABLE `prof` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL DEFAULT '',
  `avatar` varchar(255) NOT NULL DEFAULT '',
  `position` varchar(50) NOT NULL DEFAULT '',
  `tel` varchar(50) NOT NULL DEFAULT '',
  `mail` varchar(50) NOT NULL DEFAULT '',
  `page` varchar(255) NOT NULL DEFAULT '',
  `orientation` varchar(255) NOT NULL DEFAULT '',
  `url` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=gb2312;
