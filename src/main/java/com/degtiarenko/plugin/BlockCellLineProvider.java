package com.degtiarenko.plugin;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.execution.lineMarker.LineMarkerActionWrapper;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.SeparatorPlacement;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

import static com.degtiarenko.plugin.CellUtil.BLOCK_CELL_SEPARATOR;
import static com.degtiarenko.plugin.CellUtil.isFileOfGoodType;

public class BlockCellLineProvider implements LineMarkerProvider {
    private static final String CELL_ACTION_ID = "PyExecuteCellAction";

    private EditorColorsManager colorsManager = EditorColorsManager.getInstance();
    private AnAction action = ActionManager.getInstance().getAction(CELL_ACTION_ID);

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if(isFileOfGoodType(element.getContainingFile())) {
            if (isBlockCellComment(element) || isBeginningOfFile(element)) {
                return createBlockCellLineMarker(element, colorsManager);
            }
        }
        return null;
    }

    private boolean isBeginningOfFile(@NotNull PsiElement element) {
        return element.getPrevSibling() == null && element.getParent() instanceof PyFile;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> list, @NotNull Collection<LineMarkerInfo> collection) {

    }

    private boolean isBlockCellComment(PsiElement comment) {
        return (comment instanceof PsiComment) && (comment.getText().equals(BLOCK_CELL_SEPARATOR));
    }

    @NotNull
    private LineMarkerInfo<PsiElement> createBlockCellLineMarker(@NotNull PsiElement element,
                                                                 @NotNull EditorColorsManager colorsManager) {
        Function<PsiElement, String> tooltipProvider = psiElement -> action.toString();
        final DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new LineMarkerActionWrapper(element, action));

        LineMarkerInfo<PsiElement> info = new LineMarkerInfo<PsiElement>(
                element,
                element.getTextRange(),
                AllIcons.General.Run,
                Pass.LINE_MARKERS,
                tooltipProvider,
                null,
                GutterIconRenderer.Alignment.CENTER
        ) {
            @Override
            public GutterIconRenderer createGutterRenderer() {
                return new LineMarkerGutterIconRenderer<PsiElement>(this) {
                    @Override
                    public AnAction getClickAction() {
                        return new AnAction() {
                            @Override
                            public void actionPerformed(AnActionEvent e) {
                                Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
                                if (editor != null) {
                                    editor.getCaretModel().moveToOffset(element.getTextOffset());
                                }
                                action.actionPerformed(e);
                            }
                        };
                    }

                    @Override
                    public boolean isNavigateAction() {
                        return true;
                    }

                    @Override
                    public ActionGroup getPopupMenuActions() {
                        return null;
                    }
                };
            }
        };
        EditorColorsScheme scheme = colorsManager.getGlobalScheme();
        info.separatorColor = scheme.getColor(CodeInsightColors.METHOD_SEPARATORS_COLOR);
        info.separatorPlacement = SeparatorPlacement.TOP;
        return info;
    }
}
