package com.degtiarenko.plugin;

import com.degtiarenko.plugin.util.CellUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import org.junit.Assert;

public class NewCellActionTest extends BaseTestCase {
    private static final String EMPTY_CCPY = "empty.ccpy";
    private static final String NEW_CELL_ACTION_ID = "NewCellAction";
    private static final String NON_EMPTY_CCPY = "ham_with_comment.ccpy";

    public void testActionPerformed_emptyFile_cellInTheBeginning() {
        myFixture.configureByFile(EMPTY_CCPY);

        Document document = myFixture.getEditor().getDocument();
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(DocumentEvent event) {
                Assert.assertFalse(document.getText().startsWith("\n"));
                Assert.assertTrue(document.getText().contains(CellUtil.BLOCK_CELL_SEPARATOR));
            }
        });
        myFixture.performEditorAction(NEW_CELL_ACTION_ID);
    }

    public void testActionPerformed_nonEmptyFile_cellInTheBeginning() {
        myFixture.configureByFile(NON_EMPTY_CCPY);

        Document document = myFixture.getEditor().getDocument();
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(DocumentEvent event) {
                Assert.assertFalse(document.getText().startsWith("\n"));
                Assert.assertTrue(document.getText().contains(CellUtil.BLOCK_CELL_SEPARATOR));
            }
        });
        myFixture.performEditorAction(NEW_CELL_ACTION_ID);
    }
}
