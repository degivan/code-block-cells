package com.degtiarenko.plugin;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class CellUtil {
    public static final String BLOCK_CELL_SEPARATOR = "#%%";

    @NotNull
    public static PsiElement getCellStart(PsiElement element) {
        while (!(element.getParent() == null || element.getParent() instanceof PsiFile)) {
            element = element.getParent();
        }
        while (!(element instanceof PsiComment && element.getText().equals(BLOCK_CELL_SEPARATOR))) {
            PsiElement prev = element.getPrevSibling();
            if (prev == null) {
                break;
            }
            element = prev;
        }
        return element;
    }

    @NotNull
    public static String getCodeInCell(PsiElement element) {
        StringBuilder text = new StringBuilder();
        while (true) {
            element = element.getNextSibling();
            if (element != null && !element.getText().equals(BLOCK_CELL_SEPARATOR)) {
                text.append(element.getText());
            } else {
                break;
            }
        }
        return text.toString();
    }
}
