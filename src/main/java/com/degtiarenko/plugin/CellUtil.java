package com.degtiarenko.plugin;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CellUtil {
    public static final String BLOCK_CELL_SEPARATOR = "#%%";

    @Nullable
    public static PsiElement getCellStart(@NotNull PsiElement element) {
        PsiElement prev = Optional.ofNullable(PsiTreeUtil.findFirstParent(element,
                elem -> elem.getParent() instanceof PyFile)).orElse(element);
        while (!(prev.getText().equals(BLOCK_CELL_SEPARATOR))) {
            prev = PsiTreeUtil.getPrevSiblingOfType(element, PsiComment.class);
            if (prev == null) {
                break;
            }
        }
        if (prev == null) {
            return element.getContainingFile().getFirstChild();
        }
        return prev.getNextSibling();
    }

    @NotNull
    public static String getCodeInCell(@Nullable PsiElement element) {
        StringBuilder text = new StringBuilder();
        while (true) {
            if (element != null && !element.getText().equals(BLOCK_CELL_SEPARATOR)) {
                text.append(element.getText());
            } else {
                break;
            }
            element = element.getNextSibling();
        }
        return text.toString();
    }
}
