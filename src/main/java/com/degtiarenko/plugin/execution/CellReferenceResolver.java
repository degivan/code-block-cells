package com.degtiarenko.plugin.execution;

import com.degtiarenko.plugin.CellUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
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
    private final List<PyReferenceExpression> unresolvedReferences = new ArrayList<>();

    public CellReferenceResolver(PsiElement cellStart, @NotNull PsiFile file) {
        this.cellStart = cellStart;
        this.file = file;
    }

    public List<PyReferenceExpression> getUnresolvedReferences() {
        return unresolvedReferences;
    }

    public String getResolvingCode() {
        List<PyReferenceExpression> references = getReferencesInCell(cellStart);
        while (!references.isEmpty()) {
            List<PsiElement> cellStarts = findDependentCells(references);
            resolvingCells.addAll(cellStarts);
            references = cellStarts.stream()
                    .flatMap(elem -> getReferencesInCell(elem).stream())
                    .collect(toList());

        }
        return getCodeFromCells(resolvingCells);
    }

    private String getCodeFromCells(List<PsiElement> cellStarts) {
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
    private List<PsiElement> findDependentCells(@NotNull List<PyReferenceExpression> references) {
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
    private List<PsiElement> resolveReferences(@NotNull List<PyReferenceExpression> references,
                                               @NotNull PsiFile file) {
        List<PsiElement> result = new ArrayList<>();
        for (PyReferenceExpression expression : references) {
            PsiPolyVariantReference reference = expression.getReference();
            PsiElement target = reference.resolve();
            if (target == null || !target.getContainingFile().equals(file)) {
                List<ResolveResult> resolveResults = Arrays.asList(reference.multiResolve(false));
                for (ResolveResult resolveResult : resolveResults) {
                    if (resolveResult.isValidResult() && resolveResult instanceof ImportedResolveResult) {
                        final PyImportedNameDefiner definer = ((ImportedResolveResult) resolveResult).getDefiner();
                        if (definer != null) {
                            target = definer;
                        }
                        break;
                    }
                }
            }
            if (target == null) {
                unresolvedReferences.add(expression);
            } else if (target.getContainingFile().equals(file)) {
                result.add(target);
            }
        }
        return result;
    }
}
