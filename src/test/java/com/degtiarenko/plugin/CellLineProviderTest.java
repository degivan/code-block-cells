package com.degtiarenko.plugin;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import java.io.File;

public class CellLineProviderTest extends LightPlatformCodeInsightFixtureTestCase {

    public void test() {
        PsiFile file = myFixture.configureByFile("spam.py");
        CellLineProvider lineProvider = new CellLineProvider();

        assertNull("Line provider should not return anything",
                lineProvider.getLineMarkerInfo(file.getFirstChild()));
    }

    @Override
    protected String getTestDataPath() {
        return new File("testData").getAbsolutePath();
    }
}