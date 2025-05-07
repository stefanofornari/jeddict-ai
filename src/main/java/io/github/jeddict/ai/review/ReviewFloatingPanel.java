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

import io.github.jeddict.ai.util.ColorUtil;
import static io.github.jeddict.ai.util.ColorUtil.darken;
import static io.github.jeddict.ai.util.ColorUtil.lighten;
import static io.github.jeddict.ai.util.EditorUtil.getBackgroundColorFromMimeType;
import static io.github.jeddict.ai.util.EditorUtil.getFontFromMimeType;
import static io.github.jeddict.ai.util.EditorUtil.getTextColorFromMimeType;
import static io.github.jeddict.ai.util.MimeUtil.MIME_PLAIN_TEXT;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import static java.awt.Font.BOLD;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class ReviewFloatingPanel extends JComponent {

    public ReviewFloatingPanel(List<ReviewValue> reviewValues) {
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

            JTextArea header = new JTextArea(reviewValue.getValue() + ": " + reviewValue.getTitle());
            header.setEditable(false);
            header.setLineWrap(true);
            header.setWrapStyleWord(true);
            header.setOpaque(false);
            header.setFont(new Font(newFont.getFamily(), BOLD, newFont.getSize() + 1));
            header.setForeground(lighten(textColor, 1.75f));
            header.setBorder(null);
            header.setFocusable(false);
            header.setSize(new Dimension(300, Short.MAX_VALUE));
            header.setPreferredSize(header.getPreferredSize());
            header.setMaximumSize(new Dimension(Short.MAX_VALUE, header.getPreferredSize().height));

            JTextArea textArea = new JTextArea(reviewValue.getDescription());
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setOpaque(false);
            textArea.setForeground(lighten(textColor, 1.5f));
            textArea.setFont(newFont);
            textArea.setFocusable(false);
            textArea.setBorder(null);

            // Set a fixed width, max height to calculate preferred size properly
            textArea.setSize(new Dimension(300, Short.MAX_VALUE));
            Dimension prefSize = textArea.getPreferredSize();
            textArea.setPreferredSize(prefSize);
            textArea.setMaximumSize(new Dimension(Short.MAX_VALUE, prefSize.height));
            panel.add(header);
            panel.add(textArea);
//            JButton learnMoreButton = new JButton("Learn More");
//            learnMoreButton.setAlignmentX(Component.LEFT_ALIGNMENT);
//            learnMoreButton.addActionListener(e -> {
//
//            });
//            panel.add(learnMoreButton);

            add(panel);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width = Math.max(size.width, 320);
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
