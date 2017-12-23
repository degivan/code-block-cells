package com.degtiarenko.plugin.highlighting;

import com.degtiarenko.plugin.util.CellUtil;
import com.degtiarenko.plugin.util.EditorUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.JBColor;
import com.intellij.util.containers.HashMap;

import java.awt.*;
import java.util.Map;

public class CellHighlightListener implements CaretListener {
    private final Project project;
    private Map<RangeHighlighter, PsiElement> highlighterElements = new HashMap<>();

    public CellHighlightListener(Project project) {
        this.project = project;
    }

    @Override
    public void caretPositionChanged(CaretEvent e) {
        final Editor editor = e.getEditor();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (virtualFile == null) {
            return;
        }
        PsiFile file = PsiUtilCore.getPsiFile(project, virtualFile);
        PsiElement element = EditorUtil.getCaretElement(editor, file);
        if (element == null) {
            return;
        }
        element = CellUtil.getCellStart(element);
        PsiElement cellEnd = CellUtil.getCellEnd(element);
        for(RangeHighlighter highlighter: highlighterElements.keySet()) {
            if (!highlighterElements.get(highlighter).equals(element)) {
                editor.getMarkupModel().removeHighlighter(highlighter);
                highlighterElements.remove(highlighter);
            }
        }
        if (highlighterElements.isEmpty()) {
            highlightCell(editor, element, cellEnd);
        }
    }

    private void highlightCell(Editor editor, PsiElement cellStart, PsiElement cellEnd) {
        final int start = cellStart != null ? cellStart.getTextOffset() : 0;
        final int end = cellEnd != null ? cellEnd.getTextOffset() : editor.getDocument().getTextLength() - 1;
        final TextAttributes textAttributes = new TextAttributes() {
            @Override
            public Color getBackgroundColor() {
                return JBColor.LIGHT_GRAY;
            }
        };
        RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(start, end, HighlighterLayer.ADDITIONAL_SYNTAX + 100,
                textAttributes, HighlighterTargetArea.LINES_IN_RANGE);
        highlighterElements.put(highlighter, cellStart);
    }
}
