package com.degtiarenko.plugin.execution;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.FoldingModel;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.util.TimeoutUtil;
import com.jetbrains.python.console.PythonConsoleView;
import com.jetbrains.python.console.pydev.ConsoleCommunication;
import com.jetbrains.python.console.pydev.ConsoleCommunicationListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static java.util.stream.Collectors.joining;

public class CellExecutionHandler {
    public static final String UNRESOLVED_REFERENCES_PREFIX = "Unresolved references: ";

    @NotNull
    private final PythonConsoleView consoleView;

    @NotNull
    private final ConsoleCommunication consoleCommunication;

    private volatile boolean ready = true;

    public CellExecutionHandler(@NotNull PythonConsoleView consoleView) {
        this.consoleView = consoleView;
        consoleCommunication = consoleView.getExecuteActionHandler().getConsoleCommunication();
        consoleCommunication.addCommunicationListener(new ExecutionConsoleCommunicationListener());
    }

    public void execute(String text, boolean fold, ProgressIndicator progressIndicator) {
        if (!text.isEmpty()) {
            ready = false;
            if (fold) {
                foldExecutedCode(consoleView);
                executeInConsole(text, progressIndicator);
            } else {
                executeInConsole(text, progressIndicator);
            }
        }
    }

    private void executeInConsole(String text, ProgressIndicator progressIndicator) {
        consoleView.executeInConsole(text);
        sleepTillReadyOrCanceled(progressIndicator);
    }

    public void showWarning(@NotNull List<String> unresolvedReferences) {
        if (!unresolvedReferences.isEmpty()) {
            String warning = getWarning(unresolvedReferences);
            Editor editor = consoleView.getEditor();
            Document document = editor.getDocument();
            int oldLength = document.getTextLength();

            consoleView.printText(warning, ConsoleViewContentType.LOG_WARNING_OUTPUT);
            document.addDocumentListener(new DocumentListener() {
                @Override
                public void documentChanged(DocumentEvent event) {
                    document.removeDocumentListener(this);
                    FoldingModel foldingModel = editor.getFoldingModel();
                    foldingModel.runBatchFoldingOperation(() -> {
                        FoldRegion region = foldingModel.addFoldRegion(oldLength, document.getTextLength() - 1,
                                "WARNING: Unresolved references...");
                        if (region != null) {
                            region.setExpanded(false);
                        }
                    });
                }
            });
        }
    }

    private void sleepTillReadyOrCanceled(ProgressIndicator progressIndicator) {
        while (!ready) {
            if (progressIndicator.isCanceled()) {
                consoleCommunication.interrupt();
                break;
            }
            TimeoutUtil.sleep(300);
        }
    }

    private static String getWarning(@NotNull List<String> unresolvedReferences) {
        return unresolvedReferences.stream()
                .collect(joining(", ", UNRESOLVED_REFERENCES_PREFIX, ".\n"));
    }

    private static void foldExecutedCode(@NotNull PythonConsoleView consoleView) {
        CellDocumentListener listener = new CellDocumentListener(consoleView);
        consoleView.getEditor().getDocument().addDocumentListener(listener);
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
