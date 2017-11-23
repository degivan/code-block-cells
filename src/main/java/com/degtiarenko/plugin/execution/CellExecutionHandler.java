package com.degtiarenko.plugin.execution;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.util.TimeoutUtil;
import com.jetbrains.python.console.PythonConsoleExecuteActionHandler;
import com.jetbrains.python.console.PythonConsoleView;
import com.jetbrains.python.psi.PyReferenceExpression;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static java.util.stream.Collectors.joining;

public class CellExecutionHandler {
    private final PythonConsoleView consoleView;
    private final PythonConsoleExecuteActionHandler executeActionHandler;

    public CellExecutionHandler(PythonConsoleView consoleView) {
        this.consoleView = consoleView;
        this.executeActionHandler = consoleView.getExecuteActionHandler();
    }

    public void execute(String text, boolean fold) {
        while (!executeActionHandler.canExecuteNow()) {
            TimeoutUtil.sleep(300);
        }
        if (!text.isEmpty()) {
            if (fold) {
                foldExecutedCode(consoleView, text);
            }
            consoleView.executeInConsole(text);
        }
    }

    public void printWarning(List<PyReferenceExpression> unresolvedReferences) {
        if (!unresolvedReferences.isEmpty()) {
            String warning = getWarning(unresolvedReferences);
            final Editor editor = consoleView.getEditor();
            final Document document = editor.getDocument();
            int finish = document.getTextLength();
            document.insertString(finish, warning);
        }
    }

    private static String getWarning(@NotNull List<PyReferenceExpression> unresolvedReferences) {
        return unresolvedReferences.stream()
                .map(PsiElement::getText)
                .collect(joining(", ", "These references are unresolved: ", ".\n"));
    }

    private static void foldExecutedCode(@NotNull PythonConsoleView codeExecutor, @NotNull String text) {
        Editor consoleEditor = codeExecutor.getEditor();
        Document oldDocument = consoleEditor.getDocument();
        CellDocumentListener listener = new CellDocumentListener(consoleEditor, oldDocument, text);
        oldDocument.addDocumentListener(listener);
    }
}
