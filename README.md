# CoolCLK / WDisk

\[[English](README.md)] \[[简体中文](README_zh-CN.md)]

---

## Info

This is a simple web disk by **Springboot** framework _(Using **Bootstrap** for css)_.

## Using

Download **Java 17** and **release file**. Don't forget to configurate your **Java**.

You only need to use command `java -jar wdisk.jar` and it will be running if there is _no problem_...

## Configuration

Create `application.properties` on jar root or under folder `config/`. Anyway, you just need create it and you can start configurating.

| Name                          | Value       | Description                                                                    | Default Value |
|-------------------------------|-------------|--------------------------------------------------------------------------------|---------------|
| wdisk.password                | **String**  | For you visit your web disk, or you also can set it to _nothing_.              | 123456        |
| wdisk.realPath                | **String**  | Where need your web disk _mirror_ from your computer.                          | .             |
| wdisk.loginCookieName         | **String**  | The cookie after you login to autologin. _Maybe it is not important..._        | password      |
| wdisk.loginCookieLife         | **Int**     | Autologin cookie can be used time.                                             | 604800        |
| wdisk.allowGuestListFiles     | **Boolean** | Set guest is allowing to see your files list.                                  | false         |
| wdisk.allowGuestDownloadFiles | **Boolean** | Set guest is allowing to download your files.                                  | false         |
| wdisk.allowGuestControlFiles  | **Boolean** | Set guest is allowing to change _(Like upload, delete, rename...)_ your files. | false         |
