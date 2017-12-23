package com.degtiarenko.plugin.execution;

import com.degtiarenko.plugin.util.CellUtil;
import com.google.common.collect.Lists;
import com.intellij.execution.ExecutionHelper;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.Consumer;
import com.intellij.util.ObjectUtils;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.python.console.*;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.degtiarenko.plugin.util.CellUtil.getCellText;
import static com.degtiarenko.plugin.util.EditorUtil.getCaretElement;

public class PyExecuteCellAction extends AnAction {

    private static final String EXECUTE_CELL_IN_CONSOLE = "Execute Cell in Console";
    private static final String DESCRIPTION = "Executes selected cell in Python/Django console";
    private final CellExecutionManager executionManager;

    public PyExecuteCellAction() {
        super(EXECUTE_CELL_IN_CONSOLE, DESCRIPTION, null);
        executionManager = new CellExecutionManager();
    }

    public void actionPerformed(AnActionEvent e) {
        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        PsiFile file = CommonDataKeys.PSI_FILE.getData(e.getDataContext());
        if (editor != null && file != null && CellUtil.isFileOfGoodType(file)) {
            PsiElement element = getCaretElement(editor, file);
            final String cellText = getCellText(element);
            final Pair<String, List<String>> dependentCellsCode = getCodeFromDependentCells(element, file);
            showConsoleAndExecuteCode(e, dependentCellsCode.getFirst(), cellText,
                    dependentCellsCode.getSecond());
        }
    }

    @NotNull
    private Pair<String, List<String>> getCodeFromDependentCells(@Nullable PsiElement element,
                                                                 @NotNull PsiFile file) {
        if (element != null) {
            element = CellUtil.getCellStart(element);
            CellReferenceResolver referenceResolver = new CellReferenceResolver(element, file);
            final String resolvingCode = referenceResolver.getResolvingCode();
            final List<String> unresolvedReferences = referenceResolver.getUnresolvedReferences()
                    .stream().map(PsiElement::getText).collect(Collectors.toList());
            return new Pair<>(resolvingCode, unresolvedReferences);
        }
        return new Pair<>("", new ArrayList<>());
    }

    /**
     * Finds existing or creates a new console and then executes provided code there.
     *
     * @param e                    event
     * @param cellText             null means that there is no code to execute, only open a console
     * @param unresolvedReferences unresolved references in cell
     */
    private void showConsoleAndExecuteCode(@NotNull final AnActionEvent e, @NotNull final String resolvingCellText,
                                           @NotNull final String cellText,
                                           @NotNull List<String> unresolvedReferences) {
        final Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        Project project = e.getProject();

        findCodeExecutor(e, codeExecutor -> executeInConsole(codeExecutor, resolvingCellText, cellText,
                unresolvedReferences),
                editor, project);
    }


    public void update(AnActionEvent e) {
        Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
        PsiFile file = CommonDataKeys.PSI_FILE.getData(e.getDataContext());
        Presentation presentation = e.getPresentation();

        boolean enabled = false;
        if (editor != null && file != null && isPython(editor) && CellUtil.isFileOfGoodType(file)) {
            PsiElement element = getCaretElement(editor, file);
            String text = getCellText(element);
            enabled = !StringUtil.isEmpty(text);
        }

        presentation.setEnabledAndVisible(enabled);
    }

    private static boolean isPython(Editor editor) {
        if (editor == null) {
            return false;
        }

        Project project = editor.getProject();

        if (project == null) {
            return false;
        }

        PsiFile psi = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        return psi instanceof PyFile;
    }

    private static void selectConsole(@NotNull DataContext dataContext, @NotNull Project project,
                                      @NotNull final Consumer<PyCodeExecutor> consumer, @Nullable Editor editor) {
        Collection<RunContentDescriptor> consoles = getConsoles(project);

        ExecutionHelper
                .selectContentDescriptor(dataContext, project, consoles, "Select console to execute in", descriptor -> {
                    if (descriptor != null && descriptor.getExecutionConsole() instanceof PyCodeExecutor) {
                        ExecutionConsole console = descriptor.getExecutionConsole();
                        consumer.consume((PyCodeExecutor) console);
                        if (console instanceof PythonDebugLanguageConsoleView) {
                            XDebugSession currentSession = XDebuggerManager.getInstance(project).getCurrentSession();
                            if (currentSession != null) {
                                // Select "Console" tab in case of Debug console
                                ContentManager contentManager = currentSession.getUI().getContentManager();
                                Content content = contentManager.findContent("Console");
                                contentManager.setSelectedContent(content);
                                if (editor != null) {
                                    IdeFocusManager.findInstance().requestFocus(editor.getContentComponent(), true);
                                }
                            }
                        } else {
                            PythonConsoleToolWindow consoleToolWindow = PythonConsoleToolWindow.getInstance(project);
                            ToolWindow toolWindow = consoleToolWindow != null ? consoleToolWindow.getToolWindow() : null;
                            if (toolWindow != null && !toolWindow.isVisible()) {
                                toolWindow.show(null);
                                ContentManager contentManager = toolWindow.getContentManager();
                                Content content = contentManager.findContent(descriptor.getDisplayName());
                                if (content != null) {
                                    contentManager.setSelectedContent(content);
                                }
                            }
                        }
                    }
                });
    }

    private static Collection<RunContentDescriptor> getConsoles(Project project) {
        Collection<RunContentDescriptor> result;
        PythonConsoleToolWindow toolWindow = PythonConsoleToolWindow.getInstance(project);

        if (toolWindow != null && toolWindow.getToolWindow().isVisible()) {
            RunContentDescriptor selectedContentDescriptor = toolWindow.getSelectedContentDescriptor();
            result = selectedContentDescriptor != null ? Lists.newArrayList(selectedContentDescriptor) : Lists.newArrayList();
        } else {
            Collection<RunContentDescriptor> descriptors =
                    ExecutionHelper.findRunningConsole(project, dom -> dom.getExecutionConsole() instanceof PyCodeExecutor && isAlive(dom));

            if (descriptors.isEmpty() && toolWindow != null) {
                result = toolWindow.getConsoleContentDescriptors();
            } else {
                result = descriptors;
            }
        }
        return filterDescriptors(result);
    }

    private static List<RunContentDescriptor> filterDescriptors(Collection<RunContentDescriptor> descriptors) {
        return descriptors.stream()
                .filter(d -> ((PythonConsoleView) d.getExecutionConsole()).getExecuteActionHandler() != null)
                .collect(Collectors.toList());
    }

    private static boolean isAlive(RunContentDescriptor dom) {
        ProcessHandler processHandler = dom.getProcessHandler();
        return processHandler != null && !processHandler.isProcessTerminated();
    }

    private static void findCodeExecutor(@NotNull AnActionEvent e,
                                         @NotNull Consumer<PyCodeExecutor> consumer,
                                         @Nullable Editor editor,
                                         @Nullable Project project) {
        if (project != null) {
            if (canFindConsole(e)) {
                selectConsole(e.getDataContext(), project, consumer, editor);
            } else {
                startConsole(project, consumer);
            }
        }
    }

    private static void startConsole(final Project project,
                                     final Consumer<PyCodeExecutor> consumer) {
        final PythonConsoleToolWindow toolWindow = PythonConsoleToolWindow.getInstance(project);

        final List<RunContentDescriptor> descs = Optional.ofNullable(toolWindow)
                .map(tw -> filterDescriptors(tw.getConsoleContentDescriptors()))
                .orElseGet(ArrayList::new);
        if (toolWindow != null && descs.size() > 0) {
            toolWindow.activate(() -> {
                RunContentDescriptor descriptor = descs.get(0);
                if (descriptor != null && descriptor.getExecutionConsole() instanceof PyCodeExecutor) {
                    consumer.consume((PyCodeExecutor) descriptor.getExecutionConsole());
                }
            });
        } else {
            PythonConsoleRunnerFactory consoleRunnerFactory = PythonConsoleRunnerFactory.getInstance();
            PydevConsoleRunner runner = consoleRunnerFactory.createConsoleRunner(project, null);
            runner.addConsoleListener(consoleView -> {
                if (consoleView instanceof PyCodeExecutor) {
                    consumer.consume((PyCodeExecutor) consoleView);
                    if (toolWindow != null) {
                        toolWindow.getToolWindow().show(null);
                    }
                }
            });
            runner.run();
        }
    }

    private static boolean canFindConsole(AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            Collection<RunContentDescriptor> descriptors = getConsoles(project);
            return descriptors.size() > 0;
        } else {
            return false;
        }
    }

    private void executeInConsole(@NotNull PyCodeExecutor codeExecutor, @NotNull String resolvingCellText,
                                  @NotNull String cellText,
                                  @NotNull List<String> unresolvedReferences) {
        if (codeExecutor instanceof PythonConsoleView) {
            PythonConsoleView consoleView = (PythonConsoleView) codeExecutor;
            Task.Backgroundable task = new Task.Backgroundable(consoleView.getProject(), "Execute Code in Console",
                    true) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    CellExecutionHandler executionHandler = executionManager.getHandler(consoleView);
                    if (executionHandler == null) {
                        return;
                    }
                    executionHandler.execute(resolvingCellText, true, progressIndicator);
                    if (!progressIndicator.isCanceled()) {
                        executionHandler.showWarning(unresolvedReferences);
                        executionHandler.execute(cellText, false, progressIndicator);
                    }
                    executionManager.freeConsole(consoleView);
                }
            };
            ProgressManager.getInstance().run(task);
        } else {
            throw new RuntimeException("Wrong code executor type! This shouldn't happen in production.");
        }
    }
}
