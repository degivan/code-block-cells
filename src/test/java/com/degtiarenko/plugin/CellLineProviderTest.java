package com.degtiarenko.plugin;

import com.intellij.codeInsight.daemon.GutterMark;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.List;

public class CellLineProviderTest extends BaseTestCase {

    public void test_pyExtension_noLineMarkerInfo() {
        PsiFile file = myFixture.configureByFile("spam.py");
        CellLineProvider lineProvider = new CellLineProvider();
        for (PsiElement elem : file.getChildren()) {
            LineMarkerInfo lineMarkerInfo = lineProvider.getLineMarkerInfo(elem);
            assertNull("Line provider returned not null", lineMarkerInfo);
        }
    }

    public void test_ccpyExtensionNoComment_hasLineMarkerInfo() {
        PsiFile file = myFixture.configureByFile("ham.ccpy");
        CellLineProvider lineProvider = new CellLineProvider();

        LineMarkerInfo lineMarkerInfo = lineProvider.getLineMarkerInfo(file.getFirstChild());

        assertNotNull("Line provider returned null", lineMarkerInfo);
    }

    public void test_ccpyExtensionWithComment_correctGutters() {
        myFixture.configureByFile("ham_with_comment.ccpy");
        List<GutterMark> marks = myFixture.findAllGutters();

        assertEquals(2, marks.size());
        for (GutterMark mark : marks) {
            AnAction clickAction = ((GutterIconRenderer) mark).getClickAction();
            assertNotNull(clickAction);
        }
    }
}