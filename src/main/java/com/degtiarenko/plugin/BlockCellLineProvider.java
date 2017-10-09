package com.degtiarenko.plugin;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.SeparatorPlacement;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class BlockCellLineProvider implements LineMarkerProvider {
    private static final String BLOCK_CELL_SEPARATOR = "#%%";
    private EditorColorsManager colorsManager = EditorColorsManager.getInstance();

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        if (psiElement instanceof PsiComment) {
            PsiComment comment = (PsiComment) psiElement;
            if (isBlockCellComment(comment)) {
                return createBlockCellLineMarker(comment, colorsManager);
            }
        }
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> list, @NotNull Collection<LineMarkerInfo> collection) {
        for (PsiElement psiElement : list) {
            collection.add(getLineMarkerInfo(psiElement));
        }
    }

    private boolean isBlockCellComment(PsiComment comment) {
        return comment.getText().equals(BLOCK_CELL_SEPARATOR);
    }

    // почти полностью скопирован с LineMarkerPass#createMethodSeparatorLineMarker
    @NotNull
    private static LineMarkerInfo<PsiComment> createBlockCellLineMarker(@NotNull PsiComment comment,
                                                                        @NotNull EditorColorsManager colorsManager) {
        LineMarkerInfo<PsiComment> info = new LineMarkerInfo<>(
                comment,
                comment.getTextRange(),
                null,
                Pass.LINE_MARKERS,
                null,
                null,
                GutterIconRenderer.Alignment.RIGHT
        );
        EditorColorsScheme scheme = colorsManager.getGlobalScheme();
        info.separatorColor = scheme.getColor(CodeInsightColors.METHOD_SEPARATORS_COLOR);
        info.separatorPlacement = SeparatorPlacement.TOP;
        return info;
    }
}
