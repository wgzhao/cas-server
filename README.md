## Apereo CAS based SSO

基于 [Apereo CAS](https://apereo.github.io/cas) 的 SSO 实现，基于最新的 CAS 7.0.x 版本，底层 SpringBoot 版本为 3.2.1

采取 Overlay 模式开发。

在官方版本的基础上，主要做了以下改动

- 新增了名为 `mytheme` 的主题
- 修改了 `casLoginView.html` 页面，内嵌了企业微信扫码登录逻辑


## 运行方式

```
java -Dspring.profiles.include=dev -jar target/cas-1.5.0-SNAPSHOT.war
```

企微的主体相关信息保存在数据库中，表结构如下：

```sql
CREATE TABLE `cas_wecom_appliers` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100)  DEFAULT NULL COMMENT '显示的主体名称',
  `agentid` varchar(100)  DEFAULT NULL comment '应用的AgentID',
  `appid` varchar(100)  DEFAULT NULL comment '应用的AppID, 大部分情况下和 corpid 相同',
  `corpid` varchar(100)  DEFAULT NULL comment '企业微信的CorpID',
  `secret` varbinary(100) DEFAULT NULL COMMENT '应用的Secret',
  `state` varchar(5)  NOT NULL DEFAULT 'WWLogin',
  `profile` varchar(4)  NOT NULL COMMENT '应用的环境; dev/test/uat/prod',
  PRIMARY KEY (`id`),
  key `idx_cas_wecom_appliers_corpid` (`corpid`),
) ENGINE=InnoDB  COMMENT='企业微信主体信息'
```

如果只有一个企业微信主体，则可以考虑直接在程序内指定，不需要从数据库中读取。