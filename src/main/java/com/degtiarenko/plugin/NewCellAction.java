package com.degtiarenko.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;

import static com.degtiarenko.plugin.CellUtil.isFileOfGoodType;

public class NewCellAction extends AnAction {
    private static final String NEW_CELL_FILE_END = CellUtil.BLOCK_CELL_SEPARATOR + "\n";

    public NewCellAction()  {
        super("New Cell","Create new cell", null);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        PsiFile file = CommonDataKeys.PSI_FILE.getData(e.getDataContext());

        if (file != null && isFileOfGoodType(file)) {
            WriteCommandAction.runWriteCommandAction(e.getProject(), () -> addNewCell(editor));
        }
    }

    private void addNewCell(Editor editor) {
        if (editor != null) {
            Document document = editor.getDocument();
            int end = document.getTextLength();
            String cellSeparator = end == 0 ? NEW_CELL_FILE_END : "\n" + NEW_CELL_FILE_END;

            document.insertString(end, cellSeparator);
            editor.getCaretModel().moveToOffset(document.getTextLength());
        }
    }

    public void update(AnActionEvent e) {
        PsiFile file = CommonDataKeys.PSI_FILE.getData(e.getDataContext());
        Presentation presentation = e.getPresentation();

        if (file != null) {
            presentation.setEnabledAndVisible(isFileOfGoodType(file));
        }
    }
}

