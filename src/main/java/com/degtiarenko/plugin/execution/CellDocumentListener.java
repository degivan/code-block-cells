package com.degtiarenko.plugin.execution;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.FoldingModel;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;

import java.util.Optional;

public class CellDocumentListener implements DocumentListener {
    private final int foldStart;
    private final Editor editor;
    private int cellCount = 0;

    private volatile boolean finished = false;

    public CellDocumentListener(Document document, Editor editor) {
        this.editor = editor;
        this.foldStart = document.getTextLength();
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    @Override
    public void documentChanged(DocumentEvent event) {
        if (isNewCodeBlock(event)) {
            cellCount++;
        }
        if (finished || cellCount == 2) {
            Document document = event.getDocument();
            document.removeDocumentListener(this);
            FoldingModel foldingModel = editor.getFoldingModel();
            foldingModel.runBatchFoldingOperation(() -> {
                int foldEnd = document.getTextLength() - event.getNewLength() - 1;
                FoldRegion region = foldingModel.addFoldRegion(foldStart, foldEnd, "...");
                Optional.ofNullable(region).ifPresent(r -> r.setExpanded(false));
            });
        }
    }

    private boolean isNewCodeBlock(DocumentEvent event) {
        return event.getNewFragment().toString().startsWith("In[");
    }

}
