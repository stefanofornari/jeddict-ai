/*
 * Copyright 2019 Eric VILLARD <dev@eviweb.fr>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.jeddict.ai.actions;

import io.github.jeddict.ai.hints.LearnFix;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 * Base class for ChatAction implementations
 *
 * @author Shiwani Gupta
 */
abstract class ChatAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextComponent editor = EditorRegistry.lastFocusedComponent();
        String selectedText = editor.getSelectedText();
        final StyledDocument document = (StyledDocument) editor.getDocument();
        int selectionStartPosition = editor.getSelectionStart();
        if (selectedText == null || selectedText.isEmpty()) {
            try {
                selectionStartPosition = 0;
                selectedText = document.getText(selectionStartPosition, document.getLength());
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        final String text = selectedText;
        final int startLocation = selectionStartPosition;
        FileObject file = getFileObject(document);
        Project project = getProject(file);
        LearnFix learnFix = new LearnFix(io.github.jeddict.ai.completion.Action.QUERY, project);
        learnFix.openChat(selectedText, file.getName(), "Chat with AI", content -> {
            NbDocument.runAtomic(document, () -> {
                try {
                    document.remove(startLocation, text.length());
                    document.insertString(startLocation, content, null);
                    Reformat reformat = Reformat.get(document);
                    reformat.lock();
                    try {
                        reformat.reformat(startLocation, startLocation + content.length());
                    } finally {
                        reformat.unlock();
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        });
    }

    /**
     * Retrieves the project associated with the given JTextComponent.
     *
     * @param editor The JTextComponent from which to derive the project.
     * @return The Project associated with the document, or null if not found.
     */
    private Project getProject(FileObject fileObject) {
        try {
            if (fileObject != null) {
                return FileOwnerQuery.getOwner(fileObject);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /**
     * Retrieves the FileObject associated with the given JTextComponent.
     *
     * @param editor The JTextComponent from which to derive the FileObject.
     * @return The FileObject associated with the document, or null if not
     * found.
     */
    private FileObject getFileObject(StyledDocument document) {
        try {
            // Retrieve the DataObject directly from the document's stream property
            DataObject dataObject = NbEditorUtilities.getDataObject(document);
            if (dataObject != null) {
                return dataObject.getPrimaryFile();
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
