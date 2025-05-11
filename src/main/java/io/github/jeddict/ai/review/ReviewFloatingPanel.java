/**
 * Copyright 2025 the original author or authors from the Jeddict project (https://jeddict.github.io/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.jeddict.ai.review;

import static io.github.jeddict.ai.components.QueryPane.createIconButton;
import io.github.jeddict.ai.hints.AssistantChatManager;
import io.github.jeddict.ai.util.ColorUtil;
import static io.github.jeddict.ai.util.ColorUtil.darken;
import static io.github.jeddict.ai.util.ColorUtil.lighten;
import static io.github.jeddict.ai.util.EditorUtil.getBackgroundColorFromMimeType;
import static io.github.jeddict.ai.util.EditorUtil.getFontFromMimeType;
import static io.github.jeddict.ai.util.EditorUtil.getTextColorFromMimeType;
import static io.github.jeddict.ai.util.Icons.ICON_NEXT;
import static io.github.jeddict.ai.util.MimeUtil.MIME_PLAIN_TEXT;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import static java.awt.Font.BOLD;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.filesystems.FileObject;

public class ReviewFloatingPanel extends JComponent {
    
    private final int defaultWidth = 600;

    public ReviewFloatingPanel(ReviewFloatingWindow parent, BaseDocument document, List<ReviewValue> reviewValues) {
        Font newFont = getFontFromMimeType(MIME_PLAIN_TEXT);
        Color textColor = getTextColorFromMimeType(MIME_PLAIN_TEXT);
        Color backgroundColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);
        boolean isDark = ColorUtil.isDarkColor(backgroundColor);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);
        setBackground(Color.GRAY);

        for (final ReviewValue reviewValue : reviewValues) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setOpaque(true);
            panel.setBackground(isDark ? lighten(backgroundColor, .25f) : darken(backgroundColor, .75f));
            panel.setBorder(new EmptyBorder(5, 5, 5, 5));
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton learnMoreButton = createIconButton(reviewValue.getValue() + ": " + reviewValue.getTitle(), ICON_NEXT);
            learnMoreButton.setForeground(lighten(textColor, 1.5f));
            learnMoreButton.setBackground(isDark ? lighten(backgroundColor, .5f) : darken(backgroundColor, .5f));
            learnMoreButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            learnMoreButton.addActionListener(e -> {
                FileObject fileObject = NbEditorUtilities.getFileObject(document);
                String reviewText = reviewValue.getValue() 
                        + ": " + reviewValue.getTitle()
                        + '\n' + reviewValue.getDescription().replaceAll("\\.\\s", ".\n");
                AssistantChatManager learnFix = new AssistantChatManager(io.github.jeddict.ai.completion.Action.QUERY, fileObject);
                learnFix.openChat(null, reviewText, fileObject.getName(), "Code Review - " + fileObject.getName(), content -> {});
                parent.dispose();
            });
            
//            JTextArea header = new JTextArea();
//            header.setEditable(false);
//            header.setLineWrap(true);
//            header.setWrapStyleWord(true);
//            header.setOpaque(false);
//            header.setFont(new Font(newFont.getFamily(), BOLD, newFont.getSize() + 1));
//            header.setForeground(lighten(textColor, 1.75f));
//            header.setBorder(null);
//            header.setFocusable(false);
//            header.setSize(new Dimension(defaultWidth + 30 - learnMoreButton.getPreferredSize().width, Short.MAX_VALUE));
//            header.setPreferredSize(header.getPreferredSize());
//            header.setMaximumSize(new Dimension(Short.MAX_VALUE, header.getPreferredSize().height));

            JTextArea content = new JTextArea(reviewValue.getDescription());
            content.setEditable(false);
            content.setLineWrap(true);
            content.setWrapStyleWord(true);
            content.setOpaque(false);
            content.setForeground(lighten(textColor, 1.5f));
            content.setFont(newFont);
            content.setFocusable(false);
            content.setBorder(null);

            // Set a fixed width, max height to calculate preferred size properly
            content.setSize(new Dimension(defaultWidth, Short.MAX_VALUE));
            Dimension prefSize = content.getPreferredSize();
            content.setPreferredSize(prefSize);
            content.setMaximumSize(new Dimension(Short.MAX_VALUE, prefSize.height));
//            panel.add(header);
//            panel.add(content);
//            panel.add(learnMoreButton);
            
            
            // Create headerPanel to hold header and button horizontally
  JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
headerPanel.setOpaque(false);
//headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
//headerPanel.add(header);
headerPanel.add(learnMoreButton);


            panel.add(headerPanel);
            panel.add(content);

    add(panel);

            add(panel);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width = Math.max(size.width, defaultWidth+20);
        return size;
    }

    public void shutdown() {
        // remove listeners
        for (Component component : getComponents()) {
            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                for (MouseListener mouseListener : label.getMouseListeners()) {
                    label.removeMouseListener(mouseListener);
                }
            }
        }
    }

}
