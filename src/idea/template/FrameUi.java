package idea.template;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import idea.exception.ExceptionMessages;
import idea.exception.MyException;
import idea.log.ErrorLogs;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author zheng.li
 */
public class FrameUi extends JFrame {


    private Project project;
    private VirtualFile file;

    private JTextField moduleField = new JTextField(10);
    private JTextField keyField = new JTextField(10);
    private JTextField serviceField = new JTextField("service",10);
    private JTextField pojoField = new JTextField("pojo",10);
    private JTextField entityField = new JTextField("entity",10);
    private JTextField daoField = new JTextField("dao",10);
    private JTextField controllerField = new JTextField("controller",10);

    private JCheckBox serviceBox = new JCheckBox(BoxEnum.SERVICE.name);
    private JCheckBox pojoBox = new JCheckBox(BoxEnum.POJO.name);
    private JCheckBox listRequestBox = new JCheckBox(BoxEnum.LIST_REQUEST.name);
    private JCheckBox controllerBox = new JCheckBox(BoxEnum.CONTROLLER.name);

    private JCheckBox pathSettingBox = new JCheckBox("是否使用以下路径配置");


    private List<JTextField> pathFieldList = Arrays.asList(serviceField, pojoField, entityField, daoField,controllerField);
    private List<JCheckBox> boxList = Arrays.asList(serviceBox,pojoBox,listRequestBox, controllerBox);

    public FrameUi(AnActionEvent anActionEvent) throws HeadlessException {
        VirtualFile file = LangDataKeys.VIRTUAL_FILE.getData(anActionEvent.getDataContext());
        assert file != null;
        this.file = file;
        this.project = anActionEvent.getData(PlatformDataKeys.PROJECT);
        this.setTitle("代码文件生成  " + file.getCanonicalPath());
        this.setPreferredSize(new Dimension(1000, 500));
        this.setLocation(120, 50);
        this.pack();
        this.setVisible(true);
        JButton buttonOk = new JButton("ok");
        JButton buttonCancel = new JButton("cancel");
        this.getRootPane().setDefaultButton(buttonOk);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 主控制板展示
        JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mainPanel.setBorder(JBUI.Borders.empty(10, 30, 5, 30));
        mainPanel.setAutoscrolls(true);
        // 类名设置面板
        JPanel moduleFieldPanel = new JPanel();
        moduleFieldPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel moduleLabel = new JLabel("类名:");
        moduleLabel.setSize(new Dimension(20, 30));
        moduleFieldPanel.add(moduleLabel);
        moduleFieldPanel.add(this.moduleField);
        JPanel keyFieldPanel = new JPanel();
        keyFieldPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        keyFieldPanel.add(new JLabel("类名变量前缀:"));
        keyFieldPanel.add(this.keyField);

        JPanel tablePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tablePanel.setBorder(BorderFactory.createTitledBorder("类设置"));
        tablePanel.add(moduleFieldPanel);
        tablePanel.add(keyFieldPanel);
        // 复选框面板
        JPanel boxPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        boxPanel.setBorder(BorderFactory.createTitledBorder("选择要生成的文件"));
        boxPanel.setSize(new Dimension(60, 20));
        this.boxList.forEach(boxPanel::add);
        // 路径设置面板
        JPanel pathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pathPanel.setBorder(BorderFactory.createTitledBorder("路径设置(不包含根路径)"));
        pathPanel.setSize(new Dimension(100,30));
        pathPanel.add(this.pathSettingBox);
        this.pathFieldList.forEach(e -> {
            JLabel pathLabel = new JLabel(e.getText() + "路径:");
            e.setName(e.getText());
            pathPanel.add(pathLabel);
            e.setText(null);
            pathPanel.add(e);
        });

        // 底部按钮
        JPanel paneBottom = new JPanel();
        paneBottom.setLayout(new FlowLayout(FlowLayout.RIGHT));
        paneBottom.add(buttonOk);
        paneBottom.add(buttonCancel);
        // 元素设置
        mainPanel.add(tablePanel);
        mainPanel.add(boxPanel);
        mainPanel.add(pathPanel);
        JPanel contentPane = new JBPanel();
        contentPane.setBorder(JBUI.Borders.empty(5));
        contentPane.setLayout(new BorderLayout());
        contentPane.add(mainPanel, "Center");
        contentPane.add(paneBottom, "South");
        this.setContentPane(contentPane);
        buttonOk.addActionListener(e -> FrameUi.this.onOk());
        buttonCancel.addActionListener(e -> FrameUi.this.onCancel());

    }


    private void onOk() {
        this.dispose();
        // 项目目录路径
        ProjectPaths paths = getProjectPaths(this.file);
        if (paths == null) {
            return;
        }
        ThreadLocal<ProjectPaths> threadLocal = new ThreadLocal<>();
        threadLocal.set(paths);
        String module  = this.moduleField.getText();
        String moduleLittle = this.keyField.getText();
        try {
            CodeTemplateUtils.threadLocal = threadLocal;
            for (JCheckBox box : boxList) {
                BoxEnum boxEnum = BoxEnum.getByName(box.getSelectedObjects() != null ? box.getSelectedObjects()[0].toString() : null );
                if (boxEnum == null) {
                    continue;
                }
                switch (boxEnum) {
                    case SERVICE:
                        CodeTemplateUtils.serviceCodeGenerate(module,moduleLittle);
                        CodeTemplateUtils.serviceImplCodeGenerate(module,moduleLittle);
                        break;
                    case POJO:
                        CodeTemplateUtils.modelCodeGenerate(module,moduleLittle);
                        break;
                    case LIST_REQUEST:
                        CodeTemplateUtils.listRequestCodeGenerate(module,moduleLittle);
                        break;
                    case CONTROLLER:
//                        CodeTemplateUtils.interfaceCodeGenerate(module,moduleLittle);
                        CodeTemplateUtils.controllerCodeGenerate(module,moduleLittle);
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException | MyException e) {
            ErrorLogs.getInstance().write(e.getMessage());
            ExceptionMessages.showError(project,e.getMessage());
            return;
        } finally {
            // 移除当前threadLocal
            CodeTemplateUtils.removeThreadLocal();
        }
        Messages.showMessageDialog(project,file.getPresentableUrl() + " 完成","结果",Messages.getInformationIcon());
    }

    /**
     * 获取项目路径配置
     * @param file  VirtualFile
     * @return ProjectPaths
     */
    @Nullable
    private ProjectPaths getProjectPaths(VirtualFile file) {
        String path = file.getCanonicalPath();
        ProjectPaths paths;
        try {
            if (this.pathSettingBox.getSelectedObjects() != null) {
                paths = new ProjectPaths(path,this.pathFieldList);
            } else {
                paths = new ProjectPaths(path);
            }
        } catch (MyException e) {
            ExceptionMessages.showError(project,e.getMessage());
            return null;
        }
        return paths;
    }

    private void onCancel() {
        this.dispose();
    }


    /**
     * 复选框枚举
     */
    public enum BoxEnum {
        /**
         * service
         */
        SERVICE("service"),
        POJO("pojo"),
        LIST_REQUEST("listRequest"),
        CONTROLLER("controller");

        private String name;

        BoxEnum(String name) {
            this.name = name;
        }

        public static BoxEnum getByName(String name) {
            if (name == null) {
                return null;
            }
            for (BoxEnum e : values()) {
                if (e.name.equals(name)) {
                    return e;
                }
            }
            return null;
        }

    }


}

