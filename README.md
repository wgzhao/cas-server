## Apereo CAS based SSO

基于 [Apereo CAS](https://apereo.github.io/cas) 的 SSO 实现，基于最新的 CAS 7.0 版本，底层 SpringBoot 版本为 3.2.1

采取 Overlay 模式开发。

在官方版本的基础上，主要做了以下改动

- 新增了名为 `mytheme` 的主题
- 修改了 `casLoginView.html` 页面，内嵌了企业微信扫码登录逻辑

## 运行方式

```
java -Dspring.profiles.include=<profile> -jar target/cas-<version>.war
```
