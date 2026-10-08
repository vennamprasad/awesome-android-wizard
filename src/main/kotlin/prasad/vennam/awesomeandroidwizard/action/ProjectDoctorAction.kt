package prasad.vennam.awesomeandroidwizard.action

import prasad.vennam.awesomeandroidwizard.ui.ProjectDoctorDialog
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class ProjectDoctorAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val dialog = ProjectDoctorDialog(project)
        dialog.show()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
