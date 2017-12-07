package com.degtiarenko.plugin.execution;

import com.google.common.collect.Sets;
import com.jetbrains.python.console.PythonConsoleView;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CellExecutionManager {
    private final Map<PythonConsoleView, CellExecutionHandler> handlers = new ConcurrentHashMap<>();
    private final Set<PythonConsoleView> occupiedConsoles = Sets.newConcurrentHashSet();

    public synchronized CellExecutionHandler getHandler(PythonConsoleView consoleView) {
        if (occupiedConsoles.contains(consoleView)) {
            return null;
        } else {
            occupiedConsoles.add(consoleView);
            return handlers.computeIfAbsent(consoleView, CellExecutionHandler::new); //TODO: clear map
        }
    }

    public void freeConsole(PythonConsoleView consoleView) {
        occupiedConsoles.remove(consoleView);
    }
}
