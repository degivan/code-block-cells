package com.degtiarenko.plugin.execution;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
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
    private static final String DISPLAY_ID = "PyCells plugin";

    private final PythonConsoleView consoleView;
    private final PythonConsoleExecuteActionHandler executeActionHandler;

    public CellExecutionHandler(PythonConsoleView consoleView) {
        this.consoleView = consoleView;
        this.executeActionHandler = consoleView.getExecuteActionHandler();
    }

    public void execute(String text, boolean fold) {
        while (!executeActionHandler.canExecuteNow() || !executeActionHandler.isEnabled()) {
            TimeoutUtil.sleep(300);
        }
        if (!text.isEmpty()) {
            if (fold) {
                foldExecutedCode(consoleView, text);
            }
            consoleView.executeInConsole(text);
        }
    }

    public void showWarning(List<PyReferenceExpression> unresolvedReferences) {
        if (!unresolvedReferences.isEmpty()) {
            String warning = getWarning(unresolvedReferences);
            Notifications.Bus.notify(new Notification(DISPLAY_ID,"PyCells plugin: unresolved references",
                    warning, NotificationType.WARNING), consoleView.getProject());
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
