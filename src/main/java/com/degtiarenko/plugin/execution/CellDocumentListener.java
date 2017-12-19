package com.degtiarenko.plugin.execution;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.FoldingModel;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.jetbrains.python.console.PyConsoleUtil;

import java.util.Optional;

import static com.degtiarenko.plugin.execution.CellExecutionHandler.UNRESOLVED_REFERENCES_PREFIX;

public class CellDocumentListener implements DocumentListener {
    private static final String NEW_EXECUTION_PREFIX = "In[";
    public static final String DEPENDENT_CELLS_FOLD = "Dependent cells code";

    private final int foldStart;
    private final Editor editor;
    private int blockCount = 0;

    public CellDocumentListener(Document document, Editor editor) {
        this.editor = editor;
        this.foldStart = document.getTextLength();
    }

    @Override
    public void documentChanged(DocumentEvent event) {
        if (isNewBlock(event)) {
            blockCount++;
        }
        if (blockCount >= 2) {
            Document document = event.getDocument();
            document.removeDocumentListener(this);
            FoldingModel foldingModel = editor.getFoldingModel();
            foldingModel.runBatchFoldingOperation(() -> {
                int foldEnd = document.getTextLength() - event.getNewLength() - 1; //offset counts from 0, length from 1
                FoldRegion region = foldingModel.addFoldRegion(foldStart, foldEnd, DEPENDENT_CELLS_FOLD);
                Optional.ofNullable(region).ifPresent(r -> r.setExpanded(false));
            });
        }
    }

    private boolean isNewBlock(DocumentEvent event) {
        final String text = event.getNewFragment().toString();
        return text.startsWith(NEW_EXECUTION_PREFIX) || text.startsWith(UNRESOLVED_REFERENCES_PREFIX)
                    || text.startsWith(PyConsoleUtil.ORDINARY_PROMPT);
    }

}
