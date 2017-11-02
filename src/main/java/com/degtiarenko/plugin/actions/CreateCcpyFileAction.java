package com.degtiarenko.plugin.actions;

import com.degtiarenko.plugin.type.CellPyFileType;
import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;

public class CreateCcpyFileAction extends CreateFileFromTemplateAction implements DumbAware {
    public CreateCcpyFileAction() {
        super("Python with cells", "Creates an Python file with cells", null);
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory psiDirectory, CreateFileFromTemplateDialog.Builder builder) {
        builder
            .setTitle("Python with cells")
            .addKind("Cell Python", CellPyFileType.INSTANCE.getIcon(), "Cell Python");
    }

    @Override
    protected String getActionName(PsiDirectory psiDirectory, String newName, String templateName) {
        return "Create Cell Python " + newName;
    }
}
