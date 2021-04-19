package idea.template;

import idea.exception.MyException;
import idea.log.ErrorLogs;

import javax.swing.*;
import java.util.List;

/**
 * @author zheng.li
 */
public class ProjectPaths {

    /**
     * 当前项目service文件目录
     */
    public String servicePath;

    /**
     * 当前项目service实现文件目录
     */
    public String serviceImplPath;

    /**
     * 当前项目interface文件目录
     */
    public String interfacePath;

    /**
     * 当前项目controller 文件目录
     */
    public String controllerPath;

    /**
     * 当前项目request 文件目录
     */
    public String requestPath;

    /**
     * 当前项目model文件目录
     */
    public String modelPath;

    public static final String MALL = "mall";

    /**
     * 无配置路径时构造方法
     * @param path 根路径
     * @throws MyException 异常
     */
    public ProjectPaths(String path) throws MyException {
        String moduleName = getModuleName(path);
        this.servicePath = path + "/" + moduleName + "-service-spring-boot/src/main/java/com/kykj/tesla/"+ moduleName + "/service/";
        this.serviceImplPath = this.servicePath + "impl/";
        if (!MALL.equals(moduleName)) {
            this.interfacePath = path + "/" + moduleName + "-service-interface/src/main/java/com/kykj/tesla/"+ moduleName + "/";
        } else {
            this.interfacePath = path + "/" + moduleName + "-service-interface/src/main/java/com/kykj/tesla/"+ moduleName + "/v2/";
        }
        this.modelPath = this.interfacePath + "model/";
        this.requestPath = this.interfacePath + "request/";
        this.controllerPath = path + "/" + moduleName + "-service-spring-boot/src/main/java/com/kykj/tesla/"+ moduleName + "/controller/";
    }

    /**
     * 按照输入的路径配置 构造方法
     * @param path 根路径
     * @param pathFieldList 路径配置输入值
     * @throws MyException 异常
     */
    public ProjectPaths(String path, List<JTextField> pathFieldList) throws MyException {
        try {
            pathFieldList.forEach(e -> {
                String filePath = path + "/" + e.getText() + "/";
                if ("service".equals(e.getName())) {
                    this.servicePath = filePath;
                } else if ("request".equals(e.getName())) {
                    this.requestPath = filePath;
                } else if ("model".equals(e.getName())) {
                    this.modelPath = filePath;
                } else if ("interface".equals(e.getName())) {
                    this.interfacePath = path;
                } else if ("controller".equals(e.getName())) {
                    this.controllerPath = path;
                }
            });
        } catch (Exception e) {
            throw new MyException("路径读取失败");
        }
    }

    private String getModuleName(String path) throws MyException {
        String name;
        try {
            String serviceName = path.substring(path.lastIndexOf("/") + 1);
            name  = serviceName.substring(0,serviceName.lastIndexOf("-"));
        } catch (Exception e) {
            ErrorLogs.getInstance().write(e.getMessage());
            throw new MyException("获取项目名失败！");
        }
        return name;
    }


}
