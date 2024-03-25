CREATE TABLE cas.`cas_user` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL DEFAULT '',
  `password` varchar(255) NOT NULL DEFAULT '',
  `email` varchar(255)  NULL DEFAULT '',
  `phone` varchar(255) DEFAULT NULL,
  `expired` int(11) NOT NULL DEFAULT '0',
  `disabled` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
);

insert into cas.cas_user (username, password) values ('zhangsan', md5('123456')), ('guodongmei', md5('123456'));

select * from cas.cas_user;

create user 'cas'@'%' identified by 'cas@2022';
grant all on cas.* to cas;
