create table pm_table_accounts (id int, userid varchar(255), user_pwd varchar(255),
    email varchar(255), phone varchar(255), enabled tinyint);
create table pm_table_questions (id int, userid varchar(255), question varchar(255), answer varchar(255));
--  original password is 123123
insert into pm_table_accounts values(1, 'admin', '$2a$08$aQHelUY5kHQDxBcnW3xv7edhymZGZlUCfpjml.IKoNdEs/BfQhnXG', 'admin@example.com', '12312341234', 1);

CREATE TABLE `cas_wecom_appliers` (
  `id` int NOT NULL ,
  `name` varchar(100)   ,
  `agentid` varchar(100)   ,
  `appid` varchar(100)  ,
  `corpid` varchar(100)  ,
  `secret` varbinary(100) ,
  `state` varchar(5)  NOT NULL DEFAULT 'WWLogin',
  `profile` varchar(4)  NOT NULL default 'dev' ,
  PRIMARY KEY (`id`)
) ;
insert into cas_wecom_appliers values(1, '企业微信测试应用', '1000002', 'wwd1a2b3c4d5e6f7g8h9i0j1k2l3m4n5', 'wwd1a2b3c4d5e6f7g8h9i0j1k2l3m4n5', '1234567890abcdef1234567890abcdef', 'WWLogin', 'dev');