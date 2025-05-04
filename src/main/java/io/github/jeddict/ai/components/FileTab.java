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
package io.github.jeddict.ai.components;

import io.github.jeddict.ai.util.ColorUtil;
import static io.github.jeddict.ai.util.EditorUtil.getBackgroundColorFromMimeType;
import static io.github.jeddict.ai.util.EditorUtil.getFontFromMimeType;
import static io.github.jeddict.ai.util.EditorUtil.getTextColorFromMimeType;
import static io.github.jeddict.ai.util.MimeUtil.MIME_PLAIN_TEXT;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.border.Border;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;

public class FileTab extends JPanel {

    private final FileObject file;
    private final JPanel parentContainer;
    private final Consumer<FileObject> onCloseCallback;

    public FileTab(FileObject file, JPanel parentContainer, Consumer<FileObject> onCloseCallback) {
        this.file = file;
        this.parentContainer = parentContainer;
        this.onCloseCallback = onCloseCallback;
        initUI();
    }

    private void initUI() {
        Color bgColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);
        boolean isDark = ColorUtil.isDarkColor(bgColor);
        Color textColor = getTextColorFromMimeType(MIME_PLAIN_TEXT);
        Font font = getFontFromMimeType(MIME_PLAIN_TEXT);

        Color tabBackground = isDark
                ? new Color(255, 255, 255, 5)
                : new Color(0, 0, 0, 5);

        Color tabBorder = isDark
                ? new Color(255, 255, 255, 50)
                : new Color(0, 0, 0, 40);

        Color textFG = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), isDark ? 150 : 100);
        Color hoverTextFG = isDark ? textColor.brighter() : textColor.darker();

        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 6));
        setOpaque(false);

        JPanel roundedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.setColor(tabBorder);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 40, 40);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        roundedPanel.setOpaque(false);
        roundedPanel.setBackground(tabBackground);
        roundedPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Border border = new EmptyBorder(1, 2, 1, 2);
        roundedPanel.setBorder(border);
        roundedPanel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                openFileInEditor();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                roundedPanel.setBackground(isDark ? new Color(255, 255, 255, 30) : new Color(0, 0, 0, 30));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                roundedPanel.setBackground(tabBackground);
            }
        });

        JLabel label = new JLabel(file.getNameExt());
        label.setFont(font);
        label.setForeground(textFG);

        JButton closeButton = new JButton("✕");
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setOpaque(false);
        closeButton.setBorder(border);
        closeButton.setFont(font);
        closeButton.setForeground(textFG);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setForeground(hoverTextFG);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setForeground(textFG);
            }
        });

        closeButton.addActionListener(e -> {
            parentContainer.remove(FileTab.this);
            parentContainer.revalidate();
            parentContainer.repaint();
            if (onCloseCallback != null) {
                onCloseCallback.accept(file);
            }
        });

        roundedPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                roundedPanel.setBackground(isDark
                        ? new Color(255, 255, 255, 30)
                        : new Color(0, 0, 0, 30));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                roundedPanel.setBackground(tabBackground);
            }
        });

        roundedPanel.add(label);
        roundedPanel.add(closeButton);
        add(roundedPanel);
    }

    public String getFileName() {
        return file.getName();
    }

    public FileObject getFile() {
        return file;
    }

    private void openFileInEditor() {
        // Use the EditorCookie to open the file in the editor
        EditorCookie editorCookie = file.getLookup().lookup(EditorCookie.class);
        if (editorCookie != null) {
            editorCookie.open();
        }
    }
}
