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
    public String servicePath = "com";

    /**
     * 当前项目service实现文件目录
     */
    public String serviceImplPath = "com";

    /**
     * 当前项目interface文件目录
     */
    public String interfacePath = "com";

    /**
     * 当前项目controller 文件目录
     */
    public String controllerPath = "com";

    /**
     * 当前项目request 文件目录
     */
    public String requestPath = "com";

    /**
     * 当前项目model文件目录
     */
    public String modelPath = "com";
    /**
     * 实体文件目录
     */
    public String entityPath = "com";

    /**
     * mapper文件目录
     */
    public String mapperPath;

    public static final String MALL = "mall";

    public static final String RESTAURANT = "restaurant";

    /**
     * 无配置路径时构造方法
     * @param path 根路径
     * @throws MyException 异常
     */
    public ProjectPaths(String path) throws MyException {
        String moduleName = getModuleName(path);
        this.servicePath = path + "/" + moduleName + "-service-spring-boot/src/main/java/com/kykj/tesla/"+ moduleName + "/service/";
        this.serviceImplPath = this.servicePath + "impl/";
        if (RESTAURANT.equals(moduleName)) {
            this.interfacePath = path + "/" + moduleName + "-service-interface/src/main/java/com/kykj/"+ moduleName + "/service/";
        } else if (MALL.equals(moduleName)){
            this.interfacePath = path + "/" + moduleName + "-service-interface/src/main/java/com/kykj/tesla/"+ moduleName + "/v2/";
        } else {
            this.interfacePath = path + "/" + moduleName + "-service-interface/src/main/java/com/kykj/tesla/"+ moduleName + "/service/";
        }
        this.modelPath = this.interfacePath + "model/";
        this.requestPath = this.interfacePath + "request/";
        this.controllerPath = path + "/" + moduleName + "-service-spring-boot/src/main/java/com/kykj/tesla/"+ moduleName + "/controller/";
        this.entityPath = path + "/" + moduleName + "-service-spring-boot/src/main/java/com/kykj/tesla/"+ moduleName + "/entity/";
        this.mapperPath = path + "/" + moduleName + "-service-spring-boot/src/main/java/com/kykj/tesla/"+ moduleName + "/mapper/dao/";
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
                if (e.getText() == null || "".equals(e.getText())) {
                    return;
                }
                ErrorLogs.getInstance().write(e.getName() + ":" + e.getText());
                String filePath = path + "/" + e.getText() + "/";
                if (PathTypeEnum.SERVICE.getName().equals(e.getName())) {
                    this.servicePath = filePath;
                    this.serviceImplPath = filePath + "/impl/";
                } else if (PathTypeEnum.REQUEST.getName().equals(e.getName())) {
                    this.requestPath = filePath;
                } else if (PathTypeEnum.MODEL.getName().equals(e.getName())) {
                    this.modelPath = filePath;
                } else if (PathTypeEnum.MODEL.getName().equals(e.getName())) {
                    this.interfacePath = filePath;
                } else if (PathTypeEnum.CONTROLLER.getName().equals(e.getName())) {
                    this.controllerPath = filePath;
                }
            });
            this.entityPath = this.modelPath;
            this.mapperPath = this.modelPath;
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
