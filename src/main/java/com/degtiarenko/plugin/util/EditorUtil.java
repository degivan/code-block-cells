package com.degtiarenko.plugin.util;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditorUtil {
    @Nullable
    public static PsiElement getCaretElement(@NotNull Editor editor, @NotNull PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        element = ObjectUtils.chooseNotNull(element, file.findElementAt(offset - 1));
        return element;
    }
}
