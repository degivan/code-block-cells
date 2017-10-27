package com.degtiarenko.plugin;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.SeparatorPlacement;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

import static com.degtiarenko.plugin.CellUtil.BLOCK_CELL_SEPARATOR;

public class BlockCellLineProvider implements LineMarkerProvider {
    private EditorColorsManager colorsManager = EditorColorsManager.getInstance();

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if ((element instanceof PsiComment && isBlockCellComment((PsiComment) element))
                || (element.getPrevSibling() == null && element.getParent() instanceof PyFile)) {
            return createBlockCellLineMarker(element, colorsManager);
        }
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> list, @NotNull Collection<LineMarkerInfo> collection) {

    }

    private boolean isBlockCellComment(PsiComment comment) {
        return comment.getText().equals(BLOCK_CELL_SEPARATOR);
    }

    // почти полностью скопирован с LineMarkerPass#createMethodSeparatorLineMarker
    @NotNull
    private static LineMarkerInfo<PsiElement> createBlockCellLineMarker(@NotNull PsiElement element,
                                                                        @NotNull EditorColorsManager colorsManager) {
        LineMarkerInfo<PsiElement> info = new LineMarkerInfo<>(
                element,
                element.getTextRange(),
                AllIcons.General.Run,
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
