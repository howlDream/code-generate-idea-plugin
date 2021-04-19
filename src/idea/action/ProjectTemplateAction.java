package idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import idea.template.FrameUi;
import org.jetbrains.annotations.NotNull;


/**
 * 根据模板生产代码文件
 * @author zheng.li
 */
public class ProjectTemplateAction extends AnAction {


    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        new FrameUi(event);
    }


}
