package prasad.vennam.awesomeandroidwizard.action

import prasad.vennam.awesomeandroidwizard.ui.AwesomeWizardDialog
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class NewAwesomeProjectAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val dialog = AwesomeWizardDialog(project)
        dialog.show()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
