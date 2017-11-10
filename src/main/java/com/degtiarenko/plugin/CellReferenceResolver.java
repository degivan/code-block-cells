package com.degtiarenko.plugin;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyImportedNameDefiner;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.resolve.ImportedResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.degtiarenko.plugin.CellUtil.BLOCK_CELL_SEPARATOR;
import static com.degtiarenko.plugin.CellUtil.getCodeInCell;
import static java.util.stream.Collectors.toList;

public class CellReferenceResolver {
    private final PsiElement cellStart;
    private final PsiFile file;
    private final List<PsiElement> resolvingCells = new ArrayList<>();

    public CellReferenceResolver(PsiElement cellStart, @NotNull PsiFile file) {
        this.cellStart = cellStart;
        this.file = file;
    }

    public String getResolvingCode() {
        List<PyReferenceExpression> references = getReferencesInCell(cellStart);
        while (!references.isEmpty()) {
            List<PsiElement> cellStarts = findResolvingCells(references);
            resolvingCells.addAll(cellStarts);
            references = cellStarts.stream()
                    .flatMap(elem -> getReferencesInCell(elem).stream())
                    .collect(toList());

        }
        return getCodeInResolvingCells(resolvingCells);
    }

    private String getCodeInResolvingCells(List<PsiElement> cellStarts) {
        cellStarts = cellStarts.stream().distinct().collect(toList());
        cellStarts.sort(Comparator.comparingInt(PsiElement::getTextOffset));

        StringBuilder resolvingCode = new StringBuilder();
        for (PsiElement cellStart : cellStarts) {
            resolvingCode.append(getCodeInCell(cellStart));
            resolvingCode.append('\n');
        }
        return resolvingCode.toString();
    }

    @NotNull
    private List<PsiElement> findResolvingCells(@NotNull List<PyReferenceExpression> references) {
        List<PsiElement> elements = resolveReferences(references, file);
        return elements.stream()
                .filter(Objects::nonNull)
                .map(CellUtil::getCellStart)
                .filter(e -> e != cellStart && !resolvingCells.contains(e))
                .distinct()
                .collect(toList());
    }

    @NotNull
    private static List<PyReferenceExpression> getReferencesInCell(@Nullable PsiElement element) {
        List<PyReferenceExpression> references = new ArrayList<>();
        while (element != null && !element.getText().equals(BLOCK_CELL_SEPARATOR)) {
            if (element instanceof PyReferenceExpression) {
                references.add((PyReferenceExpression) element);
            }
            references.addAll(PsiTreeUtil.findChildrenOfType(element, PyReferenceExpression.class));
            element = element.getNextSibling();
        }
        return references;
    }

    @NotNull
    private static List<PsiElement> resolveReferences(@NotNull List<PyReferenceExpression> references,
                                                      @NotNull PsiFile file) {
        List<PsiElement> result = new ArrayList<>();
        for (PyReferenceExpression reference : references) {
            PsiElement resolver = reference.getReference().resolve();
            if (resolver != null && resolver.getContainingFile().equals(file)) {
                result.add(resolver);
            } else {
                List<ResolveResult> resolvers = Arrays.asList(reference.getReference().multiResolve(false));
                for (ResolveResult resolveResult : resolvers) {
                    if (resolveResult.isValidResult() && resolveResult instanceof ImportedResolveResult) {
                        final PyImportedNameDefiner definer = ((ImportedResolveResult) resolveResult).getDefiner();
                        result.add(definer);
                        break;
                    }
                }
            }
        }
        return result;
    }
}
