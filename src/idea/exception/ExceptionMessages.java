package idea.exception;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * @author zheng.li
 */
public class ExceptionMessages {


    public static void showError(Project project,String message) {
        Messages.showMessageDialog(project,message,"错误",Messages.getErrorIcon());
    }

}
