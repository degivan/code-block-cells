package com.degtiarenko.plugin;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.List;

import static com.degtiarenko.plugin.CellUtil.getCellStart;
import static com.degtiarenko.plugin.CellUtil.isFileOfGoodType;
import static com.intellij.psi.util.PsiTreeUtil.findChildrenOfType;
import static com.intellij.psi.util.PsiTreeUtil.nextLeaf;

public class CellUtilTest extends BaseTestCase {

    private static final String CCPY_WITH_COMMENT = "ham_with_comment.ccpy";
    private static final String BROKEN_TEST_MSG = "Test is broken";

    public void testGetCellStart_noCommentBefore_returnFileFirstChild() {
        PsiFile file = myFixture.configureByFile(CCPY_WITH_COMMENT);

        testGetCellStart(file.getFirstChild().getFirstChild(), file.getFirstChild());
    }

    public void testGetCellStart_topLevelElemCommentBefore_returnCommentElem() {
        PsiFile file = myFixture.configureByFile(CCPY_WITH_COMMENT);
        List<PsiComment> comments = new ArrayList<>(findChildrenOfType(file, PsiComment.class));
        PsiComment comment = comments.get(0);
        PsiElement nextTopLevel = nextLeaf(nextLeaf(comment));

        assertTrue(BROKEN_TEST_MSG, comment.getText().equals(CellUtil.BLOCK_CELL_SEPARATOR));

        testGetCellStart(nextTopLevel, comment.getNextSibling());
    }

    public void testGetCellStart_nonTopLevelElemCommentBefore_returnCommentElem() {
        PsiFile file = myFixture.configureByFile(CCPY_WITH_COMMENT);
        List<PsiComment> comments = new ArrayList<>(findChildrenOfType(file, PsiComment.class));
        PsiComment comment = comments.get(0);
        PsiElement nonTopLevel = PsiTreeUtil.getDeepestLast(file);

        assertTrue(BROKEN_TEST_MSG, comment.getText().equals(CellUtil.BLOCK_CELL_SEPARATOR));

        testGetCellStart(nonTopLevel, comment.getNextSibling());
    }

    private void testGetCellStart(PsiElement caretElem, PsiElement expected) {
        PsiElement cellStart = getCellStart(caretElem);
        assertEquals("Cell start is wrong", expected, cellStart);
    }

    public void testIsFileOfGoodType_pyExtension_returnFalse() {
        testIsFileOFGoodType("spam.py", false);
    }

    public void testIsFileOfGoodType_ccpyExtension_returnTrue() {
        testIsFileOFGoodType("ham.ccpy", true);
    }

    private void testIsFileOFGoodType(String fileName, boolean expected) {
        PsiFile file = myFixture.configureByFile(fileName);
        assertEquals(expected, isFileOfGoodType(file));
    }
}