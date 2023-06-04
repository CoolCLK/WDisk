# CoolCLK / WDisk

[ [English](README.md) ] [ [简体中文](README_zh-CN.md) ]

---

## 信息

这是一个基于 **Springboot** 框架的简易网盘 _(使用 **Bootstrap** 作为样式表)_。

## 使用

下载 **Java 17** 还有 **release file**，不要忘记配置你的 **Java** 。

你只需要执行指令 `java -jar wdisk.jar` 然后它就会运行了，当然前提是 _如预期所料那样_ ...

## 配置

新建文件 `application.properties` 在 jar 存放目录或者存放在 `config/` 底下。无论如何，你只需要创建它然后配置它就行了。

| 名称                            | 值          | 信息                                  | 默认值      |
|-------------------------------|------------|-------------------------------------|----------|
| wdisk.password                | **字符串**    | 作为你网盘的登录密码，你也可以设成 _空的_ 。            | 123456   |
| wdisk.realPath                | **字符串**    | 指定你网盘在你电脑上哪个地方。                     | .        |
| wdisk.loginCookieName         | **字符串**    | 自动登录的饼干名称， _尽管大多数情况下这个配置项目不那么重要..._ | password |
| wdisk.loginCookieLife         | **实字**     | 指定自动登录的有效期。                         | 604800   |
| wdisk.allowGuestListFiles     | **布尔值**    | 指定访客能不能查看你的文件列表。                    | false    |
| wdisk.allowGuestDownloadFiles | **布尔值**    | 指定访客能不能下载你的文件。                      | false    |
| wdisk.allowGuestControlFiles  | **布尔值**    | 指定访客能不能修改 _(比如上传、删除、重命名...)_ 你的文件。  | false    |
| server.port                   | **整数**     | 指定端口 _(1-65535)_ 。                  | 8080     |
| server.address                | **字符串**    | 指定主机名。                              |          |
| spring.servlet.multipart.max-file-size                   | **_储存单位_** | 指定单个上传文件最大大小。                       | 10MB     |
| spring.servlet.multipart.max-request-size                   | **_储存单位_** | 指定一次性多个上传文件最大大小。                    | 10MB     |
