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
import java.awt.*;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

public class QueryPane {
    
    private static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);
    private final static Insets MARGIN = new Insets(4, 8, 4, 8);

    public static JButton createIconButton(String text, String emoji) {
        Color bgColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);
        boolean isDark = ColorUtil.isDarkColor(bgColor);
        Color textColor = getTextColorFromMimeType(MIME_PLAIN_TEXT);
        Font font = getFontFromMimeType(MIME_PLAIN_TEXT);

        JButton button;
        if (text == null || text.isBlank()) {
            button = new JButton(emoji);
        } else {
            button = new JButton(text + " " + emoji);
        }
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Monospaced", Font.BOLD, font.getSize()));
        Color newtextColor = isDark ? textColor.darker() : new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 98);
        button.setForeground(newtextColor);
        button.setMargin(EMPTY_INSETS);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));

        button.addActionListener(e -> {
            Color clickedColor = isDark ? textColor.brighter() : textColor.darker();
            button.setForeground(clickedColor);
            new Timer(1000, evt -> button.setForeground(newtextColor)).start();
        });

        button.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                AbstractButton button = (AbstractButton) c;
                ButtonModel model = button.getModel();

                String text = button.getText();
                boolean isOnlyEmoji = text != null && !text.isEmpty() && !Character.isLetter(text.codePointAt(0));
                if (isOnlyEmoji) {
                    if (model.isRollover()) {
                        g2.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 20));
                        g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 40, 40);
                    }
                } else {
                    if (model.isRollover()) {
                        g2.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 80));
                        g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 40, 40);
                    }
                    g2.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 60));
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 40, 40);
                }
                g2.dispose();
                super.paint(g, c);
            }
        });
        return button;
    }

    public static <T> JComboBox<T> createStyledComboBox(T[] items) {
        Color bgColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);
        boolean isDark = ColorUtil.isDarkColor(bgColor);
        Color textColor = getTextColorFromMimeType(MIME_PLAIN_TEXT);
        Font font = getFontFromMimeType(MIME_PLAIN_TEXT);

        Color borderColor = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 60);
        Color background = bgColor;
        Color foreground = isDark ? textColor.darker() : new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 98);
        
        JComboBox<T> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Monospaced", Font.BOLD, font.getSize()));
        comboBox.setForeground(foreground);
        comboBox.setBackground(background);
        comboBox.setOpaque(false);
        RoundedBorder<T> border = new RoundedBorder<>(30, borderColor, comboBox);
        comboBox.setBorder(border);
        comboBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrowButton = new JButton("\u25bc");
                arrowButton.setFont(new Font("Monospaced", Font.BOLD, font.getSize()));
                arrowButton.setForeground(foreground);
                arrowButton.setOpaque(false);
                arrowButton.setContentAreaFilled(false);
                arrowButton.setBorderPainted(false);
                arrowButton.setFocusPainted(false);
                arrowButton.setBorder(new EmptyBorder(0, 0, 0, 0));
                arrowButton.setMargin(new Insets(0,0,0,0)); 
                return arrowButton;
            }

            @Override
            protected ComboPopup createPopup() {
                BasicComboPopup popup = (BasicComboPopup) super.createPopup();
                popup.setBorder(BorderFactory.createEmptyBorder());
                return popup;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(background);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
   
            @Override
            protected Insets getInsets() {
                return new Insets(2, 4, 2, 4);
            }
         
        });

        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(new Font("Monospaced", Font.BOLD, font.getSize()));
                label.setForeground(foreground);
                label.setBackground(isSelected
                        ? (isDark ? new Color(70, 70, 70) : new Color(230, 230, 230))
                        : (isDark ? new Color(50, 50, 50) : new Color(247, 247, 247)));
                label.setBorder(new EmptyBorder(4, 12, 4, 12));
                return label;
            }
        });
        return comboBox;
    }

    public static class RoundedBorder<T> extends AbstractBorder {

        private final int radius;
        private final Color color;
        private final JComboBox<T> comboBox;

        public RoundedBorder(int radius, Color color, JComboBox<T> comboBox ) {
            this.radius = radius;
            this.color = color;
            this.comboBox = comboBox;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            if (isHidden()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return isHidden() ? EMPTY_INSETS : MARGIN;
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            Insets i = isHidden() ? EMPTY_INSETS : MARGIN;
            insets.set(i.top, i.left, i.bottom, i.right);
            return insets;
        }
        
        boolean isHidden() {
            return Boolean.TRUE.equals(comboBox.getClientProperty("minimal"));
        }
    }

}
