package com.degtiarenko.plugin.highlighting;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class HighlightListenerProvider implements ProjectComponent {
    private final Project project;

    public HighlightListenerProvider(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {
        EditorFactory.getInstance().addEditorFactoryListener(new EditorFactoryListener() {
            private final Map<Editor, CellHighlightListener> listeners = new HashMap<>();

            @Override
            public void editorCreated(@NotNull EditorFactoryEvent event) {
                Editor editor = event.getEditor();
                final CellHighlightListener highlightListener = new CellHighlightListener(project);

                listeners.put(editor, highlightListener);
                editor.getCaretModel().addCaretListener(highlightListener);
            }

            @Override
            public void editorReleased(@NotNull EditorFactoryEvent event) {
                Editor editor = event.getEditor();
                editor.getCaretModel().removeCaretListener(listeners.get(editor));
            }
        }, project);
    }
}
