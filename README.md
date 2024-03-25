## Apereo CAS based SSO

基于 [Apereo CAS](https://apereo.github.io/cas) 的 SSO 实现，基于最新的 CAS 7.0 版本，底层 SpringBoot 版本为 3.2.1

采取 Overlay 模式开发。

在官方版本的基础上，主要做了以下改动

- 新增了名为 `gp51` 的主题
- 修改了 `casLoginView.html` 页面，内嵌了企业微信扫码登录逻辑
- 增加了自定义认证，和项目 [sso-wecom-api](https://gitlab.gp51.com/sso-wecom-api) 配合，实对企业微信扫描的认证。

## 运行方式

```
java -Dspring.profiles.include=<profile> -jar target/cas.war
```
