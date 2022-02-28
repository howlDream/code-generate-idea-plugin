package idea.action;

import com.alibaba.fastjson.JSON;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UastUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
//        normalTypes.put("Temporal", new FakeTemporal());
        normalTypes.put("LocalDateTime", fakeLocalDateTime);
        normalTypes.put("LocalDate", new FakeLocalDate());
//        normalTypes.put("LocalTime", new FakeLocalTime());
//        normalTypes.put("ZonedDateTime", new FakeZonedDateTime());
//        normalTypes.put("YearMonth", new FakeYearMonth());
//        normalTypes.put("UUID", new FakeUUID());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        VirtualFile file = DataKeys.VIRTUAL_FILE.getData(event.getDataContext());
        assert file != null;
        // 获取文件路径
        String path = file.getPresentableUrl();
        final PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        assert psiFile != null;
        final String fileText = psiFile.getText();
        int offset = fileText.contains("interface") ? fileText.indexOf("interface") : fileText.indexOf("class");
        if (offset < 0) {
            ExceptionMessages.showError(event.getProject(),"Can't find interface or class scope！");
            return;
        }

        PsiElement elementAt = psiFile.findElementAt(offset);
        // ADAPTS to all JVM platform languages
        UClass uClass = UastUtils.findContaining(elementAt, UClass.class);
        assert uClass != null;
        try {
            Map<String, Object> kv = parseClass(uClass.getJavaPsi(), event);
            String set = JSON.toJSONString(kv.entrySet());
            Messages.showMessageDialog(event.getProject(),set,"INFO",Messages.getInformationIcon());
        } catch (MyException e) {
            ExceptionMessages.showError(event.getProject(),e.getMessage());
        }

    }

    private Map<String, Object> parseClass(PsiClass psiClass, AnActionEvent event) throws MyException {
        Map<String,Object> map = new HashMap<>(16);
        for (PsiMethod method : psiClass.getAllMethods()) {
            // 获取RequestMapping接口注解
            PsiAnnotation annotation  = method.getAnnotation("org.springframework.web.bind.annotation.RequestMapping");
            if (annotation != null) {
                // 接口地址
                String value = getAttributeFromAnnotation(annotation, event);
                Map<String,Object> requestMap = new HashMap<>();
                for (PsiParameter parameter : method.getParameterList().getParameters()) {
                    // 获取入参类
                    ExceptionMessages.showError(event.getProject(),parameter.getType().getCanonicalText());
                    final PsiElement target =  Objects.requireNonNull(parameter.getTypeElement()).getInnermostComponentReferenceElement().resolve();
                    if (!(target instanceof PsiClass)) {
                        ExceptionMessages.showError(event.getProject(),"不是类");
                        continue;
                    }
                    final PsiClass targetClass = (PsiClass)target;
                    for (PsiField allField : targetClass.getAllFields()) {
                        // Messages.showMessageDialog(event.getProject(),allField.getName(),"INFO",Messages.getInformationIcon());
                        requestMap.put(allField.getName(),parseFieldValueType(allField.getType(),1,event));
                    }
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
    private String getAttributeFromAnnotation(PsiAnnotation annotation, AnActionEvent event) {

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
        return httpMethodWithQuotes.substring(1, httpMethodWithQuotes.length() - 1);

    }

    private Object parseFieldValueType(PsiType type, int level,  AnActionEvent event) throws MyException {

        level = ++level;

        if (type instanceof PsiPrimitiveType) {
            //primitive Type
            return getPrimitiveTypeValue(type);

        } else if (type instanceof PsiArrayType) {
            //array type

            PsiType deepType = type.getDeepComponentType();
            Object obj = parseFieldValueType(deepType, level, event);
            return obj != null ? Collections.singletonList(obj) : new ArrayList<>();

        } else {
            //reference Type

            PsiClass psiClass = PsiUtil.resolveClassInClassTypeOnly(type);
            if (psiClass == null) {
                return new LinkedHashMap<>();
            }
            if (psiClass.isEnum()) { // enum
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

                if (iterable) {// Iterable

                    PsiType deepType = PsiUtil.extractIterableTypeParameter(type, false);
                    Object obj = parseFieldValueType(deepType, level, event);
                    return obj != null ? Collections.singletonList(obj) : new ArrayList<>();

                } else { // Object

                    List<String> retain = new ArrayList<>(fieldTypeNames);
                    retain.retainAll(normalTypes.keySet());
                    if (!retain.isEmpty()) {
                        return this.getFakeValue(normalTypes.get(retain.get(0)));
                    } else {

                        if (level > 500) {
                            throw new MyException("This class reference level exceeds maximum limit or has nested references!");
                        }

                        return parseClass(psiClass, event);
                    }
                }
            }
        }
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
            default:
                return null;
        }
    }


    protected Object getFakeValue(JsonFakeValuesService jsonFakeValuesService) {
        return jsonFakeValuesService.def();
    }

}
