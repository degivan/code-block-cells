package com.degtiarenko.plugin.execution;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.FoldingModel;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.psi.PsiElement;
import com.intellij.util.TimeoutUtil;
import com.jetbrains.python.console.PythonConsoleView;
import com.jetbrains.python.console.pydev.ConsoleCommunication;
import com.jetbrains.python.console.pydev.ConsoleCommunicationListener;
import com.jetbrains.python.psi.PyReferenceExpression;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class CellExecutionHandler {
    private static final String DISPLAY_ID = "PyCells plugin";

    private final PythonConsoleView consoleView;
    private final ConsoleCommunication consoleCommunication;
    private volatile boolean ready = true;

    public CellExecutionHandler(PythonConsoleView consoleView) {
        this.consoleView = consoleView;
        consoleCommunication = consoleView.getExecuteActionHandler().getConsoleCommunication();
        consoleCommunication.addCommunicationListener(new ExecutionConsoleCommunicationListener());
    }

    public void execute(String text, boolean fold, ProgressIndicator progressIndicator) {
        if (!text.isEmpty()) {
            ready = false;
            if (fold) {
                CellDocumentListener listener = foldExecutedCode(consoleView, text);
                executeInConsole(text, progressIndicator);
                listener.setFinished(true);
            } else {
                executeInConsole(text, progressIndicator);
            }
        }
    }

    private void executeInConsole(String text, ProgressIndicator progressIndicator) {
        consoleView.executeInConsole(text);
        while (!ready) {
            if (progressIndicator.isCanceled()) {
                consoleCommunication.interrupt();
                break;
            }
            TimeoutUtil.sleep(300);
        }
    }

    public void showWarning(List<PyReferenceExpression> unresolvedReferences) {
        if (!unresolvedReferences.isEmpty()) {
            WriteCommandAction.runWriteCommandAction(consoleView.getProject(),
                    () -> {
                        String warning = getWarning(unresolvedReferences);
                        Editor editor = consoleView.getEditor();
                        Document document = editor.getDocument();
                        FoldingModel foldingModel = editor.getFoldingModel();
                        int oldLength = document.getTextLength();

                        document.insertString(oldLength, warning);
                        foldingModel.runBatchFoldingOperation(() -> {
                            FoldRegion region = foldingModel.addFoldRegion(oldLength, document.getTextLength() - 1,
                                    "WARNING: Unresolved references...");
                            Optional.ofNullable(region).ifPresent(r -> r.setExpanded(false));
                        });
                    });
        }
    }

    private static String getWarning(@NotNull List<PyReferenceExpression> unresolvedReferences) {
        return unresolvedReferences.stream()
                .map(PsiElement::getText)
                .collect(joining(", ", "These references are unresolved: ", ".\n"));
    }

    private CellDocumentListener foldExecutedCode(@NotNull PythonConsoleView codeExecutor, @NotNull String text) {
        Editor consoleEditor = codeExecutor.getEditor();
        Document oldDocument = consoleEditor.getDocument();
        CellDocumentListener listener = new CellDocumentListener(oldDocument, consoleEditor);
        oldDocument.addDocumentListener(listener);
        return listener;
    }

    private class ExecutionConsoleCommunicationListener implements ConsoleCommunicationListener {

        @Override
        public void commandExecuted(boolean more) {
            if (!more) {
                ready = true;
            }
        }

        @Override
        public void inputRequested() {

        }
    }
}
