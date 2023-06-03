package me.coolclk.wdisk;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(value = "wdisk")
public class ApplicationProperties {
    /**
     * 网络硬盘访问密码
     */
    private String password = "";

    /**
     * 网络硬盘物理路径
     */
    private String realPath = ".";

    /**
     * 用户登录后留下的Cookie名
     */
    private String loginCookieName = "password";

    /**
     * 用户登录后Cookie的寿命
     */
    private int loginCookieLife = 604800;

    /**
     * 是否允许访客查看文件列表
     */
    private boolean allowGuestListFiles = false;

    /**
     * 是否允许访客下载文件
     */
    private boolean allowGuestDownloadFiles = false;

    /**
     * 是否允许访客上传文件
     */
    private boolean allowGuestControlFiles = false;
}
