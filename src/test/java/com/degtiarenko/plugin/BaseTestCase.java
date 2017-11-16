package com.degtiarenko.plugin;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import java.io.File;

public class BaseTestCase extends LightPlatformCodeInsightFixtureTestCase {
    private static final String TEST_DATA = "testData";

    @Override
    protected String getTestDataPath() {
        return new File(TEST_DATA).getAbsolutePath();
    }
}
