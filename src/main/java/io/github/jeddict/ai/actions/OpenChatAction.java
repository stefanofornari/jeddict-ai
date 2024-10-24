/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Shiwani Gupta
 */
@ActionID(
        category = "Chat/SubActions",
        id = "io.github.jeddict.ai.actions.OpenChatAction"
)
@ActionRegistration(
        displayName = "#CTL_OpenChatAction"
)
@Messages("CTL_OpenChatAction=Open in Chat")
public class OpenChatAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {

        LearnFix learnFix;
        JTextComponent editor = EditorRegistry.lastFocusedComponent();
        String selectedText = editor.getSelectedText();
        final StyledDocument document = (StyledDocument) editor.getDocument();

        FileObject file = getFileObject(document);
        int selectionStartPosition = editor.getSelectionStart();
        if (selectedText == null || selectedText.isEmpty()) {
            selectionStartPosition = -1;
            selectedText = "";
            learnFix = new LearnFix(io.github.jeddict.ai.completion.Action.QUERY, file);
        } else {
            learnFix = new LearnFix(io.github.jeddict.ai.completion.Action.QUERY);
        }
        final String text = selectedText;
        final int startLocation = selectionStartPosition;
        learnFix.openChat(null, selectedText, file.getName(), "Chat with AI", content -> {
            NbDocument.runAtomic(document, () -> {
                if (!text.isEmpty()) {
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
