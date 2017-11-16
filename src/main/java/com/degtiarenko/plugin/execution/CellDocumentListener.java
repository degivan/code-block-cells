package com.degtiarenko.plugin.execution;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.FoldingModel;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;

public class CellDocumentListener implements DocumentListener {

    private final Editor consoleEditor;
    private int expectedLinesAmount;
    private int oldLength;

    public CellDocumentListener(Editor consoleEditor, Document oldDocument, String text) {
        this.consoleEditor = consoleEditor;
        int codeLineCount = StringUtils.countMatches(text, "\n");
        this.expectedLinesAmount = oldDocument.getLineCount() + codeLineCount + 2;
        this.oldLength = oldDocument.getTextLength();
    }

    @Override
    public void beforeDocumentChange(DocumentEvent event) {
        String output = String.valueOf(event.getNewFragment());
        if (isStartOutput(output)) {
            expectedLinesAmount += StringUtils.countMatches(output, "\n");
            oldLength += output.length();
        }
    }

    private boolean isStartOutput(String output) {
        return output.contains("PyDev console:");
    }

    @Override
    public void documentChanged(DocumentEvent event) {
        Document document = event.getDocument();
        FoldingModel foldingModel = consoleEditor.getFoldingModel();
        int finish = document.getTextLength() - 1;
        if (this.expectedLinesAmount <= document.getLineCount()) {
            document.removeDocumentListener(this);
            foldingModel.runBatchFoldingOperation(() -> {
                FoldRegion foldRegion = foldingModel.addFoldRegion(oldLength, finish, "...");
                Optional.ofNullable(foldRegion).ifPresent(region -> region.setExpanded(false));
            });
        }
    }
}
