package com.degtiarenko.plugin.actions;

import com.degtiarenko.plugin.type.CellPyFileType;
import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;

public class CreateCcpyFileAction extends CreateFileFromTemplateAction implements DumbAware {

    private static final String PYTHON_WITH_CELLS = "Python File with cells";
    private static final String TEMPLATE_NAME = "Cell Python";

    public CreateCcpyFileAction() {
        super(PYTHON_WITH_CELLS, "Creates a Python file with cells",
                CellPyFileType.INSTANCE.getIcon());
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory psiDirectory, CreateFileFromTemplateDialog.Builder builder) {
        builder
            .setTitle(PYTHON_WITH_CELLS)
            .addKind(CellPyFileType.INSTANCE.getName(), CellPyFileType.INSTANCE.getIcon(), TEMPLATE_NAME);
    }

    @Override
    protected String getActionName(PsiDirectory psiDirectory, String newName, String templateName) {
        return "Create Cell Python " + newName;
    }
}
