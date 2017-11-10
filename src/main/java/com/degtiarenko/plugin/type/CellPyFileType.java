package com.degtiarenko.plugin.type;

import com.jetbrains.python.PythonFileType;
import org.jetbrains.annotations.NotNull;

public class CellPyFileType extends PythonFileType {
    public static final PythonFileType INSTANCE = new CellPyFileType();

    private CellPyFileType() {
        super();
    }

    @NotNull
    @Override
    public String getName() {
        return "Cell Python";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Cell Python";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "ccpy";
    }
}
