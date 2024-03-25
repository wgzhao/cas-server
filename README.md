## Apereo CAS based SSO

基于 [Apereo CAS](https://apereo.github.io/cas) 的 SSO 实现，基于最新的 CAS 7.0 版本，底层 SpringBoot 版本为 3.2.1

采取 Overlay 模式开发。

在官方版本的基础上，主要做了以下改动

- 新增了名为 `gp51` 的主题
- 修改了 `casLoginView.html` 页面，内嵌了企业微信扫码登录逻辑
- 增加了自定义认证，和项目 [sso-wecom-api](https://gitlab.gp51.com/sso-wecom-api) 配合，实对企业微信扫描的认证。

## 部署注意

因为企业微信回调地址的限制，目前绑定了域名 `https://cas-test.gp622.com:10002`

其访问流是：

```
Internet -> FW(218.76.56.86:10002) -> nginx(188.175.2.18:80) -> sso-cas/sso-wecom-api
```