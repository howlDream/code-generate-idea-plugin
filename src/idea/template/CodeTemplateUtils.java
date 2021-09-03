package idea.template;

import com.alibaba.fastjson.JSON;
import idea.exception.MyException;
import idea.log.ErrorLogs;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 代码文件生成 工具类
 * @author zheng.li
 */
public class CodeTemplateUtils {

    /**
     * service文件模板路径
     */
    public static final String SERVICE_TEMPLATE = "templates/ServiceTemplate.txt";

    /**
     * service实现文件模板路径
     */
    public static final String SERVICE_IMPL_TEMPLATE = "templates/ServiceImplTemplate.txt";

    /**
     * interface 文件模板路径
     */
    public static final String INTERFACE_TEMPLATE = "templates/MerchantInterfaceTemplate.txt";

    /**
     * controller 文件模板路径
     */
    public static final String CONTROLLER_TEMPLATE = "templates/MerchantControllerTemplate.txt";

    public static final String GET_REQUEST_TEMPLATE = "templates/GetRequestTemplate.txt";

    public static final String REQUEST_TEMPLATE = "templates/ListRequestTemplate.txt";

    public static final String MODEL_TEMPLATE = "templates/ModelTemplate.txt";

    public static final String LIST_MODEL_TEMPLATE = "templates/ListModelTemplate.txt";

    public static ThreadLocal<ProjectPaths> threadLocal = null;


    /**
     * service 代码文件自动生成
     * @param module 模块名
     * @param moduleLittle 小写
     */
    public static void serviceCodeGenerate(String module,String moduleLittle) throws IOException, MyException {
        assert threadLocal != null;
        codeGenerate(module,moduleLittle, SERVICE_TEMPLATE, threadLocal.get().servicePath,SystemConstance.SERVICE_SUFFIX, threadLocal.get().servicePath);
    }

    /**
     * serviceImpl 代码文件自动生成
     * @param module 模块名
     * @param moduleLittle 小写
     */
    public static void serviceImplCodeGenerate(String module,String moduleLittle) throws IOException, MyException {
        assert threadLocal != null;
        codeGenerate(module,moduleLittle, SERVICE_IMPL_TEMPLATE,threadLocal.get().serviceImplPath,SystemConstance.SERVICE_IMPL_SUFFIX, threadLocal.get().serviceImplPath);
    }

    /**
     * interface 代码文件自动生成
     * @param module 模块
     * @param moduleLittle 模块小写
     */
    public static void interfaceCodeGenerate(String module,String moduleLittle) throws IOException, MyException {
        assert threadLocal != null;
        codeGenerate(module,moduleLittle, INTERFACE_TEMPLATE,threadLocal.get().interfacePath,SystemConstance.INTERFACE_SUFFIX, threadLocal.get().interfacePath);
    }

    /**
     * controller 代码文件自动生成
     * @param module 模块名
     * @param moduleLittle 模块小写
     */
    public static void controllerCodeGenerate(String module,String moduleLittle) throws IOException, MyException {
        assert threadLocal != null;
        codeGenerate(module,moduleLittle, CONTROLLER_TEMPLATE,threadLocal.get().controllerPath,SystemConstance.CONTROLLER_SUFFIX, threadLocal.get().controllerPath);
    }

    public static void getRequestCodeGenerate(String module,String moduleLittle) throws IOException, MyException {
        assert threadLocal != null;
        codeGenerate(module,moduleLittle,GET_REQUEST_TEMPLATE,threadLocal.get().requestPath,SystemConstance.GET_REQUEST_SUFFIX, threadLocal.get().requestPath);
    }

    public static void listRequestCodeGenerate(String module, String moduleLittle) throws IOException, MyException {
        assert threadLocal != null;
        codeGenerate(module,moduleLittle,REQUEST_TEMPLATE,threadLocal.get().requestPath,SystemConstance.LIST_REQUEST_SUFFIX, threadLocal.get().requestPath);
    }

    public static void modelCodeGenerate(String module,String moduleLittle) throws IOException, MyException {
        assert threadLocal != null;
        codeGenerate(module,moduleLittle,MODEL_TEMPLATE,threadLocal.get().modelPath,SystemConstance.MODEL_SUFFIX,threadLocal.get().modelPath);
    }

    public static void listModelCodeGenerate(String module,String moduleLittle) throws IOException, MyException {
        assert threadLocal != null;
        codeGenerate(module,moduleLittle,LIST_MODEL_TEMPLATE,threadLocal.get().modelPath,SystemConstance.LIST_MODEL_SUFFIX, threadLocal.get().modelPath);
    }

    /**
     *  代码文件生成
     * @param module 模块名
     * @param moduleLittle 模块名小写
     * @param templatePath 模板路径
     * @param filePath 生成文件路径
     * @param fileSuffix 生成文件名后缀
     * @param packagePath 包路径
     * @throws IOException IOException
     */
    public static void codeGenerate(String module, String moduleLittle, String templatePath, String filePath, String fileSuffix, String packagePath) throws IOException, MyException {

        // 1.将模版以文件的形式读入

        /*
             根据文件地址找到文件
            之所以不能读取Jar包中的文件，这主要是因为jar包是一个单独的文件而非文件夹，绝对不可能通过file:/e:/.../ResourceJar.jar/resource /res.txt这种形式的文件URL来定位res.txt。
            所以即使是相对路径，也无法定位到jar文件内的txt文件。
            我们可以用类装载器(ClassLoader)来读取jar包中的文件。
            我们需要利用this.getClass().getResourceAsStream方法，以流的形式拿到Jar包中的文件
         */
        InputStream inputStream = CodeTemplateUtils.class.getClassLoader().getResourceAsStream(templatePath);
        if (inputStream == null) {
            throw new MyException("模板文件读取失败！");
        }

        // 2.将读入的文件转为string 字符串
        // 读取其内容
        String data;
        StringBuilder sb = new StringBuilder();
        try {
            // 指定utf-8编码
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String txt;
            while ((txt = br.readLine()) != null) {
                sb.append("\n").append(txt);
            }
            br.close();
            data = sb.toString();
        } catch (Exception e) {
            ErrorLogs.getInstance().write("读取模板文件错误： " + e.getLocalizedMessage());
            throw new MyException("读取模板文件错误！");
        }

        // 3.将转为的string字符串 中的占位符 替换成自己想要的内容

        // 定义要替换的内容参数 map的key为和模版中${}内的值一致 value为要替换的结果
        Map<String, Object> param = new HashMap<>(16);
        param.put("module", module);
        param.put("moduleLittle",moduleLittle);
        param.put("path",filePathToPackagePath(packagePath));
        param.put("entityPath",filePathToPackagePath(threadLocal.get().entityPath));
        param.put("mapperPath",filePathToPackagePath(threadLocal.get().mapperPath));
        param.put("servicePath",filePathToPackagePath(threadLocal.get().servicePath));
        param.put("modelPath",filePathToPackagePath(threadLocal.get().modelPath));
        param.put("requestPath",filePathToPackagePath(threadLocal.get().requestPath));
        param.put("interfacePath",filePathToPackagePath(threadLocal.get().interfacePath));
        param.put("controllerPath",filePathToPackagePath(threadLocal.get().controllerPath));
        /*
            velocity 引擎启动之后，其尝试将日志文件写入tomcat所在目录文件中去，
            所以可以强制将日志写入tomcat标准日志中去，要做到这样就是以下所配置的属性
         */
        VelocityEngine engine = new VelocityEngine();
        Properties props = new Properties();
        props.put("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
        props.put("runtime.log.logsystem.log4j.category", "velocity");
        props.put("runtime.log.logsystem.log4j.logger", "velocity");
        engine.init(props);
        // 创建模版引起上下文 并传入要替换的参数
        VelocityContext vc = new VelocityContext(param);
        // 创建StringWriter对象 其内部是对StringBuffer进行的操作
        StringWriter writer = new StringWriter();
        // 模版引起开始替换模版内容
        engine.evaluate(vc,writer,"code_gen",data);
        // 替换之后的字符串
        String result = writer.getBuffer().toString();

        // 4.将新的字符串输出到文件

        // 新文件的路径+文件名+后缀
        ErrorLogs.getInstance().write("outFile: " + filePath + module + fileSuffix);
        File outFile = new File(filePath + module + fileSuffix);
        if (!outFile.exists()) {
            boolean isOut = outFile.createNewFile();
            if (!isOut) {
                System.out.println("生成文件失败");
                throw new MyException("生成文件失败！");
            }
        }
        Path outPath = Paths.get(outFile.getAbsolutePath());
        // 将文件写入
        try (BufferedWriter out = Files.newBufferedWriter(outPath)) {
            out.write(result);
        }
    }

    /**
     * 将文件路径 转换为 包路径
     * @param filePath 文件路径
     * @return 包路径
     */
    private static String filePathToPackagePath(String filePath) throws MyException {
        ErrorLogs.getInstance().write("paths:" + JSON.toJSONString(threadLocal.get()));
        if (StringUtils.isEmpty(filePath)) {
            return "";
        }
        String[] chars = filePath.split("/");
        int index = 0;
        StringBuilder packageSb = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            if ("com".equals(chars[i])) {
                index = i;
                packageSb.append(chars[i]);
            } else if (index > 0) {
                packageSb.append(".").append(chars[i]);
            }
        }
        if ("".equals(packageSb.toString())) {
            throw new MyException("基础包请以‘com’命名");
        }
        return packageSb.toString();
    }

    public static void removeThreadLocal() {
        threadLocal.remove();
    }

    public static void main (String[] args) throws IOException, MyException {

        ProjectPaths paths = new ProjectPaths("D:/data/mall-service");
        threadLocal = new ThreadLocal<ProjectPaths>();
        threadLocal.set(paths);

        String module = "MallV2Goods";
        String moduleLittle = "mallV2Goods";

        modelCodeGenerate(module,moduleLittle);

        listModelCodeGenerate(module,moduleLittle);

        getRequestCodeGenerate(module,moduleLittle);

        listRequestCodeGenerate(module,moduleLittle);

        serviceCodeGenerate(module,moduleLittle);

        serviceImplCodeGenerate(module,moduleLittle);

        interfaceCodeGenerate(module,moduleLittle);

        controllerCodeGenerate(module,moduleLittle);

    }


}
