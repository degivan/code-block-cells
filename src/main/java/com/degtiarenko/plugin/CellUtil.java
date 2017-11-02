package com.degtiarenko.plugin;

import com.degtiarenko.plugin.type.CellPyFileType;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CellUtil {
    public static final String BLOCK_CELL_SEPARATOR = "# %%";
    private static final String ccpyExtension = CellPyFileType.INSTANCE.getDefaultExtension();

    @Nullable
    public static PsiElement getCellStart(@NotNull PsiElement element) {
        PsiElement topLevelComponent = ObjectUtils.notNull(PsiTreeUtil.findFirstParent(element,
                elem -> elem.getParent() instanceof PyFile), element);
        while (!(topLevelComponent.getText().equals(BLOCK_CELL_SEPARATOR))) {
            topLevelComponent = PsiTreeUtil.getPrevSiblingOfType(topLevelComponent, PsiComment.class);
            if (topLevelComponent == null) {
                break;
            }
        }
        if (topLevelComponent == null) {
            return element.getContainingFile().getFirstChild();
        }
        return topLevelComponent.getNextSibling();
    }

    @NotNull
    public static String getCodeInCell(@Nullable PsiElement element) {
        StringBuilder text = new StringBuilder();
        while (element != null && !element.getText().equals(BLOCK_CELL_SEPARATOR)) {
            text.append(element.getText());
            element = element.getNextSibling();
        }
        return text.toString();
    }

    public static boolean isFileOfGoodType(@NotNull PsiFile file) {
        return file.getName().endsWith(ccpyExtension);
    }
}
