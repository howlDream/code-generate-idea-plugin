package idea.action;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiUtil;
import idea.exception.ExceptionMessages;
import idea.exception.MyException;
import idea.fake.FakeBoolean;
import idea.fake.FakeChar;
import idea.fake.FakeDecimal;
import idea.fake.FakeInteger;
import idea.fake.FakeLocalDate;
import idea.fake.FakeLocalDateTime;
import idea.fake.FakeString;
import idea.fake.JsonFakeValuesService;
import idea.log.BaseLogs;
import idea.log.ErrorLogs;
import idea.postman.PostmanCollection;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UastUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 生成当前文件接口postman导入文件【可通用】
 * @author zheng.li
 */
public class CreatePostManFileAction extends AnAction {

    private final List<String> iterableTypes = Arrays.asList(
            "Iterable",
            "Collection",
            "List",
            "Set");

    private final Map<String, JsonFakeValuesService> normalTypes = new HashMap<>();

    /**
     * 排除的字段
     */
    private static List<String> excludeField = Arrays.asList("httpHeader","browserInfo","ip","loginUserName","ignoreConCheck",
                                                            "loginUser","locale","distributorMark");


    {
        FakeDecimal fakeDecimal = new FakeDecimal();
        FakeLocalDateTime fakeLocalDateTime = new FakeLocalDateTime();
        normalTypes.put("Boolean", new FakeBoolean());
        normalTypes.put("Float", fakeDecimal);
        normalTypes.put("Double", fakeDecimal);
        normalTypes.put("BigDecimal", fakeDecimal);
        normalTypes.put("Number", new FakeInteger());
        normalTypes.put("Character", new FakeChar());
        normalTypes.put("CharSequence", new FakeString());
        normalTypes.put("Date", fakeLocalDateTime);
        normalTypes.put("LocalDateTime", fakeLocalDateTime);
        normalTypes.put("LocalDate", new FakeLocalDate());
    }


    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        VirtualFile file = DataKeys.VIRTUAL_FILE.getData(event.getDataContext());
        assert file != null;
        // 获取文件名
        String fileName = file.getName();
        // 检验文件
        final PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        assert psiFile != null;
        final String fileText = psiFile.getText();
        int offset = fileText.contains("interface") ? fileText.indexOf("interface") : fileText.indexOf("class");
        if (offset < 0) {
            ExceptionMessages.showError(event.getProject(),"Can't find interface or class scope！");
            return;
        }
        String port = Messages.showInputDialog(event.getProject(), "port（eg:6023,6038）", "Input Service Port", Messages.getQuestionIcon());
        if (port == null) {
            return;
        }
        PsiElement elementAt = psiFile.findElementAt(offset);
        // ADAPTS to all JVM platform languages
        UClass uClass = UastUtils.findContaining(elementAt, UClass.class);
        assert uClass != null;
        try {
            Map<String, Object> kv = parseInterface(uClass.getJavaPsi(), event);
            // 生成文件
            generatePostmanFile(kv,fileName,port);
            Messages.showMessageDialog(event.getProject(),"路径：" + BaseLogs.LOG_PATH + "/collection-" + fileName.replace(".java","") + ".json","INFO",Messages.getInformationIcon());
        } catch (MyException e) {
            ExceptionMessages.showError(event.getProject(),e.getMessage());
        } catch (IOException e1) {
            ExceptionMessages.showError(event.getProject(),e1.getLocalizedMessage());
        }

    }

    /**
     * 生成postman文件 new
     * @param kv 接口和入参映射
     */
    private static void generatePostmanFile(Map<String, Object> kv,String fileName,String port) throws IOException, MyException {
        Random random = new Random(System.currentTimeMillis());
        fileName = fileName.replace(".java","");
        PostmanCollection.Info info = new PostmanCollection.Info();
        info.name = fileName;
        info.postmanId = random.nextLong() + "AUTO";
        PostmanCollection.Item0 item = new PostmanCollection.Item0();
        item.item = new ArrayList<>();
        item.name = fileName;
        for (Map.Entry<String, Object> entry : kv.entrySet()) {
            String apiPath = port + entry.getKey();
            String[] pathArray = entry.getKey().split("/");
            String raw = JSON.toJSONString(entry.getValue());
            PostmanCollection.Item1 item1 = PostmanCollection.Item1Builder.buildItem1(port,apiPath,pathArray,raw).build();
            // 加入接口集合
            item.item.add(item1);
        }
        PostmanCollection postmanCollection = PostmanCollection.PostmanCollectionBuilder.aPostmanCollection(info,Collections.singletonList(item)).build();
        String content = JSONObject.toJSONString(postmanCollection);
        File outFile = new File(BaseLogs.LOG_PATH + "/collection-" + fileName + ".json");
        if (!outFile.exists()) {
            boolean isOut = outFile.createNewFile();
            if (!isOut) {
                throw new MyException("create file fail！");
            }
        }
        Path outPath = Paths.get(outFile.getAbsolutePath());
        // 将文件写入
        try (BufferedWriter out = Files.newBufferedWriter(outPath)) {
            out.write(content);
        } catch (IOException e) {
            throw new MyException("create file fail！");
        }

    }

    /**
     * 获取接口类的路径和入参映射
     * @param psiClass 类数据
     * @param event  event
     * @return Map<String, Object>
     */
    private Map<String, Object> parseInterface(PsiClass psiClass, AnActionEvent event){
        Map<String,Object> map = new LinkedHashMap<>(16);
        // 获取类上的RequestMapping注解
        PsiAnnotation classAnnotation  = psiClass.getAnnotation("org.springframework.web.bind.annotation.RequestMapping");
        String urlPrefix = "";
        if (classAnnotation != null) {
            PsiAnnotationMemberValue annotationMemberValue = classAnnotation.findAttributeValue("value");
            if (annotationMemberValue == null || StringUtils.isEmpty(annotationMemberValue.getText())) {
                annotationMemberValue = classAnnotation.findAttributeValue("path");
            }
            if (annotationMemberValue != null) {
                String path = annotationMemberValue.getText();
                if (path.contains("+")) {
                    String variable = path.substring(0,path.indexOf("+")).trim();
                    urlPrefix = "{{" + variable + "}}" + path.substring(path.indexOf("+"));
                } else {
                    urlPrefix = path;
                }
                urlPrefix = urlPrefix.replaceAll("\"","").replaceAll("\\+ ","").replaceAll("\\+","");
            }
        }
        for (PsiMethod method : psiClass.getAllMethods()) {
            // 获取RequestMapping接口注解
            PsiAnnotation annotation  = method.getAnnotation("org.springframework.web.bind.annotation.RequestMapping");
            if (annotation == null) {
                annotation = method.getAnnotation("org.springframework.web.bind.annotation.PostMapping");
            }
            if (annotation != null) {
                // 接口地址
                String value = getAttributeFromAnnotation(urlPrefix,annotation, event);
                Map<String,Object> requestMap = new HashMap<>(16);
                ErrorLogs.getInstance().write("method:  " + method.getName());
                for (PsiParameter parameter : method.getParameterList().getParameters()) {
                    // 获取入参类
                    if (parameter.getTypeElement() == null || parameter.getTypeElement().getInnermostComponentReferenceElement() == null) {
                        continue;
                    }
                    final PsiElement target =  parameter.getTypeElement().getInnermostComponentReferenceElement().resolve();
                    if (!(target instanceof PsiClass)) {
                        ExceptionMessages.showError(event.getProject(),"not class");
                        continue;
                    }
                    final PsiClass targetClass = (PsiClass)target;
                    ErrorLogs.getInstance().write("parameter：" + parameter.getText());
                    if ( parameter.getTypeElement().isInferredType() ||
                            parameter.getAnnotation("org.springframework.web.bind.annotation.RequestBody") == null ) {
                        // 参数为java基本类型，非requestBody,封装后直接返回
                        value += "?" + parameter.getName() + "=1";
                        requestMap.put(parameter.getName(),1);
                        map.put(value,requestMap);
                        ErrorLogs.getInstance().write("非json入参");
                        continue;
                    }
                    for (PsiField allField : targetClass.getAllFields()) {
                        if (targetClass.getSuperClass() != null) {
                            if ("BaseDTO".equals(targetClass.getSuperClass().getName()) && excludeField.contains(allField.getName())) {
                                // 父类一些基本入参，网关处理，这里不mock
                                continue;
                            }
                        }
                        requestMap.put(allField.getName(),parseFieldValueType(allField.getType(),1,event,allField.getName()));
                    }
                    // 只用第一个参数
                    break;

                }
                if (!requestMap.containsKey("frontDev")) {
                    // 不包含frontDev,则加一个
                    requestMap.put("frontDev",1);
                }
                map.put(value,requestMap);
            }
        }
        return map;
    }


    /**
     * 获取注解的value值
     * @param annotation 注解
     * @param event event
     * @return String
     */
    private String getAttributeFromAnnotation(String urlPrefix,PsiAnnotation annotation, AnActionEvent event) {

        String annotationQualifiedName = annotation.getQualifiedName();
        if (annotationQualifiedName == null) {
            ExceptionMessages.showError(event.getProject(),"invalid Annotation！");
            return "";
        }

        PsiAnnotationMemberValue annotationMemberValue = annotation.findAttributeValue("value");
        if (annotationMemberValue == null) {
            ExceptionMessages.showError(event.getProject(),"Can't find attribute！");
            return "";
        }
        String httpMethodWithQuotes = annotationMemberValue.getText();
        return urlPrefix + httpMethodWithQuotes.substring(1, httpMethodWithQuotes.length() - 1);

    }

    /**
     * 生成字段数据
     * @param type 字段类型
     * @param level 层级
     * @param event event
     * @return Object
     */
    private Object parseFieldValueType(PsiType type, int level,  AnActionEvent event,String valueName) {

        level = ++level;

        if (type instanceof PsiPrimitiveType) {
            //primitive Type
            return getPrimitiveTypeValue(type);

        } else if (type instanceof PsiArrayType) {
            //array type
            PsiType deepType = type.getDeepComponentType();
            Object obj = parseFieldValueType(deepType, level, event,valueName);
            return obj != null ? Collections.singletonList(obj) : new ArrayList<>();

        } else {
            //reference Type
            PsiClass psiClass = PsiUtil.resolveClassInClassTypeOnly(type);
            if (psiClass == null) {
                return new LinkedHashMap<>();
            }
            if ("com.alibaba.fastjson.JSONObject".equals(type.getCanonicalText())) {
                ErrorLogs.getInstance().write("JSONObject");
                return new LinkedHashMap<>();
            }
            if (psiClass.isEnum()) {
                // enum
                for (PsiField field : psiClass.getFields()) {
                    if (field instanceof PsiEnumConstant) {
                        return field.getName();
                    }
                }
                return "";
            } else {
                List<String> fieldTypeNames = new ArrayList<>();

                fieldTypeNames.add(type.getPresentableText());
                fieldTypeNames.addAll(Arrays.stream(type.getSuperTypes())
                        .map(PsiType::getPresentableText).collect(Collectors.toList()));


                boolean iterable = fieldTypeNames.stream().map(typeName -> {
                    int subEnd = typeName.indexOf("<");
                    return typeName.substring(0, subEnd > 0 ? subEnd : typeName.length());
                }).anyMatch(iterableTypes::contains);

                if (iterable) {
                    // Iterable

                    PsiType deepType = PsiUtil.extractIterableTypeParameter(type, false);
                    Object obj = parseFieldValueType(deepType, level, event,valueName);
                    return obj != null ? Collections.singletonList(obj) : new ArrayList<>();

                } else { // Object

                    List<String> retain = new ArrayList<>(fieldTypeNames);
                    retain.retainAll(normalTypes.keySet());
                    if (!retain.isEmpty()) {
                        return this.getFakeValue(normalTypes.get(retain.get(0)),valueName);
                    } else {

                        if (level > 5) {
                            return null;
                        }
                        return parseClass(psiClass,level,event);
                    }
                }
            }
        }
    }

    private Map<String, Object> parseClass(PsiClass psiClass, int level, AnActionEvent event) {
        ErrorLogs.getInstance().write("class: " + psiClass.getQualifiedName());
        PsiAnnotation annotation = psiClass.getAnnotation(com.fasterxml.jackson.annotation.JsonIgnoreType.class.getName());
        if (annotation != null) {
            return null;
        }
        Map<String,Object> map = new LinkedHashMap<>();
        for (PsiField field : psiClass.getAllFields()) {
            Object obj = parseFieldValueType(field.getType(), level,event,field.getName());
//            ErrorLogs.getInstance().write(field.getName() + ":" + (obj != null ? obj.toString() : "null"));
            map.put(field.getName(),obj);
        }
        return map;
    }

    /**
     * 获取基本类型的默认值
     * @param type PsiType
     * @return Object
     */
    public Object getPrimitiveTypeValue(PsiType type) {
        switch (type.getCanonicalText()) {
            case "boolean":
                return false;
            case "byte":
            case "short":
            case "int":
            case "long":
                return 0;
            case "float":
            case "double":
                return 0.0;
            case "char":
                return '0';
            case "JSONObject":
                return new JSONObject();
            default:
                return null;
        }
    }

    /**
     * 获取相应类型的假数据
     * @param jsonFakeValuesService  JsonFakeValuesService
     * @param valueName 字段名
     * @return Object
     */
    protected Object getFakeValue(JsonFakeValuesService jsonFakeValuesService,String valueName) {
        return jsonFakeValuesService.randomValue(valueName);
    }

    public static void main(String[] args) {
//        System.out.println(String.format(POSTMAN_INFO_START,"q1231231","测试") + POSTMAN_ITEM_START
//                + String.format(POSTMAN_ITEM,"6012/api/test/test","{\"myDistributorInfoId\":1}","6012/api/test/test","\"test\",\"test\"")
//                + POSTMAN_ITEM_END
//                + POSTMAN_INFO_END
//        );
//        try {
//            Map<String,Object> map = new HashMap<>();
//            Map<String,Object> valueMap = new HashMap<>();
//            valueMap.put("id",1);
//            valueMap.put("name","232");
//            map.put("/api/test/test",valueMap);
//            generatePostmanFile(map,"file","1111");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


    }

}
