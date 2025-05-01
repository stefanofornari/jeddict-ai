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
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class QueryPane {

    public static JButton createIconButton(String text, String emoji) {
        Color bgColor = getBackgroundColorFromMimeType(MIME_PLAIN_TEXT);
        boolean isDark = ColorUtil.isDarkColor(bgColor);
                
        Color textColor = getTextColorFromMimeType(MIME_PLAIN_TEXT);
        Font font = getFontFromMimeType(MIME_PLAIN_TEXT);

        JButton button = new JButton(text + " " + emoji);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.setMargin(new Insets(4, 8, 4, 8)); // Small vertical/horizontal padding
        button.setFont(new Font("Monospaced", Font.BOLD, font.getSize()));
        Color newtextColor = isDark ? textColor.darker(): new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 98);
        button.setForeground(newtextColor);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                AbstractButton button = (AbstractButton) c;
                ButtonModel model = button.getModel();

                if (model.isRollover()) {
                    g2.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 80));
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 40, 40);
                }

                g2.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 60));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 40, 40);

                g2.dispose();
                super.paint(g, c);
            }
        });
        return button;
    }

}
