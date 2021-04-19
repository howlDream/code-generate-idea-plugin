package idea.log;

import idea.constance.StringConstance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * 错误日志 单例
 * @author zheng.li
 */
public class ErrorLogs extends BaseLogs {


    /**
     * 实例
     */
    private static ErrorLogs errorLogsInstance;

    private ErrorLogs() {
    }

    @Override
    public void write(String content) {
        try {
            File logPath =  new File(LOG_PATH + "/.code-generate-idea-plugin/");
            File file = new File (logPath, "/error_log.txt");
            if (!logPath.isDirectory()) {
                if (!logPath.mkdir()) {
                    return;
                }
            }
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    System.out.println("error：创建文件失败！");
                    return;
                }
            }

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true), StandardCharsets.UTF_8));
            bw.write("\n" + StringConstance.TXT_SEPARATOR);
            bw.append("\n").append(content);
            bw.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 线程安全懒汉式获取实例
     * @return ErrorLogs实例
     */
    public static synchronized ErrorLogs getInstance() {
        if (errorLogsInstance == null) {
            errorLogsInstance = new ErrorLogs();
        }
        return errorLogsInstance;
    }
}
