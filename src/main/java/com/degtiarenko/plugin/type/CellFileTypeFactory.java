package com.degtiarenko.plugin.type;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.jetbrains.python.PythonFileType;
import org.jetbrains.annotations.NotNull;

public class CellFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        PythonFileType instance = CellPyFileType.INSTANCE;
        consumer.consume(instance, instance.getDefaultExtension());
    }
}
