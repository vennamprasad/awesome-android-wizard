package prasad.vennam.awesomeandroidwizard.action

import prasad.vennam.awesomeandroidwizard.ui.NewFeatureModuleDialog
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class NewAwesomeFeatureModuleAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val dialog = NewFeatureModuleDialog(project)
        dialog.show()
    }

    override fun update(e: AnActionEvent) {
        // Only enabled when an active project is open
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
