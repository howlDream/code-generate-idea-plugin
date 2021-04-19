package idea.log;

import java.io.IOException;

/**
 * 日志抽象类
 * @author zheng.li
 */
public abstract class BaseLogs {

    /**
     * 系统用户目录
     */
    public static final String LOG_PATH = System.getProperty("user.home") != null ? System.getProperty("user.home") : "C:/Users/zheng.li";


    /**
     * 写入日志
     * @param content 内容
     * @throws IOException 异常
     */
    void write(String content) throws IOException {}


}
